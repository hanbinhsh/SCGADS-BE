import os
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from tqdm.auto import tqdm
from sklearn.metrics import accuracy_score, f1_score
from dataset import MyDatasetTrain
from scTCHCN import StudentModel, scTCHCNTrain

# !!! 必须与调试模式一致 !!!
DEBUG_DIM = 500

class TrainModel:
    def __init__(self, base_dir,
                 batch_size=128,
                 n_epochs=96,
                 patience=32,
                 lr=1e-3,
                 weight_decay=5e-3,
                 seed=114514,
                 pretrain_model_path=""):
        preprocess_folder = base_dir + 'data_split/'
        self.batch_size = batch_size
        self.n_epochs = n_epochs
        self.patience = patience
        self.lr = lr
        self.weight_decay = weight_decay
        self.result_dir = f'{base_dir}result/'
        self.seed = seed
        self.pretrain_model_path = pretrain_model_path
        os.makedirs(self.result_dir, exist_ok=True)

        self.x_seq_train = np.load(f'{preprocess_folder}/x_seq_train.npy')
        self.y_train = np.load(f'{preprocess_folder}/y_train.npy')
        self.x_seq_test = np.load(f'{preprocess_folder}/x_seq_test.npy')
        self.y_test = np.load(f'{preprocess_folder}/y_test.npy')
        self.labels = np.load(f'{preprocess_folder}/cell_types.npy')

    def train(self):
        device = "cuda" if torch.cuda.is_available() else "cpu"
        num_classes = len(self.labels)

        # 初始化 Student Model
        # !!! 关键修复：seq_length 必须是 500，不能是 10000 !!!
        student = StudentModel(input_dim=1, hidden_dim=200, seq_length=DEBUG_DIM, num_heads=4, num_layers=1)

        if os.path.exists(self.pretrain_model_path):
            print(f"Loading pretrained model from {self.pretrain_model_path}")
            # 加载预训练权重时，如果不匹配（比如上次预训练是10000维），这里会报错，提醒你需要删掉旧文件
            try:
                student.load_state_dict(torch.load(self.pretrain_model_path, map_location=device))
            except RuntimeError as e:
                print("Error loading pretrained model. Dimensions mismatch. Ignoring pretrain weights.")
                print(e)
        else:
            print("Warning: Pretrained model not found, training from scratch.")

        # 初始化分类训练模型
        model = scTCHCNTrain(student_model=student, num_classes=num_classes).to(device)
        # !!! 关键修改：强制模型转换为 float32 !!!
        model = model.float()

        train_data = MyDatasetTrain(self.x_seq_train, self.y_train)
        val_data = MyDatasetTrain(self.x_seq_test, self.y_test)

        criterion = nn.CrossEntropyLoss()
        optimizer = torch.optim.Adam(model.parameters(), lr=self.lr, weight_decay=self.weight_decay)

        train_loader = DataLoader(train_data, batch_size=self.batch_size, shuffle=True, drop_last=True)
        # val_loader drop_last=False 防止数据丢失
        val_loader = DataLoader(val_data, batch_size=self.batch_size, shuffle=False, drop_last=False)

        min_loss, max_acc = float('inf'), 0
        best_epoch_loss, best_epoch_acc = -1, -1
        stale = 0

        with open(f"{self.result_dir}/trainresult.txt", "w") as f:
            f.write("Epoch\tTrain Loss\tTrain Acc\tValidation Loss\tValidation Acc\n")

        for epoch in range(self.n_epochs):
            model.train()
            train_loss, train_accs = [], []
            for batch in tqdm(train_loader, desc=f"Training Epoch {epoch + 1}/{self.n_epochs}"):
                x_seq, labels = batch
                # !!! 关键修改：输入为 float !!!
                x_seq = x_seq.to(device).float()
                labels = labels.to(device).long()

                out = model(x_seq)
                loss = criterion(out, labels)

                optimizer.zero_grad()
                loss.backward()
                optimizer.step()

                labels_np, preds_np = labels.cpu().numpy(), out.argmax(1).cpu().numpy()
                train_loss.append(loss.item())
                train_accs.append(accuracy_score(labels_np, preds_np))

            train_loss_avg = sum(train_loss) / len(train_loss)
            train_acc_avg = sum(train_accs) / len(train_accs)

            model.eval()
            valid_loss, val_accs = [], []
            with torch.no_grad():
                for batch in val_loader:
                    x_seq, labels = batch
                    x_seq = x_seq.to(device).float()
                    labels = labels.to(device).long()

                    out = model(x_seq)
                    loss = criterion(out, labels)

                    valid_loss.append(loss.item())
                    labels_np, preds_np = labels.cpu().numpy(), out.argmax(1).cpu().numpy()
                    val_accs.append(accuracy_score(labels_np, preds_np))

            valid_loss_avg = sum(valid_loss) / len(valid_loss)
            val_acc_avg = sum(val_accs) / len(val_accs)

            print(f"[ Valid | {epoch + 1:03d}/{self.n_epochs:03d} ] loss = {valid_loss_avg:.5f}, acc = {val_acc_avg:.5f}")

            with open(f"{self.result_dir}/trainresult.txt", "a") as f:
                f.write(f"{epoch + 1}\t{train_loss_avg:.5f}\t{train_acc_avg:.5f}\t{valid_loss_avg:.5f}\t{val_acc_avg:.5f}\n")

            if valid_loss_avg < min_loss:
                min_loss = valid_loss_avg
                best_epoch_loss = epoch + 1
                torch.save(model.state_dict(), f"{self.result_dir}/train_best.ckpt")
                stale = 0
            else:
                stale += 1
                if stale > self.patience:
                    break

        with open(f"{self.result_dir}/trainresult.txt", "a") as f:
            f.write(f"Best Validation Loss: {min_loss:.5f} (Epoch {best_epoch_loss})\n")