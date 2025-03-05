import argparse
import torch
import numpy as np
import scanpy as sc
from collections import OrderedDict
from torch.utils.data import DataLoader, TensorDataset
from dataset import MyDataset
from scLTH import SCLTHTrain

# 解析命令行参数
parser = argparse.ArgumentParser(description='Run scLTH model inference.')

# 必须提供的路径参数
parser.add_argument('--atac_path', type=str, required=True, help='Path to the ATAC data file (h5ad).')
parser.add_argument('--rna_path', type=str, required=True, help='Path to the RNA data file (h5ad).')
parser.add_argument('--label_path', type=str, required=True, help='Path to the label CSV file.')
parser.add_argument('--checkpoint', type=str, required=True, help='Path to the model checkpoint file.')
parser.add_argument('--output_num_path', type=str, required=True, help='Path to save numerical output (npy).')
parser.add_argument('--output_path', type=str, required=True, help='Path to save label output (npy).')

# 具有默认值的超参数
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

all_output_num = np.concatenate(all_output_num, axis=0)
np.save(args.output_num_path, all_output_num)
np.save(args.output_path, np.array(all_output, dtype=object))

print(f"预测完成，输出已保存至 {args.output_path} 和 {args.output_num_path}")