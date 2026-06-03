package com.sp.framework.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.alibaba.druid.pool.DruidDataSource;

/**
 * druid 配置屬性
 *
 */
@Configuration
public class DruidProperties
{
    @Value("${spring.datasource.druid.master.initialSize}")
    private int initialSize;

    @Value("${spring.datasource.druid.master.minIdle}")
    private int minIdle;

    @Value("${spring.datasource.druid.master.maxActive}")
    private int maxActive;

    @Value("${spring.datasource.druid.master.maxWait}")
    private int maxWait;

    @Value("${spring.datasource.druid.master.connectTimeout}")
    private int connectTimeout;

    @Value("${spring.datasource.druid.master.socketTimeout}")
    private int socketTimeout;

    @Value("${spring.datasource.druid.master.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.druid.master.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    @Value("${spring.datasource.druid.master.maxEvictableIdleTimeMillis}")
    private int maxEvictableIdleTimeMillis;

    @Value("${spring.datasource.druid.master.validationQuery}")
    private String validationQuery;

    @Value("${spring.datasource.druid.master.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.druid.master.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.druid.master.testOnReturn}")
    private boolean testOnReturn;

    public DruidDataSource dataSource(DruidDataSource datasource)
    {
        /** 配置初始化大小、最小、最大 */
        datasource.setInitialSize(initialSize);
        datasource.setMaxActive(maxActive);
        datasource.setMinIdle(minIdle);

        /** 配置獲取連接等待超時的時間 */
        datasource.setMaxWait(maxWait);
        
        /** 配置驅動連接超時時間，檢測數據庫建立連接的超時時間，單位是毫秒 */
        datasource.setConnectTimeout(connectTimeout);
        
        /** 配置網絡超時時間，等待數據庫操作完成的網絡超時時間，單位是毫秒 */
        datasource.setSocketTimeout(socketTimeout);

        /** 配置間隔多久才進行一次檢測，檢測需要關閉的空閒連接，單位是毫秒 */
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

        /** 配置一個連接在池中最小、最大生存的時間，單位是毫秒 */
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);

        /**
         * 用來檢測連接是否有效的sql，要求是一個查詢語句，常用select 'x'。如果validationQuery爲null，testOnBorrow、testOnReturn、testWhileIdle都不會起作用。
         */
        datasource.setValidationQuery(validationQuery);
        /** 建議配置爲true，不影響性能，並且保證安全性。申請連接的時候檢測，如果空閒時間大於timeBetweenEvictionRunsMillis，執行validationQuery檢測連接是否有效。 */
        datasource.setTestWhileIdle(testWhileIdle);
        /** 申請連接時執行validationQuery檢測連接是否有效，做了這個配置會降低性能。 */
        datasource.setTestOnBorrow(testOnBorrow);
        /** 歸還連接時執行validationQuery檢測連接是否有效，做了這個配置會降低性能。 */
        datasource.setTestOnReturn(testOnReturn);
        return datasource;
    }
}