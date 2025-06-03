package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Scmoannouser;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
    Scmoannouser findUserByName(@Param("userName") String userName);
    @MapKey("user_id")
    Map<Long, String> selectAllUserIdName();
    Long getCompanyIdByUserId(Long userId);
    String findUserCompanyName(String userName);
}
