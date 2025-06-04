package com.qaplatform.repository;

import com.qaplatform.model.UserCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 用户签到记录Repository接口
 */
@Repository
public interface UserCheckinRepository extends JpaRepository<UserCheckin, Long> {
    
    /**
     * 根据用户ID和签到日期查找签到记录
     * @param userId 用户ID
     * @param checkinDate 签到日期
     * @return 签到记录
     */
    @Query("SELECT uc FROM UserCheckin uc WHERE uc.user.id = :userId AND uc.checkinDate = :checkinDate")
    Optional<UserCheckin> findByUserIdAndCheckinDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 检查用户今天是否已经签到
     * @param userId 用户ID
     * @param today 今天日期
     * @return 是否已签到
     */
    @Query("SELECT COUNT(uc) > 0 FROM UserCheckin uc WHERE uc.user.id = :userId AND uc.checkinDate = :today")
    boolean existsByUserIdAndToday(@Param("userId") Long userId, @Param("today") LocalDate today);
    
    /**
     * 获取用户最近的签到记录
     * @param userId 用户ID
     * @return 最近的签到记录
     */
    Optional<UserCheckin> findFirstByUserIdOrderByCheckinDateDesc(Long userId);
    
    /**
     * 获取用户的所有签到记录
     * @param userId 用户ID
     * @return 签到记录列表
     */
    @Query("SELECT uc FROM UserCheckin uc WHERE uc.user.id = :userId ORDER BY uc.checkinDate DESC")
    List<UserCheckin> findAllByUserIdOrderByCheckinDateDesc(@Param("userId") Long userId);
    
    /**
     * 获取用户的连续签到天数
     * @param userId 用户ID
     * @return 连续签到天数
     */
    @Query("SELECT COALESCE(MAX(uc.consecutiveDays), 0) FROM UserCheckin uc WHERE uc.user.id = :userId")
    Integer getMaxConsecutiveDaysByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户总签到天数
     * @param userId 用户ID
     * @return 总签到天数
     */
    @Query("SELECT COUNT(uc) FROM UserCheckin uc WHERE uc.user.id = :userId")
    Long getTotalCheckinDaysByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户本月签到天数
     * @param userId 用户ID
     * @param startOfMonth 月初日期
     * @param endOfMonth 月末日期
     * @return 本月签到天数
     */
    @Query("SELECT COUNT(uc) FROM UserCheckin uc WHERE uc.user.id = :userId AND uc.checkinDate BETWEEN :startOfMonth AND :endOfMonth")
    Long getMonthlyCheckinDays(@Param("userId") Long userId, @Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);
    
    /**
     * 删除指定日期的所有签到记录
     * @param checkinDate 签到日期
     * @return 删除的记录数量
     */
    @Modifying
    @Query("DELETE FROM UserCheckin uc WHERE uc.checkinDate = :checkinDate")
    int deleteByCheckinDate(@Param("checkinDate") LocalDate checkinDate);
} 