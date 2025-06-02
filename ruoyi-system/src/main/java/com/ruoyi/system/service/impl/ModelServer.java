package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.mapper.ModelMapper;
import com.ruoyi.system.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelServer implements ModelService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ModelCacheService modelCache;

    @Autowired
    private ModelsService modelsService;


    @Override
    public List<Models> getAllModels() {
        return modelMapper.getAllModels();
    }

    @Override
    public void deleteModel(Long modelId) {
        modelMapper.deleteModel(modelId);
        // 再删除缓存
        modelsService.refreshCache();
//        modelCache.evictModel(modelId);
    }

    @Override
    public void addModel(Models model) {
        System.out.println(model.getModelId());
        modelMapper.addModel(model);
        // 再写入缓存
//        System.out.println(model.getModelId());
        modelsService.refreshCache();
//        modelCache.cacheModelImage(model);
    }

    @Override
    public void updateModel(Models model) {
        modelMapper.updateModel(model);
        // 再更新缓存
        modelsService.refreshCache();
//        modelCache.updateCacheModel(model);
    }

    @Override
    public Models getModelById(Long modelId) {
        return modelMapper.getModelById(modelId);
    }

    @Override
    public void updateModelRemark(Long modelId, String remark) {
        modelMapper.updateModelRemark(modelId, remark);
        // 更新缓存
        modelsService.refreshCache();
//        modelCache.evictModel(modelId);
    }

    @Override
    public List<Models> findModelsByUserName(String userId) {
        return modelMapper.findModelsByUserName(userId);
    }

    @Override
    public void addChildModel(Models model) {
        modelMapper.addChildModel(model);
        modelsService.refreshCache();
    }

    @Override
    public String getBaseModelName(Long baseModel) {
        return modelMapper.getBaseModelName(baseModel);
    }
}
