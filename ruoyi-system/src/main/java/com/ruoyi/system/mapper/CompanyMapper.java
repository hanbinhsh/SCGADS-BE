package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Company;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CompanyMapper {
    void insertCompany(Company company);
    List<Company> selectAllCompany();
    void deleteCompanyByID(long companyID);
}
