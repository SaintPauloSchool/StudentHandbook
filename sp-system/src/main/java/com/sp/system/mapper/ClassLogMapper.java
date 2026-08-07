package com.sp.system.mapper;

import com.sp.system.entity.ClassLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 課程日誌數據訪問層
 */
public interface ClassLogMapper {

    /**
     * 根據學生班級列表查詢過去一個月的課程日誌
     */
    List<ClassLog> selectPastMonthClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根據學生班級列表查詢今天課程日誌
     */
    List<ClassLog> selectTodayClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根據學生班級列表查詢未來七天的課程日誌
     */
    List<ClassLog> selectNextSevenDaysClassLogByStudentClass(@Param("studentClass") String studentClass);
}
