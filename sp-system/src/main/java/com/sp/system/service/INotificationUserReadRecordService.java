package com.sp.system.service;

import com.sp.system.entity.vo.NotificationWithReadStatusVO;

import java.util.List;

/**
 * 通知用户阅读记录 Service 接口
 * <p>
 * 负责与 {@code notification_user_read_record} 及关联表相关的业务逻辑：
 * <ul>
 *   <li>查询当前用户的通知列表（含阅读状态）</li>
 *   <li>标记通知为已读</li>
 * </ul>
 */
public interface INotificationUserReadRecordService {

    /**
     * 分页查询发送给当前用户的已发布通知列表（附带阅读状态）
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页数量
     * @param userId   当前家长用户ID
     * @param studentUserId 学生用户ID（可为null）
     * @return 通知列表（含 isRead、readId、sendRecordId 字段）
     */
    List<NotificationWithReadStatusVO> getPublishedNotificationsForUser(int pageNum, int pageSize, String userId, String studentUserId);

    /**
     * 查询发送给当前用户的已发布通知总数
     *
     * @param userId 当前家长用户ID
     * @param studentUserId 学生用户ID（可为null）
     * @return 总数
     */
    int countPublishedNotificationsForUser(String userId, String studentUserId);

    /**
     * 查询发送给当前用户的未读通知数量
     *
     * @param userId 当前家长用户ID
     * @param studentUserId 学生用户ID（可为null）
     * @return 未读数量
     */
    int countUnreadNotificationsForUser(String userId, String studentUserId);

    /**
     * 将指定通知对当前用户标记为已读
     * <p>
     * 若该用户的阅读记录存在且为未读（is_read='0'），则更新为已读并记录阅读时间；
     * 若记录不存在或已是已读状态，则忽略。
     *
     * @param notificationId 通知ID
     * @param userId         当前家长用户ID
     * @param studentUserId  学生用户ID（可为null）
     */
    void markAsRead(Long notificationId, String userId, String studentUserId);

    /**
     * 将指定通知对当前用户标记为已回复
     * <p>
     * 若该用户的阅读记录存在且为未回复（reply_status='0'），则更新为已回复并记录回复时间；
     * 若记录不存在或已回复，则忽略。
     *
     * @param notificationId 通知ID
     * @param userId         当前家长用户ID
     * @param studentUserId  学生用户ID
     */
    void markAsReplied(Long notificationId, String userId, String studentUserId);
}
