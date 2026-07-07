package com.sp.system.mapper;

import com.sp.system.entity.Department;

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
}
