import argparse
import os
import sys
import torch
import numpy as np
import csv
from collections import OrderedDict
from dataset import Dataset, MyDatasetTrain
from pretrain import PreTrain
from train import TrainModel
import requests
from pred import main as predict

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

        # 生成标签映射
        extract_labels(args.label_path, args.output_path+"result/extract_labels.csv")

        # 预测生成降维图
        # 修改模型为本模型
        args.checkpoint = args.output_path+"result/train_best.ckpt"
        predict(args) # 自动生成标签列表，故无需更改标签

        # TODO 上传模型市场
        # 设置参数
        user_name = args.user_name
        url = f"http://localhost:8868/findCompanyByUserName"
        params = {"userName": user_name}
        try:
            response = requests.get(url, params=params)
            response.raise_for_status()  # 如果状态码不是200，将抛出异常
            result = response.json()  # 解析 JSON 响应
            company_data = result.get("data", {})
            company_name = company_data.get("companyName")
            if company_name:
                print(f"公司名称是：{company_name}")
            else:
                print("未能获取 companyName。返回数据：", company_data)

            payload = {
                "taskName": args.task_name,
                "modelPath": f'{args.output_path}result/train_best.ckpt',
                "pretrainModelPath": f'{args.output_path}result/pretrain_best.ckpt',
                "defaultParameters": "",
                "extractLabels": f'{args.output_path}result/extract_labels.csv',
                "userName": args.user_name,
                "companyName": company_name,
                "base_model": args.base_model,
            }
            url = "http://localhost:8868/models/addChildModel"

            response = requests.post(url, data=payload)
            if response.status_code == 200:
                print("成功调用接口：", response.json())
            else:
                print(f"调用失败，状态码：{response.status_code}，响应内容：{response.text}")

        except Exception as e:
            print(f"请求异常：{e}")

        # Todo 删除split文件

        # 通知训练完成
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=2")

    except Exception as e:
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{args.user_name}的scLTH训练任务{args.task_name}出错：{e}")
        # 设置任务为错误
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=-1")
        raise e

def extract_labels(input_path='label.csv', output_path='extract_labels.csv'):
    try:
        with open(input_path, 'r', encoding='utf-8') as f:
            reader = csv.reader(f)
            header = next(reader)  # 跳过表头
            labels = [row[0] for row in reader if row]  # 提取第一列标签

        # 去重并保持顺序
        unique_labels = list(OrderedDict.fromkeys(labels))

        # 打印输出
        print(f"共发现 {len(unique_labels)} 个唯一标签：")
        for label in unique_labels:
            print(f"- {label}")

        # 保存到 extract_labels.csv
        with open(output_path, 'w', encoding='utf-8', newline='') as f_out:
            writer = csv.writer(f_out)
            writer.writerow(['label'])  # 写表头
            for label in unique_labels:
                writer.writerow([label])

        print(f"\n标签已保存到文件：{output_path}")

    except FileNotFoundError:
        print(f"文件 {input_path} 不存在。")
    except Exception as e:
        print(f"读取或写入文件时出错：{e}")

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
    parser.add_argument('--checkpoint', type=str, required=True, help='Path to the model checkpoint file.')
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

    # 父模型
    parser.add_argument('--base_model', type=int, required=True, help='Base Model.')

    # 字符串格式参数
    parser.add_argument('--parameters', type=str, default='', help='Parameters.')

    args = parser.parse_args()

    # 验证参数
    if args.use_pretrained and not args.pretrain_path:
        print("错误：当使用预训练模型时，必须提供预训练模型路径 (--pretrain_path)")
        sys.exit(1)

    # 设置任务为处理中
    requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={args.task_name}&status=1")
    main(args)