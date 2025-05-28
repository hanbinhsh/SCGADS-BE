package com.ruoyi.system.config;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.entity.ModelImage;
import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.service.ModelService;
import com.ruoyi.system.service.impl.ModelImageCacheService;
import com.ruoyi.system.service.impl.ModelImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ModelImageDataLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ModelImageDataLoader.class);

    @Autowired
    private ModelImageService modelImageService;

    @Autowired
    private ModelImageCacheService cacheService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            logger.info("开始加载模型图片数据到Redis缓存...");

            // 检查缓存是否已存在
            if (cacheService.isCacheExists()) {
                logger.info("Redis缓存中已存在模型图片数据，跳过加载");
                return;
            }

            // 从数据库加载数据
            List<Models> models = modelImageService.loadAllModelImagesFromDB();

            changeModelImage(models);

            if (models != null && !models.isEmpty()) {
                // 将数据缓存到Redis
                cacheService.cacheAllModelImages(models);
                logger.info("成功加载 {} 条模型图片数据到Redis缓存", models.size());
            } else {
                logger.warn("数据库中没有找到模型图片数据");
            }

        } catch (Exception e) {
            logger.error("加载模型图片数据到Redis缓存时发生错误: ", e);
        }
    }

    public List<Models> changeModelImage(List<Models> models) {
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        ModelService modelService = SpringUtils.getBean(ModelService.class);
        for (Models model : models) {

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
        return models;
    }
}