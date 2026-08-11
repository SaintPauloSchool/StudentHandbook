package com.sp.system.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 企業微信學校部門成員 Mapper
 */
public interface WecomSchoolDepartmentMemberMapper {

    /**
     * 檢查 userid 是否存在於部門成員表
     */
    int countByUserid(@Param("userid") String userid);
}
