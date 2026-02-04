package com.sp.system.mapper;

import com.sp.system.entity.ParentStudentRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 家长学生关系Mapper接口
 *
 */
@Mapper
public interface ParentStudentRelationMapper {

    /**
     * 根据家长ID查询学生列表
     *
     * @param parentUserId 家长用户ID
     * @return 家长学生关系集合
     */
    List<ParentStudentRelation> selectByParentId(@Param("parentUserId") String parentUserId);

    /**
     * 安全插入家长学生关系（如果不存在）
     *
     * @param parentStudentRelation 家长学生关系信息
     * @return 结果
     */
    int insertIgnore(ParentStudentRelation parentStudentRelation);
}