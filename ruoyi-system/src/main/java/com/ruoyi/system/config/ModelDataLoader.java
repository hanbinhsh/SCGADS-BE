package com.ruoyi.system.config;

import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.service.impl.ModelCacheService;
import com.ruoyi.system.service.impl.ModelUtilService;
import com.ruoyi.system.service.impl.ModelsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelDataLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ModelDataLoader.class);

    @Autowired
    private ModelsService modelsService;

    @Autowired
    private ModelCacheService cacheService;

    @Autowired
    private ModelUtilService modelUtil;

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
            List<Models> models = modelsService.loadAllModelFromDB();

            modelUtil.changeAllModelImage(models);

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


}