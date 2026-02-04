package com.sp.framework.config;

import com.sp.common.constant.Constants;
import com.sp.common.config.OverallSituationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sp.framework.interceptor.RepeatSubmitInterceptor;
import com.sp.framework.interceptor.TokenInterceptor;

/**
 * 拦截器配置
 *
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {
    /**
     * 首页地址
     */
    @Value("${user.indexUrl}")
    private String indexUrl;

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * 默认首页的设置，当输入域名是可以自动跳转到默认指定的网页
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:" + indexUrl);
        // 添加对sp-api路径的支持
        registry.addViewController("/sp-api/").setViewName("forward:/dist/index.html");
        registry.addViewController("/sp-api").setViewName("forward:/dist/index.html");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** 本地文件上传路径 */
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**").addResourceLocations("file:" + OverallSituationConfig.getProfile() + "/");

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");

        /** 前端静态资源配置 */
        registry.addResourceHandler("/dist/**").addResourceLocations("classpath:/dist/");
        registry.addResourceHandler("/sp-api/dist/**").addResourceLocations("classpath:/dist/");
        registry.addResourceHandler("/sp-api/assets/**").addResourceLocations("classpath:/dist/assets/");
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
        // 对特定路径放行，避免被token拦截
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/captchaImage",
                        "/profile/**",
                        "/dist/**",
                        "/assets/**",
                        "/sp-api/assets/**",
                        "/sp-api/dist/**",
                        "/favicon.ico",
                        "/wechat/callback",
                        "/wechat/callback/",
                        "/wechat/callback/*",
                        "/wechat/oauth/callback",
                        "/swagger-ui/**",
                        "/tool/swagger",
                        "/tool/swagger/**"
                );
    }
}