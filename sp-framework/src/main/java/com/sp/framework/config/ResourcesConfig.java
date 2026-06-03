package com.sp.framework.config;

import com.sp.common.constant.Constants;
import com.sp.common.config.OverallSituationConfig;
import com.sp.framework.interceptor.ApiSignatureInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sp.framework.interceptor.RepeatSubmitInterceptor;
import com.sp.framework.interceptor.TokenInterceptor;

/**
 * 攔截器配置
 *
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Autowired
    private ApiSignatureInterceptor apiSignatureInterceptor;

    /**
     * 默認首頁的設置，當輸入域名是可以自動跳轉到默認指定的網頁
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** 本地文件上傳路徑 */
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**").addResourceLocations("file:" + OverallSituationConfig.getProfile() + "/");

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
    }

    /**
     * 自定義攔截規則
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
        // 對特定路徑放行，避免被token攔截
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/captchaImage",
                        "/profile/**",
                        "/favicon.ico",
                        "/wechat/callback",
                        "/wechat/callback/",
                        "/wechat/callback/**",
                        "/wechat/oauth/callback",
                        "/common/upload",
                        "/common/download/**",
                        "/swagger-ui/**",
                        "/tool/swagger",
                        "/tool/swagger/**"
                );
                
        // 籤名校驗攔截器配置
        registry.addInterceptor(apiSignatureInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/captchaImage",
                        "/profile/**",
                        "/favicon.ico",
                        "/wechat/callback",
                        "/wechat/callback/",
                        "/wechat/callback/**",
                        "/wechat/oauth/callback",
                        "/common/download/**",
                        "/swagger-ui/**",
                        "/tool/swagger",
                        "/tool/swagger/**"
                );
    }
}