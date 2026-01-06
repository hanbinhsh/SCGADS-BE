package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Models;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ModelService {
    public List<Models> getAllModels();
    void deleteModel(Long modelId);

    void addModel(Models model);

    void updateModel(Models model);

    Models getModelById(Long modelId);

    void updateModelRemark(Long modelId, String remark);
    List<Models> findModelsByUserName(String userId);
    void addChildModel(Models model);
    String getBaseModelName(Long baseModel);

    void uploadAndUnzipPackage(MultipartFile file, String modelName, String modelType) throws Exception;
}
