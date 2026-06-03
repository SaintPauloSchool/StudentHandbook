package com.sp.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security工具類
 */
public class SecurityUtils {
    
    /**
     * 獲取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密碼
     *
     * @param password 密碼
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判斷密碼是否相同
     *
     * @param rawPassword 真實密碼
     * @param encodedPassword 加密後字符
     * @return 結果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 獲取用戶IP地址
     * 注意：此方法需要在Web請求上下文中調用
     */
    public static String getIp() {
        // 實際項目中應該從請求中獲取真實IP
        // 這裡簡化處理，返回默認值
        return "127.0.0.1";
    }
}