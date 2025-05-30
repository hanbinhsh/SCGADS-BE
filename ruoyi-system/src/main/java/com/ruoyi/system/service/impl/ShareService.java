package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Share;
import com.ruoyi.system.mapper.ShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShareService implements com.ruoyi.system.service.ShareService {
    @Autowired
    private ShareMapper shareMapper;

    @Override
    public Map<Object, Object> findSharesByUserId(Long userId) {
        return shareMapper.findSharesByUserId(userId);
    }

    @Override
    public Map<Object, Object> findSharesReceivedByUserId(Long userId) {
        return shareMapper.findSharesReceivedByUserId(userId);
    }
    @Override
    public void insertShare(Share shares) {
        shareMapper.insertShare(shares);
    }

    @Override
    public void deleteShareByShareId(long shareId) {
        shareMapper.deleteShareByShareId(shareId);
    }

    @Override
    public Map<String, Object> findAllShareWithDetails() {
        List<Map<String, Object>> shareDetails = shareMapper.findAllShareWithDetails();
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < shareDetails.size(); i++) {
            result.put(String.valueOf(i), shareDetails.get(i));
        }
        return result;
    }

    @Override
    public void updateShare(Share share) {
        shareMapper.updateShare(share);
    }

    @Override
    public boolean existsByTaskIdAndReceiverId(Long taskId, Long userId) {
        return shareMapper.existsByTaskIdAndReceiverId(taskId, userId);
    }

    @Override
    public boolean existsByTaskIdAndCompanyId(Long taskId, Long userCompanyId) {
        return shareMapper.existsByTaskIdAndCompanyId(taskId, userCompanyId);
    }

    @Override
    public boolean existsByTaskIdAndPassword(Long taskId, String password) {
        return shareMapper.existsByTaskIdAndPassword(taskId, password);
    }
}
