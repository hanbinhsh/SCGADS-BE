import os
import shutil

def decrypt_file(encrypted_path, decrypted_path):
    """
    模拟解密函数：如果是加密文件则解密，否则直接复制原始文件
    实际使用中请替换为真实的解密逻辑
    """
    # 示例逻辑：如果文件扩展名是 .enc 则说明是加密的
    if encrypted_path.endswith(".enc"):
        with open(encrypted_path, "rb") as f:
            encrypted_data = f.read()
        # 假设简单异或加密（仅演示用途）
        decrypted_data = bytes(b ^ 0xAA for b in encrypted_data)
        with open(decrypted_path, "wb") as f:
            f.write(decrypted_data)
    else:
        shutil.copy(encrypted_path, decrypted_path)
