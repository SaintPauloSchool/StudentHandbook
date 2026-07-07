package com.sp.system.service;

import java.util.List;

import com.sp.system.entity.ClassLog;

/**
 * 課程日誌 Service 接口
 * <p>
 * 按家長 user_id 與學籍 student_id 查詢班級日誌，底層通過 class_section 關聯 class_log。
 */
public interface IClassLogService {

    /** 查詢當天功課與測驗日誌 */
    List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId, String studentId);

    /** 查詢未來七天（不含當天）功課與測驗日誌 */
    List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId, String studentId);

    /** 查詢過去一個月（不含當天）功課與測驗日誌 */
    List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId, String studentId);
}
