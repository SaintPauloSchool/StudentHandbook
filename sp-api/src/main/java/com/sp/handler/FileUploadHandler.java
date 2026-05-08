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
     * @param originalFilename 原始文件名
     * @return 自定义文件名，如果失败返回null
     */
    public String buildCustomFileName(String parentUserId, String originalFilename) {
        try {
            // 根据家长ID查询关系信息（取第一个学生）
            List<ParentStudentRelation> relations = parentStudentRelationService.selectByParentId(parentUserId);
            if (relations == null || relations.isEmpty()) {
                logger.warn("未找到家长 {} 的关系信息", parentUserId);
                return null;
            }

            ParentStudentRelation relation = relations.get(0);
            String studentId = relation.getStudentUserId();
            String studentName = relation.getStudentName();
            String relationDesc = relation.getRelationDesc();

            // 获取班级名
            String className = departmentService.getClassNameByParentAndStudent(parentUserId, studentId);
            if (StringUtils.isEmpty(className)) {
                logger.warn("未找到班级信息");
                return null;
            }

            // 构建文件名：班级名_学生名_关系_上傳（不包含扩展名，uploadWithCustomName会添加）
            return className + "_" + studentName + "_" + relationDesc + "_上傳";
        } catch (Exception e) {
            logger.error("构建自定义文件名失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
