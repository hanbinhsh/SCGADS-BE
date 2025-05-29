package com.ruoyi.system.domain.entity;


import com.fasterxml.jackson.annotation.JsonFormat;

public class Share {

  private long shareId;
  private long taskId;
  private long sharerId;
  private long receiverId;
  private long companyId;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private java.sql.Timestamp sharedTime;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private java.sql.Timestamp dueTime;
  private String password;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public long getShareId() {
    return shareId;
  }

  public void setShareId(long shareId) {
    this.shareId = shareId;
  }


  public long getTaskId() {
    return taskId;
  }

  public void setTaskId(long taskId) {
    this.taskId = taskId;
  }


  public long getSharerId() {
    return sharerId;
  }

  public void setSharerId(long sharerId) {
    this.sharerId = sharerId;
  }


  public long getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(long receiverId) {
    this.receiverId = receiverId;
  }


  public long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(long companyId) {
    this.companyId = companyId;
  }


  public java.sql.Timestamp getSharedTime() {
    return sharedTime;
  }

  public void setSharedTime(java.sql.Timestamp sharedTime) {
    this.sharedTime = sharedTime;
  }


  public java.sql.Timestamp getDueTime() {
    return dueTime;
  }

  public void setDueTime(java.sql.Timestamp dueTime) {
    this.dueTime = dueTime;
  }

}
