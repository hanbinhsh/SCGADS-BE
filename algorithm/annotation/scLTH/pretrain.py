import os
from scLTH import SCLTH
from dataset import MyDataset
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from tqdm.auto import tqdm

class PreTrain:
    def __init__(self, base_dir,
                 dropout=0,
                 batch_size=128,
                 n_epochs=96,
                 patience=32,
                 input_dim=512,
                 num_layers=6,
                 nhead=8,
                 lr=5e-4,
                 weight_decay=1e-3):
        preprocess_folder = base_dir + 'data_split/'
        self.dropout = dropout
        self.batch_size = batch_size
        self.n_epochs = n_epochs
        self.patience = patience
        train_rna_path = '%s/x_rna_train.npy' % preprocess_folder
        test_rna_path = '%s/x_rna_test.npy' % preprocess_folder
        train_atac_path = '%s/x_atac_train.npy' % preprocess_folder
        test_atac_path = '%s/x_atac_test.npy' % preprocess_folder
        self.target_data = np.load(train_atac_path)
        self.source_data = np.load(train_rna_path)
        self.val_traget_data = np.load(test_atac_path)
        self.val_source_data = np.load(test_rna_path)
        self.result_folder = f'{base_dir}result/'  # Result folder
        os.makedirs(self.result_folder, exist_ok=True)  # Create result folder if not exists
        self.input_dim = input_dim
        self.num_layers = num_layers
        self.nhead = nhead
        self.lr = lr
        self.weight_decay = weight_decay

    def pretrain(self):
        train_data = MyDataset(self.source_data, self.target_data)
        val_data = MyDataset(self.val_source_data, self.val_traget_data)
        device = "cuda" if torch.cuda.is_available() else "cpu"
        model = SCLTH(train_data.rna.shape[1], train_data.atac.shape[1], self.dropout, input_dim = self.input_dim, num_layers=self.num_layers, nhead=self.nhead).to(device)
        criterion = nn.MSELoss()
        optimizer = torch.optim.Adam(model.parameters(), lr=self.lr, weight_decay=self.weight_decay)
        train_loader = DataLoader(train_data, batch_size=self.batch_size, shuffle=True, drop_last=True)
        val_loader = DataLoader(val_data, batch_size=self.batch_size, shuffle=True, drop_last=True)
        stale = 0
        min_loss = float('inf')
        best_epoch = 0
        result_file = os.path.join(self.result_folder, 'pretrainresult.txt')

        with open(result_file, 'w') as f:
            f.write("Epoch\tTrain Loss\tValidation Loss\n")

        for epoch in range(self.n_epochs):
            model.train()
            train_loss = []
            for batch in tqdm(train_loader):
                rna, atac = batch
                rna = rna.double()
                atac = atac.double()
                rna_r, atac_r, loss_r, kl_loss, teacher_out, student_out = model(rna.to(device), atac.to(device))
                loss_rna = criterion(rna_r.to(device), rna.to(device))
                loss_atac = criterion(atac_r.to(device), atac.to(device))
                loss_ts = criterion(teacher_out.to(device), student_out.to(device))
                loss = loss_rna + loss_atac + loss_ts + loss_r + kl_loss
                optimizer.zero_grad()
                loss.backward()
                nn.utils.clip_grad_norm_(model.parameters(), max_norm=10)
                optimizer.step()
                train_loss.append(loss.item())
            train_loss = sum(train_loss) / len(train_loss)
            print(f"[ Train | {epoch + 1:03d}/{self.n_epochs:03d} ] loss = {train_loss:.5f}")

            model.eval()
            valid_loss = []
            for batch in tqdm(val_loader):
                rna, atac = batch
                rna = rna.double()
                atac = atac.double()
                with torch.no_grad():
                    rna_r, atac_r, loss_r, kl_loss, teacher_out, student_out = model(rna.to(device), atac.to(device))
                loss_rna = criterion(rna_r.to(device), rna.to(device))
                loss_atac = criterion(atac_r.to(device), atac.to(device))
                loss_ts = criterion(teacher_out.to(device), student_out.to(device))
                loss = loss_rna + loss_atac + loss_ts + loss_r + kl_loss
                valid_loss.append(loss.item())
            valid_loss = sum(valid_loss) / len(valid_loss)
            print(f"[ Valid | {epoch + 1:03d}/{self.n_epochs:03d} ] loss = {valid_loss:.5f}")

            with open(result_file, 'a') as f:
                f.write(f"{epoch + 1}\t{train_loss:.5f}\t{valid_loss:.5f}\n")

            if valid_loss < min_loss:
                print(f"Best model found at epoch {epoch + 1}, saving model")
                torch.save(model.state_dict(), os.path.join(self.result_folder, "pretrain_best.ckpt"))
                min_loss = valid_loss
                best_epoch = epoch + 1
                stale = 0
            else:
                stale += 1
                if stale > self.patience:
                    print(f"No improvement {self.patience} consecutive epochs, early stopping")
                    break

        with open(result_file, 'a') as f:
            f.write(f"Best Validation Loss at Epoch: {best_epoch}\n")
            f.write(f"Early Stopping at Epoch: {epoch + 1}\n")
