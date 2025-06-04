package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户统计实体类 - 用于存储用户的各种统计信息
 */
@Entity
@Table(name = "user_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    private Integer totalQuestions = 0;  // 总提问数
    
    @Column(nullable = false)
    private Integer totalAnswers = 0;  // 总回答数
    
    @Column(nullable = false)
    private Integer acceptedAnswers = 0;  // 被采纳回答数
    
    @Column(nullable = false)
    private Integer totalVotesReceived = 0;  // 获得的总投票数
    
    @Column(nullable = false)
    private Integer totalVotesGiven = 0;  // 投出的总票数
    
    @Column(nullable = false)
    private Integer questionsFollowed = 0;  // 关注的问题数
    
    @Column(nullable = false)
    private Integer followersCount = 0;  // 粉丝数
    
    @Column(nullable = false)
    private Integer followingCount = 0;  // 关注的人数
    
    @Column(nullable = false)
    private Integer streak = 0;  // 连续活跃天数
    
    @Column(nullable = false)
    private Double acceptanceRate = 0.0;  // 回答采纳率
    
    @Column(nullable = false)
    private Double averageAnswerScore = 0.0;  // 平均回答得分
    
    @Column(nullable = false)
    private Integer badgesCount = 0;  // 徽章数量
    
    @Column
    private LocalDate lastActiveDate;  // 最后活跃日期
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 计算采纳率
    public void calculateAcceptanceRate() {
        if (totalAnswers > 0) {
            this.acceptanceRate = (double) acceptedAnswers / totalAnswers * 100;
        } else {
            this.acceptanceRate = 0.0;
        }
    }
} 