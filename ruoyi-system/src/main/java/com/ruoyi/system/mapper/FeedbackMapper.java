package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Feedback;
import com.ruoyi.system.domain.entity.FeedbackReply;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface FeedbackMapper {
    void insert(Feedback feedback);
    void deleteFeedback(long feedbackId);
    void insertFeedbackReply(FeedbackReply feedbackReply);
    @MapKey("feedback_id")
    Map<Object, Object> findAllFeedbackWithUserInformation();
    @MapKey("reply_id")
    Map<Object, Object> findFeedbackReply(long userID);
}
