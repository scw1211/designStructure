package com.qaplatform.config;

import com.qaplatform.interceptor.JwtAuthInterceptor;
import com.qaplatform.interceptor.UserActivityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置拦截器等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;
    
    @Autowired
    private UserActivityInterceptor userActivityInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的静态资源访问
        String projectDir = System.getProperty("user.dir");
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + projectDir + "/uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加JWT认证拦截器，应用到所有需要认证的路径
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/users/**", "/questions/*/vote", "/questions/answers/*/vote", "/questions/*/accept")
                .excludePathPatterns(
                    "/users/register",
                    "/users/login",
                    "/users/*/questions",
                    "/users/*/stats",
                    "/users/*/badges",
                    "/users/ranking",
                    "/users/search",
                    "/users/test/follow-system",
                    "/users/test/generate-token"
                );
        
        // 添加用户活动拦截器
        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns("/**");
    }
} 