package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Company;

import java.util.List;

public interface CompanyServer {
    void insertCompany(Company company);
    List<Company> selectAllCompany();
    void deleteCompanyByID(long companyID);
}
