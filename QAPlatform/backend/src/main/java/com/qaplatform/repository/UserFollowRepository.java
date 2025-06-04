package com.qaplatform.repository;

import com.qaplatform.model.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户关注关系数据访问层
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    
    /**
     * 查询用户是否已关注另一个用户
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.following.id = :followingId")
    Optional<UserFollow> findByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    
    /**
     * 统计用户的关注者数量
     */
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following.id = :userId")
    long countFollowersByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户关注的人数
     */
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.id = :userId")
    long countFollowingByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户的关注者列表 - 使用 JOIN FETCH 预加载
     */
    @Query("SELECT uf FROM UserFollow uf JOIN FETCH uf.follower WHERE uf.following.id = :userId ORDER BY uf.createdAt DESC")
    Page<UserFollow> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 获取用户关注的人列表 - 使用 JOIN FETCH 预加载
     */
    @Query("SELECT uf FROM UserFollow uf JOIN FETCH uf.following WHERE uf.follower.id = :userId ORDER BY uf.createdAt DESC")
    Page<UserFollow> findFollowingByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 删除关注关系
     */
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    /**
     * 检查是否存在关注关系
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
} 