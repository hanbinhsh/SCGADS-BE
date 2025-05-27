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


    @Override
    public List<Models> getAllModels() {
        return modelMapper.getAllModels();
    }

    @Override
    public void deleteModel(Long modelId) {
        modelMapper.deleteModel(modelId);
    }

    @Override
    public void addModel(Models model) {
        modelMapper.addModel(model);
    }

    @Override
    public void updateModel(Models model) {
        modelMapper.updateModel(model);
    }

    @Override
    public Models getModelById(Long modelId) {
        return modelMapper.getModelById(modelId);
    }

    @Override
    public void updateModelRemark(Long modelId, String remark) {
        modelMapper.updateModelRemark(modelId, remark);
    }

    @Override
    public List<Models> findModelsByUserName(String userId) {
        return modelMapper.findModelsByUserName(userId);
    }
}
