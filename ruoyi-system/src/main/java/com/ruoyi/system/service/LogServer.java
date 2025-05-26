package com.ruoyi.system.service;

import java.util.Map;

public interface LogServer {
    Map<Object, Object> findAllLogs();
    public void insertLog(long userId, String act,long importance);
}
