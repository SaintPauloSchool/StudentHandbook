package com.sp.system.service.impl;

import com.sp.system.entity.DepartmentAdmin;
import com.sp.system.mapper.DepartmentAdminMapper;
import com.sp.system.service.DepartmentAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部門管理員業務邏輯實現類
 */
@Service
public class DepartmentAdminServiceImpl implements DepartmentAdminService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentAdminServiceImpl.class);

    @Autowired
    private DepartmentAdminMapper departmentAdminMapper;

    /**
     * 批量保存部門管理員信息（處理新增和更新）
     *
     * @param admins 部門管理員列表
     */
    @Override
    @Transactional
    public void batchSaveDepartmentAdmins(List<DepartmentAdmin> admins) {
        // 檢查列表是否爲空
        if (admins == null || admins.isEmpty()) {
            return;
        }

        List<DepartmentAdmin> toInsert = new ArrayList<>();
        List<DepartmentAdmin> toUpdate = new ArrayList<>();
        int insertCount = 0;
        int updateCount = 0;

        // 遍歷每條記錄，檢查是否存在
        for (DepartmentAdmin admin : admins) {
            // 查詢是否已存在相同的 department_id 和 userid 組合
            DepartmentAdmin existing = departmentAdminMapper.selectByDepartmentIdAndUserid(
                    admin.getDepartmentId(), admin.getUserid());

            if (existing != null) {
                // 已存在，需要更新
                admin.setId(existing.getId());
                admin.setUpdateTime(LocalDateTime.now());
                toUpdate.add(admin);
            } else {
                // 不存在，需要新增
                admin.setCreateTime(LocalDateTime.now());
                admin.setUpdateTime(LocalDateTime.now());
                toInsert.add(admin);
            }
        }

        // 批量插入新記錄
        if (!toInsert.isEmpty()) {
            departmentAdminMapper.batchInsertDepartmentAdmins(toInsert);
            insertCount = toInsert.size();
            logger.info("新增部門管理員 {} 條", insertCount);
        }

        // 逐條更新已有記錄
        if (!toUpdate.isEmpty()) {
            for (DepartmentAdmin admin : toUpdate) {
                departmentAdminMapper.updateByDepartmentIdAndUserid(admin);
            }
            updateCount = toUpdate.size();
            logger.info("更新部門管理員 {} 條", updateCount);
        }

        logger.info("部門管理員同步完成 - 新增: {}, 更新: {}", insertCount, updateCount);
    }

}
