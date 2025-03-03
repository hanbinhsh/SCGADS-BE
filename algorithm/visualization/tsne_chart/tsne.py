import numpy as np
import scanpy as sc
from sklearn.manifold import TSNE
import matplotlib.colors as mcolors
import random
import json

# ---------------------------
# 数据读取与 TSNE 降维
# ---------------------------
adata = sc.read_h5ad('G:/Projects/seqData/mouse_skin_shareseq_rna_10k/rna.h5ad')
adata = adata.X.todense()
adata = np.asarray(adata)

# 读取真实标签，要求 CSV 文件每行只有一个标签，跳过表头
y_g = np.loadtxt('G:/Projects/seqData/mouse_skin_shareseq_rna_10k/Label.csv',
                 dtype=str, delimiter=',', skiprows=1)
# 读取预测标签
y_p_1 = np.load('output.npy', allow_pickle=True)

# 使用 TSNE 将数据降维到二维
tsne = TSNE(n_components=2, random_state=20020130)
embedded_data = tsne.fit_transform(adata)
tsne_x = embedded_data[:, 0]
tsne_y = embedded_data[:, 1]

# ---------------------------
# 生成真实输出的 JS 文件
# ---------------------------
# 生成二维坐标 js 文件 (data.js)
combined_data = list(zip(tsne_x, tsne_y))
js_data = "export const data = [" + ", ".join([f"[{x}, {y}]" for x, y in combined_data]) + "];"
with open('data.js', 'w') as file:
    file.write(js_data)

# 生成标签数据 js 文件 (label.js)
data_list = y_g.tolist()
js_labels = "export const labels = " + str(data_list).replace("'", '"') + ";"
with open('label.js', 'w') as file:
    file.write(js_labels)

# ---------------------------
# 根据真实标签动态生成颜色映射配置（config.js）
# ---------------------------
# 将 unique 标签转换为字符串，确保 JSON 序列化时不会报错
unique_ground_truth = np.unique(y_g)
unique_ground_truth = [str(x) for x in unique_ground_truth]
category_count = len(unique_ground_truth)

# 使用 matplotlib 内置的 TABLEAU_COLORS 作为基础颜色
base_colors = list(mcolors.TABLEAU_COLORS.values())
if category_count > len(base_colors):
    additional_colors = ['#%06X' % random.randint(0, 0xFFFFFF) for _ in range(category_count - len(base_colors))]
    color_list = base_colors + additional_colors
else:
    color_list = base_colors[:category_count]

# 构造动态映射：直接按照 unique 标签顺序映射颜色
final_mapping = {ct: color_list[i] for i, ct in enumerate(unique_ground_truth)}

pieces = []
for i, ct in enumerate(unique_ground_truth):
    pieces.append({"value": int(i), "label": ct, "color": final_mapping[ct]})
js_config = f"""
export const CATEGORY_COUNT = {category_count};
export const COLOR_LIST = {color_list};
export const pieces = {pieces};
"""
with open('config.js', 'w') as file:
    file.write(js_config)

# ---------------------------
# 生成预测输出的 JS 文件（文件名后加 _pred）
# ---------------------------
# 生成预测输出的 TSNE 坐标文件 (data_pred.js)【TSNE 坐标与真实输出一致】
with open('data_pred.js', 'w') as file:
    file.write(js_data)

# 生成预测标签的 js 文件 (label_pred.js)
data_list_pred = y_p_1.tolist()
js_labels_pred = "export const labels = " + str(data_list_pred).replace("'", '"') + ";"
with open('label_pred.js', 'w') as file:
    file.write(js_labels_pred)

# 根据预测标签动态生成颜色映射配置（config_pred.js）
unique_pred = np.unique(y_p_1)
unique_pred = [str(x) for x in unique_pred]
category_count_pred = len(unique_pred)
base_colors_pred = list(mcolors.TABLEAU_COLORS.values())
if category_count_pred > len(base_colors_pred):
    additional_colors_pred = ['#%06X' % random.randint(0, 0xFFFFFF) for _ in range(category_count_pred - len(base_colors_pred))]
    color_list_pred = base_colors_pred + additional_colors_pred
else:
    color_list_pred = base_colors_pred[:category_count_pred]

final_mapping_pred = {ct: color_list_pred[i] for i, ct in enumerate(unique_pred)}
pieces_pred = []
for i, ct in enumerate(unique_pred):
    pieces_pred.append({"value": int(i), "label": ct, "color": final_mapping_pred[ct]})
js_config_pred = f"""
export const CATEGORY_COUNT = {category_count_pred};
export const COLOR_LIST = {color_list_pred};
export const pieces = {pieces_pred};
"""
with open('config_pred.js', 'w') as file:
    file.write(js_config_pred)

print("运行成功")
