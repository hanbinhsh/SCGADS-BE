import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from tqdm.auto import tqdm
import torch.nn.functional as F
from sklearn.model_selection import train_test_split
from torch.utils.tensorboard import SummaryWriter

def kl_divergence(mu, logvar):
    return -0.5 * torch.sum(1 + logvar - mu.pow(2) - logvar.exp(), dim=1)

def binary_cross_entropy(recon_x, x):
    return -torch.sum(x * torch.log(recon_x + 1e-8) + (1 - x) * torch.log(1 - recon_x + 1e-8), dim=-1)

def elbo(recon_x, x, z_params, binary=True):
    mu, logvar = z_params
    kld = kl_divergence(mu, logvar)
    if binary:
        likelihood = -binary_cross_entropy(recon_x, x)
    else:
        likelihood = -F.mse_loss(recon_x, x)
    return torch.sum(likelihood), torch.sum(kld)

class Encoder(nn.Module):
    def __init__(self, input_dim, dropout=0):
        super(Encoder, self).__init__()
        self.net = nn.Sequential(
            nn.Linear(input_dim, 1024),
            nn.BatchNorm1d(1024),
            nn.ReLU(),
            nn.Dropout(dropout),

            nn.Linear(1024, 256),
            nn.BatchNorm1d(256),
            nn.ReLU(),
            nn.Dropout(dropout),

            nn.Linear(256, 128),
            nn.BatchNorm1d(128),
            nn.ReLU(),
            nn.Dropout(dropout),
        )
        self.sample = GaussianSample(128, 128)

    def forward(self, x):
        x = self.net(x)
        return self.sample(x)

class Decoder(nn.Module):
    def __init__(self, out_dim, dropout=0):
        super(Decoder, self).__init__()
        self.net = nn.Sequential(
            nn.Linear(512, 1024),
            nn.BatchNorm1d(1024),
            nn.ReLU(),
            nn.Dropout(dropout),

            nn.Linear(1024, 2048),
            nn.BatchNorm1d(2048),
            nn.ReLU(),
            nn.Dropout(dropout),

            nn.Linear(2048, out_dim),
            nn.BatchNorm1d(out_dim),
            nn.ReLU(),
            nn.Dropout(dropout),
        )

    def forward(self, x):
        x = self.net(x)
        return x

class Combiner(nn.Module):
    def __init__(self, dropout=0):
        super(Combiner, self).__init__()
        self.combine = nn.Sequential(
            nn.Linear(256, 512),
            nn.BatchNorm1d(512),
            nn.LeakyReLU(),
            nn.Dropout(dropout),
        )

    def forward(self, x, y):
        z = torch.cat((x, y), dim=1)
        z = self.combine(z)
        return z

class TransformerModel(nn.Module):
    def __init__(self, input_dim, num_layers=6, nhead=8):
        super(TransformerModel, self).__init__()
        self.transformer = nn.Transformer(
            d_model=input_dim,
            nhead=nhead,
            num_encoder_layers=num_layers,
            num_decoder_layers=num_layers
        )
        self.fc_out = nn.Linear(input_dim, input_dim)

    def forward(self, x):
        x = self.transformer(x, x)
        return self.fc_out(x)

class TeacherStudentModel(nn.Module):
    def __init__(self, input_dim, pretrain = True, num_layers=6, nhead=8):
        super(TeacherStudentModel, self).__init__()
        self.teacher_model = TransformerModel(input_dim=input_dim, num_layers=num_layers, nhead=nhead)
        self.student_model = TransformerModel(input_dim=input_dim, num_layers=num_layers, nhead=nhead)
        self.pretrain = pretrain

    def forward(self, x):
        if self.pretrain:
            teacher_out = self.teacher_model(x.unsqueeze(1))
            teacher_out_noise = teacher_out + torch.randn_like(teacher_out) * 0.1
            student_out = self.student_model(teacher_out_noise)
            return teacher_out, student_out
        else:
            student_out = self.student_model(x.unsqueeze(1))
            return student_out

