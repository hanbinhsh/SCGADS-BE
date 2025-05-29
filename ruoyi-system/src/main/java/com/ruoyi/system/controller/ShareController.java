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
}
