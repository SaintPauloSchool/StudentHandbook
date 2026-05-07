package com.sp.system.entity;

import java.time.LocalDateTime;

/**
 * 部门管理员实体类
 */
public class DepartmentAdmin {
    private Long id;                      // 主键ID
    private Long departmentId;            // 部门ID
    private String userid;                // 部门管理员的userid
    private Integer type;                 // 部门管理员的类型：1-校区负责人, 2-年级负责人, 3-班主任, 4-任课老师, 5-学段负责人
    private String subject;               // 教师或班主任的科目
    private LocalDateTime createTime;     // 创建时间
    private LocalDateTime updateTime;     // 更新时间

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
