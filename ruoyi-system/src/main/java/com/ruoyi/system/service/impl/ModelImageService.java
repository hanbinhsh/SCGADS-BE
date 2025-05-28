package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.ModelImage;
import com.ruoyi.system.mapper.ModelImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelImageService {

    @Autowired
    private ModelImageMapper modelImageMapper;

    @Autowired
    private ModelImageCacheService cacheService;

    /**
     * 从数据库加载所有模型图片数据
     */
    public List<ModelImage> loadAllModelImagesFromDB() {
        return modelImageMapper.selectAllModelImages();
    }

    /**
     * 获取单个模型图片（优先从缓存获取）
     */
    public ModelImage getModelImage(Long modelId) {
        // 先从缓存获取
        ModelImage modelImage = cacheService.getModelImageFromCache(modelId);
        if (modelImage == null) {
            // 缓存中没有，从数据库获取
            modelImage = modelImageMapper.selectModelImageById(modelId);
            if (modelImage != null) {
                // 放入缓存
                cacheService.cacheModelImage(modelImage);
            }
        }
        return modelImage;
    }

    /**
     * 获取所有模型图片（优先从缓存获取）
     */
    public List<ModelImage> getAllModelImages() {
        // 先从缓存获取
        List<ModelImage> modelImages = cacheService.getAllModelImagesFromCache();
        if (modelImages == null || modelImages.isEmpty()) {
            // 缓存中没有，从数据库获取
            modelImages = loadAllModelImagesFromDB();
            if (modelImages != null && !modelImages.isEmpty()) {
                // 放入缓存
                cacheService.cacheAllModelImages(modelImages);
            }
        }
        return modelImages;
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
        List<ModelImage> modelImages = loadAllModelImagesFromDB();
        if (modelImages != null && !modelImages.isEmpty()) {
            cacheService.cacheAllModelImages(modelImages);
        }
    }
}