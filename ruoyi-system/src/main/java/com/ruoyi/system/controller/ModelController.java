package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.service.impl.ModelUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.system.service.impl.ModelsService;

import java.util.List;

@RestController
@RequestMapping("/models")
public class ModelController {
    @Autowired
    private com.ruoyi.system.service.ModelService modelService;

    @Autowired
    private ModelsService modelsService;

    @Autowired
    private ModelUtilService modelUtil;

    @GetMapping("/findAllModels")
    public ResponseEntity<List<Models>> getAllModels() {

        List<Models> models = modelsService.getAllModel();
        modelUtil.changeAllModelImage(models);

        return ResponseEntity.ok(models);
    }

    @PostMapping("/updateModel")
    public Result updateModel(@RequestParam("modelId") long modelId,
                              @RequestParam("modelName") String modelName,
                              @RequestParam("modelType") String modelType,
                              @RequestParam("modelPath") String modelPath,
                              @RequestParam("pretrainModelPath") String pretrainModelPath,
                              @RequestParam("predictFilePath") String predictFilePath,
                              @RequestParam("trainFilePath") String trainFilePath,
                              @RequestParam("figurePath") String figurePath,
                              @RequestParam("defaultParameters") String defaultParameters,
                              @RequestParam("remark") String remark,
                              @RequestParam("extractLabels") String extractLabels,
                              @RequestParam("userName") String userName,
                              @RequestParam("companyName") String companyName,
                              @RequestParam("pretrainModel") boolean pretrainModel) {
        Models model = new Models();
        model.setModelId(modelId);
        model.setModelName(modelName);
        model.setModelType(modelType);
        model.setModelPath(modelPath);
        model.setPretrainModelPath(pretrainModelPath);
        model.setPredictFilePath(predictFilePath);
        model.setTrainFilePath(trainFilePath);
        model.setFigurePath(figurePath);
        model.setDefaultParameters(defaultParameters);
        model.setRemark(remark);
        model.setExtractLabels(extractLabels);
        model.setUserName(userName);
        model.setCompanyName(companyName);
        model.setPretrainModel(pretrainModel);
        modelService.updateModel(model);
        return Result.success();
    }

    @PostMapping("/updateModelRemark")
    public Result updateModelRemark(@RequestParam("modelId") long modelId,
                              @RequestParam("remark") String remark) {
        modelService.updateModelRemark(modelId, remark);

        return Result.success();
    }

    @PostMapping("/addModel")
    public Result addModel(@RequestParam("modelName") String modelName,
                           @RequestParam("modelType") String modelType,
                           @RequestParam("modelPath") String modelPath,
                           @RequestParam("pretrainModelPath") String pretrainModelPath,
                           @RequestParam("predictFilePath") String predictFilePath,
                           @RequestParam("trainFilePath") String trainFilePath,
                           @RequestParam("figurePath") String figurePath,
                           @RequestParam("defaultParameters") String defaultParameters,
                           @RequestParam("remark") String remark,
                           @RequestParam("extractLabels") String extractLabels,
                           @RequestParam("userName") String userName,
                           @RequestParam("companyName") String companyName,
                           @RequestParam("pretrainModel") boolean pretrainModel) {
        Models model = new Models();
        model.setModelName(modelName);
        model.setModelType(modelType);
        model.setModelPath(modelPath);
        model.setPretrainModelPath(pretrainModelPath);
        model.setPredictFilePath(predictFilePath);
        model.setTrainFilePath(trainFilePath);
        model.setFigurePath(figurePath);
        model.setDefaultParameters(defaultParameters);
        model.setRemark(remark);
        model.setExtractLabels(extractLabels);
        model.setUserName(userName);
        model.setCompanyName(companyName);
        model.setPretrainModel(pretrainModel);
        modelService.addModel(model);
        return Result.success();
    }

    @PostMapping("/addChildModel")
    public Result addChildModel(@RequestParam("taskName") String taskName,
                                @RequestParam("modelPath") String modelPath,
                                @RequestParam("pretrainModelPath") String pretrainModelPath,
                                @RequestParam("defaultParameters") String defaultParameters,
                                @RequestParam("extractLabels") String extractLabels,
                                @RequestParam("userName") String userName,
                                @RequestParam("companyName") String companyName,
                                @RequestParam("base_model") long base_model) {
        Models model = modelService.getModelById(base_model);   // 获取基础模型
        model.setModelName(taskName);                           // 更改模型名称为任务名
        model.setModelPath(modelPath);                          // 更新模型地址
        model.setPretrainModelPath(pretrainModelPath);          // 更新预训练模型地址
        model.setDefaultParameters(defaultParameters);          // 更新参数
        model.setRemark("");                                    // 清空注释
        model.setExtractLabels(extractLabels);                  // 更新标签映射
        model.setUserName(userName);                            // 更新用户名
        model.setCompanyName(companyName);                      // 更新公司名
        model.setBaseModel(base_model);                         // 更新基础模型

        modelService.addChildModel(model);
        return Result.success();
    }

    @DeleteMapping("/deleteModel")
    public Result deleteModel(@RequestParam("modelId") Long modelId) {
        modelService.deleteModel(modelId);
        return Result.success();
    }

    @GetMapping("/findModelsByUserName")
    public Result<List<Models>> findModelsByUserName(@RequestParam("userName") String userName) {
        List<Models> models = modelService.findModelsByUserName(userName);
        return Result.success(models);
    }
}
