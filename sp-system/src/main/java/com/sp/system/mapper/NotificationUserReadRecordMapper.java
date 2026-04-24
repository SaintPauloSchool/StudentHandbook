package com.sp.system.mapper;

import com.sp.system.entity.NotificationUserReadRecord;
import com.sp.system.entity.vo.NotificationWithReadStatusVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知用户阅读记录 Mapper 接口
 */
public interface NotificationUserReadRecordMapper {

    /**
     * 根据通知ID和用户ID查询阅读记录（跨 send_record 联查）
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 阅读记录；若不存在则返回null
     */
    NotificationUserReadRecord selectByNotificationAndUser(@Param("notificationId") Long notificationId,
                                                           @Param("userId") String userId);

    /**
     * 将指定记录标记为已读
     *
     * @param readId 阅读记录ID
     * @return 影响行数
     */
    int markAsRead(@Param("readId") Long readId);

    /**
     * 将指定记录标记为已回复
     *
     * @param readId 阅读记录ID
     * @return 影响行数
     */
    int markAsReplied(@Param("readId") Long readId);

    /**
     * 分页查询已发布通知列表（仅返回发送给当前用户的通知，附带阅读状态）
     *
     * @param offset 偏移量
     * @param limit  每页数量
     * @param userId 当前家长用户ID
     * @return 通知列表（含 isRead、readId、sendRecordId 字段）
     */
    List<NotificationWithReadStatusVO> selectPublishedNotificationsForUser(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("userId") String userId);

    /**
     * 查询发送给当前用户的已发布通知总数
     *
     * @param userId 当前家长用户ID
     * @return 总数
     */
    int countPublishedNotificationsForUser(@Param("userId") String userId);
}
