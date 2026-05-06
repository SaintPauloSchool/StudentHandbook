package com.sp.system.entity;

import com.sp.common.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * Token实体类
 *
 */
public class Token extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private String userId;

    /** 用户类型 (1: parent, 0: student, 2: staff) */
    private Integer userType;

    /** Token值 */
    private String token;

    /** 过期时间 */
    private LocalDateTime expireTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

}