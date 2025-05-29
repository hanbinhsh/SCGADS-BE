package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Scmoannouser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    List<Scmoannouser> findUsers();
    Scmoannouser findUserByUserName(String userName);
    void register(Scmoannouser scmoannouser);
    Scmoannouser findUserByUserNameAndPassword(@Param("userName") String userName, @Param("password") String password);
    Scmoannouser findUserByUserEmail(String email);
    Scmoannouser findUserByUserPhone(String phone);
    void deleteUserByUserID(long userID);
    void updateUser(Scmoannouser scmoannouser);
    Scmoannouser findUserByUserId(long userId);
    void approveUser(long userId);
    public Scmoannouser findUserByName(@Param("userName") String userName);
}
