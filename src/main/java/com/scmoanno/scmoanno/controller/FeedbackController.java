package com.scmoanno.scmoanno.controller;

import com.scmoanno.scmoanno.entity.Feedback;
import com.scmoanno.scmoanno.entity.Result;
import com.scmoanno.scmoanno.entity.Scmoannotask;
import com.scmoanno.scmoanno.servers.FeedbackServer;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
public class FeedbackController {
    @Resource
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
