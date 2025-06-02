package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Models;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

@Service
public class ModelCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String MODEL_IMAGE_KEY_PREFIX = "model_image:";
    private static final String MODEL_ID_KEY_PREFIX = "model_id:";
    private static final String MODEL_TYPE_KEY_PREFIX = "model_type:";
    private static final String ALL_MODELS_KEY = "all_model_images";


    public void updateCacheModel(Models model) {
        evictModel(model.getModelId());
        cacheModelImage(model);
    }


    /**
     * 缓存单个模型图片
     */
    public void cacheModelImage(Models model) {
//        System.out.println(model.getModelId());
        if (model != null && model.getModelId() != null) {
            String hashKey = "model:" + model.getModelId();
            redisTemplate.opsForHash().putAll(hashKey, modelToMap(model));
            redisTemplate.expire(hashKey, 2, TimeUnit.HOURS);
            // 同时维护一个Set记录所有modelId（用于快速获取全部模型）
            redisTemplate.opsForSet().add("all_model_ids", model.getModelId().toString());
        }
    }

    /**
     * 缓存所有模型图片
     */
    public void cacheAllModelImages(List<Models> models) {
        if (models != null && !models.isEmpty()) {
            // 缓存整个列表
//            redisTemplate.opsForValue().set(ALL_MODELS_KEY, models, 2, TimeUnit.HOURS);
            for (Models model : models) {
                // 使用Hash结构存储单个模型
                cacheModelImage(model);
            }

            // 分别缓存每个模型
//            for (ModelImage modelImage : modelImages) {
//                cacheModelImage(modelImage);
//
                // 按id分组缓存
//                if (models.getModelId() != null) {
//                    String idKey = MODEL_ID_KEY_PREFIX + models.getModelId();
//                    redisTemplate.opsForSet().add(idKey, models);
//                    redisTemplate.expire(idKey, 2, TimeUnit.HOURS);
//                }
//            }
        }
    }

    private Map<String, Object> modelToMap(Models model) {
        Map<String, Object> map = new HashMap<>();

        // 基本字段
        map.put("modelId", model.getModelId());
        map.put("modelName", model.getModelName());
        map.put("modelType", model.getModelType());
        map.put("modelPath", model.getModelPath());
        map.put("pretrainModelPath", model.getPretrainModelPath());
        map.put("predictFilePath", model.getPredictFilePath());
        map.put("trainFilePath", model.getTrainFilePath());
        map.put("figurePath", model.getFigurePath());
        map.put("defaultParameters", model.getDefaultParameters());
        map.put("remark", model.getRemark());
        map.put("extractLabels", model.getExtractLabels());
        map.put("userName", model.getUserName());
        map.put("companyName", model.getCompanyName());

        // 日期字段（转换为字符串格式）
        if (model.getCreatedTime() != null) {
            map.put("createdTime", model.getCreatedTime().toString());
        } else {
            map.put("createdTime", null);
        }

        // 布尔值
        map.put("pretrainModel", model.getPretrainModel());

        // 数值类型
        map.put("baseModel", model.getBaseModel());

        // 二进制数据（特殊处理）
        if (model.getFigureByte() != null) {
            // 将byte[]转为Base64字符串存储
            map.put("figureByte", Base64.getEncoder().encodeToString(model.getFigureByte()));
        } else {
            map.put("figureByte", null);
        }

        return map;
    }

    /**
     * 从缓存获取所有模型图片
     */
    @SuppressWarnings("unchecked")
    public List<Models> getAllModelFromCache() {
//        return (List<Models>) redisTemplate.opsForValue().get(ALL_MODELS_KEY);

        // 1. 从Redis获取所有modelId
        Set<Object> modelIds = redisTemplate.opsForSet().members("all_model_ids");
        List<Models> result = new ArrayList<>();
        for (Object id : modelIds) {
            Models model = getModelFromCache(Long.valueOf((String) id));
            if (model != null) {
                result.add(model);
            }
        }
        return result;

    }

    /**
     * 从缓存获取指定类型的模型图片
     */
    public Set<Object> getModelImagesByTypeFromCache(String modelType) {
        String typeKey = MODEL_TYPE_KEY_PREFIX + modelType;
        return redisTemplate.opsForSet().members(typeKey);
    }

    /**
     * 从Redis获取单个模型
     */
    public Models getModelFromCache(Long modelId) {
        String hashKey = "model:" + modelId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        if (entries.isEmpty()) {
            return null;
        }
        // 将Map转回Models对象
        Models model = new Models();
        model = mapToModel(entries);
        return model;
    }

    private Models mapToModel(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Models model = new Models();

        // 基本字段
        model.setModelId(map.get("modelId") != null ? Long.parseLong(map.get("modelId").toString()) : null);
        model.setModelName((String) map.get("modelName"));
        model.setModelType((String) map.get("modelType"));
        model.setModelPath((String) map.get("modelPath"));
        model.setPretrainModelPath((String) map.get("pretrainModelPath"));
        model.setPredictFilePath((String) map.get("predictFilePath"));
        model.setTrainFilePath((String) map.get("trainFilePath"));
        model.setFigurePath((String) map.get("figurePath"));
        model.setDefaultParameters((String) map.get("defaultParameters"));
        model.setRemark((String) map.get("remark"));
        model.setExtractLabels((String) map.get("extractLabels"));
        model.setUserName((String) map.get("userName"));
        model.setCompanyName((String) map.get("companyName"));

        // 日期字段
        if (map.get("createdTime") != null) {
            model.setCreatedTime(Timestamp.valueOf((String) map.get("createdTime")));
        }

        // 布尔值
        model.setPretrainModel(Boolean.parseBoolean(map.get("pretrainModel").toString()));

        // 数值类型
        model.setBaseModel(map.get("baseModel") != null ? Long.parseLong(map.get("baseModel").toString()) : 0L);

        // 二进制数据
        if (map.get("figureByte") != null) {
            model.setFigureByte(Base64.getDecoder().decode((String) map.get("figureByte")));
        }

        return model;
    }

    // 删除缓存
    public void evictModel(Long modelId) {
        // 删除Hash数据
        redisTemplate.delete("model:" + modelId);
        // 从Set中移除modelId
        redisTemplate.opsForSet().remove("all_model_ids", modelId.toString());
    }

    /**
     * 清除所有模型图片缓存
     */
    public void clearAllModelImageCache() {

        // 1. 获取所有modelId（从全局Set）
        Set<Object> modelIds = redisTemplate.opsForSet().members("all_model_ids");

        for (Object id : modelIds) {
            redisTemplate.delete("model:" + Long.valueOf((String) id));
        }

        // 2. 批量删除所有模型Hash（使用pipeline提高性能）
//        if (!CollectionUtils.isEmpty(modelIds)) {
//            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//                for (Object id : modelIds) {
//                    connection.del(("model:" + Long.valueOf((String) id).getBytes());
//                }
//                return null;
//            });
//        }

        // 3. 删除全局索引
        redisTemplate.delete("all_model_ids");
    }

    /**
     * 检查缓存是否存在
     */
    public boolean isCacheExists() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ALL_MODELS_KEY));
    }
}