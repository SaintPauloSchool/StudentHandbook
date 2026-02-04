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
 * 课程日志Service业务层处理
 */
@Service
public class ClassLogServiceImpl implements IClassLogService {

    private static final Logger logger = LoggerFactory.getLogger(ClassLogServiceImpl.class);
    
    private static final String COURSE_TYPE_HOMEWORK = "功課";
    private static final String COURSE_TYPE_EXAM = "測驗";
    
    @Autowired
    private ClassLogMapper classLogMapper;

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private IClassSectionService classSectionService;
    
    @Override
    public List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }
            
            // 使用class_section_sp查询过去一个月的class_log数据
            List<ClassLog> classLogs = classLogMapper.selectPastMonthClassLogByStudentClass(classSectionSp);
            // 根据规范，仅返回'功課'和'測驗'类型的课程日志
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根据家长用户ID获取过去一个月课程日志列表失败: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    @Override
    public List<ClassLog> getClassLogListByParentUserId(String parentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }
            
            // 使用class_section_sp查询class_log数据
            List<ClassLog> classLogs = classLogMapper.selectClassLogByStudentClass(classSectionSp);
            // 根据规范，仅返回'功課'和'測驗'类型的课程日志
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根据家长用户ID获取课程日志列表失败: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    @Override
    public List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }
            
            // 使用class_section_sp查询当天的class_log数据
            List<ClassLog> classLogs = classLogMapper.selectTodayClassLogByStudentClass(classSectionSp);
            // 根据规范，仅返回'功課'和'測驗'类型的课程日志
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根据家长用户ID获取当天课程日志列表失败: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    @Override
    public List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId);
            if (classSectionSp == null) {
                return Collections.emptyList();
            }
            
            // 使用class_section_sp查询未来七天（不含当天）的class_log数据
            List<ClassLog> classLogs = classLogMapper.selectNextSevenDaysClassLogByStudentClass(classSectionSp);
            // 根据规范，仅返回'功課'和'測驗'类型的课程日志
            return filterValidClassLogs(classLogs);
        } catch (Exception e) {
            logger.error("根据家长用户ID获取未来七天课程日志列表失败: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    @Override
    public ClassLog getClassLogDetailByParentUserId(String id, String parentUserId) {
        try {
            String classSectionSp = getClassSectionSpByParentUserId(parentUserId);
            if (classSectionSp == null) {
                return null;
            }
            
            // 从外部class_log表获取指定ID且匹配学生班级的数据
            ClassLog classLog = classLogMapper.selectClassLogByIdAndStudentClass(id, classSectionSp);
            // 根据规范，仅返回'功課'和'測驗'类型的课程日志
            if (classLog != null && !(COURSE_TYPE_HOMEWORK.equals(classLog.getCourseType()) || COURSE_TYPE_EXAM.equals(classLog.getCourseType()))) {
                return null; // 如果日志类型不符合要求，返回null
            }
            return classLog;
        } catch (Exception e) {
            logger.error("根据家长用户ID获取课程日志详细信息失败: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    @Transactional
    public void batchUpsertClassLogs(List<ClassLog> classLogs) {
        try {
            if (classLogs == null || classLogs.isEmpty()) {
                logger.info("没有需要传输的课程日志数据");
                return;
            }
    
            // 提取所有ID
            List<String> ids = classLogs.stream()
                    .map(ClassLog::getId)
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
    
            if (ids.isEmpty()) {
                logger.info("没有有效的课程日志ID需要处理");
                return;
            }
    
            // 批量查询现有记录
            List<ClassLog> existingLogs;
            try {
                existingLogs = classLogMapper.selectClassLogsByIds(ids);
            } catch (Exception e) {
                logger.error("批量查询现有课程日志记录时发生数据库异常: {}", e.getMessage(), e);
                return; // 发生数据库异常时直接返回
            }
    
            // 将现有记录放入Map便于快速查找
            Map<String, ClassLog> existingLogsMap = new HashMap<>();
            for (ClassLog existingLog : existingLogs) {
                if (existingLog != null && existingLog.getId() != null) {
                    existingLogsMap.put(existingLog.getId(), existingLog);
                }
            }
    
            // 分离需要插入和更新的记录
            List<ClassLog> toInsert = new ArrayList<>();
            List<ClassLog> toUpdate = new ArrayList<>();
    
            for (ClassLog classLog : classLogs) {
                if (classLog.getId() == null || classLog.getId().trim().isEmpty()) {
                    continue; // 跳过ID为空的记录
                }
    
                if (existingLogsMap.containsKey(classLog.getId())) {
                    // 记录已存在，需要更新
                    toUpdate.add(classLog);
                } else {
                    // 记录不存在，需要插入
                    toInsert.add(classLog);
                }
            }
    
            // 批量插入新记录
            if (!toInsert.isEmpty()) {
                for (ClassLog classLog : toInsert) {
                    try {
                        insertClassLog(classLog);
                    } catch (Exception e) {
                        logger.error("插入课程日志数据时发生错误，ID: {}，错误: {}", classLog.getId(), e.getMessage());
                    }
                }
                logger.info("成功插入 {} 条新课程日志数据", toInsert.size());
            }
    
            // 批量更新现有记录
            if (!toUpdate.isEmpty()) {
                for (ClassLog classLog : toUpdate) {
                    try {
                        updateClassLog(classLog);
                    } catch (Exception e) {
                        logger.error("更新课程日志数据时发生错误，ID: {}，错误: {}", classLog.getId(), e.getMessage());
                    }
                }
                logger.info("成功更新 {} 条课程日志数据", toUpdate.size());
            }
    
            logger.info("成功传输 {} 条课程日志数据到目标数据库 ({} 条插入, {} 条更新)",
                    classLogs.size(), toInsert.size(), toUpdate.size());
        } catch (Exception e) {
            logger.error("批量更新课程日志数据时发生异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 根据家长用户ID获取对应的班级代码(SP)
     * 
     * @param parentUserId 家长用户ID
     * @return 班级代码(SP)，如果无法获取则返回null
     */
    private String getClassSectionSpByParentUserId(String parentUserId) {
        try {
            // 根据parentUserId查询部门信息
            List<Department> departments = departmentService.getDepartmentsByParentUserId(parentUserId);
            if (departments.isEmpty()) {
                logger.warn("未找到家长用户 {} 对应的部门信息", parentUserId);
                return null;
            }
            
            String departmentName = departments.get(0).getName();
            logger.debug("获取到部门名称: {}", departmentName);
            
            // 使用部门名称查询class_section表获取class_section_sp
            ClassSection classSection = classSectionService.getClassSectionByDsedj(departmentName);
            if (classSection == null || classSection.getClassSectionSp() == null) {
                logger.warn("未找到部门 {} 对应的课程班级信息", departmentName);
                return null;
            }
            
            return classSection.getClassSectionSp();
        } catch (Exception e) {
            logger.error("根据家长用户ID获取班级代码时发生异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 过滤有效的课程日志（仅返回'功課'和'測驗'类型的课程日志）
     * 
     * @param classLogs 课程日志列表
     * @return 过滤后的有效课程日志列表
     */
    private List<ClassLog> filterValidClassLogs(List<ClassLog> classLogs) {
        if (classLogs == null) {
            return java.util.Collections.emptyList();
        }
        
        try {
            return classLogs.stream()
                    .filter(log -> COURSE_TYPE_HOMEWORK.equals(log.getCourseType()) || COURSE_TYPE_EXAM.equals(log.getCourseType()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("过滤课程日志时发生异常: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    @Transactional
    public void insertClassLog(ClassLog classLog) {
        try {
            classLogMapper.insertClassLog(classLog);
        } catch (Exception e) {
            logger.error("插入课程日志数据时发生异常: {}", e.getMessage(), e);
            throw e; // 重新抛出异常，让调用方处理
        }
    }

    @Transactional
    public void updateClassLog(ClassLog classLog) {
        try {
            classLogMapper.updateClassLogById(classLog);
        } catch (Exception e) {
            logger.error("更新课程日志数据时发生异常: {}", e.getMessage(), e);
            throw e; // 重新抛出异常，让调用方处理
        }
    }

}