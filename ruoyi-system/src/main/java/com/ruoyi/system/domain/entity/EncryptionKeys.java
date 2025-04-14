package com.ruoyi.system.domain.entity;

public class EncryptionKeys {
    private int id;
    private String aesKey;
    private String iv;

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
    // 新增方法：将Hex字符串转为byte[]
    public byte[] getAesKeyBytes() {
        return hexStringToByteArray(aesKey);
    }

    public byte[] getIvBytes() {
        return hexStringToByteArray(iv);
    }

    // Hex字符串转byte[]
    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
}
