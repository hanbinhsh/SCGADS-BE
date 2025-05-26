package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Log;
import com.ruoyi.system.mapper.LogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service
public class LogServer implements com.ruoyi.system.service.LogServer{
    @Autowired
    private LogMapper logMapper;

    @Override
    public Map<Object, Object> findAllLogs() {
        return logMapper.findAllLogs();
    }

    @Override
    public void insertLog(long userId,String act ,long importance){
        logMapper.insertLog(userId, act,importance);
    }
}
