package com.qaplatform.interceptor;

import com.qaplatform.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器
 * 解析JWT token并将用户ID添加到请求属性中
 */
@Component
@Slf4j
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Authorization请求头
        String authHeader = request.getHeader("Authorization");
        
        log.debug("JWT拦截器处理请求: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Authorization头: {}", authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                // 提取token
                String token = authHeader.substring(7);
                log.debug("提取的token: {}", token);
                
                // 验证token并获取用户ID
                if (JwtUtil.validateToken(token)) {
                    Long userId = JwtUtil.getUserIdFromToken(token);
                    
                    // 将用户ID添加到请求属性中
                    request.setAttribute("currentUserId", userId);
                    
                    log.debug("JWT认证成功，用户ID: {}", userId);
                } else {
                    log.debug("JWT token验证失败");
                }
            } catch (Exception e) {
                log.warn("JWT token解析失败: {}", e.getMessage());
            }
        } else {
            log.debug("没有找到Bearer token");
        }
        
        return true;
    }
} 