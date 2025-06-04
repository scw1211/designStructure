package com.qaplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户统计视图DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsViewDTO {
    
    private Long userId;
    private String username;
    private String email;
    private Integer reputation;
    private String role;
    private LocalDateTime userCreatedAt;
    private Integer totalQuestions;
    private Integer totalAnswers;
    private Integer acceptedAnswers;
    private Integer totalVotesReceived;
    private Integer totalVotesGiven;
    private Integer questionsFollowed;
    private Integer usersFollowing;
    private Integer followersCount;
    private Integer badgesCount;
    private Double acceptanceRate;
    private Double averageAnswerScore;
    private LocalDateTime lastActiveDate;
    private Integer activityCount7d;
    private Integer activityCount30d;
} 