class Stochastic(nn.Module):
    def reparametrize(self, mu, logvar):
        epsilon = torch.randn(mu.size(), requires_grad=False, device=mu.device)  # 生成随机正态分布噪声
        std = logvar.mul(0.5).exp_()  # 计算标准差
        z = mu.addcmul(std, epsilon)
        return z

class GaussianSample(Stochastic):
    def __init__(self, in_features, out_features):
        super(GaussianSample, self).__init__()
        self.in_features = in_features
        self.out_features = out_features

        self.mu = nn.Linear(in_features, out_features)
        self.log_var = nn.Linear(in_features, out_features)

    def forward(self, x):
        mu = self.mu(x)
        log_var = self.log_var(x)

        return self.reparametrize(mu, log_var), mu, log_var

class SCLTH(nn.Module):
    def __init__(self, sourse_size, target_size, dropout=0, input_dim=512, num_layers=6, nhead=8):
        super(SCLTH, self).__init__()
        self.encoder_rna = Encoder(sourse_size, dropout)
        self.encoder_atac = Encoder(target_size, dropout)
        self.decoder_rna = Decoder(sourse_size, dropout)
        self.decoder_atac = Decoder(target_size, dropout)
        self.combiner = Combiner(dropout)
        self.tsm = TeacherStudentModel(input_dim=input_dim, num_layers=num_layers, nhead=nhead)

    def forward(self, x, y):
        z_rna, mu_rna, log_var_rna = self.encoder_rna(x)
        z_atac, mu_atac, log_var_atac = self.encoder_atac(y)
        z = self.combiner(z_rna, z_atac)
        z_rna_recon = self.decoder_rna(z)
        z_atac_recon = self.decoder_atac(z)
        teacher_output, student_output = self.tsm(z)

        r_likelihood, r_kl_loss = elbo(z_rna_recon, x, (mu_rna, log_var_rna), binary=False)
        a_likelihood, a_kl_loss = elbo(z_atac_recon, y, (mu_atac, log_var_atac), binary=False)
        return z_rna_recon, z_atac_recon, (-r_likelihood + a_likelihood).mean(), (
                    r_kl_loss + a_kl_loss).mean(), teacher_output, student_output

class Classifier(nn.Module):
    def __init__(self, vae_hidden_size, num_class, dropout=0):
        super(Classifier, self).__init__()
        self.fc_out = nn.Sequential(
            nn.Linear(vae_hidden_size, 256),
            nn.BatchNorm1d(256),
            nn.ReLU(),
            nn.Dropout(dropout),

            nn.Linear(256, 128),
            nn.BatchNorm1d(128),
            nn.ReLU(),
            nn.Dropout(dropout),

            nn.Linear(128, num_class),
            nn.BatchNorm1d(num_class),
            nn.ReLU(),
            nn.Dropout(dropout),
        )

    def forward(self, x):
        return self.fc_out(x.squeeze(1))

class SCLTHTrain(nn.Module):
    def __init__(self, sourse_size, num_classes, dropout=0, input_dim=512, num_layers=6, nhead=8):
        super(SCLTHTrain, self).__init__()
        self.encoder_rna = Encoder(sourse_size, dropout)
        self.encoder_atac = Encoder(sourse_size, dropout)
        self.combiner = Combiner(dropout)
        self.tsm = TeacherStudentModel(input_dim = input_dim, pretrain = False, num_layers=num_layers, nhead=nhead)
        self.classifier = Classifier(512, num_classes)

    def forward(self, x, y):
        z_rna, _, _ = self.encoder_rna(x)
        z_atac, _, _ = self.encoder_atac(y)
        z = self.combiner(z_rna, z_atac)
        student_output = self.tsm(z)
        out = self.classifier(student_output)
        return out