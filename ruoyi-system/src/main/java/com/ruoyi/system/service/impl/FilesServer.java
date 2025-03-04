package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.domain.entity.Scmoannoresult;
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
    public void insertResult(Scmoannoresult result) {
        filesMapper.insertResult(result);
    }

    @Override
    @Transactional
    public void insertFiles(Scmoannofiles files) {
        filesMapper.insertFiles(files);
    }

    @Override
    @Transactional
    public void updateFiles1(Scmoannofiles files, String taskName) {
        filesMapper.updateFiles1(files, taskName);
    }

    @Override
    @Transactional
    public void updateFiles2(Scmoannofiles files, String taskName) {
        filesMapper.updateFiles2(files, taskName);
    }

    @Override
    @Transactional
    public void updateFiles3(Scmoannofiles files, String taskName) {
        filesMapper.updateFiles3(files, taskName);
    }

    @Override
    @Transactional
    public void updateResult1(Scmoannoresult result, String taskName) {
        filesMapper.updateResult1(result, taskName);
    }

    @Override
    @Transactional
    public void updateResult2(Scmoannoresult result, String taskName) {
        filesMapper.updateResult2(result, taskName);
    }

    @Override
    @Transactional
    public void updateResult3(Scmoannoresult result, String taskName) {
        filesMapper.updateResult3(result, taskName);
    }

    @Override
    @Transactional
    public Scmoannofiles findFileByTaskName(String taskName) {
        return filesMapper.findFileByTaskName(taskName);
    }

    @Override
    @Transactional
    public Scmoannoresult findResultByTaskName(String taskName) {
        return filesMapper.findResultByTaskName(taskName);
    }
}
