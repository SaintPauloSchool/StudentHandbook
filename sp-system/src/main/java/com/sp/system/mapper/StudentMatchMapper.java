package com.sp.system.mapper;

import com.sp.system.entity.vo.StudentRelationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 學生匹配數據層
 */
public interface StudentMatchMapper {

    /**
     * 根據家長 user_id 查詢已匹配學生列表（學籍信息來自 student_profiles.student_info）
     */
    List<StudentRelationVO> selectStudentsByParentUserId(@Param("parentUserId") String parentUserId,
                                                       @Param("studentProfilesDatabase") String studentProfilesDatabase);

    /**
     * 根據家長與學籍 student_id 查詢學生關係
     */
    StudentRelationVO selectByParentAndStudentId(@Param("parentUserId") String parentUserId,
                                                 @Param("studentId") String studentId,
                                                 @Param("studentProfilesDatabase") String studentProfilesDatabase);

    /**
     * 根據家長與學籍 student_id 查詢班級代碼（student_info.class_section）
     */
    String selectClassSectionByParentAndStudentId(@Param("parentUserId") String parentUserId,
                                                  @Param("studentId") String studentId,
                                                  @Param("studentProfilesDatabase") String studentProfilesDatabase);
}
