package com.scmoanno.scmoanno.controller;

import com.scmoanno.scmoanno.entity.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class TaskProgress {
    // 注释任务处理
    @RequestMapping("/progress")
    @CrossOrigin(origins = "*")
    public Result<String> pytest(@RequestParam String taskName) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", "G:/process.py", taskName);
        Process process = processBuilder.start();
        System.out.println("任务 " + taskName + " 处理中");
        return Result.success();
    }

    // 数据清洗任务处理

    // 模型训练任务处理

    // 任务处理完成
    @RequestMapping("/complete")
    @CrossOrigin(origins = "*")
    public Result<String> complete(@RequestParam String taskName) {
        System.out.println("任务 " + taskName + " 处理完成");
        return Result.success();
    }
}
