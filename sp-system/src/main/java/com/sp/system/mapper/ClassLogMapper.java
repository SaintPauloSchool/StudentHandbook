package com.sp.system.mapper;

import com.sp.system.entity.ClassLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 課程日誌數據訪問層
 */
public interface ClassLogMapper {

    /**
     * 根據ID列表批量查詢課程日誌
     * @param ids 課程日誌ID列表
     * @return 課程日誌列表
     */
    List<ClassLog> selectClassLogsByIds(@Param("ids") List<String> ids);
    
    /**
     * 插入課程日誌數據
     * @param classLog 課程日誌信息
     * @return 影響行數
     */
    int insertClassLog(ClassLog classLog);
    
    /**
     * 更新課程日誌數據
     * @param classLog 課程日誌信息
     * @return 影響行數
     */
    int updateClassLogById(ClassLog classLog);

    /**
     * 根據學生班級列表查詢過去一個月的課程日誌
     * @param studentClass 學生班級列表
     * @return 課程日誌列表
     */
    List<ClassLog> selectPastMonthClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根據學生班級列表查詢今天課程日誌
     * @param studentClass 學生班級列表
     * @return 課程日誌列表
     */
    List<ClassLog> selectTodayClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根據學生班級列表查詢未來七天的課程日誌
     * @param studentClass 學生班級列表
     * @return 課程日誌列表
     */
    List<ClassLog> selectNextSevenDaysClassLogByStudentClass(@Param("studentClass") String studentClass);

}