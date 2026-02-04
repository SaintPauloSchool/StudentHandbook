package com.sp.system.mapper;

import com.sp.system.entity.Department;
import java.util.List;

/**
 * 部门数据访问层
 */
public interface DepartmentMapper {

    /**
     * 根据部门ID查询部门信息
     * @param id 部门ID
     * @return 部门信息
     */
    Department selectById(Long id);

    /**
     * 批量插入部门信息
     * @param departments 部门列表
     */
    void batchInsertDepartments(List<Department> departments);

    /**
     * 查询特定结构的班级部门ID
     * @return 部门ID
     */
    Long selectClassDepartmentId();
}