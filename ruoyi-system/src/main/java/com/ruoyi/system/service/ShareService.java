package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Share;

import java.util.List;
import java.util.Map;

public interface ShareService {
    public Map<Object, Object> findSharesByUserId(Long userId);
    Map<Object, Object> findSharesReceivedByUserId(Long userId);
    void insertShare(Share shares);
    void deleteShareByShareId(long shareId);

    /**
     * 获取所有分享的详细信息（包含用户名、任务名等）
     */
    Map<String, Object> findAllShareWithDetails();

    /**
     * 更新分享设置
     */
    void updateShare(Share share);

    boolean existsByTaskIdAndReceiverId(Long taskId, Long userId);
    boolean existsByTaskIdAndCompanyId(Long taskId, Long userCompanyId);
    boolean existsByTaskIdAndPassword(Long taskId, String password);
}
