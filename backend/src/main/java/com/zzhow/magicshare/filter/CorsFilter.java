package com.zzhow.magicshare.filter;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 添加响应头，允许跨域
 *
 * @author ZZHow
 * @date 2025/01/14
 */
@Component
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, token");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        chain.doFilter(request, response);
    }

}