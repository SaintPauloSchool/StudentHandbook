package com.sp.handler;

import com.sp.common.utils.StringUtils;
import com.sp.system.entity.ParentStudentRelation;
import com.sp.system.service.DepartmentService;
import com.sp.system.service.IParentStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件上傳處理器
 */
@Component
public class FileUploadHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadHandler.class);

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    @Autowired
    private DepartmentService departmentService;

    /**
     * 構建自定義文件名：班級名_學生名_關係.擴展名
     *
     * @param parentUserId 家長用戶ID
     * @param studentUserId 學生用戶ID（必須傳入）
     * @return 自定義文件名
     * @throws RuntimeException 當學生信息不匹配時拋出異常
     */
    public String buildCustomFileName(String parentUserId, String studentUserId) {
        try {
            ParentStudentRelation relation = null;
            
            if (StringUtils.isEmpty(studentUserId)) {
                // 如果沒有傳入studentUserId，直接拋出異常
                logger.error("當前家長跟學生的信息不對，無法上傳文件");
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }
            
            // 如果傳入了studentUserId，根據parentUserId和studentUserId查詢特定學生
            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(parentUserId);
            // 判斷relations是否爲空
            if (relations == null || relations.isEmpty()) {
                logger.error("未找到家長 {} 的關聯學生信息", parentUserId);
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }
            
            // 使用Stream查找匹配的學生
            relation = relations.stream()
                .filter(r -> studentUserId.equals(r.getStudentUserId()))
                .findFirst()
                .orElse(null);
            
            // 判斷relation是否爲空
            if (relation == null) {
                logger.error("未找到家長 {} 與學生 {} 的關係信息", parentUserId, studentUserId);
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }

            String studentId = relation.getStudentUserId();
            String studentName = relation.getStudentName();
            String relationDesc = relation.getRelationDesc();

            // 獲取班級名
            String className = departmentService.getClassNameByParentAndStudent(parentUserId, studentId);
            if (StringUtils.isEmpty(className)) {
                logger.error("未找到班級信息");
                throw new RuntimeException("未找到班級信息，無法上傳文件");
            }

            // 構建文件名：班級名_學生名_關係_上傳（不包含擴展名，uploadWithCustomName會添加）
            return className + "_" + studentName + "_" + relationDesc + "_上傳";
        } catch (RuntimeException e) {
            // 重新拋出運行時異常
            throw e;
        } catch (Exception e) {
            logger.error("構建自定義文件名失敗: {}", e.getMessage(), e);
            throw new RuntimeException("文件上傳失敗: " + e.getMessage());
        }
    }
}
