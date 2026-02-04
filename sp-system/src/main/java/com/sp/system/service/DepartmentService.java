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
     * 根据家长用户ID获取关联的部门信息
     * @param parentUserId 家长用户ID
     * @return 部门列表
     */
    List<Department> getDepartmentsByParentUserId(String parentUserId);

    /**
     * 查询特定结构的班级部门ID
     * @return 部门ID
     */
    Long getClassDepartmentId();
}