package com.ruoyi.system.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.entity.ModelImage;
import com.ruoyi.system.service.impl.ModelImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/model-image")
public class ModelImageController {

    @Autowired
    private ModelImageService modelImageService;

    /**
     * 获取所有模型图片
     */
    @GetMapping("/list")
    public AjaxResult list() {
        List<ModelImage> modelImages = modelImageService.getAllModelImages();
        return AjaxResult.success(modelImages);
    }

    /**
     * 根据ID获取模型图片
     */
    @GetMapping("/{modelId}")
    public AjaxResult getById(@PathVariable Long modelId) {
        ModelImage modelImage = modelImageService.getModelImage(modelId);
        return AjaxResult.success(modelImage);
    }

    /**
     * 根据类型获取模型图片
     */
    @GetMapping("/type/{modelType}")
    public AjaxResult getByType(@PathVariable String modelType) {
        List<ModelImage> modelImages = modelImageService.getModelImagesByType(modelType);
        return AjaxResult.success(modelImages);
    }

    /**
     * 刷新缓存
     */
    @PostMapping("/refresh-cache")
    public AjaxResult refreshCache() {
        modelImageService.refreshCache();
        return AjaxResult.success("缓存刷新成功");
    }
}