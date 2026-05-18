package com.sp.system.service;

import com.sp.system.entity.DepartmentAdmin;

import java.util.List;

/**
 * 部门管理员业务逻辑接口
 */
public interface DepartmentAdminService {

    /**
     * 批量保存部门管理员信息（处理新增和更新）
     * @param admins 部门管理员列表
     */
    void batchSaveDepartmentAdmins(List<DepartmentAdmin> admins);

}
