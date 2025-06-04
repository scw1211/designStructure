package com.qaplatform.repository;

import com.qaplatform.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户徽章Repository接口
 */
@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    /**
     * 根据用户ID查找徽章
     */
    List<UserBadge> findByUserId(Long userId);
    
    /**
     * 根据徽章名称查找
     */
    List<UserBadge> findByBadgeName(String badgeName);
    
    /**
     * 根据用户ID和徽章名称查找
     */
    List<UserBadge> findByUserIdAndBadgeName(Long userId, String badgeName);
    
    /**
     * 检查用户是否拥有特定徽章
     */
    boolean existsByUserIdAndBadgeName(Long userId, String badgeName);
    
    /**
     * 统计用户徽章数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计特定徽章的用户数量
     */
    long countByBadgeName(String badgeName);
} 