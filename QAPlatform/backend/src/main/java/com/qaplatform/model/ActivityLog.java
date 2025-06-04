package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * 活动日志实体类 - 记录用户的所有操作
 */
@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;  // 活动类型
    
    @Column(nullable = false, length = 500)
    private String description;  // 活动描述
    
    @Column(name = "target_type", length = 50)
    private String targetType;  // 目标类型（问题、回答、标签等）
    
    @Column(name = "target_id")
    private Long targetId;  // 目标ID
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;  // IP地址
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;  // 用户代理
    
    @Column(nullable = false)
    private Integer reputation = 0;  // 此次活动获得的声望值
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 活动类型枚举
     */
    public enum ActivityType {
        LOGIN("登录"),
        LOGOUT("登出"),
        REGISTER("注册"),
        ASK_QUESTION("提问"),
        ANSWER_QUESTION("回答问题"),
        EDIT_QUESTION("编辑问题"),
        EDIT_ANSWER("编辑回答"),
        DELETE_QUESTION("删除问题"),
        DELETE_ANSWER("删除回答"),
        VOTE_UP("点赞"),
        VOTE_DOWN("踩"),
        CANCEL_VOTE("取消投票"),
        ACCEPT_ANSWER("采纳回答"),
        FOLLOW_QUESTION("关注问题"),
        UNFOLLOW_QUESTION("取消关注问题"),
        FOLLOW_USER("关注用户"),
        UNFOLLOW_USER("取消关注用户"),
        CREATE_TAG("创建标签"),
        BADGE_AWARDED("获得徽章"),
        PROFILE_UPDATE("更新个人资料"),
        VIEW_QUESTION("查看问题"),
        SEARCH("搜索"),
        REPORT("举报"),
        MODERATE("管理操作");
        
        private final String description;
        
        ActivityType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造方法
    public ActivityLog(User user, ActivityType activityType, String description, 
                      String targetType, Long targetId) {
        this.user = user;
        this.activityType = activityType;
        this.description = description;
        this.targetType = targetType;
        this.targetId = targetId;
    }
} 