package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Feedback;
import com.ruoyi.system.domain.entity.FeedbackReply;
import com.ruoyi.system.mapper.FeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;

@Service
public class FeedbackServer implements com.ruoyi.system.service.FeedbackServer {
    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public void insert(Feedback feedback) {
        feedback.setCreateTime(new Timestamp(System.currentTimeMillis()));
        feedbackMapper.insert(feedback);
    }

    @Override
    public void insertFeedbackReply(FeedbackReply feedbackReply) {
        feedbackReply.setReplyTime(new Timestamp(System.currentTimeMillis()));
        feedbackMapper.insertFeedbackReply(feedbackReply);
    }

    @Override
    public void deleteFeedback(long feedbackId) {
        feedbackMapper.deleteFeedback(feedbackId);
    }

    @Override
    public Map<Object, Object> findAllFeedbackWithUserInformation() {
        return feedbackMapper.findAllFeedbackWithUserInformation();
    }
}
