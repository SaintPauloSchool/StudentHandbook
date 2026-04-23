package com.sp.system.service.impl;

import com.sp.system.entity.NotificationAnswer;
import com.sp.system.mapper.NotificationAnswerMapper;
import com.sp.system.service.INotificationAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知回答Service实现类
 */
@Service
public class NotificationAnswerServiceImpl implements INotificationAnswerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationAnswerServiceImpl.class);
    
    @Autowired
    private NotificationAnswerMapper notificationAnswerMapper;
    
    /**
     * 批量保存答案
     * @param answers 答案列表
     * @return 插入记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveAnswers(List<NotificationAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            logger.warn("答案列表为空，无需保存");
            return 0;
        }
        
        try {
            int result = notificationAnswerMapper.batchInsertAnswers(answers);
            logger.info("成功保存 {} 条答案记录", result);
            return result;
        } catch (Exception e) {
            logger.error("保存答案失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存答案失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据通知ID和用户ID查询答案列表
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 答案列表
     */
    @Override
    public List<NotificationAnswer> selectAnswersByNotificationAndUser(Long notificationId, Long userId) {
        try {
            return notificationAnswerMapper.selectAnswersByNotificationAndUser(notificationId, userId);
        } catch (Exception e) {
            logger.error("查询答案失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询答案失败: " + e.getMessage());
        }
    }
}
