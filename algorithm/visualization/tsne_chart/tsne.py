import os
import sys
import numpy as np
import scanpy as sc
from sklearn.manifold import TSNE
import matplotlib.colors as mcolors
import random
import json


def ensure_directory(path):
    """ 确保目录存在，如果不存在则创建 """
    if not os.path.exists(path):
        os.makedirs(path)


def main(username, taskname, has_labels):
    # 定义存储路径
    output_dir = os.path.abspath(f'../../../temp/Result/{username}/{taskname}/')
    ensure_directory(output_dir)

    # 数据读取
    adata = sc.read_h5ad('G:/Projects/seqData/mouse_skin_shareseq_rna_10k/rna.h5ad')
    adata = adata.X.todense()
    adata = np.asarray(adata)

    # TSNE降维
    tsne = TSNE(n_components=2, random_state=20020130)
    embedded_data = tsne.fit_transform(adata)
    tsne_x = embedded_data[:, 0]
    tsne_y = embedded_data[:, 1]
    combined_data = list(zip(tsne_x, tsne_y))

    # 生成 TSNE 坐标 JS 文件
    with open(os.path.join(output_dir, 'data.js'), 'w') as file:
        file.write("export const data = [" + ", ".join([f"[{x}, {y}]" for x, y in combined_data]) + "];")

    # 处理真实标签
    if has_labels:
        y_g = np.loadtxt('G:/Projects/seqData/mouse_skin_shareseq_rna_10k/Label.csv', dtype=str, delimiter=',',
                         skiprows=1)

        # 生成真实标签 JS 文件
        with open(os.path.join(output_dir, 'label.js'), 'w') as file:
            file.write("export const labels = " + str(y_g.tolist()).replace("'", '"') + ";")

        # 生成真实标签的颜色映射
        unique_ground_truth = np.unique(y_g)
        base_colors = list(mcolors.TABLEAU_COLORS.values())
        color_list = base_colors[:len(unique_ground_truth)] if len(unique_ground_truth) <= len(
            base_colors) else base_colors + ['#%06X' % random.randint(0, 0xFFFFFF) for _ in
                                             range(len(unique_ground_truth) - len(base_colors))]

        final_mapping = {ct: color_list[i] for i, ct in enumerate(unique_ground_truth)}
        pieces = [{"value": i, "label": ct, "color": final_mapping[ct]} for i, ct in enumerate(unique_ground_truth)]
        js_config = f"""
export const CATEGORY_COUNT = {len(unique_ground_truth)};
export const COLOR_LIST = {color_list};
export const pieces = {pieces};
"""
        with open(os.path.join(output_dir, 'config.js'), 'w') as file:
            file.write(js_config)

    # 处理预测标签
    y_p_1 = np.load('output.npy', allow_pickle=True)
    with open(os.path.join(output_dir, 'label_pred.js'), 'w') as file:
        file.write("export const labels = " + str(y_p_1.tolist()).replace("'", '"') + ";")

    unique_pred = np.unique(y_p_1)
    base_colors_pred = list(mcolors.TABLEAU_COLORS.values())
    color_list_pred = base_colors_pred[:len(unique_pred)] if len(unique_pred) <= len(
        base_colors_pred) else base_colors_pred + ['#%06X' % random.randint(0, 0xFFFFFF) for _ in
                                                   range(len(unique_pred) - len(base_colors_pred))]

    final_mapping_pred = {ct: color_list_pred[i] for i, ct in enumerate(unique_pred)}
    pieces_pred = [{"value": i, "label": ct, "color": final_mapping_pred[ct]} for i, ct in enumerate(unique_pred)]
    js_config_pred = f"""
export const CATEGORY_COUNT = {len(unique_pred)};
export const COLOR_LIST = {color_list_pred};
export const pieces = {pieces_pred};
"""
    with open(os.path.join(output_dir, 'config_pred.js'), 'w') as file:
        file.write(js_config_pred)

    print("运行成功，文件已生成至:", output_dir)


if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("使用方法: python script.py <用户名> <任务名> <是否有真实标签(true/false)>")
        sys.exit(1)

    username = sys.argv[1]
    taskname = sys.argv[2]
    has_labels = sys.argv[3].lower() == 'true'
    main(username, taskname, has_labels)
