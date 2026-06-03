package com.sp.web.core.config;

import com.sp.common.config.OverallSituationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger2的接口配置
 *
 */
@Configuration
public class SwaggerConfig
{
    /** 是否開啓swagger */
    @Value("${swagger.enabled}")
    private boolean enabled;
    
    /**
     * 創建API
     */
    @Bean
    public Docket createRestApi()
    {
        return new Docket(DocumentationType.OAS_30)
                // 是否啓用Swagger
                .enable(enabled)
                // 用來創建該API的基本信息，展示在文檔的頁面中（自定義展示的信息）
                .apiInfo(apiInfo())
                // 設置哪些接口暴露給Swagger展示
                .select()
                // 掃描所有有註解的api，用這種方式更靈活
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 掃描指定包中的swagger註解
                //.apis(RequestHandlerSelectors.basePackage("com.sp.project.tool.swagger"))
                // 掃描所有 .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 添加摘要信息
     */
    private ApiInfo apiInfo()
    {
        // 用ApiInfoBuilder進行定製
        return new ApiInfoBuilder()
                // 設置標題
                .title("標題：系統接口文檔")
                // 描述
                .description("描述")
                // 作者信息
                .contact(new Contact(OverallSituationConfig.getName(), null, null))
                // 版本
                .version("版本號:" + OverallSituationConfig.getVersion())
                .build();
    }
}
