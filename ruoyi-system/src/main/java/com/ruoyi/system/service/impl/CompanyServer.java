package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Scmoannouser;
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

    @Override
    public void updateCompany(Company company) {
        companyMapper.updateCompany(company);
    }

    @Override
    public void addUserToCompany(long userID, long companyID) {
        companyMapper.addUserToCompany(userID, companyID);
    }

    @Override
    public void removeUserFromCompany(long userID, long companyID) {
        companyMapper.removeUserFromCompany(userID, companyID);
    }

    @Override
    public List<Scmoannouser> getCompanyUsers(long companyID) {
        return companyMapper.getCompanyUsers(companyID);
    }

    @Override
    public Company findCompanyByUserID(Long userID) {
        return companyMapper.findCompanyByUserID(userID);
    }

    @Override
    public Company findCompanyByUserName(String userName) {
        return companyMapper.findCompanyByUserName(userName);
    }
}
