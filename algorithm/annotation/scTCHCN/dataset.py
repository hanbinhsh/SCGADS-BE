import torch
import warnings
warnings.filterwarnings('ignore')
import pandas as pd
import scanpy as sc
import os
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics.pairwise import cosine_similarity
from scipy import sparse

# 调试模式配置
DEBUG_DIM = 500

def read_label(label_path):
    y_train = pd.read_csv(label_path)
    if y_train.shape[1] > 1:
        y_vals = y_train.iloc[:, 0].values
    else:
        y_train = y_train.T
        y_vals = y_train.values[0]

    cell_types = []
    labels = []
    for i in y_vals:
        i = str(i).upper()
        if i not in cell_types:
            cell_types.append(i)
        labels.append(cell_types.index(i))

    return labels, cell_types

class Dataset:
    @staticmethod
    def split_dataset(rna_path, atac_path, label_path, base_dir, seed=5201314):
        # 1. 读取 RNA 数据
        adata = sc.read_h5ad(rna_path)

        if sparse.issparse(adata.X):
            adata.X = adata.X.todense()

        sc.pp.normalize_total(adata, target_sum=1e4)
        sc.pp.log1p(adata)

        # 提取 Sequence 输入
        adata_seq = adata.copy()
        sc.pp.highly_variable_genes(adata_seq, n_top_genes=DEBUG_DIM)
        adata_seq = adata_seq[:, adata_seq.var['highly_variable']]

        if adata_seq.shape[1] < DEBUG_DIM:
            pad = np.zeros((adata_seq.shape[0], DEBUG_DIM - adata_seq.shape[1]))
            x_seq = np.hstack([adata_seq.X, pad])
        else:
            x_seq = adata_seq.X[:, :DEBUG_DIM]

        x_seq = np.asarray(x_seq, dtype=np.float32)

        # 2. 构建图特征
        print("正在构建图结构数据...")
        adata_graph = adata.copy()
        sc.pp.highly_variable_genes(adata_graph, n_top_genes=250)
        adata_graph = adata_graph[:, adata_graph.var['highly_variable']]
        if adata_graph.shape[1] < 250:
             pad = np.zeros((adata_graph.shape[0], 250 - adata_graph.shape[1]))
             x_pca_input = np.hstack([adata_graph.X, pad])
        else:
            x_pca_input = adata_graph.X[:, :250]

        similarity_matrix = cosine_similarity(x_pca_input)
        similarity_matrix = (1 + similarity_matrix) / 2
        np.fill_diagonal(similarity_matrix, 1)

        n_cells = x_seq.shape[0]
        top_k = 19

        x_graph_all = np.zeros((n_cells, 20, 250), dtype=np.float32)
        adj_all = np.zeros((n_cells, 20, 20), dtype=np.float32)

        for i in range(n_cells):
            sim_row = similarity_matrix[i]
            indices = np.argsort(-sim_row)
            indices = indices[indices != i]
            top_indices = indices[:top_k]
            node_indices = np.insert(top_indices, 0, i)
            x_graph_all[i] = x_pca_input[node_indices]
            sub_sim = similarity_matrix[np.ix_(node_indices, node_indices)]
            adj_all[i] = sub_sim

        print("图结构构建完成。")

        # 3. 读取标签
        labels, cell_types = read_label(label_path)
        labels = np.array(labels)

        # 确保标签数量与数据一致
        if len(labels) != n_cells:
            print(f"Warning: Label count ({len(labels)}) does not match Cell count ({n_cells}). Truncating to match.")
            labels = labels[:n_cells]

        # 4. 数据分割
        indices = np.arange(n_cells)
        idx_train, idx_test, y_train, y_test = train_test_split(
            indices, labels, test_size=0.2, random_state=seed, stratify=labels
        )

        # 5. 保存
        directory = os.path.join(base_dir, 'data_split')
        if not os.path.exists(directory):
            os.makedirs(directory)

        # 保存特征 (使用 idx_train 索引原始数据)
        np.save(f'{directory}/x_seq_train.npy', x_seq[idx_train])
        np.save(f'{directory}/x_seq_test.npy', x_seq[idx_test])
        np.save(f'{directory}/x_graph_train.npy', x_graph_all[idx_train])
        np.save(f'{directory}/x_graph_test.npy', x_graph_all[idx_test])
        np.save(f'{directory}/adj_train.npy', adj_all[idx_train])
        np.save(f'{directory}/adj_test.npy', adj_all[idx_test])

        # !!! 修复点：直接保存 y_train/y_test，不要再索引 !!!
        np.save(f'{directory}/y_train.npy', y_train)
        np.save(f'{directory}/y_test.npy', y_test)

        # 转换字符串标签
        y_train_str = np.array([cell_types[i] for i in y_train])
        y_test_str = np.array([cell_types[i] for i in y_test])

        np.save(f'{directory}/y_train_str.npy', y_train_str)
        np.save(f'{directory}/y_test_str.npy', y_test_str)
        np.save(f'{directory}/cell_types.npy', cell_types)

class MyDatasetPretrain(torch.utils.data.Dataset):
    def __init__(self, x_seq, x_graph, adj):
        self.x_seq = x_seq
        self.x_graph = x_graph
        self.adj = adj

    def __getitem__(self, index):
        return self.x_seq[index], self.x_graph[index], self.adj[index]

    def __len__(self):
        return len(self.x_seq)

class MyDatasetTrain(torch.utils.data.Dataset):
    def __init__(self, x_seq, label):
        self.x_seq = x_seq
        self.label = label

    def __getitem__(self, index):
        return self.x_seq[index], self.label[index]

    def __len__(self):
        return len(self.x_seq)

class MyDatasetPred(torch.utils.data.Dataset):
    def __init__(self, x_seq):
        self.x_seq = x_seq

    def __getitem__(self, index):
        return self.x_seq[index]

    def __len__(self):
        return len(self.x_seq)