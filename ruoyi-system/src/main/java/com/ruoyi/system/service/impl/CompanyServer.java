package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.mapper.CompanyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServer implements com.ruoyi.system.service.CompanyServer {
    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public void insertCompany(Company company) {
        companyMapper.insertCompany(company);
    }

    @Override
    public List<Company> selectAllCompany() {
        return companyMapper.selectAllCompany();
    }

    @Override
    public void deleteCompanyByID(long companyID) {
        companyMapper.deleteCompanyByID(companyID);
    }
}
