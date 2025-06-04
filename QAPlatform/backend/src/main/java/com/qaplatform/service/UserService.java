package com.qaplatform.service;

import com.qaplatform.model.User;
import com.qaplatform.model.UserCheckin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑
 */
public interface UserService {
    
    /**
     * 注册新用户
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param isAdmin 是否为管理员
     * @return 创建的用户
     */
    User registerUser(String username, String password, String email, boolean isAdmin);
    
    /**
     * 用户认证
     * @param username 用户名
     * @param password 密码
     * @return 认证成功的用户
     */
    User authenticateUser(String username, String password);
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象
     */
    Optional<User> findById(Long id);
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 更新后的用户
     */
    User updateUser(User user);
    
    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 更新后的用户
     */
    User updateAvatar(Long userId, String avatarUrl);
    
    /**
     * 更改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 启用/禁用用户
     * @param userId 用户ID
     * @param enabled 是否启用
     * @return 更新后的用户
     */
    User setUserEnabled(Long userId, boolean enabled);
    
    /**
     * 更新用户声望值
     * @param userId 用户ID
     * @param delta 变化值（可正可负）
     * @return 更新后的用户
     */
    User updateReputation(Long userId, int delta);
    
    /**
     * 获取用户排行榜
     * @param limit 限制数量
     * @return 用户列表
     */
    List<User> getTopUsers(int limit);
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();
    
    /**
     * 搜索用户
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 用户分页
     */
    Page<User> searchUsers(String keyword, Pageable pageable);
    
    /**
     * 删除用户
     * @param userId 用户ID
     */
    void deleteUser(Long userId);
    
    /**
     * 更改用户角色
     * @param userId 用户ID
     * @param newRole 新角色
     * @return 更新后的用户
     */
    User changeUserRole(Long userId, User.UserRole newRole);
    
    /**
     * 根据角色获取用户列表
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> getUsersByRole(User.UserRole role);
    
    /**
     * 获取用户统计信息
     * @return 统计信息Map
     */
    Map<String, Long> getUserStats();
    
    // ================ 签到功能相关方法 ================
    
    /**
     * 用户每日签到
     * @param userId 用户ID
     * @return 签到结果信息
     */
    Map<String, Object> dailyCheckin(Long userId);
    
    /**
     * 检查用户今天是否已经签到
     * @param userId 用户ID
     * @return 是否已签到
     */
    boolean hasCheckinToday(Long userId);
    
    /**
     * 获取用户签到统计信息
     * @param userId 用户ID
     * @return 签到统计信息
     */
    Map<String, Object> getUserCheckinStats(Long userId);
    
    /**
     * 获取用户签到历史记录
     * @param userId 用户ID
     * @return 签到历史记录列表
     */
    List<UserCheckin> getUserCheckinHistory(Long userId);
    
    /**
     * 删除用户今日签到记录（仅用于测试）
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteTodayCheckin(Long userId);
} 