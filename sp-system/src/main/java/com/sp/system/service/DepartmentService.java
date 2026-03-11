package com.sp.system.service;

import com.sp.system.entity.Department;
import java.util.List;

/**
 * 部门业务逻辑接口
 */
public interface DepartmentService {

    /**
     * 批量保存部门信息
     * @param departments 部门列表
     */
    void batchSaveDepartments(List<Department> departments);

    /**
     * 根据家长用户ID和学生ID获取关联的部门信息
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID
     * @return 部门列表（只返回一条）
     */
    List<Department> getDepartmentsByParentUserId(String parentUserId, String studentUserId);

    /**
     * 查询特定结构的班级部门 ID
     * @return 部门 ID 列表
     */
    List<Long> getClassDepartmentId();
}