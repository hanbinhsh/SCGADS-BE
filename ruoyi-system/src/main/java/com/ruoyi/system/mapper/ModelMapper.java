package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Models;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ModelMapper {
    List<Models> getAllModels();
    void deleteModel(Long modelId);
    void addModel(Models model);
    void updateModel(Models model);
    Models getModelById(Long modelId);
    void updateModelRemark(@Param("modelId") long modelId, @Param("remark") String remark);
    List<Models> findModelsByUserName(String userName);
    void addChildModel(Models model);
}