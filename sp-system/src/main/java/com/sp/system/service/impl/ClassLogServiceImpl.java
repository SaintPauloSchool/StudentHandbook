package com.sp.system.service.impl;

import com.sp.common.config.StudentProfilesProperties;
import com.sp.system.entity.ClassLog;
import com.sp.system.mapper.ClassLogMapper;
import com.sp.system.mapper.StudentMatchMapper;
import com.sp.system.service.IClassLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 課程日誌服務實現
 * <p>
 * 通過家長 user_id 與學籍 student_id 解析班級（student_info.class_section），
 * 再按班級查詢 class_log 表中的功課與測驗記錄。
 */
@Service
public class ClassLogServiceImpl implements IClassLogService {

    private static final Logger logger = LoggerFactory.getLogger(ClassLogServiceImpl.class);

    @Autowired
    private ClassLogMapper classLogMapper;

    @Autowired
    private StudentMatchMapper studentMatchMapper;

    @Autowired
    private StudentProfilesProperties studentProfilesProperties;

    /** 查詢過去一個月（不含當天）的功課與測驗日誌 */
    @Override
    public List<ClassLog> getPastMonthClassLogListByParentUserId(String parentUserId, String studentId) {
        return queryClassLogs(parentUserId, studentId, classLogMapper::selectPastMonthClassLogByStudentClass,
                "獲取過去一個月課程日誌列表失敗");
    }

    /** 查詢當天的功課與測驗日誌 */
    @Override
    public List<ClassLog> getTodayClassLogListByParentUserId(String parentUserId, String studentId) {
        return queryClassLogs(parentUserId, studentId, classLogMapper::selectTodayClassLogByStudentClass,
                "獲取當天課程日誌列表失敗");
    }

    /** 查詢未來七天（不含當天）的功課與測驗日誌 */
    @Override
    public List<ClassLog> getNextSevenDaysClassLogListByParentUserId(String parentUserId, String studentId) {
        return queryClassLogs(parentUserId, studentId, classLogMapper::selectNextSevenDaysClassLogByStudentClass,
                "獲取未來七天課程日誌列表失敗");
    }

    /**
     * 按家長與學生查詢課程日誌的通用流程。
     *
     * @param parentUserId 家長 user_id
     * @param studentId    學籍 student_id
     * @param queryFn      按班級查詢日誌的函數（如過去一個月、當天、未來七天）
     * @param errorMsg     異常時的日誌前綴
     * @return 課程日誌列表，無數據或失敗時返回空列表
     */
    private List<ClassLog> queryClassLogs(String parentUserId, String studentId,
                                          Function<String, List<ClassLog>> queryFn, String errorMsg) {
        try {
            String classSection = resolveClassSection(parentUserId, studentId);
            if (!StringUtils.hasText(classSection)) {
                return Collections.emptyList();
            }
            // class_section 對應 class_log.student_class
            List<ClassLog> classLogs = queryFn.apply(classSection);
            return classLogs != null ? classLogs : Collections.emptyList();
        } catch (Exception e) {
            logger.error("{}: {}", errorMsg, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 通過 sys_student_match 關聯 student_info，按家長與學生 ID 取得班級代碼
     */
    private String resolveClassSection(String parentUserId, String studentId) {
        // 根據家長與學籍 student_id 查詢班級代碼
        String classSection = studentMatchMapper.selectClassSectionByParentAndStudentId(
                parentUserId, studentId, studentProfilesProperties.getDatabase());
        if (!StringUtils.hasText(classSection)) {
            logger.warn("未找到家長 {} 和學生 {} 對應的班級信息", parentUserId, studentId);
            return null;
        }
        return classSection;
    }
}
