package com.ruoyi.system.domain.entity;


import java.io.File;

public class Scmoannoresult {

  private long resultId;
  private String configFile;
  private String dataFile;
  private String lableFile;
  private String taskName;


  public long getResultId() {
    return resultId;
  }

  public void setResultId(long resultId) {
    this.resultId = resultId;
  }


  public String getConfigFile() {
    return configFile;
  }

  public void setConfigFile(String configFile) {
    this.configFile = configFile;
  }


  public String getDataFile() {
    return dataFile;
  }

  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
  }


  public String getLableFile() {
    return lableFile;
  }

  public void setLableFile(String lableFile) {
    this.lableFile = lableFile;
  }


  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

//  public void deleteFile(String filePath) {
//    File file = new File(filePath);
//    if (file.exists()) {
//      file.delete();
//    }
//  }
}
