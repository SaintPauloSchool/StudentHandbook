package com.sp.system.service.impl;

import com.sp.system.entity.DepartmentAdmin;
import com.sp.system.mapper.DepartmentAdminMapper;
import com.sp.system.service.DepartmentAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理员业务逻辑实现类
 */
@Service
public class DepartmentAdminServiceImpl implements DepartmentAdminService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentAdminServiceImpl.class);

    @Autowired
    private DepartmentAdminMapper departmentAdminMapper;

    /**
     * 批量保存部门管理员信息（处理新增和更新）
     *
     * @param admins 部门管理员列表
     */
    @Override
    @Transactional
    public void batchSaveDepartmentAdmins(List<DepartmentAdmin> admins) {
        // 检查列表是否为空
        if (admins == null || admins.isEmpty()) {
            return;
        }

        List<DepartmentAdmin> toInsert = new ArrayList<>();
        List<DepartmentAdmin> toUpdate = new ArrayList<>();
        int insertCount = 0;
        int updateCount = 0;

        // 遍历每条记录，检查是否存在
        for (DepartmentAdmin admin : admins) {
            // 查询是否已存在相同的 department_id 和 userid 组合
            DepartmentAdmin existing = departmentAdminMapper.selectByDepartmentIdAndUserid(
                    admin.getDepartmentId(), admin.getUserid());

            if (existing != null) {
                // 已存在，需要更新
                admin.setId(existing.getId());
                toUpdate.add(admin);
            } else {
                // 不存在，需要新增
                toInsert.add(admin);
            }
        }

        // 批量插入新记录
        if (!toInsert.isEmpty()) {
            departmentAdminMapper.batchInsertDepartmentAdmins(toInsert);
            insertCount = toInsert.size();
            logger.info("新增部门管理员 {} 条", insertCount);
        }

        // 逐条更新已有记录
        if (!toUpdate.isEmpty()) {
            for (DepartmentAdmin admin : toUpdate) {
                departmentAdminMapper.updateByDepartmentIdAndUserid(admin);
            }
            updateCount = toUpdate.size();
            logger.info("更新部门管理员 {} 条", updateCount);
        }

        logger.info("部门管理员同步完成 - 新增: {}, 更新: {}", insertCount, updateCount);
    }

}
