package com.sp.system.mapper;

import com.sp.system.entity.ParentStudentRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 家長學生關係Mapper接口
 *
 */
@Mapper
public interface ParentStudentRelationMapper {

    /**
     * 根據家長ID查詢學生列表
     *
     * @param parentUserId 家長用戶ID
     * @return 家長學生關係集合
     */
    List<ParentStudentRelation> selectByParentId(@Param("parentUserId") String parentUserId);

    /**
     * 安全插入家長學生關係（如果不存在）
     *
     * @param parentStudentRelation 家長學生關係信息
     * @return 結果
     */
    int insertIgnore(ParentStudentRelation parentStudentRelation);

    /**
     * 根據家長ID和學生ID查詢關係信息
     *
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 家長學生關係信息
     */
    ParentStudentRelation selectByParentAndStudent(@Param("parentUserId") String parentUserId,
                                                    @Param("studentUserId") String studentUserId);
}