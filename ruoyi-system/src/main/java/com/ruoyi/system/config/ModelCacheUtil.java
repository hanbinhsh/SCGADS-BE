package com.ruoyi.system.config;

import com.ruoyi.system.domain.entity.Models;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Optional;

@Component
public class ModelCacheUtil {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private static final String ALL_MODELS_KEY = "all_model_images";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    // 写入或更新缓存
    public void cacheModel(Models model) {
        String key = ALL_MODELS_KEY + model.getModelId();
        redisTemplate.opsForValue().set(key, model, CACHE_TTL);
    }

    // 删除缓存
    public void evictModel(Long modelId) {
        String key = ALL_MODELS_KEY + modelId;
        redisTemplate.delete(key);
    }

    // 获取缓存
    public Optional<Models> getModel(Long modelId) {
        String key = ALL_MODELS_KEY + modelId;
        return Optional.ofNullable((Models) redisTemplate.opsForValue().get(key));
    }
}