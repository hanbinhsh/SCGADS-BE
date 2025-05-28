package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.ModelImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

@Service
public class ModelImageCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String MODEL_IMAGE_KEY_PREFIX = "model_image:";
    private static final String MODEL_TYPE_KEY_PREFIX = "model_type:";
    private static final String ALL_MODELS_KEY = "all_model_images";

    /**
     * 缓存单个模型图片
     */
    public void cacheModelImage(ModelImage modelImage) {
        if (modelImage != null && modelImage.getModelId() != null) {
            String key = MODEL_IMAGE_KEY_PREFIX + modelImage.getModelId();
            redisTemplate.opsForValue().set(key, modelImage, 24, TimeUnit.HOURS);
        }
    }

    /**
     * 缓存所有模型图片
     */
    public void cacheAllModelImages(List<ModelImage> modelImages) {
        if (modelImages != null && !modelImages.isEmpty()) {
            // 缓存整个列表
            redisTemplate.opsForValue().set(ALL_MODELS_KEY, modelImages, 24, TimeUnit.HOURS);

            // 分别缓存每个模型
            for (ModelImage modelImage : modelImages) {
                cacheModelImage(modelImage);

                // 按类型分组缓存
                if (modelImage.getModelType() != null) {
                    String typeKey = MODEL_TYPE_KEY_PREFIX + modelImage.getModelType();
                    redisTemplate.opsForSet().add(typeKey, modelImage);
                    redisTemplate.expire(typeKey, 24, TimeUnit.HOURS);
                }
            }
        }
    }

    /**
     * 从缓存获取单个模型图片
     */
    public ModelImage getModelImageFromCache(Long modelId) {
        String key = MODEL_IMAGE_KEY_PREFIX + modelId;
        return (ModelImage) redisTemplate.opsForValue().get(key);
    }

    /**
     * 从缓存获取所有模型图片
     */
    @SuppressWarnings("unchecked")
    public List<ModelImage> getAllModelImagesFromCache() {
        return (List<ModelImage>) redisTemplate.opsForValue().get(ALL_MODELS_KEY);
    }

    /**
     * 从缓存获取指定类型的模型图片
     */
    public Set<Object> getModelImagesByTypeFromCache(String modelType) {
        String typeKey = MODEL_TYPE_KEY_PREFIX + modelType;
        return redisTemplate.opsForSet().members(typeKey);
    }

    /**
     * 清除所有模型图片缓存
     */
    public void clearAllModelImageCache() {
        // 获取所有相关的key
        Set<String> keys = redisTemplate.keys(MODEL_IMAGE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        Set<String> typeKeys = redisTemplate.keys(MODEL_TYPE_KEY_PREFIX + "*");
        if (typeKeys != null && !typeKeys.isEmpty()) {
            redisTemplate.delete(typeKeys);
        }

        redisTemplate.delete(ALL_MODELS_KEY);
    }

    /**
     * 检查缓存是否存在
     */
    public boolean isCacheExists() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ALL_MODELS_KEY));
    }
}