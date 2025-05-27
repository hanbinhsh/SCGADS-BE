package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Models;

import java.util.List;

public interface ModelService {
    public List<Models> getAllModels();
    void deleteModel(Long modelId);

    void addModel(Models model);

    void updateModel(Models model);

    Models getModelById(Long modelId);

    void updateModelRemark(Long modelId, String remark);
    List<Models> findModelsByUserName(String userId);
}
