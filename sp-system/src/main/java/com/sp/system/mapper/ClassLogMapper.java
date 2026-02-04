package com.sp.system.mapper;

import com.sp.system.entity.ClassLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程日志数据访问层
 */
public interface ClassLogMapper {

    /**
     * 根据ID列表批量查询课程日志
     * @param ids 课程日志ID列表
     * @return 课程日志列表
     */
    List<ClassLog> selectClassLogsByIds(@Param("ids") List<String> ids);
    
    /**
     * 插入课程日志数据
     * @param classLog 课程日志信息
     * @return 影响行数
     */
    int insertClassLog(ClassLog classLog);
    
    /**
     * 更新课程日志数据
     * @param classLog 课程日志信息
     * @return 影响行数
     */
    int updateClassLogById(ClassLog classLog);
    /**
     * 根据学生班级列表查询课程日志
     * @param studentClass 学生班级列表
     * @return 课程日志列表
     */
    List<ClassLog> selectClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根据学生班级列表查询过去一个月的课程日志
     * @param studentClass 学生班级列表
     * @return 课程日志列表
     */
    List<ClassLog> selectPastMonthClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根据学生班级列表查询今天课程日志
     * @param studentClass 学生班级列表
     * @return 课程日志列表
     */
    List<ClassLog> selectTodayClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根据学生班级列表查询未来七天的课程日志
     * @param studentClass 学生班级列表
     * @return 课程日志列表
     */
    List<ClassLog> selectNextSevenDaysClassLogByStudentClass(@Param("studentClass") String studentClass);

    /**
     * 根据ID和学生班级列表查询单个课程日志
     * @param id 课程日志ID
     * @param studentClass 学生班级列表
     * @return 课程日志信息
     */
    ClassLog selectClassLogByIdAndStudentClass(@Param("id") String id, @Param("studentClass") String studentClass);

}