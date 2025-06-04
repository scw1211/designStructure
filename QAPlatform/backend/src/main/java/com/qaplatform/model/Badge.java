package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 徽章实体类 - 用户成就系统
 */
@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Badge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;  // 徽章名称
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;  // 徽章描述
    
    @Column(nullable = false, length = 200)
    private String iconUrl;  // 徽章图标URL
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType type;  // 徽章类型
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeLevel level;  // 徽章等级
    
    @Column(nullable = false)
    private Integer requiredCount;  // 获得徽章所需数量
    
    @Column(nullable = false)
    private Boolean active = true;  // 是否启用
    
    @ManyToMany(mappedBy = "badges")
    private Set<User> users = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 徽章类型枚举
     */
    public enum BadgeType {
        QUESTION("提问相关"),
        ANSWER("回答相关"),
        VOTE("投票相关"),
        PARTICIPATION("参与相关"),
        SOCIAL("社交相关"),
        STREAK("连续性相关"),
        MODERATION("管理相关");
        
        private final String description;
        
        BadgeType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 徽章等级枚举
     */
    public enum BadgeLevel {
        BRONZE("铜级", "#CD7F32"),
        SILVER("银级", "#C0C0C0"),
        GOLD("金级", "#FFD700"),
        PLATINUM("白金", "#E5E4E2"),
        DIAMOND("钻石", "#B9F2FF");
        
        private final String name;
        private final String color;
        
        BadgeLevel(String name, String color) {
            this.name = name;
            this.color = color;
        }
        
        public String getName() {
            return name;
        }
        
        public String getColor() {
            return color;
        }
    }
} 