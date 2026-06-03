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
 * 家長學生關係Service業務層處理
 *
 */
@Service
public class ParentStudentRelationServiceImpl implements IParentStudentRelationService {

    private static final Logger logger = LoggerFactory.getLogger(ParentStudentRelationServiceImpl.class);

    @Autowired
    private ParentStudentRelationMapper parentStudentRelationMapper;

    /**
     * 根據家長ID查詢學生列表
     *
     * @param parentUserId 家長用戶ID
     * @return 家長學生關係集合
     */
    @Override
    public List<ParentStudentRelation> selectByParentId(String parentUserId) {
        return parentStudentRelationMapper.selectByParentId(parentUserId);
    }

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
    @Override
    @Transactional
    public void createAndSaveParentStudentRelation(String parentUserId, String studentUserId, String studentName, 
                                                 String relation, String mobile, String externalUserid) {
        // 創建家長學生關係實體
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
        // 日誌記錄由調用方處理
        logger.info("創建並保存家長學生關係記錄，共處理 {} 個家長", result);
    }

    /**
     * 獲取作答人信息（學生姓名 + 關係）
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID
     * @return 作答人信息，例如：「吳煜鍵 - 媽媽」
     */
    @Override
    public String getAnswererInfo(String parentUserId, String studentUserId) {
        try {
            if (parentUserId == null || parentUserId.isEmpty() || studentUserId == null || studentUserId.isEmpty()) {
                return "";
            }
            
            // 直接根據parentUserId和studentUserId查詢，更高效
            ParentStudentRelation relation = parentStudentRelationMapper.selectByParentAndStudent(parentUserId, studentUserId);
            
            if (relation != null) {
                String studentName = relation.getStudentName();
                String relationDesc = relation.getRelationDesc();
                
                if (studentName != null && relationDesc != null) {
                    return studentName + " - " + relationDesc;
                } else if (studentName != null) {
                    return studentName;
                }
            }
            
            return "";
        } catch (Exception e) {
            logger.error("獲取作答人信息失敗: {}", e.getMessage(), e);
            return "";
        }
    }

}