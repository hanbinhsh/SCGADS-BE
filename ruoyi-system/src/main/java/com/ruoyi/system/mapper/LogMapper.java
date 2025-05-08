package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface LogMapper {
    @MapKey("log_id")
    Map<Object, Object> findAllLogs();
}
