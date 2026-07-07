package com.sp.system.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 部門家長綁定數據層
 */
public interface DepartmentParentBindingMapper {

    /**
     * 根據家長ID和學生ID查詢綁定的部門
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 部門ID（只返回一條）
     */
    Long selectByParentUserId(@Param("parentUserId") String parentUserId, @Param("studentUserId") String studentUserId);

    /**
     * 檢查家長是否綁定了學生（即綁定了學生用戶ID的記錄）
     * @param parentUserId 家長用戶ID
     * @return 記錄數量
     */
    int checkHasBoundStudents(@Param("parentUserId") String parentUserId);
}
