package com.sp.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 學籍庫 student_profiles 輔助配置（庫名從 datasource url 解析，供跨庫 SQL 使用）
 */
@Component
public class StudentProfilesProperties {

    @Value("${spring.datasource.druid.student-profiles.url:}")
    private String url;

    public String getDatabase() {
        if (url == null || url.isEmpty()) {
            return "student_profiles";
        }
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return "student_profiles";
        }
        int slash = url.indexOf('/', schemeEnd + 3);
        if (slash < 0 || slash + 1 >= url.length()) {
            return "student_profiles";
        }
        int query = url.indexOf('?', slash);
        return query > 0 ? url.substring(slash + 1, query) : url.substring(slash + 1);
    }
}
