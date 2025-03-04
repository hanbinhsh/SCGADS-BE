package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Feedback;
import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.service.FeedbackServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
public class FeedbackController {
    @Autowired
    private FeedbackServer feedbackServer;

    @PostMapping("/feedback")
    public Result<String> feedback(@RequestBody Feedback feedback) {
        feedbackServer.insert(feedback);
        return Result.success();
    }

    @RequestMapping("/findAllFeedbackWithUserInformation")
    @CrossOrigin(origins = "*")
    public Result<Map<Object,Object>> findAllFeedbackWithUserInformation(){
        Map<Object, Object> feedbackWithUserInfo = feedbackServer.findAllFeedbackWithUserInformation();

        // 遍历所有反馈信息和用户信息，并转换用户头像
        for (Object feedback : feedbackWithUserInfo.keySet()) {
            Object userInfo = feedbackWithUserInfo.get(feedback);
            if (userInfo instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) userInfo;
                if (userMap.get("avatar") instanceof byte[] avatarBytes) {
                    String base64Avatar = Base64.getEncoder().encodeToString(avatarBytes);
                    userMap.put("avatarBase64", base64Avatar); // 添加 Base64 编码字段
                }
            }
        }

        return Result.success(feedbackWithUserInfo);
    }

    @DeleteMapping("/deleteFeedback/{feedbackId}")
    public Result<String> deleteFeedback(@PathVariable long feedbackId) {
        feedbackServer.deleteFeedback(feedbackId);
        return Result.success();
    }
}
