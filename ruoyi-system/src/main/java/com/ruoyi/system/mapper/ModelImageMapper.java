package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.ModelImage;
import com.ruoyi.system.domain.entity.Models;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ModelImageMapper {

    /**
     * 查询所有模型图片数据
     */
    List<Models> selectAllModelImages();

    /**
     * 根据模型ID查询
     */
    Models selectModelImageById(Long modelId);

    /**
     * 根据模型类型查询
     */
    List<ModelImage> selectModelImagesByType(String modelType);
}