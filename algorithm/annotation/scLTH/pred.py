import argparse
import os
import sys

import torch
import numpy as np
import scanpy as sc
from collections import OrderedDict
from torch.utils.data import DataLoader, TensorDataset
from dataset import MyDataset
from scLTH import SCLTHTrain
import requests

def ensure_directory(path):
    """ 确保目录存在，如果不存在则创建 """
    if not os.path.exists(path):
        os.makedirs(path)

def main(args):
    requests.post(
        f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH任务{args.task_name}正在处理，结果生成到{args.output_path}")
    try:
        args.output_path = args.output_path + "output.npy"
        ensure_directory(os.path.dirname(args.output_path))
        device = "cuda" if torch.cuda.is_available() else "cpu"

        # 加载 checkpoint
        checkpoint = torch.load(args.checkpoint)

        # 读取数据
        labels = np.loadtxt(args.label_path, dtype=str, delimiter=',', skiprows=1)
        rna = sc.read_h5ad(args.rna_path)
        atac = sc.read_h5ad(args.atac_path)
        rna = rna.X.todense()
        atac = atac.X

        # 转换为 numpy 数组
        rna = np.asarray(rna)
        atac = np.asarray(atac)

        # 创建数据集
        data = MyDataset(rna, atac)

        dataset = TensorDataset(torch.tensor(rna).double(), torch.tensor(atac).double())
        dataloader = DataLoader(dataset, batch_size=args.batch_size, shuffle=False)

        # 初始化模型
        num_classes = len(set(labels))
        model = SCLTHTrain(
            sourse_size=data.rna.shape[1], num_classes=num_classes,
            dropout=args.dropout, input_dim=args.input_dim,
            num_layers=args.num_layers, nhead=args.nhead
        ).to(device)

        # 加载模型权重
        model.encoder_rna.load_state_dict(
            {k.replace('encoder_rna.', ''): v for k, v in checkpoint.items() if k.startswith('encoder_rna.')},
            strict=False)
        model.encoder_atac.load_state_dict(
            {k.replace('encoder_atac.', ''): v for k, v in checkpoint.items() if k.startswith('encoder_atac.')},
            strict=False)
        model.combiner.load_state_dict(
            {k.replace('combiner.', ''): v for k, v in checkpoint.items() if k.startswith('combiner.')},
            strict=False)
        model.tsm.load_state_dict(
            {k.replace('tsm.', ''): v for k, v in checkpoint.items() if k.startswith('tsm.')}, strict=False)
        model.classifier.load_state_dict(
            {k.replace('classifier.', ''): v for k, v in checkpoint.items() if k.startswith('classifier.')}, strict=False)

        model.eval()

        all_output = []
        all_output_num = []
        unique_labels = list(OrderedDict.fromkeys(labels))  # 获取唯一标签列表

        with torch.no_grad():
            for batch_rna, batch_atac in dataloader:
                batch_rna, batch_atac = batch_rna.to(device), batch_atac.to(device)
                output = model(batch_rna, batch_atac)
                predicted_indices = output.argmax(1).cpu().numpy()
                all_output_num.append(predicted_indices)
                predicted_labels = [unique_labels[i] for i in predicted_indices]
                all_output.extend(predicted_labels)

        np.save(args.output_path, np.array(all_output, dtype=object))

        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH任务{args.task_name}预测完成，输出已保存至 {args.output_path}")
        requests.post(f"http://localhost:8868/tsneUmapChartProgress?type={args.task_type}&taskName={args.task_name}&userName={args.user_name}&seq_dir={args.rna_path}&label_dir={args.label_path}&outputnpyPath={args.output_path}")
    except Exception as e:
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH任务{args.task_name}出错：{e}")
        # 设置任务为错误
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=-1")


if __name__ == "__main__":
    # 设置任务为处理中->模型预测->tsne->umap->设置任务为成功
    if len(sys.argv) < 9:
        print("参数错误")
        sys.exit(1)

    # 解析命令行参数
    parser = argparse.ArgumentParser(description='Run scLTH model inference.')

    # 必须提供的路径参数
    parser.add_argument('--atac_path', type=str, required=True, help='Path to the ATAC data file (h5ad).')
    parser.add_argument('--rna_path', type=str, required=True, help='Path to the RNA data file (h5ad).')
    parser.add_argument('--label_path', type=str, required=True, help='Path to the label CSV file.')
    parser.add_argument('--checkpoint', type=str, required=True, help='Path to the model checkpoint file.')
    parser.add_argument('--output_path', type=str, required=True, help='Path to save label output (npy).')

    parser.add_argument('--user_name', type=str, required=True, help='User name.')
    parser.add_argument('--task_name', type=str, required=True, help='Task name.')
    parser.add_argument('--task_type', type=str, required=True, help='Task type.')

    # 预训练相关参数
    parser.add_argument('--use_pretrained', action='store_true', help='Whether to use existing pretrained model.')
    parser.add_argument('--pretrain_path', type=str, default='', help='Path to existing pretrained model (required if use_pretrained is True).')

    # 训练超参数
    parser.add_argument('--seed', type=int, default=1224455, help='Random seed.')
    parser.add_argument('--pretrain_patience', type=int, default=32, help='Early stopping patience for pretraining.')
    parser.add_argument('--pretrain_epochs', type=int, default=96, help='Number of epochs for pretraining.')
    # 预测超参数
    parser.add_argument('--n_epochs', type=int, default=96, help='Number of epochs.')
    parser.add_argument('--dropout', type=float, default=0.05, help='Dropout rate.')
    parser.add_argument('--batch_size', type=int, default=128, help='Batch size.')
    parser.add_argument('--patience', type=int, default=8, help='Early stopping patience.')
    parser.add_argument('--input_dim', type=int, default=512, help='Input dimension.')
    parser.add_argument('--num_layers', type=int, default=8, help='Number of transformer layers.')
    parser.add_argument('--nhead', type=int, default=16, help='Number of attention heads.')
    parser.add_argument('--lr', type=float, default=5e-4, help='Learning rate.')
    parser.add_argument('--weight_decay', type=float, default=5e-3, help='Weight decay.')

    args = parser.parse_args()

    # 设置任务为处理中
    requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=1")
    main(args)