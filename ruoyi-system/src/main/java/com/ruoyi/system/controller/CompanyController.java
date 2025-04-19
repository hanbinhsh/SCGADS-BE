package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.service.CompanyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CompanyController {
    @Autowired
    private CompanyServer companyServer;

    @PostMapping("/insertCompany")
    public Result<String> insertCompany(@RequestBody Company company) {
        companyServer.insertCompany(company);
        return Result.success();
    }

    @PostMapping("/selectAllCompany")
    public Result<List<Company>> selectAllCompany() {
        List<Company> companyList = companyServer.selectAllCompany();

        return Result.success(companyList);
    }
    @GetMapping("/deleteCompanyByID")
    public Result<String> deleteCompanyByID(@RequestParam long companyID) {
        companyServer.deleteCompanyByID(companyID);
        return Result.success();
    }
}
