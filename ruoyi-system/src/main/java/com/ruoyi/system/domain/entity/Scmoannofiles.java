package com.ruoyi.system.domain.entity;


import java.io.File;

public class Scmoannofiles {

  private long fileId;
  private String scRnaSeqFile;
  private String scAtacSeqFile;
  private String tagFile;
  private String taskName;


  public long getFileId() {
    return fileId;
  }

  public void setFileId(long fileId) {
    this.fileId = fileId;
  }


  public String getScRna_SeqFile() {
    return scRnaSeqFile;
  }

  public void setScRna_SeqFile(String scRna_SeqFile) {
    this.scRnaSeqFile = scRna_SeqFile;
  }


  public String getScAtac_SeqFile() {
    return scAtacSeqFile;
  }

  public void setScAtac_SeqFile(String scAtac_SeqFile) {
    this.scAtacSeqFile = scAtac_SeqFile;
  }


  public String getTagFile() {
    return tagFile;
  }

  public void setTagFile(String tagFile) {
    this.tagFile = tagFile;
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
