import os
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from tqdm.auto import tqdm
from scTCHCN import scTCHCN, MinkowskiLoss
from dataset import MyDatasetPretrain
# 引入 AMP
from torch.cuda.amp import autocast, GradScaler

class PreTrain:
    def __init__(self, base_dir,
                 batch_size=12,
                 n_epochs=96,
                 patience=32,
                 lr=1e-3):
        preprocess_folder = base_dir + 'data_split/'
        self.batch_size = batch_size
        self.n_epochs = n_epochs
        self.patience = patience
        self.lr = lr

        # Load Data
        self.x_seq_train = np.load(f'{preprocess_folder}/x_seq_train.npy')
        self.x_graph_train = np.load(f'{preprocess_folder}/x_graph_train.npy')
        self.adj_train = np.load(f'{preprocess_folder}/adj_train.npy')

        self.x_seq_test = np.load(f'{preprocess_folder}/x_seq_test.npy')
        self.x_graph_test = np.load(f'{preprocess_folder}/x_graph_test.npy')
        self.adj_test = np.load(f'{preprocess_folder}/adj_test.npy')

        # 自动获取输入维度 (应该是 500)
        self.input_dim = self.x_seq_train.shape[1]
        print(f"Detected Input Dimension: {self.input_dim}")

        self.result_folder = f'{base_dir}result/'
        os.makedirs(self.result_folder, exist_ok=True)

    def pretrain(self):
        train_data = MyDatasetPretrain(self.x_seq_train, self.x_graph_train, self.adj_train)
        val_data = MyDatasetPretrain(self.x_seq_test, self.x_graph_test, self.adj_test)

        device = "cuda" if torch.cuda.is_available() else "cpu"

        # Initialize Model
        # 动态设置维度，graph_output_dim 固定为 250
        model = scTCHCN(
            seq_input_dim=1, seq_embed_dim=200,
            graph_input_dim=250, graph_embed_dim=5000,
            teacher_input_dim=1, teacher_embed_dim=200,
            student_input_dim=1, student_embed_dim=200,
            graph_output_dim=250
        ).to(device)

        # 这一步对于 Linformer 很重要，需要传入正确的 seq_len
        # 由于我们只修改了 dataset，需要确保模型内部的 PositionalEncoding 和 Linformer 使用正确的长度
        # scTCHCN 默认是 10000，我们需要在 scTCHCN.py 实例化时传入 seq_length
        # 为了不修改 scTCHCN.py 的构造函数签名太复杂，建议在 scTCHCN.py 里把默认值 10000 改为 500
        # 或者在这里修改 scTCHCN 的 __init__ 调用（需要你修改 scTCHCN.py 接收 seq_length 参数）
        # **为了最快解决，我们假设你已经按步骤1把数据降维到500**
        # 并且我们需要去修改 scTCHCN.py 让它接受 500 长度

        model = model.float()

        criterion1 = nn.MSELoss().to(device)
        criterion2 = nn.MSELoss().to(device)
        criterion3 = nn.MSELoss().to(device)
        criterion_minkowski = MinkowskiLoss(p=50).to(device)

        optimizer = torch.optim.Adam(model.parameters(), lr=self.lr)
        scheduler = torch.optim.lr_scheduler.LambdaLR(optimizer, lambda epoch: 0.96 ** (epoch))
        scaler = GradScaler() # 初始化 Scaler

        train_loader = DataLoader(train_data, batch_size=self.batch_size, shuffle=True, drop_last=True, num_workers=4) # 增加 num_workers
        val_loader = DataLoader(val_data, batch_size=self.batch_size, shuffle=False, drop_last=True, num_workers=4)

        stale = 0
        min_loss = float('inf')
        result_file = os.path.join(self.result_folder, 'pretrainresult.txt')

        with open(result_file, 'w') as f:
            f.write("Epoch\tTrain Loss\tValidation Loss\n")

        for epoch in range(self.n_epochs):
            model.train()
            train_loss = []

            for batch in tqdm(train_loader, desc=f"Pretrain Epoch {epoch+1}/{self.n_epochs}"):
                x_seq, x_graph, adj = batch
                x_seq = x_seq.to(device).float()
                x_graph = x_graph.to(device).float()
                adj = adj.to(device).float()

                x_seq_view = x_seq.view(x_seq.size(0), -1)
                x_graph_view = x_graph.view(x_graph.size(0), -1)

                # 使用 AMP 上下文
                with autocast():
                    teacher_feature, student_feature, seq_decoder, graph_decoder = model(x_seq, x_graph, adj)

                    graph_decoder_flat = graph_decoder.reshape(graph_decoder.size(0), -1)

                    loss1 = criterion1(student_feature, teacher_feature)
                    loss2 = criterion2(seq_decoder, x_seq_view)
                    loss3 = criterion3(graph_decoder_flat, x_graph_view)
                    loss_m = criterion_minkowski(seq_decoder, student_feature)

                    loss = loss1 + loss2 + loss3 + loss_m

                optimizer.zero_grad()
                # 使用 scaler 进行反向传播
                scaler.scale(loss).backward()
                scaler.step(optimizer)
                scaler.update()

                train_loss.append(loss.item())

            scheduler.step()
            train_loss_avg = sum(train_loss) / len(train_loss)

            # Validation
            model.eval()
            valid_loss = []
            with torch.no_grad():
                for batch in val_loader:
                    x_seq, x_graph, adj = batch
                    x_seq = x_seq.to(device).float()
                    x_graph = x_graph.to(device).float()
                    adj = adj.to(device).float()

                    x_seq_view = x_seq.view(x_seq.size(0), -1)
                    x_graph_view = x_graph.view(x_graph.size(0), -1)

                    # 验证集也可以开 autocast 加速推理
                    with autocast():
                        teacher_feature, student_feature, seq_decoder, graph_decoder = model(x_seq, x_graph, adj)
                        graph_decoder_flat = graph_decoder.reshape(graph_decoder.size(0), -1)

                        loss1 = criterion1(student_feature, teacher_feature)
                        loss2 = criterion2(seq_decoder, x_seq_view)
                        loss3 = criterion3(graph_decoder_flat, x_graph_view)
                        loss_m = criterion_minkowski(seq_decoder, student_feature)
                        loss = loss1 + loss2 + loss3 + loss_m

                    valid_loss.append(loss.item())

            valid_loss_avg = sum(valid_loss) / len(valid_loss)
            print(f"[ Pretrain | {epoch + 1:03d}/{self.n_epochs:03d} ] Train Loss: {train_loss_avg:.5f}, Valid Loss: {valid_loss_avg:.5f}")

            with open(result_file, 'a') as f:
                f.write(f"{epoch + 1}\t{train_loss_avg:.5f}\t{valid_loss_avg:.5f}\n")

            if valid_loss_avg < min_loss:
                torch.save(model.state_dict(), os.path.join(self.result_folder, "pretrain_best.ckpt"))
                torch.save(model.studentModel.state_dict(), os.path.join(self.result_folder, "pretrain_student_best.pt"))
                min_loss = valid_loss_avg
                stale = 0
            else:
                stale += 1
                if stale > self.patience:
                    break