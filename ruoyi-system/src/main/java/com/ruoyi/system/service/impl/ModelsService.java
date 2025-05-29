package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.ModelImage;
import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.mapper.ModelImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelsService {

    @Autowired
    private ModelImageMapper modelImageMapper;

    @Autowired
    private ModelCacheService cacheService;

    /**
     * 从数据库加载所有模型图片数据
     */
    public List<Models> loadAllModelFromDB() {
        return modelImageMapper.selectAllModelImages();
    }

    /**
     * 获取单个模型图片（优先从缓存获取）
     */
    public Models getModelImage(Long modelId) {
        // 先从缓存获取
        Models model= cacheService.getModelFromCache(modelId);
        if (model == null) {
            // 缓存中没有，从数据库获取
            model = modelImageMapper.selectModelImageById(modelId);
            if (model != null) {
                // 放入缓存
                cacheService.cacheModelImage(model);
            }
        }
        return model;
    }

    /**
     * 获取所有模型（优先从缓存获取）
     */
    public List<Models> getAllModel() {
        // 先从缓存获取
        List<Models> models = cacheService.getAllModelFromCache();
        if (models == null || models.isEmpty()) {
            // 缓存中没有，从数据库获取
            models = loadAllModelFromDB();
            if (models != null && !models.isEmpty()) {
                // 放入缓存
                cacheService.cacheAllModelImages(models);
            }
        }
        return models;
    }

    /**
     * 根据类型获取模型图片
     */
    public List<ModelImage> getModelImagesByType(String modelType) {
        return modelImageMapper.selectModelImagesByType(modelType);
    }

    /**
     * 刷新缓存
     */
    public void refreshCache() {
        // 清除旧缓存
        cacheService.clearAllModelImageCache();
        // 重新加载数据到缓存
        List<Models> models = loadAllModelFromDB();
        if (models != null && !models.isEmpty()) {
            cacheService.cacheAllModelImages(models);
        }
    }
}