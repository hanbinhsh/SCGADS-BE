package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Scmoannouser;
import com.ruoyi.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServer implements com.ruoyi.system.service.UserServer {
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public List<Scmoannouser> findUsers() {
        return userMapper.findUsers();
    }

    @Override
    @Transactional
    public Scmoannouser findUserByUserName(String userName) {
        return userMapper.findUserByUserName(userName);
    }

    @Override
    public void deleteUserByUserID(long userID) {
        userMapper.deleteUserByUserID(userID);
    }

    @Override
    @Transactional
    public Scmoannouser findUserByUserNameAndPassword(String userName, String password) {
        return userMapper.findUserByUserNameAndPassword(userName, password);
    }

    @Override
    public void register(Scmoannouser scmoannouser) {
        userMapper.register(scmoannouser);
    }

    @Override
    public Scmoannouser findUserByEmail(String email) {
        return userMapper.findUserByUserEmail(email);
    }

    @Override
    public Scmoannouser findUserByPhone(String phone) {
        return userMapper.findUserByUserPhone(phone);
    }

    @Override
    public void updateUser(Scmoannouser scmoannouser) {
        userMapper.updateUser(scmoannouser);
    }

    @Override
    public Scmoannouser findUserByUserId(long userId){return userMapper.findUserByUserId(userId);}

    @Override
    public void approveUser(long userId){userMapper.approveUser(userId);}

    @Transactional(readOnly = true)
    @Override
    public Scmoannouser findUserByName(String userName){
        return userMapper.findUserByName(userName);
    }


}
