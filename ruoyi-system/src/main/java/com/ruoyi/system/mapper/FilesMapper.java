package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Scmoannofiles;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FilesMapper {
    public void insertFiles(Scmoannofiles files);
    public void updateFiles1(@Param("randomFileName") String randomFileName, @Param("taskName") String taskName);
    public void updateFiles2(@Param("randomFileName") String randomFileName, @Param("taskName") String taskName);
    public void updateFiles3(@Param("randomFileName") String randomFileName, @Param("taskName") String taskName);

    Scmoannofiles findFileByTaskName(String taskName);

    String findFileByHash(@Param("hash") String hash);

    void updateFileHashNum(@Param("fileName") String fileName, @Param("index") int index);

    void insertFileHash(@Param("hash") String hash, @Param("randomFileName") String randomFileName);

    int getFileHashNum(@Param("fileName") String fileName);

    Scmoannofiles findFilesByTaskName(@Param("taskName") String taskName);
}
