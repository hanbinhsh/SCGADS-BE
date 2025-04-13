package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Company;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyMapper {
    void insertCompany(Company company);
}
