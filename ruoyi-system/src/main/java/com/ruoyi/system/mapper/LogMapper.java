package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface LogMapper {
    @MapKey("log_id")
    Map<Object, Object> findAllLogs();

    public void insertLog(@Param("userId") long userId, @Param("act") String act, @Param("importance") long importance);
}
