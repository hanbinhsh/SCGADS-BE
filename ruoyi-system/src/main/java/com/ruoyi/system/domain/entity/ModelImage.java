package com.ruoyi.system.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ModelImage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long modelId;
    private String modelName;
    private String modelType;
    private String modelPath;
    private String pretrainModelPath;
    private String predictFilePath;
    private String trainFilePath;
    private String figurePath;
    private String defaultParameters;
    private String remark;
    private String extractLabels;
    private String userName;
    private String companyName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.sql.Timestamp createdTime;
    private boolean pretrainModel;
    private long baseModel;

    private byte[] figureByte;

    public byte[] getFigureByte() {
        return figureByte;
    }

    public void setFigureByte(byte[] figureByte) {
        this.figureByte = figureByte;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
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

    public String getUserName() {
        return userName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isPretrainModel() {
        return pretrainModel;
    }

    public void setPretrainModel(boolean pretrainModel) {
        this.pretrainModel = pretrainModel;
    }

    public String getPretrainModelPath() {
        return pretrainModelPath;
    }

    public void setPretrainModelPath(String pretrainModelPath) {
        this.pretrainModelPath = pretrainModelPath;
    }

    public long getBaseModel() {
        return baseModel;
    }

    public void setBaseModel(long baseModel) {
        this.baseModel = baseModel;
    }
}