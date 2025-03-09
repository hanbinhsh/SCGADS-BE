package com.ruoyi.system.domain.entity;

public class Scmoannouser {
  private long userId;
  private String userName;
  private String psw;
  private String email;
  private long isAdmin;
  private String phone;
  private byte[] avatar;
  private long companyId;
  // New
  private String avatarBase64; // 头像的 Base64 编码字符串

  // Getters & Setters
  public long getUserId() {return userId;}
  public void setUserId(long userId) {this.userId = userId;}
  public String getUserName() {return userName;}
  public void setUserName(String userName) {this.userName = userName;}
  public String getPsw() {return psw;}
  public void setPsw(String psw) {this.psw = psw;}
  public String getEmail() {return email;}
  public void setEmail(String email) {this.email = email;}
  public long getIsAdmin() {return isAdmin;}
  public void setIsAdmin(long isAdmin) {this.isAdmin = isAdmin;}
  public String getPhone() {return phone;}
  public void setPhone(String phone) {this.phone = phone;}
  public byte[] getAvatar() {return avatar;}
  public void setAvatar(byte[] avatar) {this.avatar = avatar;}
  public String getAvatarBase64() {return avatarBase64;}
  public void setAvatarBase64(String avatarBase64) {this.avatarBase64 = avatarBase64;}
  public long getCompanyId() {return companyId;}
  public void setCompanyId(long companyId) {this.companyId = companyId;}

}
