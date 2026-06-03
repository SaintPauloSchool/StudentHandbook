package com.sp.system.mapper;

import com.sp.system.entity.Department;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部門數據訪問層
 */
public interface DepartmentMapper {

    /**
     * 根據部門ID查詢部門信息
     * @param id 部門ID
     * @return 部門信息
     */
    Department selectById(Long id);

    /**
     * 批量插入部門信息
     * @param departments 部門列表
     */
    void batchInsertDepartments(@Param("list") List<Department> departments);

    /**
     * 查詢特定結構的班級部門 ID
     * @return 部門 ID 列表
     */
    List<Long> selectClassDepartmentId();
}