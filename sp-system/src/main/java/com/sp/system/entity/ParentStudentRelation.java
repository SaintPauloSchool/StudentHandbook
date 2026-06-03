package com.sp.system.entity;

import com.sp.common.core.domain.BaseEntity;

/**
 * 家長學生關係實體類
 *
 */
public class ParentStudentRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主鍵ID */
    private Long id;

    /** 家長用戶ID */
    private String parentUserId;

    /** 學生用戶ID */
    private String studentUserId;

    /** 學生姓名 */
    private String studentName;

    /** 關係描述 */
    private String relationDesc;

    /** 家長手機號 */
    private String mobile;

    /** 家長外部用戶ID */
    private String externalUserid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRelationDesc() {
        return relationDesc;
    }

    public void setRelationDesc(String relationDesc) {
        this.relationDesc = relationDesc;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getExternalUserid() {
        return externalUserid;
    }

    public void setExternalUserid(String externalUserid) {
        this.externalUserid = externalUserid;
    }

}