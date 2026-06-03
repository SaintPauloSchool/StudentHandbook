package com.sp.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 程序註解配置
 *
 */
@Configuration
// 表示通過aop框架暴露該代理對象,AopContext能夠訪問
@EnableAspectJAutoProxy(exposeProxy = true)
// 指定要掃描的Mapper類的包的路徑
@MapperScan("com.sp.**.mapper")
// 啓用事務管理
@EnableTransactionManagement
public class ApplicationConfig
{

}