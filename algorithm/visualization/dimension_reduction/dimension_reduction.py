import os
import sys
import numpy as np
import scanpy as sc
from sklearn.manifold import TSNE
import matplotlib.colors as mcolors
import random
import json
import requests
import os
import umap

def ensure_directory(path):
    """ 确保目录存在，如果不存在则创建 """
    if not os.path.exists(path):
        os.makedirs(path)

def main_umap(username, taskname, has_labels, outputnpy_dir, output_dir, label_dir, seq_dir):
    requests.post(
        f"http://localhost:8868/complete?info=" + f"用户{username}的umap任务{taskname}正在处理，结果生成到{output_dir}")
    try:
        ensure_directory(output_dir)

        # 数据读取
        adata = sc.read_h5ad(seq_dir)
        adata = adata.X.todense()
        adata = np.asarray(adata)

        # UMAP降维
        reducer = umap.UMAP(n_components=2, random_state=20020130)
        embedded_data = reducer.fit_transform(adata)
        umap_x = embedded_data[:, 0]
        umap_y = embedded_data[:, 1]
        combined_data = list(zip(umap_x, umap_y))

        # 生成 UMAP 坐标 JS 文件
        with open(os.path.join(output_dir, 'data_umap.js'), 'w') as file:
            file.write("export const data = [" + ", ".join([f"[{x}, {y}]" for x, y in combined_data]) + "];")

        # 处理真实标签
        if has_labels:
            y_g = np.loadtxt(label_dir, dtype=str, delimiter=',', skiprows=1)

            # 生成真实标签 JS 文件
            with open(os.path.join(output_dir, 'label_umap.js'), 'w') as file:
                file.write("export const labels = " + str(y_g.tolist()).replace("'", '"') + ";")

            # 生成真实标签的颜色映射
#             unique_ground_truth = np.unique(y_g)
#             base_colors = list(mcolors.TABLEAU_COLORS.values())
#             color_list = base_colors[:len(unique_ground_truth)] if len(unique_ground_truth) <= len(base_colors) else base_colors + ['#%06X' % random.randint(0, 0xFFFFFF) for _ in range(len(unique_ground_truth) - len(base_colors))]
#
#             final_mapping = {ct: color_list[i] for i, ct in enumerate(unique_ground_truth)}
#             pieces = [{"value": i, "label": ct, "color": final_mapping[ct]} for i, ct in enumerate(unique_ground_truth)]
#             js_config = f"""
#     export const CATEGORY_COUNT = {len(unique_ground_truth)};
#     export const COLOR_LIST = {color_list};
#     export const pieces = {pieces};
#     """
#             with open(os.path.join(output_dir, 'config_umap.js'), 'w') as file:
#                 file.write(js_config)

        # 处理预测标签
        y_p_1 = np.load(outputnpy_dir, allow_pickle=True)
        with open(os.path.join(output_dir, 'label_pred_umap.js'), 'w') as file:
            file.write("export const labels = " + str(y_p_1.tolist()).replace("'", '"') + ";")

        unique_pred = np.unique(y_p_1)
        base_colors_pred = list(mcolors.TABLEAU_COLORS.values())
        color_list_pred = base_colors_pred[:len(unique_pred)] if len(unique_pred) <= len(base_colors_pred) else base_colors_pred + ['#%06X' % random.randint(0, 0xFFFFFF) for _ in range(len(unique_pred) - len(base_colors_pred))]

        final_mapping_pred = {ct: color_list_pred[i] for i, ct in enumerate(unique_pred)}
        pieces_pred = [{"value": i, "label": ct, "color": final_mapping_pred[ct]} for i, ct in enumerate(unique_pred)]
        js_config_pred = f"""
    export const CATEGORY_COUNT = {len(unique_pred)};
    export const COLOR_LIST = {color_list_pred};
    export const pieces = {pieces_pred};
    """
        with open(os.path.join(output_dir, 'config_umap.js'), 'w') as file: # config_pred_umap.js
            file.write(js_config_pred)

        print("运行成功，文件已生成至:", output_dir)

        # 发送 HTTP 请求
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{username}的umap任务{taskname}处理完成，结果生成到{output_dir}")
        # 设置任务为成功
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={taskname}&status=2")
    except Exception as e:
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{username}的umap任务{taskname}出错：{e}")
        # 设置任务为错误
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={taskname}&status=-1")

