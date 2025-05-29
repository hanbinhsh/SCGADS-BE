package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Scmoannouser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CompanyServer {
    void insertCompany(Company company);
    List<Company> selectAllCompany();
    void deleteCompanyByID(long companyID);
    void updateCompany(Company company);
    void addUserToCompany(long userID, long companyID);
    void removeUserFromCompany(long userID, long companyID);
    List<Scmoannouser> getCompanyUsers(long companyID);
    Company findCompanyByUserID(Long userID);
    Company findCompanyByUserName(String userName);
    public Company findCompanyByCompanyName(String companyName);
    Map<Long, String>  selectAllCompanyIdName();
}
