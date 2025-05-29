package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Share;

import java.util.List;
import java.util.Map;

public interface ShareService {
    public Map<Object, Object> findSharesByUserId(Long userId);
    Map<Object, Object> findSharesReceivedByUserId(Long userId);
    void insertShare(Share shares);
    void deleteShareByShareId(long shareId);
}
