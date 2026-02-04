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
     * 安全插入家长学生关系（如果不存在则插入，否则更新）
     *
     * @param parentStudentRelation 家长学生关系
     * @return 结果
     */
    int insertIfNotExists(ParentStudentRelation parentStudentRelation);


}