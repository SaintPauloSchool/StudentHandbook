package com.sp.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.sp.system.entity.NotificationAnswer;
import com.sp.system.entity.vo.AnswerItemVO;
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
     * 将前端传来的答案数据转换为实体对象并保存
     * @param answersData 前端传来的答案数据列表
     * @param userId 用户ID（parentUserId）
     * @param userType 用户类型
     * @return 插入记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAnswers(List<AnswerItemVO> answersData, String userId, String userType) {
        if (answersData == null || answersData.isEmpty()) {
            logger.warn("答案数据为空，无需保存");
            return 0;
        }
        
        try {
            // 转换答案数据
            List<NotificationAnswer> answers = convertToNotificationAnswers(answersData, userId, userType);
            
            // 批量保存
            int result = notificationAnswerMapper.batchInsertAnswers(answers);
            logger.info("成功提交 {} 条答案记录", result);
            return result;
        } catch (Exception e) {
            logger.error("提交答案失败: {}", e.getMessage(), e);
            throw new RuntimeException("提交答案失败: " + e.getMessage());
        }
    }
    
    /**
     * 将前端传来的答案数据转换为实体对象
     * @param answersData 前端答案数据
     * @param userId 用户ID（parentUserId）
     * @param userType 用户类型
     * @return 答案实体列表
     */
    private List<NotificationAnswer> convertToNotificationAnswers(List<AnswerItemVO> answersData, String userId, String userType) {
        return answersData.stream().map(answerItem -> {
            NotificationAnswer answer = new NotificationAnswer();
            
            // 设置通知ID
            answer.setNotificationId(answerItem.getNotificationId());
            
            // 设置问题ID
            answer.setQuestionId(answerItem.getQuestionId());
            
            // 设置答案数据（JSON格式）
            Object answerDataObj = answerItem.getAnswerData();
            if (answerDataObj instanceof List) {
                // 如果是数组，转换为JSON字符串
                answer.setAnswerData(JSON.toJSONString(answerDataObj));
            } else if (answerDataObj instanceof String) {
                // 如果已经是字符串，直接使用
                answer.setAnswerData((String) answerDataObj);
            }
            
            // 设置用户ID和类型
            answer.setUserId(userId);
            answer.setUserType(userType);
            
            return answer;
        }).collect(java.util.stream.Collectors.toList());
    }
}
