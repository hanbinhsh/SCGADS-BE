import argparse
import os
import sys
import torch
import numpy as np
import scanpy as sc
from collections import OrderedDict
from torch.utils.data import DataLoader, TensorDataset
from dataset import MyDatasetPred
from scTCHCN import StudentModel, scTCHCNTrain
import requests

# !!! 必须与训练时的设置保持一致 !!!
DEBUG_DIM = 500

def ensure_directory(path):
    if not os.path.exists(path):
        os.makedirs(path)

def main(args):
    requests.post(
        f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scTCHCN任务{args.task_name}正在处理，结果生成到{args.output_path}")
    try:
        args.output_path = args.output_path + "output.npy"
        ensure_directory(os.path.dirname(args.output_path))
        device = "cuda" if torch.cuda.is_available() else "cpu"

        # 加载 checkpoint
        checkpoint = torch.load(args.checkpoint, map_location=device)

        # 读取数据 (仅需要 RNA)
        labels_raw = np.loadtxt(args.label_path, dtype=str, delimiter=',', skiprows=1)
        adata = sc.read_h5ad(args.rna_path)

        # 预处理保持和 Dataset 一致
        if hasattr(adata.X, 'todense'):
             rna_data = adata.X.todense()
        else:
             rna_data = adata.X

        # 简单的 padding/truncating 逻辑以匹配 DEBUG_DIM (500)
        current_dim = rna_data.shape[1]

        # !!! 关键修改：使用 DEBUG_DIM (500) 而不是 10000 !!!
        if current_dim < DEBUG_DIM:
            pad = np.zeros((rna_data.shape[0], DEBUG_DIM - current_dim))
            rna = np.hstack([rna_data, pad])
        else:
            rna = rna_data[:, :DEBUG_DIM]

        # !!! 关键修改：转换为 float32 !!!
        rna = np.asarray(rna, dtype=np.float32)

        # 创建数据集
        dataset = TensorDataset(torch.tensor(rna).float())
        dataloader = DataLoader(dataset, batch_size=args.batch_size, shuffle=False)

        # 初始化模型
        # 从 label 文件获取类别数
        unique_labels = list(OrderedDict.fromkeys(labels_raw))
        num_classes = len(unique_labels)

        # !!! 关键修改：StudentModel 初始化 seq_length 必须为 500 !!!
        student = StudentModel(input_dim=1, hidden_dim=200, seq_length=DEBUG_DIM, num_heads=4, num_layers=1)

        # scTCHCNTrain 内部会根据 scTCHCN.py 中的 DEBUG_SEQ_LEN 初始化 Classifier
        # 只要 scTCHCN.py 和这里的 StudentModel 配置一致即可
        model = scTCHCNTrain(student_model=student, num_classes=num_classes).to(device)

        # !!! 关键修改：模型转 float !!!
        model = model.float()

        # 加载模型权重
        model.load_state_dict(checkpoint)
        model.eval()

        all_output = []

        with torch.no_grad():
            for batch in dataloader:
                batch_rna = batch[0].to(device)
                output = model(batch_rna)
                predicted_indices = output.argmax(1).cpu().numpy()
                predicted_labels = [unique_labels[i] for i in predicted_indices]
                all_output.extend(predicted_labels)

        np.save(args.output_path, np.array(all_output, dtype=object))

        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scTCHCN任务{args.task_name}预测完成，输出已保存至 {args.output_path}")
        requests.post(f"http://localhost:8868/tsneUmapChartProgress?type={args.task_type}&taskName={args.task_name}&userName={args.user_name}&seq_dir={args.rna_path}&label_dir={args.label_path}&outputnpyPath={args.output_path}")
    except Exception as e:
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scTCHCN任务{args.task_name}出错：{e}")
        # 设置任务为错误
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=-1")
        raise e

if __name__ == "__main__":
    if len(sys.argv) < 8:
        print("参数错误")
        sys.exit(1)

    # 解析命令行参数
    parser = argparse.ArgumentParser(description='Run scTCHCN model inference.')

    parser.add_argument('--atac_path', type=str, required=True, help='Path to the ATAC data file.')
    parser.add_argument('--rna_path', type=str, required=True, help='Path to the RNA data file.')
    parser.add_argument('--label_path', type=str, required=True, help='Path to the label CSV file.')
    parser.add_argument('--checkpoint', type=str, required=True, help='Path to the model checkpoint file.')
    parser.add_argument('--output_path', type=str, required=True, help='Path to save label output (npy).')

    parser.add_argument('--user_name', type=str, required=True, help='User name.')
    parser.add_argument('--task_name', type=str, required=True, help='Task name.')
    parser.add_argument('--task_type', type=str, required=True, help='Task type.')
    parser.add_argument('--base_model', type=int, required=True, help='Base Model.')

    # 额外参数
    parser.add_argument('--batch_size', type=int, default=128, help='Batch size.')

    args = parser.parse_args()

    # 设置任务为处理中
    requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=1")
    main(args)