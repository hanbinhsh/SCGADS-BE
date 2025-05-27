import torch
import warnings
warnings.filterwarnings('ignore')
import pandas as pd
import scanpy as sc
import os
torch.set_default_tensor_type(torch.DoubleTensor)
import numpy as np
from sklearn.model_selection import train_test_split


def read_label(label_path):
    y_train = pd.read_csv(label_path)
    y_train = y_train.T
    y_train = y_train.values[0]

    cell_types = []
    labels = []
    for i in y_train:
        i = str(i).upper()
        if not cell_types.__contains__(i):
            cell_types.append(i)
        labels.append(cell_types.index(i))

    return labels, cell_types


class Dataset:
    @staticmethod
    def split_dataset(rna_path, atac_path, label_path, base_dir, seed = 5201314):
        adata_rna = sc.read_h5ad(rna_path)
        adata_atac = sc.read_h5ad(atac_path)
        labels, cell_types = read_label(label_path)
        adata_rna = adata_rna.X.todense()
        adata_atac = adata_atac.X

        adata_rna = np.asarray(adata_rna)

        x_rna_train, x_rna_test, x_atac_train, x_atac_test, y_train, y_test = train_test_split(
            adata_rna, adata_atac, labels, test_size=0.2, random_state=seed, stratify=labels
        )

        x_rna_train = np.asarray(x_rna_train)
        x_rna_test = np.asarray(x_rna_test)
        y_train = np.asarray(y_train)
        y_test = np.asarray(y_test)

        y_train_str = []
        y_test_str = []
        for i in y_train:
            y_train_str.append(cell_types[i])
        for i in y_test:
            y_test_str.append(cell_types[i])

        directory = os.path.join(base_dir, 'data_split')
        if not os.path.exists(directory):
            os.makedirs(directory)

        np.save(base_dir + 'data_split' + '/x_rna_train.npy'    , x_rna_train)
        np.save(base_dir + 'data_split' + '/x_rna_test.npy'     , x_rna_test)
        np.save(base_dir + 'data_split' + '/x_atac_train.npy'   , x_atac_train)
        np.save(base_dir + 'data_split' + '/x_atac_test.npy'    , x_atac_test)
        np.save(base_dir + 'data_split' + '/y_train_str.npy'    , y_train_str)
        np.save(base_dir + 'data_split' + '/y_test_str.npy'     , y_test_str)
        np.save(base_dir + 'data_split' + '/y_train.npy'        , y_train)
        np.save(base_dir + 'data_split' + '/y_test.npy'         , y_test)
        np.save(base_dir + 'data_split' + '/cell_types.npy'     , cell_types)

class MyDataset(torch.utils.data.Dataset):
    def __init__(self, rna, atac):
        # 从数据文件中加载数据并转换为numpy数组
        self.rna = np.array(rna)
        self.atac = np.array(atac)

    def __getitem__(self, index):
        x = self.rna[index]
        y = self.atac[index]
        return x, y

    def __len__(self):
        return len(self.rna)

class MyDatasetTrain(torch.utils.data.Dataset):
    def __init__(self, rna, atac, label):
        self.rna = np.array(rna)
        self.atac = np.array(atac)
        self.label = np.array(label)

    def __getitem__(self, index):
        r = self.rna[index]
        a = self.atac[index]
        y = self.label[index]
        return r, a, y

    def __len__(self):
        return len(self.rna)