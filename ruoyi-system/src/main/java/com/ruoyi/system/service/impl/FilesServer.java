package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.mapper.FilesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilesServer implements com.ruoyi.system.service.FilesServer {

    @Autowired
    private FilesMapper filesMapper;

    @Override
    @Transactional
    public void insertFiles(Scmoannofiles files) {
        filesMapper.insertFiles(files);
    }

    @Override
    @Transactional
    public void updateFiles1(String randomFileName, String taskName) {
        filesMapper.updateFiles1(randomFileName, taskName);
    }

    @Override
    @Transactional
    public void updateFiles2(String randomFileName, String taskName) {
        filesMapper.updateFiles2(randomFileName, taskName);
    }

    @Override
    @Transactional
    public void updateFiles3(String randomFileName, String taskName) {
        filesMapper.updateFiles3(randomFileName, taskName);
    }

    @Override
    @Transactional
    public Scmoannofiles findFileByTaskName(String taskName) {
        return filesMapper.findFileByTaskName(taskName);
    }

    @Override
    @Transactional
    public String findFileByHash(String hash) {
        return filesMapper.findFileByHash(hash);
    }

    @Override
    @Transactional
    public void updateFileHashNum(String fileName, int index) {
        filesMapper.updateFileHashNum(fileName, index);
    }

    @Override
    public void insertFileHash(String hash, String randomFileName) {
        filesMapper.insertFileHash(hash, randomFileName);
    }

    @Override
    public int getFileHashNum(String fileName) {
        return filesMapper.getFileHashNum(fileName);
    }
}
