package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.service.ModelService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/models")
public class ModelController {
    @Autowired
    private ModelService modelService;

    @GetMapping("/list")
    public ResponseEntity<List<Models>> getAllModels() {
        List<Models> models = modelService.getAllModels();
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录

        for (Models model : models) {
            String figurePath = "";
            StringBuilder figDir  = new StringBuilder(baseDir + "/algorithm/");
            if(model.getModelType().equals("single")){
                figurePath = "annotation/" + model.getModelName() + "/figs/" + model.getFigurePath();
            } else if(model.getModelType().equals("multi")){
                figurePath = "annotation/" + model.getModelName() + "/figs/" + model.getFigurePath();
            } else if(model.getModelType().equals("Deno")){
                figurePath = "denoising/" + model.getModelName() + "/figs/" + model.getFigurePath();
            } else {
                System.out.println("模型 " + model.getModelName() + " 类型出错");
            }

            // 完整的图片路径
            String fullPath = figDir.append(figurePath).toString();

            // 读取图片并转为字节流
            try {
                File file = new File(fullPath);
                if(file.exists() && file.isFile()) {
                    byte[] fileBytes = Files.readAllBytes(Paths.get(fullPath)); // 将文件转为字节数组
                    model.setFigureByte(fileBytes); // 存储到模型的figByte字段
                } else {
                    System.out.println("文件不存在: " + fullPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("读取图片失败: " + fullPath);
            }
        }

        return ResponseEntity.ok(models);
    }
}
