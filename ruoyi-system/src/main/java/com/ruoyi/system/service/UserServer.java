package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Scmoannouser;

import java.util.List;

public interface UserServer {
    List<Scmoannouser> findUsers();
    Scmoannouser findUserByUserName(String userName);
    void deleteUserByUserID(long userID);
    void register(Scmoannouser scmoannouser);
    Scmoannouser findUserByUserNameAndPassword(String userName, String password);
    Scmoannouser findUserByEmail(String email);
    Scmoannouser findUserByPhone(String phone);
    void updateUser(Scmoannouser scmoannouser);
    Scmoannouser findUserByUserId(long userId);
    void approveUser(long userId);
}
