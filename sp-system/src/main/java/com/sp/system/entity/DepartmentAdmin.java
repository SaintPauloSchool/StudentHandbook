package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 部門管理員實體類
 */
public class DepartmentAdmin {
    private Long id;                      // 主鍵ID
    private Long departmentId;            // 部門ID
    private String userid;                // 部門管理員的userid
    private Integer type;                 // 部門管理員的類型：1-校區負責人, 2-年級負責人, 3-班主任, 4-任課老師, 5-學段負責人
    private String subject;               // 教師或班主任的科目
    private LocalDateTime createTime;     // 創建時間
    private LocalDateTime updateTime;     // 更新時間

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
