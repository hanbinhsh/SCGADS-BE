package com.ruoyi.system.domain.entity;


public class Log {

  private long logId;
  private long userId;
  private String action;
  private long importance;
  private java.sql.Timestamp timestamp;


  public long getLogId() {
    return logId;
  }

  public void setLogId(long logId) {
    this.logId = logId;
  }


  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }


  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }


  public long getImportance() {
    return importance;
  }

  public void setImportance(long importance) {
    this.importance = importance;
  }


  public java.sql.Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(java.sql.Timestamp timestamp) {
    this.timestamp = timestamp;
  }

}
