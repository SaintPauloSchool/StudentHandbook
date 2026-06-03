package com.sp.system.service.impl;

import com.sp.system.entity.ClassLog;
import com.sp.system.entity.ClassSection;
import com.sp.system.entity.Department;
import com.sp.system.mapper.ClassLogMapper;
import com.sp.system.service.DepartmentService;
import com.sp.system.service.IClassLogService;
import com.sp.system.service.IClassSectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 課程日誌Service業務層處理
 */
@Service
public class ClassLogServiceImpl implements IClassLogService {

    private static final Logger logger = LoggerFactory.getLogger(ClassLogServiceImpl.class);
    
    @Autowired
    private ClassLogMapper classLogMapper;

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private IClassSectionService classSectionService;

    /**
     * 根據家長用戶ID和學生用戶ID獲取過去一個月課程日誌列表
     *
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 過去一個月課程日誌列表
     */
    @Override
    public List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId, String studentUserId) {
        try {
            // 獲取班級部門ID
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId, studentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }
            
            // 使用class_section_sp查詢過去一個月的class_log數據
            List<ClassLog> classLogs = classLogMapper.selectPastMonthClassLogByStudentClass(classSectionSp);
            // 根據規範，僅返回'功課'和'測驗'類型的課程日誌
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取過去一個月課程日誌列表失敗: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 根據家長用戶ID和學生用戶ID獲取當天課程日誌列表
     *
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 當天課程日誌列表
     */
    @Override
    public List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId, String studentUserId) {
        try {
            // 獲取班級部門ID
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId, studentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }
            
            // 使用class_section_sp查詢當天的class_log數據
            List<ClassLog> classLogs = classLogMapper.selectTodayClassLogByStudentClass(classSectionSp);
            // 根據規範，僅返回'功課'和'測驗'類型的課程日誌
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取當天課程日誌列表失敗: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 根據家長用戶ID和學生用戶ID獲取未來七天課程日誌列表
     *
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 未來七天課程日誌列表
     */
    @Override
    public List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId, String studentUserId) {
        try {
            // 獲取班級部門ID
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId, studentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }
            
            // 使用class_section_sp查詢未來七天（不含當天）的class_log數據
            List<ClassLog> classLogs = classLogMapper.selectNextSevenDaysClassLogByStudentClass(classSectionSp);
            // 根據規範，僅返回'功課'和'測驗'類型的課程日誌
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取未來七天課程日誌列表失敗: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 批量插入或更新課程日誌數據
     *
     * @param classLogs 課程日誌數據列表
     */
    @Override
    @Transactional
    public void batchUpsertClassLogs(List<ClassLog> classLogs) {
        try {
            // 檢查參數
            if (classLogs == null || classLogs.isEmpty()) {
                logger.info("沒有需要傳輸的課程日誌數據");
                return;
            }
    
            // 提取所有ID
            List<String> ids = classLogs.stream()
                    .map(ClassLog::getId)
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
    
            if (ids.isEmpty()) {
                logger.info("沒有有效的課程日誌ID需要處理");
                return;
            }
    
            // 批量查詢現有記錄
            List<ClassLog> existingLogs;
            try {
                existingLogs = classLogMapper.selectClassLogsByIds(ids);
            } catch (Exception e) {
                logger.error("批量查詢現有課程日誌記錄時發生數據庫異常: {}", e.getMessage(), e);
                return; // 發生數據庫異常時直接返回
            }
    
            // 將現有記錄放入Map便於快速查找
            Map<String, ClassLog> existingLogsMap = new HashMap<>();
            for (ClassLog existingLog : existingLogs) {
                if (existingLog != null && existingLog.getId() != null) {
                    existingLogsMap.put(existingLog.getId(), existingLog);
                }
            }
    
            // 分離需要插入和更新的記錄
            List<ClassLog> toInsert = new ArrayList<>();
            List<ClassLog> toUpdate = new ArrayList<>();
    
            for (ClassLog classLog : classLogs) {
                if (classLog.getId() == null || classLog.getId().trim().isEmpty()) {
                    continue; // 跳過ID爲空的記錄
                }
    
                if (existingLogsMap.containsKey(classLog.getId())) {
                    // 記錄已存在，需要更新
                    toUpdate.add(classLog);
                } else {
                    // 記錄不存在，需要插入
                    toInsert.add(classLog);
                }
            }
    
            // 批量插入新記錄
            if (!toInsert.isEmpty()) {
                for (ClassLog classLog : toInsert) {
                    try {
                        insertClassLog(classLog);
                    } catch (Exception e) {
                        logger.error("插入課程日誌數據時發生錯誤，ID: {}，錯誤: {}", classLog.getId(), e.getMessage());
                    }
                }
                logger.info("成功插入 {} 條新課程日誌數據", toInsert.size());
            }
    
            // 批量更新現有記錄
            if (!toUpdate.isEmpty()) {
                for (ClassLog classLog : toUpdate) {
                    try {
                        updateClassLog(classLog);
                    } catch (Exception e) {
                        logger.error("更新課程日誌數據時發生錯誤，ID: {}，錯誤: {}", classLog.getId(), e.getMessage());
                    }
                }
                logger.info("成功更新 {} 條課程日誌數據", toUpdate.size());
            }
    
            logger.info("成功傳輸 {} 條課程日誌數據到目標數據庫 ({} 條插入, {} 條更新)",
                    classLogs.size(), toInsert.size(), toUpdate.size());
        } catch (Exception e) {
            logger.error("批量更新課程日誌數據時發生異常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 根據家長用戶ID和學生用戶ID獲取對應的班級代碼(SP)
     * 
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 班級代碼(SP)，如果無法獲取則返回null
     */
    private String getClassSectionSpByParentUserId(String parentUserId, String studentUserId) {
        try {
            // 根據parentUserId和studentUserId查詢部門信息
            List<Department> departments = departmentService.getDepartmentsByParentUserId(parentUserId, studentUserId);
            if (departments.isEmpty()) {
                logger.warn("未找到家長用戶 {} 和學生用戶 {} 對應的部門信息", parentUserId, studentUserId);
                return null;
            }
            
            String departmentName = departments.get(0).getName();
            logger.debug("獲取到部門名稱: {}", departmentName);
            
            // 使用部門名稱查詢class_section表獲取class_section_sp
            ClassSection classSection = classSectionService.getClassSectionByDsedj(departmentName);
            if (classSection == null || classSection.getClassSectionSp() == null) {
                logger.warn("未找到部門 {} 對應的課程班級信息", departmentName);
                return null;
            }
            
            return classSection.getClassSectionSp();
        } catch (Exception e) {
            logger.error("根據家長用戶ID和學生用戶ID獲取班級代碼時發生異常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 過濾有效的課程日誌（僅返回'功課'和'測驗'類型的課程日誌）
     * 
     * @param classLogs 課程日誌列表
     * @return 過濾後的有效課程日誌列表
     */
    private List<ClassLog> filterValidClassLogs(List<ClassLog> classLogs) {
        if (classLogs == null) {
            return java.util.Collections.emptyList();
        }
        
        try {
            return classLogs;
        } catch (Exception e) {
            logger.error("過濾課程日誌時發生異常: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 批量插入課程日誌數據
     *
     * @param classLog 課程日誌列表
     */
    @Transactional
    public void insertClassLog(ClassLog classLog) {
        try {
            classLogMapper.insertClassLog(classLog);
        } catch (Exception e) {
            logger.error("插入課程日誌數據時發生異常: {}", e.getMessage(), e);
            throw e; // 重新拋出異常，讓調用方處理
        }
    }

    /**
     * 批量更新課程日誌數據
     *
     * @param classLog 課程日誌列表
     */
    @Transactional
    public void updateClassLog(ClassLog classLog) {
        try {
            classLogMapper.updateClassLogById(classLog);
        } catch (Exception e) {
            logger.error("更新課程日誌數據時發生異常: {}", e.getMessage(), e);
            throw e; // 重新拋出異常，讓調用方處理
        }
    }

}