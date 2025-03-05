package com.ruoyi.system.domain.entity;


public class Models {

  private long modelId;
  private String modelName;
  private String modelType;
  private String modelPath;
  private String predictFilePath;
  private String trainFilePath;
  private String defaultParameters;


  public long getModelId() {
    return modelId;
  }

  public void setModelId(long modelId) {
    this.modelId = modelId;
  }


  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }


  public String getModelType() {
    return modelType;
  }

  public void setModelType(String modelType) {
    this.modelType = modelType;
  }


  public String getModelPath() {
    return modelPath;
  }

  public void setModelPath(String modelPath) {
    this.modelPath = modelPath;
  }


  public String getPredictFilePath() {
    return predictFilePath;
  }

  public void setPredictFilePath(String predictFilePath) {
    this.predictFilePath = predictFilePath;
  }


  public String getTrainFilePath() {
    return trainFilePath;
  }

  public void setTrainFilePath(String trainFilePath) {
    this.trainFilePath = trainFilePath;
  }


  public String getDefaultParameters() {
    return defaultParameters;
  }

  public void setDefaultParameters(String defaultParameters) {
    this.defaultParameters = defaultParameters;
  }

}
