package com.sp.system.service;

import com.sp.system.entity.vo.StudentRelationVO;

import java.util.List;

/**
 * 家長學生關係服務（通過 sys_student_match + student_profiles 獲取學生信息）
 */
public interface IStudentRelationService {

    List<StudentRelationVO> selectByParentId(String parentUserId);

    /**
     * 根據家長與學籍 student_id 查詢單條學生關係
     */
    StudentRelationVO getByParentAndStudentId(String parentUserId, String studentId);

    /**
     * 組裝作答人展示信息（學生姓名 + 與家長關係）
     *
     * @param parentUserId 作答家長的 user_id
     * @param studentId    學籍 student_id
     * @return 如「陳俊熙 - 媽媽」；查無資料或失敗時返回空字串
     */
    String getAnswererInfo(String parentUserId, String studentId);
}
