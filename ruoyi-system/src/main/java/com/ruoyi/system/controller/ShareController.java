package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.domain.entity.Share;
import com.ruoyi.system.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/share")
public class ShareController {
    @Autowired
    private ShareService shareService;

    @RequestMapping("/findSharesByUserId")
    @CrossOrigin(origins = "*")
    public Result<List<Object>> findSharesByUserId(@RequestParam long userID) {
        Map<Object, Object> map = shareService.findSharesByUserId(userID);
        List<Object> maplist = new ArrayList<>(map.values()); // 提取值转换为列表
        return Result.success(maplist);
    }

    @RequestMapping("/findSharesReceivedByUserId")
    @CrossOrigin(origins = "*")
    public Result<List<Object>> findSharesReceivedByUserId(@RequestParam long userID) {
        Map<Object, Object> map = shareService.findSharesReceivedByUserId(userID);
        List<Object> maplist = new ArrayList<>(map.values()); // 提取值转换为列表
        return Result.success(maplist);
    }

    @RequestMapping("/insertShare")
    public Map<String, Object> insertShare(@RequestBody List<Share> share) {
        int count = share.size();
        for (Share shares : share) {
            shareService.insertShare(shares);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200 );
        result.put("msg", "创建成功并返回相应资源数据");
        result.put("count",count);
        return result;
    }

    @RequestMapping("/deleteShareByShareId")
    public Map<String, Object> deleteShareByShareId(@RequestBody Map<String, String> map) {
        shareService.deleteShareByShareId(Long.parseLong(map.get("shareId")));
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200 );
        result.put("msg", "删除成功并返回相应资源数据");
        return result;
    }

    /**
     * 获取所有分享详情（前端主要调用的接口）
     */
    @GetMapping("/findAllShareWithDetails")
    @CrossOrigin(origins = "*")
    public Result<Map<String, Object>> findAllShareWithDetails() {
        try {
            Map<String, Object> shareDetails = shareService.findAllShareWithDetails();
            return Result.success(shareDetails);
        } catch (Exception e) {
            return Result.error("获取分享详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新分享设置
     */
    @PutMapping("/updateShare")
    @CrossOrigin(origins = "*")
    public Map<String, Object> updateShare(@RequestBody Share share) {
        System.out.println(share);
        Map<String, Object> result = new HashMap<>();
        try {
            shareService.updateShare(share);
            result.put("code", 200);
            result.put("msg", "分享设置更新成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新失败: " + e.getMessage());
        }
        return result;
    }
}
