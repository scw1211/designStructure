package com.qaplatform.repository;

import com.qaplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户Repository接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :loginId OR u.email = :loginId")
    Optional<User> findByUsernameOrEmail(@Param("loginId") String loginId);
    
    /**
     * 根据声望值排序获取用户列表
     */
    List<User> findAllByOrderByReputationDesc();
    
    /**
     * 根据启用状态查找用户
     */
    List<User> findByEnabled(Boolean enabled);
    
    /**
     * 根据角色查找用户
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * 根据用户名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.bio LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 获取声望值前N的用户
     */
    @Query("SELECT u FROM User u ORDER BY u.reputation DESC LIMIT :limit")
    List<User> findTopByReputation(@Param("limit") int limit);
    
    /**
     * 统计包含特定标签的问题数量
     */
    @Query("SELECT COUNT(q) FROM Question q JOIN q.tags t WHERE t.name = :tagName")
    long countByTagsContaining(@Param("tagName") String tagName);
    
    /**
     * 统计某个标签下已回答的问题数量
     */
    @Query("SELECT COUNT(q) FROM Question q JOIN q.tags t WHERE t.id = :tagId AND q.answerCount > 0")
    long countAnsweredQuestionsByTag(@Param("tagId") Long tagId);
    
    /**
     * 按声望排序获取顶级用户
     */
    @Query("SELECT u FROM User u ORDER BY u.reputation DESC")
    List<User> findTopUsersByReputation(org.springframework.data.domain.Pageable pageable);
    
    // ========== 高级分析查询方法 ==========
    
    /**
     * 统计某个时间之后创建的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :date")
    long countByCreatedAtAfter(@Param("date") LocalDateTime date);
    
    /**
     * 统计某个时间范围内创建的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 统计活跃用户数量（在指定时间后有发布问题或回答的用户）
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u LEFT JOIN Question q ON u.id = q.author.id " +
           "LEFT JOIN Answer a ON u.id = a.author.id " +
           "WHERE q.createdAt > :date OR a.createdAt > :date")
    long countActiveUsers(@Param("date") LocalDateTime date);
} 