package com.sp.system.entity;

import com.sp.common.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * Token實體類
 *
 */
public class Token extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主鍵ID */
    private Long id;

    /** 用戶ID */
    private String userId;

    /** 用戶類型 (1: parent, 0: student, 2: staff) */
    private Integer userType;

    /** Token值 */
    private String token;

    /** 過期時間 */
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