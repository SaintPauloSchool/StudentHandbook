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
 * 外部课程日志数据服务 - 用于定时任务从外部数据库获取数据
 */
@Service
public class ExternalClassLogService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalClassLogService.class);

    // 源数据库配置
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
        // 初始化源数据库连接
        this.sourceDataSource = createDataSource(sourceHost, sourcePort, sourceDatabase, sourceUsername, sourcePassword);
        this.sourceJdbcTemplate = new JdbcTemplate(sourceDataSource);
    }

    /**
     * 创建数据源
     */
    private DataSource createDataSource(String host, String port, String database, String username, String password) {
        com.alibaba.druid.pool.DruidDataSource dataSource = new com.alibaba.druid.pool.DruidDataSource();
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&connectTimeout=5000&socketTimeout=30000");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 配置 Druid 连接池属性
        dataSource.setInitialSize(2);
        dataSource.setMinIdle(2);
        dataSource.setMaxActive(5); // 限制连接数，避免过多连接
        dataSource.setMaxWait(10000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(true); // 更改此行以确保连接可用
        dataSource.setTestOnReturn(false);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);

        return dataSource;
    }

    /**
     * 从外部数据库获取所有ClassLog数据
     */
    public List<ClassLog> getAllClassLogsFromExternal() {
        try {
            String sql = "SELECT id, studentClass, teacher, course, courseType, content, startDate, endDate, updateDate FROM class_log WHERE startDate >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) ORDER BY startDate";
            return sourceJdbcTemplate.query(sql, new ClassLogRowMapper());
        } catch (Exception e) {
            logger.error("从外部数据库获取课程日志数据失败: {}", e.getMessage());
            // 返回空列表而不是抛出异常
            return java.util.Collections.emptyList();
        }
    }

    /**
     * RowMapper 用于将结果集映射到 ClassLog 对象
     */
    private static class ClassLogRowMapper implements RowMapper<ClassLog> {
        @Override
        public ClassLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClassLog classLog = new ClassLog();
            classLog.setId(rs.getString("id"));
            classLog.setStudentClass(rs.getString("studentClass"));
            classLog.setTeacher(rs.getString("teacher"));
            classLog.setCourse(rs.getString("course"));
            classLog.setCourseType(rs.getString("courseType"));
            classLog.setContent(rs.getString("content"));
            classLog.setStartDate(rs.getString("startDate"));
            classLog.setEndDate(rs.getString("endDate"));
            classLog.setUpdateDate(rs.getString("updateDate"));
            return classLog;
        }
    }
}