import csv
from collections import OrderedDict

def extract_labels(input_path='label.csv', output_path='extract_labels.csv'):
    try:
        with open(input_path, 'r', encoding='utf-8') as f:
            reader = csv.reader(f)
            header = next(reader)  # 跳过表头
            labels = [row[0] for row in reader if row]  # 提取第一列标签

        # 去重并保持顺序
        unique_labels = list(OrderedDict.fromkeys(labels))

        # 打印输出
        print(f"共发现 {len(unique_labels)} 个唯一标签：")
        for label in unique_labels:
            print(f"- {label}")

        # 保存到 extract_labels.csv
        with open(output_path, 'w', encoding='utf-8', newline='') as f_out:
            writer = csv.writer(f_out)
            writer.writerow(['label'])  # 写表头
            for label in unique_labels:
                writer.writerow([label])

        print(f"\n标签已保存到文件：{output_path}")

    except FileNotFoundError:
        print(f"文件 {input_path} 不存在。")
    except Exception as e:
        print(f"读取或写入文件时出错：{e}")

if __name__ == '__main__':
    extract_labels()
