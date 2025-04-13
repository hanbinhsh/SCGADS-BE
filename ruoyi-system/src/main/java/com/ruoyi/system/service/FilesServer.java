package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Scmoannofiles;

public interface FilesServer {
    void insertFiles(Scmoannofiles files);
    void updateFiles1(String randomFileName, String taskName);
    void updateFiles2(String randomFileName, String taskName);
    void updateFiles3(String randomFileName, String taskName);

    Scmoannofiles findFileByTaskName(String taskName);

    String findFileByHash(String hash);

    void updateFileHashNum(String fileName, int index);

    void insertFileHash(String hash, String randomFileName);

    int getFileHashNum(String fileName);
}
