package com.sp.system.service.impl;

import com.sp.common.config.StudentProfilesProperties;
import com.sp.system.entity.vo.StudentRelationVO;
import com.sp.system.mapper.StudentMatchMapper;
import com.sp.system.service.IStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 家長學生關係服務實現
 * <p>
 * 通過 sys_student_match 關聯學籍庫 student_info 與家校通訊錄 sys_school_family_contact。
 */
@Service
public class StudentRelationServiceImpl implements IStudentRelationService {

    private static final Logger logger = LoggerFactory.getLogger(StudentRelationServiceImpl.class);

    @Autowired
    private StudentMatchMapper studentMatchMapper;

    @Autowired
    private StudentProfilesProperties studentProfilesProperties;

    /**
     * 查詢家長關聯的全部學生（含姓名、班級、親屬關係）
     *
     * @param parentUserId 家長 user_id
     */
    @Override
    public List<StudentRelationVO> selectByParentId(String parentUserId) {
        return studentMatchMapper.selectStudentsByParentUserId(parentUserId, studentProfilesProperties.getDatabase());
    }

    @Override
    public StudentRelationVO getByParentAndStudentId(String parentUserId, String studentId) {
        return studentMatchMapper.selectByParentAndStudentId(
                parentUserId, studentId, studentProfilesProperties.getDatabase());
    }

    /**
     * 組裝作答人展示信息，用於通知詳情頁標示「誰代學生作答」。
     *
     * @param parentUserId 作答家長的 user_id（來自 notification_answer.user_id）
     * @param studentId    學籍 student_id
     * @return 如「陳俊熙 - 媽媽」；參數缺失、查無關聯或異常時返回空字串
     */
    @Override
    public String getAnswererInfo(String parentUserId, String studentId) {
        try {
            if (parentUserId == null || parentUserId.isEmpty() || studentId == null || studentId.isEmpty()) {
                return "";
            }

            StudentRelationVO relation = getByParentAndStudentId(parentUserId, studentId);

            if (relation == null) {
                return "";
            }

            String studentName = relation.getStudentName();
            String relationDesc = relation.getRelationDesc();
            if (studentName != null && relationDesc != null) {
                return studentName + " - " + relationDesc;
            }
            return studentName != null ? studentName : "";
        } catch (Exception e) {
            logger.error("獲取作答人信息失敗: {}", e.getMessage(), e);
            return "";
        }
    }
}
