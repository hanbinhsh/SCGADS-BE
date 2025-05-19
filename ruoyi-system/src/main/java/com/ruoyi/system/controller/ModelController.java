package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/models")
public class ModelController {
    @Autowired
    private ModelService modelService;

    @GetMapping("/findAllModels")
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
            } else if(model.getModelType().equals("deno")){
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

    @PostMapping("/updateModel")
    public Result updateModel(@RequestParam("modelId") int modelId,
                              @RequestParam("modelName") String modelName,
                              @RequestParam("modelType") String modelType,
                              @RequestParam("modelPath") String modelPath,
                              @RequestParam("predictFilePath") String predictFilePath,
                              @RequestParam("trainFilePath") String trainFilePath,
                              @RequestParam("figurePath") String figurePath,
                              @RequestParam("defaultParameters") String defaultParameters,
                              @RequestParam("remark") String remark) {
        Models model = new Models();
        model.setModelId(modelId);
        model.setModelName(modelName);
        model.setModelType(modelType);
        model.setModelPath(modelPath);
        model.setPredictFilePath(predictFilePath);
        model.setTrainFilePath(trainFilePath);
        model.setFigurePath(figurePath);
        model.setDefaultParameters(defaultParameters);
        model.setRemark(remark);
        modelService.updateModel(model);
        return Result.success();
    }

    @PostMapping("/addModel")
    public Result addModel(@RequestParam("modelName") String modelName,
                           @RequestParam("modelType") String modelType,
                           @RequestParam("modelPath") String modelPath,
                           @RequestParam("predictFilePath") String predictFilePath,
                           @RequestParam("trainFilePath") String trainFilePath,
                           @RequestParam("figurePath") String figurePath,
                           @RequestParam("defaultParameters") String defaultParameters,
                           @RequestParam("remark") String remark) {
        Models model = new Models();
        model.setModelName(modelName);
        model.setModelType(modelType);
        model.setModelPath(modelPath);
        model.setPredictFilePath(predictFilePath);
        model.setTrainFilePath(trainFilePath);
        model.setFigurePath(figurePath);
        model.setDefaultParameters(defaultParameters);
        model.setRemark(remark);
        modelService.addModel(model);
        return Result.success();
    }

    @DeleteMapping("/deleteModel")
    public Result deleteModel(@RequestParam("modelId") Long modelId) {
        modelService.deleteModel(modelId);
        return Result.success();
    }

}
