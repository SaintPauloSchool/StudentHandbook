package com.sp.framework.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.util.Utils;
import com.sp.common.enums.DataSourceType;
import com.sp.common.utils.spring.SpringUtils;
import com.sp.framework.config.properties.DruidProperties;
import com.sp.framework.datasource.DynamicDataSource;

/**
 * druid 配置多數據源
 *
 */
@Configuration
public class DruidConfig
{
    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource masterDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
    public DataSource slaveDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.student-profiles")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.student-profiles", name = "enabled", havingValue = "true")
    public DataSource studentProfilesDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(DataSource masterDataSource)
    {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER.name(), masterDataSource);
        setDataSource(targetDataSources, DataSourceType.SLAVE.name(), "slaveDataSource");
        setDataSource(targetDataSources, DataSourceType.STUDENT_PROFILES.name(), "studentProfilesDataSource");
        return new DynamicDataSource(masterDataSource, targetDataSources);
    }

    /**
     * 設置數據源
     * 
     * @param targetDataSources 備選數據源集合
     * @param sourceName 數據源名稱
     * @param beanName bean名稱
     */
    public void setDataSource(Map<Object, Object> targetDataSources, String sourceName, String beanName)
    {
        try
        {
            DataSource dataSource = SpringUtils.getBean(beanName);
            targetDataSources.put(sourceName, dataSource);
        }
        catch (Exception e)
        {
        }
    }

    /**
     * 去除監控頁面底部的廣告
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
    public FilterRegistrationBean removeDruidFilterRegistrationBean()
    {
        // 提取common.js的配置路徑
        String pattern = "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        final String filePath = "support/http/resources/js/common.js";
        // 創建filter進行過濾
        Filter filter = new Filter()
        {
            @Override
            public void init(javax.servlet.FilterConfig filterConfig) throws ServletException
            {
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException
            {
                chain.doFilter(request, response);
                // 重置緩衝區，響應頭不會被重置
                response.resetBuffer();
                // 獲取common.js
                String text = Utils.readFromResource(filePath);
                // 正則替換banner, 除去底部的廣告信息
                response.getWriter().write(text);
            }

            @Override
            public void destroy()
            {
            }
        };
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }
}