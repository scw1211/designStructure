package com.qaplatform.service;

import com.qaplatform.dto.*;
import com.qaplatform.model.*;
import com.qaplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 数据分析服务
 * 提供用户统计、徽章系统、活动追踪等高级功能
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsService {
    
    private final UserStatisticsRepository userStatisticsRepository;
    private final BadgeRepository badgeRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ViewRepository viewRepository;
    private final UserRepository userRepository;
    
    /**
     * 获取用户统计信息
     */
    public UserStatisticsViewDTO getUserStatistics(Long userId) {
        Optional<Object[]> result = viewRepository.findUserStatistics(userId);
        if (result.isPresent()) {
            return convertToUserStatisticsDTO(result.get());
        }
        return null;
    }
    
    /**
     * 获取用户排行榜
     */
    public List<UserStatisticsViewDTO> getUserLeaderboard(int page, int size) {
        List<Object[]> results = viewRepository.findAllUserStatistics(size, page * size);
        List<UserStatisticsViewDTO> leaderboard = new ArrayList<>();
        for (Object[] row : results) {
            leaderboard.add(convertToUserStatisticsDTO(row));
        }
        return leaderboard;
    }
    
    /**
     * 获取热门问题
     */
    public List<TrendingQuestionDTO> getTrendingQuestions(int page, int size) {
        List<Object[]> results = viewRepository.findTrendingQuestions(size, page * size);
        List<TrendingQuestionDTO> trending = new ArrayList<>();
        for (Object[] row : results) {
            trending.add(convertToTrendingQuestionDTO(row));
        }
        return trending;
    }
    
    /**
     * 获取专家用户
     */
    public List<ExpertUserDTO> getExpertUsers(int page, int size) {
        List<Object[]> results = viewRepository.findExpertUsers(size, page * size);
        List<ExpertUserDTO> experts = new ArrayList<>();
        for (Object[] row : results) {
            experts.add(convertToExpertUserDTO(row));
        }
        return experts;
    }
    
    /**
     * 记录用户活动
     */
    public void logActivity(Long userId, ActivityLog.ActivityType activityType, 
                           String description, String targetType, Long targetId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            ActivityLog log = new ActivityLog();
            log.setUser(user);
            log.setActivityType(activityType);
            log.setDescription(description);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setCreatedAt(LocalDateTime.now());
            activityLogRepository.save(log);
        }
    }
    
    /**
     * 获取用户活动记录
     */
    public List<ActivityLog> getUserActivities(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).getContent();
    }
    
    /**
     * 检查并奖励徽章
     */
    public List<Badge> checkAndAwardBadges(Long userId) {
        Optional<UserStatistics> statsOpt = userStatisticsRepository.findByUserId(userId);
        if (statsOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        UserStatistics stats = statsOpt.get();
        List<Badge> newBadges = new ArrayList<>();
        
        // 检查各种徽章条件
        checkQuestionBadges(stats, newBadges);
        checkAnswerBadges(stats, newBadges);
        checkVoteBadges(stats, newBadges);
        
        // 为用户授予新徽章
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            for (Badge badge : newBadges) {
                if (!user.getBadges().contains(badge)) {
                    user.addBadge(badge);
                    logActivity(userId, ActivityLog.ActivityType.BADGE_AWARDED, 
                              "获得徽章: " + badge.getName(), "badge", badge.getId());
                }
            }
            userRepository.save(user);
        }
        
        return newBadges;
    }
    
    /**
     * 更新用户统计信息
     */
    public UserStatistics updateUserStatistics(Long userId) {
        // 这里可以调用存储过程或手动计算
        // 为了简化，我们先返回现有的统计信息
        return userStatisticsRepository.findByUserId(userId).orElse(null);
    }
    
    /**
     * 获取活跃度统计
     */
    public List<Object[]> getActivityStatistics(int page, int size) {
        return viewRepository.findActivityStatistics(size, page * size);
    }
    
    /**
     * 获取标签统计
     */
    public List<Object[]> getTagStatistics(int page, int size) {
        return viewRepository.findTagStatistics(size, page * size);
    }
    
    // 私有辅助方法
    
    private void checkQuestionBadges(UserStatistics stats, List<Badge> newBadges) {
        if (stats.getTotalQuestions() >= 1) {
            addBadgeIfEligible("Curious", newBadges);
        }
        if (stats.getTotalQuestions() >= 10) {
            addBadgeIfEligible("Inquisitive", newBadges);
        }
    }
    
    private void checkAnswerBadges(UserStatistics stats, List<Badge> newBadges) {
        if (stats.getTotalAnswers() >= 1) {
            addBadgeIfEligible("Helper", newBadges);
        }
        if (stats.getAcceptedAnswers() >= 10) {
            addBadgeIfEligible("Teacher", newBadges);
        }
        if (stats.getAcceptedAnswers() >= 50 && stats.getAcceptanceRate() >= 80) {
            addBadgeIfEligible("Expert", newBadges);
        }
    }
    
    private void checkVoteBadges(UserStatistics stats, List<Badge> newBadges) {
        if (stats.getTotalVotesReceived() >= 100) {
            addBadgeIfEligible("Popular", newBadges);
        }
    }
    
    private void addBadgeIfEligible(String badgeName, List<Badge> newBadges) {
        Optional<Badge> badge = badgeRepository.findByName(badgeName);
        badge.ifPresent(newBadges::add);
    }
    
    // DTO转换方法
    
    private UserStatisticsViewDTO convertToUserStatisticsDTO(Object[] row) {
        UserStatisticsViewDTO dto = new UserStatisticsViewDTO();
        dto.setUserId(((Number) row[0]).longValue());
        dto.setUsername((String) row[1]);
        dto.setEmail((String) row[2]);
        dto.setReputation(((Number) row[3]).intValue());
        dto.setRole((String) row[4]);
        dto.setUserCreatedAt((LocalDateTime) row[5]);
        dto.setTotalQuestions(((Number) row[6]).intValue());
        dto.setTotalAnswers(((Number) row[7]).intValue());
        dto.setAcceptedAnswers(((Number) row[8]).intValue());
        dto.setTotalVotesReceived(((Number) row[9]).intValue());
        dto.setTotalVotesGiven(((Number) row[10]).intValue());
        dto.setQuestionsFollowed(((Number) row[11]).intValue());
        dto.setUsersFollowing(((Number) row[12]).intValue());
        dto.setFollowersCount(((Number) row[13]).intValue());
        dto.setBadgesCount(((Number) row[14]).intValue());
        dto.setAcceptanceRate(((Number) row[15]).doubleValue());
        dto.setAverageAnswerScore(((Number) row[16]).doubleValue());
        dto.setLastActiveDate((LocalDateTime) row[17]);
        dto.setActivityCount7d(((Number) row[18]).intValue());
        dto.setActivityCount30d(((Number) row[19]).intValue());
        return dto;
    }
    
    private TrendingQuestionDTO convertToTrendingQuestionDTO(Object[] row) {
        TrendingQuestionDTO dto = new TrendingQuestionDTO();
        dto.setId(((Number) row[0]).longValue());
        dto.setTitle((String) row[1]);
        dto.setContent((String) row[2]);
        dto.setViewCount(((Number) row[3]).intValue());
        dto.setAnswerCount(((Number) row[4]).intValue());
        dto.setVotes(((Number) row[5]).intValue());
        dto.setSolved((Boolean) row[6]);
        dto.setCreatedAt((LocalDateTime) row[7]);
        dto.setUpdatedAt((LocalDateTime) row[8]);
        dto.setAuthorUsername((String) row[9]);
        dto.setAuthorReputation(((Number) row[10]).intValue());
        dto.setTrendingScore(((Number) row[11]).doubleValue());
        dto.setTags((String) row[12]);
        return dto;
    }
    
    private ExpertUserDTO convertToExpertUserDTO(Object[] row) {
        ExpertUserDTO dto = new ExpertUserDTO();
        dto.setId(((Number) row[0]).longValue());
        dto.setUsername((String) row[1]);
        dto.setReputation(((Number) row[2]).intValue());
        dto.setAvatar((String) row[3]);
        dto.setTotalQuestions(((Number) row[4]).intValue());
        dto.setTotalAnswers(((Number) row[5]).intValue());
        dto.setAcceptedAnswers(((Number) row[6]).intValue());
        dto.setAcceptanceRate(((Number) row[7]).doubleValue());
        dto.setTotalVotesReceived(((Number) row[8]).intValue());
        dto.setBadgesCount(((Number) row[9]).intValue());
        dto.setAverageAnswerScore(((Number) row[10]).doubleValue());
        dto.setExpertiseScore(((Number) row[11]).doubleValue());
        dto.setTopTags((String) row[12]);
        return dto;
    }
} 