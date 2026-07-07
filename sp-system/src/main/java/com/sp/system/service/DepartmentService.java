package com.sp.system.service;

import com.sp.system.entity.Department;
import java.util.List;

/**
 * 部門業務邏輯接口
 */
public interface DepartmentService {

    /**
     * 根據家長用戶ID和學生ID獲取關聯的部門信息
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 部門列表（只返回一條）
     */
    List<Department> getDepartmentsByParentUserId(String parentUserId, String studentUserId);

    /**
     * 根據家長ID和學生ID獲取班級名
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 班級名
     */
    String getClassNameByParentAndStudent(String parentUserId, String studentUserId);
}
