import os
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from tqdm.auto import tqdm
from sklearn.metrics import accuracy_score, f1_score
from dataset import MyDatasetTrain
from scLTH import SCLTHTrain

class TrainModel:
    def __init__(self, base_dir,
                 batch_size=128,
                 n_epochs=96,
                 patience=32,
                 lr=5e-4,
                 weight_decay=5e-3,
                 dp=0.1,
                 input_dim=512,
                 num_layers=6,
                 nhead=8,
                 seed = 114514,
                 pretrain_model_path = ""):
        preprocess_folder = base_dir + 'data_split/'
        self.batch_size = batch_size
        self.n_epochs = n_epochs
        self.patience = patience
        self.lr = lr
        self.weight_decay = weight_decay
        self.dp = dp
        self.result_dir = f'{base_dir}result/'
        self.input_dim = input_dim
        self.num_layers = num_layers
        self.nhead = nhead
        self.seed = seed
        os.makedirs(self.result_dir, exist_ok=True)

        train_rna_path = f'{preprocess_folder}/X_rna_train.npy'
        test_rna_path = f'{preprocess_folder}/X_rna_test.npy'
        train_atac_path = f'{preprocess_folder}/X_atac_train.npy'
        test_atac_path = f'{preprocess_folder}/X_atac_test.npy'
        train_label_path = f'{preprocess_folder}/y_train.npy'
        test_label_path = f'{preprocess_folder}/y_test.npy'
        label_path = f'{preprocess_folder}/cell_types.npy'

        self.atac_train = np.load(train_atac_path)
        self.rna_train = np.load(train_rna_path)
        self.atac_test = np.load(test_atac_path)
        self.rna_test = np.load(test_rna_path)
        self.y_train = np.load(train_label_path)
        self.y_test = np.load(test_label_path)
        self.labels = np.load(label_path)
        self.pretrain_model_path = pretrain_model_path

    def train(self):
        device = "cuda" if torch.cuda.is_available() else "cpu"
        input_dim = self.atac_train.shape[1]
        num_classes = len(self.labels)

        model = SCLTHTrain(sourse_size=input_dim, num_classes=num_classes, dropout=self.dp, input_dim = self.input_dim, num_layers=self.num_layers, nhead=self.nhead).to(device)
        model_state_dict = torch.load(self.pretrain_model_path)
        model.encoder_rna.load_state_dict(
            {k.replace('encoder_rna.', ''): v for k, v in model_state_dict.items() if k.startswith('encoder_rna.')},
            strict=False)
        model.encoder_atac.load_state_dict(
            {k.replace('encoder_atac.', ''): v for k, v in model_state_dict.items() if k.startswith('encoder_atac.')},
            strict=False)
        model.combiner.load_state_dict(
            {k.replace('combiner.', ''): v for k, v in model_state_dict.items() if k.startswith('combiner.')},
            strict=False)
        model.tsm.load_state_dict(
            {k.replace('tsm.', ''): v for k, v in model_state_dict.items() if k.startswith('tsm.')}, strict=False)

        train_data = MyDatasetTrain(self.rna_train, self.atac_train, self.y_train)
        val_data = MyDatasetTrain(self.rna_test, self.atac_test, self.y_test)
        criterion_label = nn.CrossEntropyLoss()
        optimizer = torch.optim.Adam(model.parameters(), lr=self.lr, weight_decay=self.weight_decay)
        train_loader = DataLoader(train_data, batch_size=self.batch_size, shuffle=True, drop_last=True)
        val_loader = DataLoader(val_data, batch_size=self.batch_size, shuffle=False, drop_last=True)

        min_loss, max_acc, max_f1 = float('inf'), 0, 0
        best_epoch_loss, best_epoch_acc, best_epoch_f1, early_stop_epoch = -1, -1, -1, -1
        best_pred_loss, best_pred_acc, best_pred_f1 = None, None, None
        stale = 0
        max_acc_onbestloss, max_f1_onbestloss, max_f1_onbestacc = None, None, None

        with open(f"{self.result_dir}/trainresult.txt", "w") as f:
            f.write("Epoch\tTrain Loss\tTrain Acc\tTrain F1\tValidation Loss\tValidation Acc\tValidation F1\n")

        for epoch in range(self.n_epochs):
            model.train()
            train_loss, train_accs, train_f1s = [], [], []
            for batch in tqdm(train_loader, desc=f"Training Epoch {epoch + 1}/{self.n_epochs}"):
                rna, atac, labels = batch
                rna, atac, labels = rna.to(device).double(), atac.to(device).double(), labels.to(device).long()

                out = model(rna, atac)
                loss = criterion_label(out, labels)

                optimizer.zero_grad()
                loss.backward()
                nn.utils.clip_grad_norm_(model.parameters(), max_norm=10)
                optimizer.step()

                labels_np, preds_np = labels.cpu().numpy(), out.argmax(1).cpu().numpy()
                train_loss.append(loss.item())
                train_accs.append(accuracy_score(labels_np, preds_np))
                train_f1s.append(f1_score(labels_np, preds_np, average='macro'))

            train_loss = sum(train_loss) / len(train_loss)
            train_acc = sum(train_accs) / len(train_accs)
            train_f1 = sum(train_f1s) / len(train_f1s)
            print(
                f"[ Train | {epoch + 1:03d}/{self.n_epochs:03d} ] loss = {train_loss:.5f}, acc = {train_acc:.5f}, f1 = {train_f1:.5f}")

            model.eval()
            valid_loss, val_accs, val_f1s, all_preds = [], [], [], []
            for batch in tqdm(val_loader, desc="Validating"):
                rna, atac, labels = batch
                rna, atac, labels = rna.to(device).double(), atac.to(device).double(), labels.to(device).long()
                with torch.no_grad():
                    out = model(rna, atac)
                    loss = criterion_label(out, labels)
                valid_loss.append(loss.item())

                labels_np, preds_np = labels.cpu().numpy(), out.argmax(1).cpu().numpy()
                val_accs.append(accuracy_score(labels_np, preds_np))
                val_f1s.append(f1_score(labels_np, preds_np, average='macro'))
                all_preds.extend(preds_np)

            valid_loss = sum(valid_loss) / len(valid_loss)
            val_acc = sum(val_accs) / len(val_accs)
            val_f1 = sum(val_f1s) / len(val_f1s)

            print(
                f"[ Valid | {epoch + 1:03d}/{self.n_epochs:03d} ] loss = {valid_loss:.5f}, acc = {val_acc:.5f}, f1 = {val_f1:.5f}")

            with open(f"{self.result_dir}/trainresult.txt", "a") as f:
                f.write(f"{epoch + 1}\t{train_loss:.5f}\t{train_acc:.5f}\t{train_f1:.5f}\t{valid_loss:.5f}\t{val_acc:.5f}\t{val_f1:.5f}\n")

            # best loss
            if valid_loss < min_loss:
                min_loss = valid_loss
                best_epoch_loss = epoch + 1
                best_pred_loss = all_preds
                print(f"--> (Best)")
                max_acc_onbestloss = val_acc
                max_f1_onbestloss = val_f1
                torch.save(model.state_dict(), f"{self.result_dir}/train_best.ckpt")
            # best acc
            if val_acc > max_acc:
                max_acc = val_acc
                best_epoch_acc = epoch + 1
                best_pred_acc = all_preds
                max_f1_onbestacc = val_f1
            # best f1
            if val_f1 > max_f1:
                max_f1 = val_f1
                best_epoch_f1 = epoch + 1

            if valid_loss > min_loss:
                stale += 1
                if stale > self.patience:
                    early_stop_epoch = epoch + 1
                    break
            else:
                stale = 0

        # Save results
        with open(f"{self.result_dir}/trainresult_pred.txt", "w") as f_pred:
            f_pred.write("\n".join(map(str, best_pred_loss)))
        with open(f"{self.result_dir}/trainresult.txt", "a") as f:
            f.write(f"Best Validation Loss: {min_loss:.5f} (Epoch {best_epoch_loss})\n")
            f.write(f"Best Validation Accuracy (Best Loss): {max_acc_onbestloss:.5f} (Epoch {best_epoch_loss})\n")
            f.write(f"Best Validation F1 (Best Loss): {max_f1_onbestloss:.5f} (Epoch {best_epoch_loss})\n\n")

            f.write(f"Best Validation Accuracy: {max_acc:.5f} (Epoch {best_epoch_acc})\n")
            f.write(f"Best Validation F1 (Best Acc): {max_f1_onbestacc:.5f} (Epoch {best_epoch_acc})\n\n")

            f.write(f"Best Validation F1: {max_f1:.5f} (Epoch {best_epoch_f1})\n")
            f.write(f"Early Stopping at Epoch {early_stop_epoch}, Acc: {val_acc:.5f}, F1: {val_f1:.5f}\n")