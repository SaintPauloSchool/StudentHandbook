package com.sp.system.service;

import com.sp.system.entity.ClassLog;

import java.util.List;

/**
 * 课程日志Service接口
 */
public interface IClassLogService {


    /**
     * 根据家长用户ID和学生用户ID获取当天的课程日志列表
     * 
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID
     * @return 课程日志列表
     */
    List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId, String studentUserId);
    
    /**
     * 根据家长用户ID和学生用户ID获取未来七天（不含当天）的课程日志列表
     * 
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID
     * @return 课程日志列表
     */
    List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId, String studentUserId);

    /**
     * 根据家长用户ID和学生用户ID获取过去一个月的课程日志列表
     * 
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID
     * @return 课程日志列表
     */
    List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId, String studentUserId);

    /**
     * 批量插入或更新ClassLog数据到目标数据库
     */
    void batchUpsertClassLogs(List<ClassLog> classLogs);

}