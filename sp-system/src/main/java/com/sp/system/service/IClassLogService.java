package com.sp.system.service;

import com.sp.system.entity.ClassLog;

import java.util.List;

/**
 * 課程日誌Service接口
 */
public interface IClassLogService {

    /**
     * 根據家長用戶ID和學生用戶ID獲取當天的課程日誌列表
     */
    List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId, String studentUserId);

    /**
     * 根據家長用戶ID和學生用戶ID獲取未來七天（不含當天）的課程日誌列表
     */
    List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId, String studentUserId);

    /**
     * 根據家長用戶ID和學生用戶ID獲取過去一個月的課程日誌列表
     */
    List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId, String studentUserId);
}
