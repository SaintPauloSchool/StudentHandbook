package com.sp.system.service.impl;

import com.sp.system.entity.ParentStudentRelation;
import com.sp.system.mapper.ParentStudentRelationMapper;
import com.sp.system.service.IParentStudentRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 家长学生关系Service业务层处理
 *
 */
@Service
public class ParentStudentRelationServiceImpl implements IParentStudentRelationService {

    @Autowired
    private ParentStudentRelationMapper parentStudentRelationMapper;

    /**
     * 根据家长ID查询学生列表
     *
     * @param parentUserId 家长用户ID
     * @return 家长学生关系集合
     */
    @Override
    public List<ParentStudentRelation> selectByParentId(String parentUserId) {
        return parentStudentRelationMapper.selectByParentId(parentUserId);
    }

    /**
     * 安全插入家长学生关系（如果不存在则插入，否则更新）
     *
     * @param parentStudentRelation 家长学生关系
     * @return 结果
     */
    @Override
    @Transactional
    public int insertIfNotExists(ParentStudentRelation parentStudentRelation) {
        return parentStudentRelationMapper.insertIgnore(parentStudentRelation);
    }

}