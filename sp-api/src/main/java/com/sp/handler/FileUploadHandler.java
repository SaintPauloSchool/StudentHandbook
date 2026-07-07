package com.sp.handler;

import com.sp.common.utils.StringUtils;
import com.sp.system.entity.vo.StudentRelationVO;
import com.sp.system.service.IStudentRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文件上傳處理器
 * <p>
 * 根據家長與學生信息生成上傳文件名，格式：{班級}_{學生姓名}_{親屬關係}_上傳
 */
@Component
public class FileUploadHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadHandler.class);

    @Autowired
    private IStudentRelationService studentRelationService;

    /**
     * 構建通知附件的自定義文件名
     *
     * @param parentUserId 家長 user_id
     * @param studentId    學籍 student_id
     * @return 如「P3D_陳俊熙_媽媽_上傳」
     * @throws RuntimeException 學生未關聯或班級信息缺失時
     */
    public String buildCustomFileName(String parentUserId, String studentId) {
        try {
            if (StringUtils.isEmpty(studentId)) {
                logger.error("當前家長跟學生的信息不對，無法上傳文件");
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }

            StudentRelationVO relation = studentRelationService.getByParentAndStudentId(parentUserId, studentId);
            if (relation == null) {
                logger.error("未找到家長 {} 與學生 {} 的關係信息", parentUserId, studentId);
                throw new RuntimeException("當前家長跟學生的信息不對，無法上傳文件");
            }

            String classSection = relation.getClassSection();
            if (StringUtils.isEmpty(classSection)) {
                logger.error("未找到班級信息");
                throw new RuntimeException("未找到班級信息，無法上傳文件");
            }

            return classSection + "_" + relation.getStudentName() + "_" + relation.getRelationDesc() + "_上傳";
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("構建自定義文件名失敗: {}", e.getMessage(), e);
            throw new RuntimeException("文件上傳失敗: " + e.getMessage());
        }
    }
}
