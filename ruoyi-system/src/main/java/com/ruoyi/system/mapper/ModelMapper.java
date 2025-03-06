package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Models;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ModelMapper {
    public List<Models> getAllModels();
    void deleteModel(Long modelId);

    void addModel(Models model);
    void updateModel(Models model);
}
