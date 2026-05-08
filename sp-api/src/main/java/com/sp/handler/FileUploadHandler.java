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
 * 文件上传处理器
 */
@Component
public class FileUploadHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadHandler.class);

    @Autowired
    private IParentStudentRelationService parentStudentRelationService;

    @Autowired
    private DepartmentService departmentService;

    /**
     * 构建自定义文件名：班级名_学生名_关系.扩展名
     *
     * @param parentUserId 家长用户ID
     * @param studentUserId 学生用户ID（必须传入）
     * @return 自定义文件名
     * @throws RuntimeException 当学生信息不匹配时抛出异常
     */
    public String buildCustomFileName(String parentUserId, String studentUserId) {
        try {
            ParentStudentRelation relation = null;
            
            if (StringUtils.isEmpty(studentUserId)) {
                // 如果没有传入studentUserId，直接抛出异常
                logger.error("當前家長跟學生的信息不對，無法上傳文件");
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }
            
            // 如果传入了studentUserId，根据parentUserId和studentUserId查询特定学生
            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(parentUserId);
            // 判断relations是否为空
            if (relations == null || relations.isEmpty()) {
                logger.error("未找到家长 {} 的关联学生信息", parentUserId);
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }
            
            // 使用Stream查找匹配的学生
            relation = relations.stream()
                .filter(r -> studentUserId.equals(r.getStudentUserId()))
                .findFirst()
                .orElse(null);
            
            // 判断relation是否为空
            if (relation == null) {
                logger.error("未找到家长 {} 与学生 {} 的关系信息", parentUserId, studentUserId);
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }

            String studentId = relation.getStudentUserId();
            String studentName = relation.getStudentName();
            String relationDesc = relation.getRelationDesc();

            // 获取班级名
            String className = departmentService.getClassNameByParentAndStudent(parentUserId, studentId);
            if (StringUtils.isEmpty(className)) {
                logger.error("未找到班级信息");
                throw new RuntimeException("未找到班级信息，无法上传文件");
            }

            // 构建文件名：班级名_学生名_关系_上傳（不包含扩展名，uploadWithCustomName会添加）
            return className + "_" + studentName + "_" + relationDesc + "_上傳";
        } catch (RuntimeException e) {
            // 重新抛出运行时异常
            throw e;
        } catch (Exception e) {
            logger.error("构建自定义文件名失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}