def main_tsne(username, taskname, has_labels, outputnpy_dir, output_dir, label_dir, seq_dir):
    requests.post(
        f"http://localhost:8868/complete?info=" + f"用户{username}的tsne任务{taskname}正在处理，结果生成到{output_dir}")
    try:
        ensure_directory(output_dir)

        # 数据读取
        adata = sc.read_h5ad(seq_dir)
        adata = adata.X.todense()
        adata = np.asarray(adata)

        # TSNE降维
        tsne = TSNE(n_components=2, random_state=20020130)
        embedded_data = tsne.fit_transform(adata)
        tsne_x = embedded_data[:, 0]
        tsne_y = embedded_data[:, 1]
        combined_data = list(zip(tsne_x, tsne_y))

        # 生成 TSNE 坐标 JS 文件
        with open(os.path.join(output_dir, 'data_tsne.js'), 'w') as file:
            file.write("export const data = [" + ", ".join([f"[{x}, {y}]" for x, y in combined_data]) + "];")

        # 处理真实标签
        if has_labels:
            y_g = np.loadtxt(label_dir, dtype=str, delimiter=',',
                             skiprows=1)

            # 生成真实标签 JS 文件
            with open(os.path.join(output_dir, 'label_tsne.js'), 'w') as file:
                file.write("export const labels = " + str(y_g.tolist()).replace("'", '"') + ";")

            # 生成真实标签的颜色映射
#             unique_ground_truth = np.unique(y_g)
#             base_colors = list(mcolors.TABLEAU_COLORS.values())
#             color_list = base_colors[:len(unique_ground_truth)] if len(unique_ground_truth) <= len(
#                 base_colors) else base_colors + ['#%06X' % random.randint(0, 0xFFFFFF) for _ in
#                                                  range(len(unique_ground_truth) - len(base_colors))]
#
#             final_mapping = {ct: color_list[i] for i, ct in enumerate(unique_ground_truth)}
#             pieces = [{"value": i, "label": ct, "color": final_mapping[ct]} for i, ct in enumerate(unique_ground_truth)]
#             js_config = f"""
#     export const CATEGORY_COUNT = {len(unique_ground_truth)};
#     export const COLOR_LIST = {color_list};
#     export const pieces = {pieces};
#     """
#             with open(os.path.join(output_dir, 'config_tsne.js'), 'w') as file:
#                 file.write(js_config)

        # 处理预测标签
        y_p_1 = np.load(outputnpy_dir, allow_pickle=True)
        with open(os.path.join(output_dir, 'label_pred_tsne.js'), 'w') as file:
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
        with open(os.path.join(output_dir, 'config_tsne.js'), 'w') as file: # config_pred_tsne.js
            file.write(js_config_pred)

        print("运行成功，文件已生成至:", output_dir)

        # 发送 HTTP 请求
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{username}的tsne任务{taskname}处理完成，结果生成到{output_dir}")
        main_umap(username, taskname, has_labels, outputnpy_dir, output_dir, label_dir, seq_dir)
    except Exception as e:
        requests.post(f"http://localhost:8868/complete?info=" + f"用户{username}的tsne任务{taskname}出错：{e}")
        # 设置任务为错误
        requests.post(f"http://localhost:8868/updateTaskStatusByTaskName?taskName={taskname}&status=-1")

if __name__ == "__main__":
    # tsne->umap->设置任务为成功
    if len(sys.argv) != 8:
        print("使用方法: python script.py <用户名> <任务名> <是否有真实标签(true/false)> <预测文件outputnpy路径> <输出路径> <真实标签路径> <seq序列路径>")
        sys.exit(1)

    username = sys.argv[1]
    taskname = sys.argv[2]
    has_labels = sys.argv[3].lower() == 'true'
    outputnpy_dir = sys.argv[4]
    output_dir = sys.argv[5]
    label_dir = sys.argv[6]
    seq_dir = sys.argv[7]
    main_tsne(username, taskname, has_labels, outputnpy_dir, output_dir, label_dir, seq_dir)
