package com.ruoyi.system.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test/redis")
public class RedisTestController {


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/set")
    public String setValue() {
        redisTemplate.opsForValue().set("testKey", "Hello Redis");
        return "Value set";
    }

    @GetMapping("/get")
    public String getValue() {
        return (String) redisTemplate.opsForValue().get("testKey");
    }
}