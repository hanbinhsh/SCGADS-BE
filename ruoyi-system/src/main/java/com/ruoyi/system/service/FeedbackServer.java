package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Feedback;
import com.ruoyi.system.domain.entity.FeedbackReply;

import java.util.Map;

public interface FeedbackServer {
    void insert(Feedback feedback);
    void deleteFeedback(long feedbackId);
    void insertFeedbackReply(FeedbackReply feedbackReply);
    Map<Object, Object> findAllFeedbackWithUserInformation();
    Map<Object, Object> findFeedbackReply(long userId);
}
