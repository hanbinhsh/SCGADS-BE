package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Scmoannouser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CompanyServer {
    void insertCompany(Company company);
    List<Company> selectAllCompany();
    void deleteCompanyByID(long companyID);
    void updateCompany(Company company);
    void addUserToCompany(long userID, long companyID);
    void removeUserFromCompany(long userID, long companyID);
    List<Scmoannouser> getCompanyUsers(long companyID);
    Company findCompanyByUserID(Long userID);
}
