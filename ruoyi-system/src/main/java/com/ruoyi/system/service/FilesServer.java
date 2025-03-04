package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.domain.entity.Scmoannoresult;

public interface FilesServer {
    void insertFiles(Scmoannofiles files);
    void updateFiles1(Scmoannofiles files, String taskName);
    void updateFiles2(Scmoannofiles files, String taskName);
    void updateFiles3(Scmoannofiles files, String taskName);

    Scmoannofiles findFileByTaskName(String taskName);

    void insertResult(Scmoannoresult result);
    void updateResult1(Scmoannoresult result, String taskName);
    void updateResult2(Scmoannoresult result, String taskName);
    void updateResult3(Scmoannoresult result, String taskName);
    Scmoannoresult findResultByTaskName(String taskName);
}
