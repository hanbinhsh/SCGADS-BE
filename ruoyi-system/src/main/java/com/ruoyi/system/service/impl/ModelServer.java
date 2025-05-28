package com.ruoyi.system.service.impl;

import com.ruoyi.system.config.ModelCacheUtil;
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
    private ModelCacheUtil modelCache;


    @Override
    public List<Models> getAllModels() {
        return modelMapper.getAllModels();
    }

    @Override
    public void deleteModel(Long modelId) {
        modelMapper.deleteModel(modelId);
        // 再删除缓存
        modelCache.evictModel(modelId);
    }

    @Override
    public void addModel(Models model) {
        modelMapper.addModel(model);
        // 再写入缓存
        modelCache.cacheModel(model);
    }

    @Override
    public void updateModel(Models model) {
        modelMapper.updateModel(model);
        // 再更新缓存
        modelCache.cacheModel(model);
    }

    @Override
    public Models getModelById(Long modelId) {
        return modelMapper.getModelById(modelId);
    }

    @Override
    public void updateModelRemark(Long modelId, String remark) {
        modelMapper.updateModelRemark(modelId, remark);
        // 直接删除缓存
        modelCache.evictModel(modelId);
    }

    @Override
    public List<Models> findModelsByUserName(String userId) {
        return modelMapper.findModelsByUserName(userId);
    }

    @Override
    public void addChildModel(Models model) {
        modelMapper.addChildModel(model);
    }

    @Override
    public String getBaseModelName(Long baseModel) {
        return modelMapper.getBaseModelName(baseModel);
    }
}
