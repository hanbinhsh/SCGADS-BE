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

    public void deleteShareByShareId(@Param("shareId") long shareId);

    /**
     * 获取所有分享的详细信息，包含用户名、任务名、公司名等
     */
    @MapKey("share_id")
    List<Map<String, Object>> findAllShareWithDetails();

     /**
     * 更新分享设置（主要更新密码和到期时间）
     */
    void updateShare(Share share);
}
