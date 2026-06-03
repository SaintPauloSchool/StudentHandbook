package com.sp.system.service;

import com.sp.system.entity.DepartmentAdmin;

import java.util.List;

/**
 * 部門管理員業務邏輯接口
 */
public interface DepartmentAdminService {

    /**
     * 批量保存部門管理員信息（處理新增和更新）
     * @param admins 部門管理員列表
     */
    void batchSaveDepartmentAdmins(List<DepartmentAdmin> admins);

}
