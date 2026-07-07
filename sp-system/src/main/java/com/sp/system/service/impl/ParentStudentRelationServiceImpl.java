package com.sp.system.service.impl;

import com.sp.system.entity.ParentStudentRelation;
import com.sp.system.mapper.ParentStudentRelationMapper;
import com.sp.system.service.IParentStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 家長學生關係Service業務層處理
 */
@Service
public class ParentStudentRelationServiceImpl implements IParentStudentRelationService {

    private static final Logger logger = LoggerFactory.getLogger(ParentStudentRelationServiceImpl.class);

    @Autowired
    private ParentStudentRelationMapper parentStudentRelationMapper;

    @Override
    public List<ParentStudentRelation> selectByParentId(String parentUserId) {
        return parentStudentRelationMapper.selectByParentId(parentUserId);
    }

    @Override
    public String getAnswererInfo(String parentUserId, String studentUserId) {
        try {
            if (parentUserId == null || parentUserId.isEmpty() || studentUserId == null || studentUserId.isEmpty()) {
                return "";
            }

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
