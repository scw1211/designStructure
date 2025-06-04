package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到记录实体类
 */
@Entity
@Table(name = "user_checkins", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "checkin_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "checkins"})
    private User user;
    
    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;  // 签到日期
    
    @Column(name = "consecutive_days", nullable = false)
    private Integer consecutiveDays = 1;  // 连续签到天数
    
    @Column(name = "reputation_earned", nullable = false)
    private Integer reputationEarned = 10;  // 获得的声望值
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 