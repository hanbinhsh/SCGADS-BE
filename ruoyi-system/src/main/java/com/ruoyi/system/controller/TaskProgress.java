package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
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
    public Result pytest(@RequestParam("type") String type,
                                 @RequestParam("taskName") String taskName,
                                 @RequestParam("userName") String userName) throws IOException {
        // 确定是否有真实标签
        String hasLabels = "training".equals(type) ? "true" : "false";
        // Python 脚本路径
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String pythonScriptPath = baseDir + "/algorithm/visualization/tsne_chart/tsne.py";
        String outputnpyPath = "G:/JAVA/RuoYi-Vue-master/algorithm/visualization/tsne_chart/output.npy"; // TODO
        String outputPath = baseDir + "/temp/Result/" + userName + '/' + taskName + '/';
        // 构造 ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, userName, taskName, hasLabels, outputnpyPath, outputPath);
        // 启动进程
        Process process = processBuilder.start();
        System.out.println("任务 " + taskName + " 处理中");
        return Result.success();
    }

    // 数据清洗任务处理

    // 模型训练任务处理

    // 任务处理完成
    @RequestMapping("/complete")
    @CrossOrigin(origins = "*")
    public Result<String> complete(@RequestParam String info) {
        System.out.println(info);
        return Result.success();
    }
}
