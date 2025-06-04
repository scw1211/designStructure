package com.qaplatform.repository;

import com.qaplatform.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动日志Repository
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    /**
     * 根据用户ID查找活动日志
     */
    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据活动类型查找
     */
    Page<ActivityLog> findByActivityTypeOrderByCreatedAtDesc(ActivityLog.ActivityType activityType, Pageable pageable);
    
    /**
     * 查找指定时间段内的活动
     */
    @Query("SELECT al FROM ActivityLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<ActivityLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查找用户在指定时间段内的活动
     */
    @Query("SELECT al FROM ActivityLog al WHERE al.user.id = :userId AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<ActivityLog> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取活动统计
     */
    @Query("SELECT al.activityType, COUNT(al) FROM ActivityLog al WHERE al.createdAt >= :startDate GROUP BY al.activityType")
    List<Object[]> getActivityStatistics(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 获取最近的用户活动
     */
    @Query("SELECT al FROM ActivityLog al WHERE al.user.id = :userId ORDER BY al.createdAt DESC")
    List<ActivityLog> findRecentUserActivities(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 查找目标相关的活动
     */
    @Query("SELECT al FROM ActivityLog al WHERE al.targetType = :targetType AND al.targetId = :targetId ORDER BY al.createdAt DESC")
    List<ActivityLog> findByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    /**
     * 删除旧的活动日志（数据清理）
     */
    @Query("DELETE FROM ActivityLog al WHERE al.createdAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
} 