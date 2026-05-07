package com.sp.system.service.impl;

import com.sp.system.entity.DepartmentAdmin;
import com.sp.system.mapper.DepartmentAdminMapper;
import com.sp.system.service.DepartmentAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门管理员业务逻辑实现类
 */
@Service
public class DepartmentAdminServiceImpl implements DepartmentAdminService {

    @Autowired
    private DepartmentAdminMapper departmentAdminMapper;

    /**
     * 批量保存部门管理员信息
     *
     * @param admins 部门管理员列表
     */
    @Override
    @Transactional
    public void batchSaveDepartmentAdmins(List<DepartmentAdmin> admins) {
        // 检查列表是否为空
        if (admins != null && !admins.isEmpty()) {
            // 批量插入或更新部门管理员信息
            departmentAdminMapper.batchInsertDepartmentAdmins(admins);
        }
    }

}
