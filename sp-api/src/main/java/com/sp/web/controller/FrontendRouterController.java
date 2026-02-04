package com.sp.web.controller;

import com.sp.common.annotation.Anonymous;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 前端路由控制器，用于处理Vue Router的History模式
 * 为特定的前端路由提供映射，避免与API端点冲突
 */
@Anonymous
@Controller
public class FrontendRouterController {

    /**
     * 处理登录页面路由
     */
    @Anonymous
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage() {
        return "forward:/dist/index.html";
    }

    /**
     * 处理学生手册页面路由
     */
    @Anonymous
    @RequestMapping(value = "/handbook", method = RequestMethod.GET)
    public String handbookPage() {
        return "forward:/dist/index.html";
    }

    /**
     * 处理根路径
     */
    @Anonymous
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String homePage() {
        // 如果是根路径，但实际访问的是 /sp-api/，则返回Vue应用
        return "forward:/dist/index.html";
    }
}