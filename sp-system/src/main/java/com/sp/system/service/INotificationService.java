package com.sp.system.service;

import java.util.Map;

/**
 * 通知Service接口
 * <p>
 * 负责通知主体数据的查询（与用户无关的通用逻辑）。
 * 与用户阅读状态相关的业务请使用 {@link INotificationUserReadRecordService}。
 */
public interface INotificationService {

    /**
     * 根据ID查询通知详情（包含问题列表）
     *
     * @param notificationId 通知ID
     * @return 包含 notification 和 questions 的 Map；若通知不存在则返回 null
     */
    Map<String, Object> selectNotificationDetail(Long notificationId);
}
