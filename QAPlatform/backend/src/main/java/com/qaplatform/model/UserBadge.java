package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户徽章实体类
 * 表示用户获得的徽章记录
 */
@Entity
@Table(name = "user_badges", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 徽章名称
     */
    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;
    
    /**
     * 徽章描述
     */
    @Column(name = "badge_description", length = 500)
    private String badgeDescription;
    
    /**
     * 徽章级别
     */
    @Column(name = "badge_level", length = 20)
    private String badgeLevel;
    
    /**
     * 徽章图标/emoji
     */
    @Column(name = "badge_icon", length = 10)
    private String badgeIcon;
    
    /**
     * 获得徽章的时间
     */
    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;
    
    /**
     * 获得徽章的原因
     */
    @Column(name = "earned_reason", length = 500)
    private String earnedReason;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (earnedAt == null) {
            earnedAt = LocalDateTime.now();
        }
    }
} 