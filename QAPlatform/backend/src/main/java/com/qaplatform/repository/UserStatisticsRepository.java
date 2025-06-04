package com.qaplatform.repository;

import com.qaplatform.model.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户统计信息Repository
 */
@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    
    /**
     * 根据用户ID查找统计信息
     */
    Optional<UserStatistics> findByUserId(Long userId);
    
    /**
     * 获取排行榜前N位用户
     */
    @Query("SELECT us FROM UserStatistics us ORDER BY us.totalVotesReceived DESC")
    List<UserStatistics> findTopByVotesReceived(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 获取采纳率最高的用户
     */
    @Query("SELECT us FROM UserStatistics us WHERE us.totalAnswers >= :minAnswers ORDER BY us.acceptanceRate DESC")
    List<UserStatistics> findTopByAcceptanceRate(@Param("minAnswers") Integer minAnswers, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 获取最活跃用户（基于活动数量）
     */
    @Query("SELECT us FROM UserStatistics us ORDER BY (us.totalQuestions + us.totalAnswers) DESC")
    List<UserStatistics> findMostActive(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 获取新用户统计
     */
    @Query(value = "SELECT * FROM user_statistics us WHERE us.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) ORDER BY us.created_at DESC", nativeQuery = true)
    List<UserStatistics> findNewUsers(org.springframework.data.domain.Pageable pageable);
} 