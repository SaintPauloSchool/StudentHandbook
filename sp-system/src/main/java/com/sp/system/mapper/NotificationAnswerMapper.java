package com.sp.system.mapper;

import com.sp.system.entity.NotificationAnswer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知回答數據層
 */
public interface NotificationAnswerMapper {

    /**
     * 批量插入回答記錄
     *
     * @param answers 回答列表
     * @return 成功插入的行數
     */
    int batchInsertAnswers(@Param("answers") List<NotificationAnswer> answers);

    /**
     * 統計指定學生對某問題是否已有回答（用於提交前防重複）
     *
     * @param notificationId 通知 ID
     * @param questionId     問題 ID
     * @param studentId      學籍 student_id
     * @return 已有記錄時 &gt; 0
     */
    int countByNotificationQuestionStudent(@Param("notificationId") Long notificationId,
                                           @Param("questionId") Long questionId,
                                           @Param("studentId") String studentId);

    /**
     * 查詢指定學生在某一通知下的全部回答
     *
     * @param notificationId 通知 ID
     * @param studentId      學籍 student_id
     * @return 按 question_id、answer_id 排序的回答列表
     */
    List<NotificationAnswer> selectUserAnswers(@Param("notificationId") Long notificationId,
                                               @Param("studentId") String studentId);
}
