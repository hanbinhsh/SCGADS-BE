from dataset import Dataset
from pretrain import PreTrain
from train import TrainModel
import torch
import numpy as np

preprocess_folder = '../seqData/'
dataset_name = 'mouse_skin_shareseq_rna_10k'
torch.backends.cudnn.deterministic = True
torch.backends.cudnn.benchmark = False
################################################
# hyperparameters
pretrain_state = False
seed = 1224455
#
n_epochs         = 96
dropout          = 0.05
batch_size       = 128
patience         = 8
input_dim        = 512
num_layers       = 16
nhead            = 16
lr               = 5e-4
weight_decay     = 5e-3
################################################
np.random.seed(seed)
torch.manual_seed(seed)
if torch.cuda.is_available():
    print('gpu')
    torch.cuda.manual_seed_all(seed)

if pretrain_state:
    Dataset.split_dataset(dataset_name = dataset_name,
                          rna_path = '%s/mouse_skin_shareseq_rna_10k/rna.h5ad' % preprocess_folder,
                          atac_path = '%s/mouse_skin_shareseq_rna_10k/atac.h5ad' % preprocess_folder,
                          label_path = '%s/mouse_skin_shareseq_rna_10k/Label.csv' % preprocess_folder,
                          seed=seed)
    pretrain = PreTrain(dataset_name=dataset_name,
                        n_epochs = n_epochs,
                        dropout = dropout,
                        batch_size = batch_size,
                        patience = patience,
                        input_dim = input_dim,
                        num_layers = num_layers,
                        nhead = nhead,
                        lr = lr,
                        weight_decay=weight_decay)
    pretrain.pretrain()
else:
    train_model = TrainModel(dataset_name=dataset_name,
                             batch_size = batch_size,
                             n_epochs = n_epochs,
                             patience = patience,
                             lr = lr,
                             weight_decay = weight_decay,
                             dp = dropout,
                             input_dim = input_dim,
                             num_layers = num_layers,
                             nhead = nhead,
                             seed = seed)
    train_model.train()
