package com.sp.system.mapper;

import com.sp.system.entity.DepartmentAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门管理员数据访问层
 */
public interface DepartmentAdminMapper {

    /**
     * 批量插入部门管理员信息（忽略重复）
     * @param admins 部门管理员列表
     */
    void batchInsertDepartmentAdmins(@Param("list") List<DepartmentAdmin> admins);

}
