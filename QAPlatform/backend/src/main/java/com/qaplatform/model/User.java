package com.qaplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 * 使用了JPA注解进行ORM映射
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"questions", "answers", "followedQuestions", "votedQuestions", "following", "followers", "statistics", "badges", "activityLogs"})
@ToString(exclude = {"questions", "answers", "followedQuestions", "votedQuestions", "following", "followers", "statistics", "badges", "activityLogs"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "questions", "answers", "followedQuestions", "votedQuestions", "following", "followers", "statistics", "badges", "activityLogs"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    @JsonIgnore
    private String password;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(length = 200)
    private String avatar;  // 头像URL
    
    @Column(length = 500)
    private String bio;  // 个人简介
    
    @Column(length = 200)
    private String website;  // 个人网站
    
    @Column(length = 100)
    private String location;  // 所在地
    
    @Column(nullable = false)
    private Integer reputation = 0;  // 声望值
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;  // 用户角色
    
    @Column(nullable = false)
    private Boolean enabled = true;  // 账号是否启用
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 用户提出的问题
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Question> questions = new HashSet<>();
    
    // 用户的回答
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Answer> answers = new HashSet<>();
    
    // 用户关注的问题
    @ManyToMany(mappedBy = "followers")
    @JsonIgnore
    private Set<Question> followedQuestions = new HashSet<>();
    
    // 用户点赞的问题
    @ManyToMany(mappedBy = "voters")
    @JsonIgnore
    private Set<Question> votedQuestions = new HashSet<>();
    
    // 用户统计信息
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private UserStatistics statistics;
    
    // 用户获得的徽章
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_badges",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    @JsonIgnore
    private Set<Badge> badges = new HashSet<>();
    
    // 用户的活动日志
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ActivityLog> activityLogs = new HashSet<>();
    
    // 用户关注的其他用户
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_follows",
        joinColumns = @JoinColumn(name = "follower_id"),
        inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    @JsonIgnore
    private Set<User> following = new HashSet<>();
    
    // 关注当前用户的用户
    @ManyToMany(mappedBy = "following", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> followers = new HashSet<>();
    
    /**
     * 用户角色枚举
     */
    public enum UserRole {
        USER("普通用户"),
        ADMIN("管理员"),
        MODERATOR("版主");
        
        private final String description;
        
        UserRole(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 添加徽章
    public void addBadge(Badge badge) {
        badges.add(badge);
        badge.getUsers().add(this);
    }
    
    // 移除徽章
    public void removeBadge(Badge badge) {
        badges.remove(badge);
        badge.getUsers().remove(this);
    }
    
    // 关注用户
    public void followUser(User user) {
        following.add(user);
    }
    
    // 取消关注用户
    public void unfollowUser(User user) {
        following.remove(user);
    }
} 