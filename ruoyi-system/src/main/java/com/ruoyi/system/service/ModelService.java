package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Models;

import java.util.List;

public interface ModelService {
    public List<Models> getAllModels();
    void deleteModel(Long modelId);

    void addModel(Models model);

    void updateModel(Models model);
}
