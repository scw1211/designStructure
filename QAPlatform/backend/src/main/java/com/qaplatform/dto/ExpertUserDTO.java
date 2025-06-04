package com.qaplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 专家用户DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertUserDTO {
    
    private Long id;
    private String username;
    private Integer reputation;
    private String avatar;
    private Integer totalQuestions;
    private Integer totalAnswers;
    private Integer acceptedAnswers;
    private Double acceptanceRate;
    private Integer totalVotesReceived;
    private Integer badgesCount;
    private Double averageAnswerScore;
    private Double expertiseScore;
    private String topTags;
} 