package com.sp.system.service;

import com.sp.system.entity.vo.AnswerItemVO;

import java.util.List;

/**
 * 通知回答Service接口
 */
public interface INotificationAnswerService {
    
    /**
     * 将前端传来的答案数据转换为实体对象并保存
     * @param answersData 前端传来的答案数据列表
     * @param userId 用户ID（parentUserId）
     * @param userType 用户类型
     * @return 插入记录数
     */
    int submitAnswers(List<AnswerItemVO> answersData, String userId, String userType);
}
