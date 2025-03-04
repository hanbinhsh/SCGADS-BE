package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Feedback;

import java.util.Map;

public interface FeedbackServer {
    void insert(Feedback feedback);
    void deleteFeedback(long feedbackId);
    Map<Object, Object> findAllFeedbackWithUserInformation();
}
