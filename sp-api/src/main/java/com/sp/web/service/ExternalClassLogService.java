package com.sp.web.service;

import com.sp.system.entity.ClassLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 外部課程日誌數據服務 - 用於定時任務從外部數據庫獲取數據
 */
@Service
public class ExternalClassLogService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalClassLogService.class);

    // 源數據庫配置
    @Value("${data.transfer.source.host:10.32.64.25}")
    private String sourceHost;

    @Value("${data.transfer.source.port:3306}")
    private String sourcePort;

    @Value("${data.transfer.source.database:esp_center}")
    private String sourceDatabase;

    @Value("${data.transfer.source.username:StuReader}")
    private String sourceUsername;

    @Value("${data.transfer.source.password:System4read}")
    private String sourcePassword;

    private DataSource sourceDataSource;
    private JdbcTemplate sourceJdbcTemplate;

    @PostConstruct
    public void init() {
        // 初始化源數據庫連接
        this.sourceDataSource = createDataSource(sourceHost, sourcePort, sourceDatabase, sourceUsername, sourcePassword);
        this.sourceJdbcTemplate = new JdbcTemplate(sourceDataSource);
    }

    /**
     * 創建數據源
     */
    private DataSource createDataSource(String host, String port, String database, String username, String password) {
        com.alibaba.druid.pool.DruidDataSource dataSource = new com.alibaba.druid.pool.DruidDataSource();
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&connectTimeout=5000&socketTimeout=30000");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 配置 Druid 連接池屬性
        dataSource.setInitialSize(2);
        dataSource.setMinIdle(2);
        dataSource.setMaxActive(5); // 限制連接數，避免過多連接
        dataSource.setMaxWait(10000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(true); // 更改此行以確保連接可用
        dataSource.setTestOnReturn(false);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);

        return dataSource;
    }

    /**
     * 從外部數據庫獲取所有ClassLog數據
     */
    public List<ClassLog> getAllClassLogsFromExternal() {
        try {
            String sql = "SELECT distinct id, student_class, teacher, course, course_type, content, start_date, end_date FROM class_log_temp WHERE start_date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) ORDER BY start_date";
            return sourceJdbcTemplate.query(sql, new ClassLogRowMapper());
        } catch (Exception e) {
            logger.error("從外部數據庫獲取課程日誌數據失敗: {}", e.getMessage());
            // 返回空列表而不是拋出異常
            return java.util.Collections.emptyList();
        }
    }

    /**
     * RowMapper 用於將結果集映射到 ClassLog 對象
     */
    private static class ClassLogRowMapper implements RowMapper<ClassLog> {
        @Override
        public ClassLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClassLog classLog = new ClassLog();
            classLog.setId(rs.getString("id"));
            classLog.setStudentClass(rs.getString("student_class"));
            classLog.setTeacher(rs.getString("teacher"));
            classLog.setCourse(rs.getString("course"));
            classLog.setCourseType(rs.getString("course_type"));
            classLog.setContent(rs.getString("content"));
            classLog.setStartDate(rs.getString("start_date"));
            classLog.setEndDate(rs.getString("end_date"));
            classLog.setUpdateDate(rs.getString("update_date"));
            return classLog;
        }
    }
}