import argparse
import os
import sys
import torch
import numpy as np
import scanpy as sc
from torch.utils.data import DataLoader
from dataset import Dataset, MyDatasetTrain
from pretrain import PreTrain
from train import TrainModel
import requests

def ensure_directory(path):
    """ 确保目录存在，如果不存在则创建 """
    if not os.path.exists(path):
        os.makedirs(path)

def main(args):
    requests.post(
        f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH训练任务{args.task_name}正在处理")
    try:
        # 设置随机种子
        np.random.seed(args.seed)
        torch.manual_seed(args.seed)
        torch.backends.cudnn.deterministic = True
        torch.backends.cudnn.benchmark = False
        if torch.cuda.is_available():
            torch.cuda.manual_seed_all(args.seed)

        # 确保输出目录存在
        ensure_directory(os.path.dirname(args.output_path))
        device = "cuda" if torch.cuda.is_available() else "cpu"

        # 数据预处理和分割 保存到用户名/任务名/data_split/
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH训练任务{args.task_name}正在进行数据预处理")
        Dataset.split_dataset(
            rna_path=args.rna_path,
            atac_path=args.atac_path,
            label_path=args.label_path,
            seed=args.seed,
            base_dir=args.output_path,
        )

        # 预训练阶段 保存到用户名/任务名/result/
        if not args.use_pretrained:
            requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH训练任务{args.task_name}正在进行预训练")
            pretrain = PreTrain(
                base_dir=args.output_path,
                n_epochs=args.pretrain_epochs,
                dropout=args.dropout,
                batch_size=args.batch_size,
                patience=args.pretrain_patience,
                input_dim=args.input_dim,
                num_layers=args.num_layers,
                nhead=args.nhead,
                lr=args.lr,
                weight_decay=args.weight_decay
            )
            pretrain.pretrain()
        else:
            # 使用提供的预训练模型
            if not os.path.exists(args.pretrain_path):
                raise FileNotFoundError(f"预训练模型文件不存在: {args.pretrain_path}")

        # 训练分类器 保存到用户名/任务名/result/
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH训练任务{args.task_name}正在训练分类器")
        train_model = TrainModel(
            base_dir=args.output_path,
            batch_size=args.batch_size,
            n_epochs=args.n_epochs,
            patience=args.patience,
            lr=args.lr,
            weight_decay=args.weight_decay,
            dp=args.dropout,
            input_dim=args.input_dim,
            num_layers=args.num_layers,
            nhead=args.nhead,
            seed=args.seed
        )
        train_model.train()

        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH训练任务{args.task_name}训练完成，模型已保存至 {args.output_path}")
        # 通知训练完成
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=2")

        # TODO 生成标签映射

    except Exception as e:
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH训练任务{args.task_name}出错：{e}")
        # 设置任务为错误
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=-1")
        raise e

if __name__ == "__main__":
    if len(sys.argv) < 9:
        print("参数错误")
        sys.exit(1)

    # 解析命令行参数
    parser = argparse.ArgumentParser(description='Run scLTH model training.')

    # 必须提供的路径参数
    parser.add_argument('--atac_path', type=str, required=True, help='Path to the ATAC data file (h5ad).')
    parser.add_argument('--rna_path', type=str, required=True, help='Path to the RNA data file (h5ad).')
    parser.add_argument('--label_path', type=str, required=True, help='Path to the label CSV file.')
    parser.add_argument('--output_path', type=str, required=True, help='Path to save the trained model.')

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
    parser.add_argument('--n_epochs', type=int, default=96, help='Number of epochs for classifier training.')
    parser.add_argument('--dropout', type=float, default=0.05, help='Dropout rate.')
    parser.add_argument('--batch_size', type=int, default=128, help='Batch size.')
    parser.add_argument('--patience', type=int, default=8, help='Early stopping patience for classifier training.')
    parser.add_argument('--input_dim', type=int, default=512, help='Input dimension.')
    parser.add_argument('--num_layers', type=int, default=16, help='Number of transformer layers.')
    parser.add_argument('--nhead', type=int, default=16, help='Number of attention heads.')
    parser.add_argument('--lr', type=float, default=5e-4, help='Learning rate.')
    parser.add_argument('--weight_decay', type=float, default=5e-3, help='Weight decay.')

    args = parser.parse_args()

    # 验证参数
    if args.use_pretrained and not args.pretrain_path:
        print("错误：当使用预训练模型时，必须提供预训练模型路径 (--pretrain_path)")
        sys.exit(1)

    # 设置任务为处理中
    requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=1")
    main(args)