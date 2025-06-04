package com.qaplatform.interceptor;

import com.qaplatform.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户活跃状态拦截器
 * 在用户访问API时自动更新其在线状态
 */
@Component
public class UserActivityInterceptor implements HandlerInterceptor {

    @Autowired
    private OnlineUserService onlineUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取用户ID（如果有的话）
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                onlineUserService.updateUserActivity(userId);
            } catch (NumberFormatException e) {
                // 忽略无效的用户ID
            }
        }

        return true;
    }
} 