package com.sp.system.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 家校通訊錄數據層
 */
public interface SchoolFamilyContactMapper {

    /**
     * 檢查家長是否存在家校通訊錄記錄
     */
    int countByParentUserId(@Param("parentUserId") String parentUserId);
}
