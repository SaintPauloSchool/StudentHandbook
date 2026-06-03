package com.sp.system.service;

import com.sp.system.entity.ParentStudentRelation;

import java.util.List;

/**
 * 家長學生關係Service接口
 *
 */
public interface IParentStudentRelationService {

    /**
     * 根據家長ID查詢學生列表
     *
     * @param parentUserId 家長用戶ID
     * @return 家長學生關係集合
     */
    List<ParentStudentRelation> selectByParentId(String parentUserId);

    /**
     * 創建並保存家長學生關係記錄
     *
     * @param parentUserId    家長用戶ID
     * @param studentUserId  學生用戶ID
     * @param studentName    學生姓名
     * @param relation       關係描述
     * @param mobile         家長手機號
     * @param externalUserid 家長外部用戶ID
     */
    void createAndSaveParentStudentRelation(String parentUserId, String studentUserId, String studentName, 
                                          String relation, String mobile, String externalUserid);

    /**
     * 獲取作答人信息（學生姓名 + 關係）
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 作答人信息，例如：「吳煜鍵 - 媽媽」
     */
    String getAnswererInfo(String parentUserId, String studentUserId);

}