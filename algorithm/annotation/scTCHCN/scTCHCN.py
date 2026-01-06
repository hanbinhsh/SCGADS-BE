import torch
import torch.nn as nn
import torch.nn.functional as F
from linformer import Linformer
from torch_geometric.nn import GATConv, GCNConv, SAGEConv, GINConv, TransformerConv

# 调试维度
DEBUG_SEQ_LEN = 500

class PositionalEncoding(nn.Module):
    def __init__(self, d_model, max_len=DEBUG_SEQ_LEN):
        super(PositionalEncoding, self).__init__()
        pe = torch.zeros(max_len, d_model)
        position = torch.arange(0, max_len, dtype=torch.float).unsqueeze(1)
        div_term = torch.exp(torch.arange(0, d_model, 2).float() * (-torch.log(torch.tensor(float(max_len))) / d_model))
        pe[:, 0::2] = torch.sin(position * div_term)
        pe[:, 1::2] = torch.cos(position * div_term)
        pe = pe.unsqueeze(0)
        self.register_buffer('pe', pe)

    def forward(self, x):
        x = x + self.pe[:, :x.size(1)]
        return x

class MinkowskiLoss(nn.Module):
    def __init__(self, p=3):
        super(MinkowskiLoss, self).__init__()
        self.p = p

    def forward(self, x, x_hat):
        loss_per_sample = torch.sum(torch.abs(x - x_hat) ** self.p, dim=1) ** (1 / self.p)
        return torch.mean(loss_per_sample)

