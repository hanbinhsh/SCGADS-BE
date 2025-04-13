package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Feedback;
import com.ruoyi.system.mapper.CompanyMapper;
import com.ruoyi.system.mapper.FeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class CompanyServer implements com.ruoyi.system.service.CompanyServer {
    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public void insertCompany(Company company) {
        companyMapper.insertCompany(company);
    }

}
