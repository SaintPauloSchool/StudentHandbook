package com.sp.system.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 行事曆事件實體類
 */
public class CalendarEvent {
    private Long eventId;            // 事件 ID
    private LocalDate eventDate;     // 事件日期
    private String title;            // 事件標題
    private Integer targetType;      // 對象類型（0: 全校, 1: 幼稚園, 2: 小學, 3: 中學）
    private String createBy;         // 創建者
    private LocalDateTime createTime;// 創建時間
    private String updateBy;         // 更新者
    private LocalDateTime updateTime;// 更新時間
    private String remark;           // 備註

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
