package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Company;
import com.ruoyi.system.domain.entity.Scmoannouser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompanyMapper {
    void insertCompany(Company company);
    List<Company> selectAllCompany();
    void deleteCompanyByID(long companyID);
    void updateCompany(Company company);
    void addUserToCompany(@Param("userID") Long userID, @Param("companyID") Long companyID);
    void removeUserFromCompany(@Param("userID") Long userID, @Param("companyID") Long companyID);
    List<Scmoannouser> getCompanyUsers(long companyID);
    Company findCompanyByUserID(@Param("userID") Long userID);
    Company findCompanyByUserName(@Param("userName") String userName);
}
