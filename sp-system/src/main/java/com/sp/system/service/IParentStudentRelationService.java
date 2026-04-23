package com.sp.system.service;

import com.sp.system.entity.ParentStudentRelation;

import java.util.List;

/**
 * 家长学生关系Service接口
 *
 */
public interface IParentStudentRelationService {

    /**
     * 根据家长ID查询学生列表
     *
     * @param parentUserId 家长用户ID
     * @return 家长学生关系集合
     */
    List<ParentStudentRelation> selectByParentId(String parentUserId);

    /**
     * 创建并保存家长学生关系记录
     *
     * @param parentUserId    家长用户ID
     * @param studentUserId  学生用户ID
     * @param studentName    学生姓名
     * @param relation       关系描述
     * @param mobile         家长手机号
     * @param externalUserid 家长外部用户ID
     */
    void createAndSaveParentStudentRelation(String parentUserId, String studentUserId, String studentName, 
                                          String relation, String mobile, String externalUserid);

    /**
     * 获取作答人信息（学生姓名 + 关系）
     * @param parentUserId 家长用户ID
     * @return 作答人信息，例如：“吴煜键 - 妈妈”
     */
    String getAnswererInfo(String parentUserId);

}