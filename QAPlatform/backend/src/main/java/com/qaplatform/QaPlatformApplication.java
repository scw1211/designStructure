package com.qaplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 问答平台主启动类
 */
@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories
public class QaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(QaPlatformApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("问答平台系统启动成功！");
        System.out.println("后端访问地址: http://localhost:8080/api");
        System.out.println("API文档地址: http://localhost:8080/api/swagger-ui.html");
        System.out.println("数据库表将自动创建");
        System.out.println("========================================");
    }
} 