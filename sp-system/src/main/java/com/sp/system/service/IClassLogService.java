package com.sp.system.service;

import com.sp.system.entity.ClassLog;
import com.sp.common.core.page.TableDataInfo;

import java.util.List;

/**
 * 课程日志Service接口
 */
public interface IClassLogService {

    /**
     * 根据家长用户ID获取课程日志列表
     * 
     * @param parentUserId 家长用户ID
     * @return 课程日志列表
     */
    List<ClassLog> getClassLogListByParentUserId(String parentUserId);

    /**
     * 根据家长用户ID获取当天的课程日志列表
     * 
     * @param parentUserId 家长用户ID
     * @return 课程日志列表
     */
    List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId);
    
    /**
     * 根据家长用户ID获取未来七天（不含当天）的课程日志列表
     * 
     * @param parentUserId 家长用户ID
     * @return 课程日志列表
     */
    List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId);
    
    /**
     * 根据家长用户ID和日志ID获取课程日志详细信息
     * 
     * @param id 课程日志ID
     * @param parentUserId 家长用户ID
     * @return 课程日志信息
     */
    ClassLog getClassLogDetailByParentUserId(String id, String parentUserId);
    
    /**
     * 批量插入或更新ClassLog数据到目标数据库
     */
    void batchUpsertClassLogs(List<ClassLog> classLogs);
    
    /**
     * 根据家长用户ID获取过去一个月的课程日志列表
     * 
     * @param parentUserId 家长用户ID
     * @return 课程日志列表
     */
    List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId);
}