class LinformerSeqEncoder(nn.Module):
    def __init__(self, input_dim, embed_dim, seq_length=DEBUG_SEQ_LEN, num_heads=4, num_layers=1):
        super(LinformerSeqEncoder, self).__init__()
        self.embedding = nn.Linear(input_dim, embed_dim)
        self.positional_encoding = PositionalEncoding(embed_dim, max_len=seq_length)
        self.linformer = Linformer(dim=embed_dim, seq_len=seq_length, depth=num_layers, heads=num_heads, k=64)
        self.fc = nn.Linear(seq_length, seq_length // 2)

    def forward(self, x):
        x = x.unsqueeze(-1).float()
        x = self.embedding(x)
        x = self.positional_encoding(x)
        x = self.linformer(x)
        x = x.permute(0, 2, 1)
        x = self.fc(x)
        x = x.permute(0, 2, 1)
        x = torch.sum(x, dim=2, keepdim=True)
        x = x.reshape(x.size(0), -1)
        return x

class LinformerSeqDecoder(nn.Module):
    def __init__(self, input_dim, embed_dim):
        super(LinformerSeqDecoder, self).__init__()
        self.fc = nn.Linear(DEBUG_SEQ_LEN // 2, DEBUG_SEQ_LEN)
        self.linformer = Linformer(dim=embed_dim, seq_len=DEBUG_SEQ_LEN//2, depth=1, heads=4, k=64)
        self.positional_encoding = PositionalEncoding(embed_dim, max_len=DEBUG_SEQ_LEN)
        self.embedding = nn.Linear(input_dim, embed_dim)

    def forward(self, x):
        x = x.unsqueeze(-1)
        x = self.embedding(x)
        x = self.positional_encoding(x)
        x = self.linformer(x)
        x = x.permute(0, 2, 1)
        x = self.fc(x)
        x = x.permute(0, 2, 1)
        x = torch.sum(x, dim=2, keepdim=True)
        x = x.reshape(x.size(0), -1)
        return x

class GraphEncoder(nn.Module):
    def __init__(self, input_dim, hidden_dim, gnn_type='GIN', num_heads=4):
        super(GraphEncoder, self).__init__()
        if gnn_type == 'GIN':
            nn1 = nn.Sequential(nn.Linear(input_dim, hidden_dim), nn.ReLU(), nn.Linear(hidden_dim, hidden_dim))
            self.encoder = GINConv(nn1)
        else:
            raise ValueError(f"For this implementation, use GIN")

    def forward(self, x, edge_index):
        edge_index = edge_index.long()
        x = x.float()
        x = self.encoder(x, edge_index)
        return x

class GraphDecoder(nn.Module):
    def __init__(self, hidden_dim, output_dim, gnn_type='GIN', num_heads=4):
        super(GraphDecoder, self).__init__()
        nn2 = nn.Sequential(nn.Linear(hidden_dim, output_dim), nn.ReLU(), nn.Linear(output_dim, output_dim))
        self.decoder = GINConv(nn2)

    def forward(self, x, edge_index):
        edge_index = edge_index.long()
        x = self.decoder(x, edge_index)
        return x

class TeacherModel(nn.Module):
    def __init__(self, input_dim, embed_dim, seq_length=DEBUG_SEQ_LEN, num_heads=4, num_layers=3):
        super(TeacherModel, self).__init__()
        self.embedding = nn.Linear(input_dim, embed_dim)
        self.positional_encoding = PositionalEncoding(embed_dim, max_len=seq_length)
        self.linformer = Linformer(dim=embed_dim, seq_len=seq_length, depth=num_layers, heads=num_heads, k=64)
        self.fc = nn.Linear(embed_dim, input_dim)

    def forward(self, x):
        x = x.unsqueeze(-1)
        x = self.embedding(x)
        x = self.positional_encoding(x)
        x = self.linformer(x)
        x = self.fc(x)
        x = torch.sum(x, dim=2, keepdim=True)
        x = x.reshape(x.size(0), -1)
        return x

class StudentModel(nn.Module):
    def __init__(self, input_dim, hidden_dim, seq_length=DEBUG_SEQ_LEN, num_heads=4, num_layers=1):
        super(StudentModel, self).__init__()
        self.embedding = nn.Linear(input_dim, hidden_dim)
        self.positional_encoding = PositionalEncoding(hidden_dim, max_len=seq_length)
        self.linformer = Linformer(dim=hidden_dim, seq_len=seq_length, depth=num_layers, heads=num_heads, k=64)
        self.fc = nn.Linear(hidden_dim, input_dim)

    def forward(self, x):
        x = x.float().unsqueeze(-1)
        x = self.embedding(x)
        x = self.positional_encoding(x)
        x = self.linformer(x)
        x = self.fc(x)
        x = torch.sum(x, dim=2, keepdim=True)
        x = x.reshape(x.size(0), -1)
        return x

class scTCHCN(nn.Module):
    def __init__(self, seq_input_dim=1, seq_embed_dim=200,
                 graph_input_dim=250, graph_embed_dim=DEBUG_SEQ_LEN//2,
                 teacher_input_dim=1, teacher_embed_dim=200,
                 student_input_dim=1, student_embed_dim=200,
                 graph_output_dim=250):
        super(scTCHCN, self).__init__()

        self.linformerSeqEncoder = LinformerSeqEncoder(input_dim=seq_input_dim, embed_dim=seq_embed_dim, seq_length=DEBUG_SEQ_LEN)
        self.linformerSeqDecoder = LinformerSeqDecoder(input_dim=seq_input_dim, embed_dim=seq_embed_dim)

        graph_embed_actual = DEBUG_SEQ_LEN // 2

        self.GraphEncoder = GraphEncoder(graph_input_dim, graph_embed_actual)
        self.GraphDecoder = GraphDecoder(graph_input_dim, graph_output_dim)

        self.teacherModel = TeacherModel(teacher_input_dim, teacher_embed_dim, seq_length=DEBUG_SEQ_LEN)
        self.studentModel = StudentModel(student_input_dim, student_embed_dim, seq_length=DEBUG_SEQ_LEN)

    def forward(self, x_seq, x_graph, adj):
        batch_size = x_graph.size(0)

        # Seq Encoder
        seq_encoded = self.linformerSeqEncoder(x_seq)

        # Graph Encoder
        x_graph_flat = x_graph.reshape(-1, x_graph.size(-1))
        edge_indices = []
        for i in range(batch_size):
            idx = adj[i].nonzero().t() + i * 20
            edge_indices.append(idx)
        batch_edge_index = torch.cat(edge_indices, dim=1).to(x_graph.device)

        graph_encoded_flat = self.GraphEncoder(x_graph_flat, batch_edge_index)
        graph_encoded = graph_encoded_flat.reshape(batch_size, 20, -1)
        graph_feature = graph_encoded[:, 0, :]

        concat_seq_graph = torch.cat((seq_encoded, graph_feature), dim=1)

        # Teacher
        teacher_feature = self.teacherModel(concat_seq_graph)

        split_limit = DEBUG_SEQ_LEN // 2
        split_seq = teacher_feature[:, :split_limit]
        split_graph = teacher_feature[:, split_limit:] # (Batch, 250)

        # Student
        sum_xseq_teacher = teacher_feature * 0.5 + x_seq * 0.5
        student_feature = self.studentModel(sum_xseq_teacher)

        # Decoders
        seq_decoded = self.linformerSeqDecoder(split_seq)

        # !!! 关键修复：使用 repeat 扩展特征 !!!
        # split_graph shape: (Batch, 250)
        # Target: (Batch*20, 250)
        # 将全局特征复制给每个节点
        split_graph_expanded = split_graph.unsqueeze(1).repeat(1, 20, 1) # (Batch, 20, 250)
        split_graph_reshaped = split_graph_expanded.reshape(batch_size * 20, 250) # (Batch*20, 250)

        graph_decoded_flat = self.GraphDecoder(split_graph_reshaped, batch_edge_index)
        graph_decoded = graph_decoded_flat.reshape(batch_size, 20, 250)

        return teacher_feature, student_feature, seq_decoded, graph_decoded

class Classifier(nn.Module):
    def __init__(self, input_dim, num_classes, dropout=0.1):
        super(Classifier, self).__init__()
        self.fc = nn.Sequential(
            nn.Linear(input_dim, 256),
            nn.BatchNorm1d(256),
            nn.ReLU(),
            nn.Dropout(dropout),
            nn.Linear(256, 128),
            nn.BatchNorm1d(128),
            nn.ReLU(),
            nn.Dropout(dropout),
            nn.Linear(128, num_classes)
        )

    def forward(self, x):
        return self.fc(x)

class scTCHCNTrain(nn.Module):
    def __init__(self, student_model, num_classes):
        super(scTCHCNTrain, self).__init__()
        self.student_model = student_model
        self.classifier = Classifier(DEBUG_SEQ_LEN, num_classes)

    def forward(self, x_seq):
        features = self.student_model(x_seq)
        out = self.classifier(features)
        return out