package com.sp.system.service.impl;

import com.sp.system.entity.ParentStudentRelation;
import com.sp.system.mapper.ParentStudentRelationMapper;
import com.sp.system.service.IParentStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ParentStudentRelationServiceImpl.class);

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
     * 创建并保存家长学生关系记录
     *
     * @param parentUserId    家长用户ID
     * @param studentUserId  学生用户ID
     * @param studentName    学生姓名
     * @param relation       关系描述
     * @param mobile         家长手机号
     * @param externalUserid 家长外部用户ID
     */
    @Override
    @Transactional
    public void createAndSaveParentStudentRelation(String parentUserId, String studentUserId, String studentName, 
                                                 String relation, String mobile, String externalUserid) {
        // 创建家长学生关系实体
        ParentStudentRelation relationEntity = new ParentStudentRelation();
        relationEntity.setParentUserId(parentUserId);
        relationEntity.setStudentUserId(studentUserId);
        relationEntity.setStudentName(studentName);
        relationEntity.setRelationDesc(relation);
        relationEntity.setMobile(mobile);
        relationEntity.setExternalUserid(externalUserid);
        relationEntity.setCreateTime(LocalDateTime.now());
        relationEntity.setUpdateTime(LocalDateTime.now());
        // 插入
        int result = parentStudentRelationMapper.insertIgnore(relationEntity);
        // 日志记录由调用方处理
        logger.info("创建并保存家长学生关系记录，共處理 {} 个家长", result);
    }

}