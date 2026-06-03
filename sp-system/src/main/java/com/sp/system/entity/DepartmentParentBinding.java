package com.sp.system.entity;

import com.sp.common.core.domain.BaseEntity;

/**
 * 部門家長綁定實體類
 *
 */
public class DepartmentParentBinding extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主鍵ID */
    private Long id;

    /** 部門ID */
    private Long departmentId;

    /** 家長用戶ID */
    private String parentUserId;

    /** 學生用戶ID */
    private String studentUserId;

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

    public String getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(String parentUserId) {
        this.parentUserId = parentUserId;
    }

    public String getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(String studentUserId) {
        this.studentUserId = studentUserId;
    }
}