package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/share")
public class ShareController {
    @Autowired
    private ShareService shareService;

    @RequestMapping("/findSharesByUserId")
    @CrossOrigin(origins = "*")
    public Result findSharesByUserId(@RequestParam long userID) {
        return Result.success(shareService.findSharesByUserId(userID));
    }

    @RequestMapping("/findSharesReceivedByUserId")
    @CrossOrigin(origins = "*")
    public Result findSharesReceivedByUserId(@RequestParam long userID) {
        return Result.success(shareService.findSharesReceivedByUserId(userID));
    }
}
