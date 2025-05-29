package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.domain.entity.Scmoannouser;
import com.ruoyi.system.service.CompanyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/updateCompany")
    @CrossOrigin(origins = "*")
    public Result<String> updateCompany(@RequestParam("companyId") Long companyId,
                                             @RequestParam("companyName") String companyName ){
        Company company = new Company();
        company.setCompanyId(companyId);
        company.setCompanyName(companyName);
        companyServer.updateCompany(company);
        return Result.success();
    }

    @PostMapping("/addUserToCompany")
    @CrossOrigin(origins = "*")
    public Result<String> addUserToCompany(@RequestBody Map<String, String> map){
        companyServer.addUserToCompany( Long.parseLong(map.get("userId")), Long.parseLong(map.get("companyId")));
        System.out.println("更新成功，Id = " + map.get("companyId"));
        return Result.success();
    }

    @PostMapping("/removeUserFromCompany")
    @CrossOrigin(origins = "*")
    public Result<String> removeUserFromCompany(@RequestBody Map<String, String> map){
        companyServer.removeUserFromCompany( Long.parseLong(map.get("userId")), Long.parseLong(map.get("companyId")));
        return Result.success();
    }

    @GetMapping("/getCompanyUsers")
    @CrossOrigin(origins = "*")
    public Result<List<Scmoannouser>> getCompanyUsers(@RequestParam long companyId){
        List<Scmoannouser> userList = companyServer.getCompanyUsers(companyId);

        // 遍历所有用户并转换头像
        for (Scmoannouser user : userList) {
            if (user.getAvatar() != null) {
                String base64Avatar = Base64.getEncoder().encodeToString(user.getAvatar());
                user.setAvatarBase64(base64Avatar); // 添加 Base64 编码字段
                user.setAvatar(null); // 清除 BLOB 字段
            }
        }

        return Result.success(userList);
    }

    @GetMapping("/findCompanyByUserID")
    @CrossOrigin(origins = "*")
    public Result<Company> findCompanyByUserID(@RequestParam long userId){
        Company company = companyServer.findCompanyByUserID(userId);
        return Result.success(company);
    }

    @GetMapping("/findCompanyByUserName")
    @CrossOrigin(origins = "*")
    public Result<Company> findCompanyByUserName(@RequestParam String userName){
        Company company = companyServer.findCompanyByUserName(userName);
        return Result.success(company);
    }
}
