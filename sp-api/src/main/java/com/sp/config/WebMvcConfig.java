package com.sp.config;

import com.sp.framework.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射静态资源路径
        registry.addResourceHandler("/dist/**")
                .addResourceLocations("classpath:/dist/");
        
        // 其他静态资源配置
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
                
        // 添加对sp-api路径下dist目录的支持
        registry.addResourceHandler("/sp-api/dist/**")
                .addResourceLocations("classpath:/dist/");
                
        // 添加对sp-api路径下assets目录的支持
        registry.addResourceHandler("/sp-api/assets/**")
                .addResourceLocations("classpath:/dist/assets/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将根路径映射到index.html
        registry.addViewController("/").setViewName("forward:/dist/index.html");
        registry.addViewController("/index").setViewName("forward:/dist/index.html");
        registry.addViewController("/index.html").setViewName("forward:/dist/index.html");
        // 添加对sp-api路径下index.html的支持
        registry.addViewController("/sp-api/").setViewName("forward:/dist/index.html");
        registry.addViewController("/sp-api").setViewName("forward:/dist/index.html");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册token拦截器，排除不需要验证的路径
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/captchaImage",
                        "/profile/**",
                        "/system/handbook/list",
                        "/system/handbook/students",
                        "/sp-api/",
                        "/sp-api",
                        "/sp-api/assets/**",
                        "/sp-api/dist/**",
                        "/dist/**",
                        "/assets/**"
                );
    }
}