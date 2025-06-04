package com.qaplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 热门问题DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingQuestionDTO {
    
    private Long id;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer answerCount;
    private Integer votes;
    private Boolean solved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorUsername;
    private Integer authorReputation;
    private Double trendingScore;
    private String tags;
} 