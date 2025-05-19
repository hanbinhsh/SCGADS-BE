package com.ruoyi.system.domain.entity;


public class Models {

  private long modelId;
  private String modelName;
  private String modelType;
  private String modelPath;
  private String predictFilePath;
  private String trainFilePath;
  private String figurePath;
  private String defaultParameters;
  private String remark;
  private String extractLabels;

  private byte[] figureByte;

  public byte[] getFigureByte() {
    return figureByte;
  }

  public void setFigureByte(byte[] figureByte) {
    this.figureByte = figureByte;
  }

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


  public String getFigurePath() {
    return figurePath;
  }

  public void setFigurePath(String figurePath) {
    this.figurePath = figurePath;
  }


  public String getDefaultParameters() {
    return defaultParameters;
  }

  public void setDefaultParameters(String defaultParameters) {
    this.defaultParameters = defaultParameters;
  }

  public String getRemark() {return remark;}

  public void setRemark(String remark) {this.remark = remark;}

    public String getExtractLabels() {
        return extractLabels;
    }

    public void setExtractLabels(String extractLabels) {
        this.extractLabels = extractLabels;
    }
}
