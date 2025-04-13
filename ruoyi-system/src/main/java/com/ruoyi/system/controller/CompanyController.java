package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Feedback;
import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.service.CompanyServer;
import com.ruoyi.system.service.FeedbackServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompanyController {
    @Autowired
    private CompanyServer companyServer;

    @PostMapping("/insertCompany")
    public Result<String> insertCompany(@RequestBody Company company) {
        companyServer.insertCompany(company);
        return Result.success();
    }
}
