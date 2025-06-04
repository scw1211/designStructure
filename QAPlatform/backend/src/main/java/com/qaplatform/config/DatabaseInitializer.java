package com.qaplatform.config;

import com.qaplatform.model.Badge;
import com.qaplatform.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据库初始化组件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {
    
    private final JdbcTemplate jdbcTemplate;
    private final BadgeRepository badgeRepository;
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化数据库高级功能...");
        
        try {
            // 初始化徽章数据
            initializeBadges();
            log.info("数据库高级功能初始化完成！");
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
        }
    }
    
    private void initializeBadges() {
        try {
            log.info("初始化徽章数据...");
            
            // 检查是否已经初始化过
            long badgeCount = badgeRepository.count();
            if (badgeCount > 0) {
                log.info("徽章数据已存在，跳过初始化");
                return;
            }
            
            List<Badge> badges = createDefaultBadges();
            badgeRepository.saveAll(badges);
            
            log.info("徽章数据初始化完成，共创建 {} 个徽章", badges.size());
            
        } catch (Exception e) {
            log.warn("初始化徽章数据时出现问题: {}", e.getMessage());
        }
    }
    
    private List<Badge> createDefaultBadges() {
        return List.of(
            // 提问相关徽章
            new Badge(null, "Curious", "提出第一个问题", "/icons/curious.png", 
                     Badge.BadgeType.QUESTION, Badge.BadgeLevel.BRONZE, 1, true, null, null),
            new Badge(null, "Helper", "提供第一个回答", "/icons/helper.png", 
                     Badge.BadgeType.ANSWER, Badge.BadgeLevel.BRONZE, 1, true, null, null),
            new Badge(null, "Teacher", "获得10个被采纳回答", "/icons/teacher.png", 
                     Badge.BadgeType.ANSWER, Badge.BadgeLevel.SILVER, 10, true, null, null),
            new Badge(null, "Expert", "获得50个被采纳回答", "/icons/expert.png", 
                     Badge.BadgeType.ANSWER, Badge.BadgeLevel.GOLD, 50, true, null, null),
            new Badge(null, "Popular", "获得100个投票", "/icons/popular.png", 
                     Badge.BadgeType.VOTE, Badge.BadgeLevel.SILVER, 100, true, null, null)
        );
    }
} 