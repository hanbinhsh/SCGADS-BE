package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Share;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShareMapper {
    @MapKey("share_id")
    Map<Object, Object> findSharesByUserId(Long userId);

    @MapKey("share_id")
    Map<Object, Object> findSharesReceivedByUserId(Long userId);

    public void insertShare(@Param("shares") Share shares);
}
