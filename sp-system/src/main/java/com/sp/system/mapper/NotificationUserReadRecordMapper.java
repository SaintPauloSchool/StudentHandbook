package com.sp.system.mapper;

import com.sp.system.entity.NotificationUserReadRecord;
import com.sp.system.entity.vo.NotificationWithReadStatusVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知用戶閱讀記錄 Mapper 接口
 */
public interface NotificationUserReadRecordMapper {

    /**
     * 根據通知ID和用戶ID查詢閱讀記錄（跨 send_record 聯查）
     *
     * @param notificationId 通知ID
     * @param userId         用戶ID
     * @param studentId  學籍 student_id（可爲null）
     * @return 閱讀記錄；若不存在則返回null
     */
    NotificationUserReadRecord selectByNotificationAndUser(@Param("notificationId") Long notificationId,
                                                           @Param("userId") String userId,
                                                           @Param("studentId") String studentId);

    /**
     * 將指定記錄標記爲已讀
     *
     * @param readId 閱讀記錄ID
     * @param readTime 閱讀時間
     * @return 影響行數
     */
    int markAsRead(@Param("readId") Long readId, @Param("readTime") LocalDateTime readTime);

    /**
     * 將指定記錄標記爲已回復
     *
     * @param readId 閱讀記錄ID
     * @param replyTime 回復時間
     * @return 影響行數
     */
    int markAsReplied(@Param("readId") Long readId, @Param("replyTime") LocalDateTime replyTime);

    /**
     * 查詢已發布通知列表（僅返回發送給當前用戶的通知，附帶閱讀狀態）
     * <p>
     * 分頁由 PageHelper 攔截處理，勿在 SQL 中手動 LIMIT。
     *
     * @param userId    當前家長用戶ID
     * @param studentId 學生ID（可爲null）
     * @return 通知列表（含 isRead、readId、sendRecordId 字段）
     */
    List<NotificationWithReadStatusVO> selectPublishedNotificationsForUser(
            @Param("userId") String userId,
            @Param("studentId") String studentId);

    /**
     * 查詢發送給當前用戶的未讀通知數量
     *
     * @param userId 當前家長用戶ID
     * @param studentId 學籍 student_id（可爲null）
     * @return 未讀數量
     */
    int countUnreadNotificationsForUser(@Param("userId") String userId, @Param("studentId") String studentId);
}
