package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.service.impl.LogServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;

@RestController
public class LogController {
    @Autowired
    private LogServer logServer;

    @RequestMapping("/findAllLogs")
    @CrossOrigin(origins = "*")
    public Result<Map<Object,Object>> findAllLogs(){
        Map<Object, Object> LogsWithUserInfo = logServer.findAllLogs();

        // 遍历所有日志信息和用户信息，并转换用户头像
        for (Object log : LogsWithUserInfo.keySet()) {
            Object logInfo = LogsWithUserInfo.get(log);
            if (logInfo instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) logInfo;
                if (userMap.get("avatar") instanceof byte[] avatarBytes) {
                    String base64Avatar = Base64.getEncoder().encodeToString(avatarBytes);
                    userMap.put("avatarBase64", base64Avatar); // 添加 Base64 编码字段
                }
            }
        }

        return Result.success(LogsWithUserInfo);
    }

    @RequestMapping("/insertLog")
    public void insertDeleteFileLog(@RequestBody Map<String, String> map) {
        this.logServer.insertLog(Long.parseLong(map.get("userId")),map.get("act"),Long.parseLong(map.get("importance")));
    }
}
