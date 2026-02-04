package com.sp.config;

import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomDispatcherServlet extends DispatcherServlet {

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 在请求处理前设置属性，防止错误页面处理
        request.setAttribute("javax.servlet.error.status_code", null);
        request.setAttribute("javax.servlet.error.message", null);
        request.setAttribute("javax.servlet.error.exception", null);
        request.setAttribute("javax.servlet.error.request_uri", null);
        request.setAttribute("javax.servlet.error.servlet_name", null);

        super.doService(request, response);
    }

    @Override
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 对于找不到处理器的情况，直接返回200状态码并转发到index.html
        // 这样可以避免触发错误页面处理逻辑
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_OK);
            request.getRequestDispatcher("/dist/index.html").forward(request, response);
        }
    }
}