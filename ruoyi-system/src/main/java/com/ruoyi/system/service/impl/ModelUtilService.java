package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.entity.Models;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ModelUtilService {
    public void changeAllModelImage(List<Models> models) {
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        com.ruoyi.system.service.ModelService modelService = SpringUtils.getBean(com.ruoyi.system.service.ModelService.class);
        for (Models model : models) {
            changeModelImage(model);
        }
    }

    public void changeModelImage(Models model) {
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        com.ruoyi.system.service.ModelService modelService = SpringUtils.getBean(com.ruoyi.system.service.ModelService.class);
        String modelName = model.getModelName();
        if(model.getBaseModel()!=0){ // 读取父模型的名称
            modelName = modelService.getBaseModelName(model.getBaseModel());
        }

        String figurePath = "";
        StringBuilder figDir  = new StringBuilder(baseDir + "/algorithm/");
        if(model.getModelType().equals("single")){
            figurePath = "annotation/" + modelName + "/figs/" + model.getFigurePath();
        } else if(model.getModelType().equals("multi")){
            figurePath = "annotation/" + modelName + "/figs/" + model.getFigurePath();
        } else if(model.getModelType().equals("deno")){
            figurePath = "denoising/" + modelName + "/figs/" + model.getFigurePath();
        } else {
            System.out.println("模型 " + modelName + " 类型出错");
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
}
