package com.qaplatform.repository;

import com.qaplatform.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 徽章Repository
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    /**
     * 根据名称查找徽章
     */
    Optional<Badge> findByName(String name);
    
    /**
     * 根据类型查找徽章
     */
    List<Badge> findByType(Badge.BadgeType type);
    
    /**
     * 根据等级查找徽章
     */
    List<Badge> findByLevel(Badge.BadgeLevel level);
    
    /**
     * 查找启用的徽章
     */
    List<Badge> findByActiveTrue();
    
    /**
     * 根据类型和等级查找徽章
     */
    List<Badge> findByTypeAndLevel(Badge.BadgeType type, Badge.BadgeLevel level);
    
    /**
     * 获取用户可能获得的徽章（基于所需数量）
     */
    @Query("SELECT b FROM Badge b WHERE b.active = true AND b.requiredCount <= :userScore AND b.type = :type")
    List<Badge> findEligibleBadges(@Param("userScore") Integer userScore, @Param("type") Badge.BadgeType type);
} 