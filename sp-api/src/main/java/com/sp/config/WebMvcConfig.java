package com.sp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 其他靜態資源配置
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 前端分離後，不再處理靜態頁面的映射
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TokenInterceptor 已經在 ResourcesConfig 裡面註冊了，這裡不再重複註冊，避免一次請求攔截兩次
    }
}