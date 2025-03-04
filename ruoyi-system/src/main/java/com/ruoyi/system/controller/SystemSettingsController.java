package com.ruoyi.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/system-settings")
public class SystemSettingsController {
    private static String baseDir = System.getProperty("user.dir") + "/config.json"; // 获取当前项目的根目录
    private static final String CONFIG_FILE_PATH = baseDir; // 项目根目录下的配置文件路径
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 获取配置
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (configFile.exists()) {
                return objectMapper.readValue(configFile, Map.class);
            } else {
                return Map.of("Auto Progress", true); // 默认配置
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of("Auto Progress", true); // 默认配置
        }
    }

    // 保存配置
    @PostMapping("/saveConfig")
    public String saveConfig(@RequestBody Map<String, Object> config) {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            objectMapper.writeValue(configFile, config); // 将配置保存到config.json
            return "Settings saved successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error saving settings!";
        }
    }
}
