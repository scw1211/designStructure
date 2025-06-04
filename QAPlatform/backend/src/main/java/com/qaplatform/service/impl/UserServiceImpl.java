package com.qaplatform.service.impl;

import com.qaplatform.service.UserService;
import com.qaplatform.model.User;
import com.qaplatform.model.UserCheckin;
import com.qaplatform.repository.UserRepository;
import com.qaplatform.repository.UserCheckinRepository;
import com.qaplatform.factory.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserCheckinRepository userCheckinRepository;
    
    @Autowired
    @Qualifier("regularUserFactory")
    private UserFactory regularUserFactory;
    
    @Autowired
    @Qualifier("adminUserFactory")
    private UserFactory adminUserFactory;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public User registerUser(String username, String password, String email, boolean isAdmin) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 使用工厂模式创建用户
        UserFactory factory = isAdmin ? adminUserFactory : regularUserFactory;
        User user = factory.createUser(username, password, email);
        
        // 保存用户
        User savedUser = userRepository.save(user);
        log.info("新用户注册成功: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    @Override
    public User authenticateUser(String username, String password) {
        // 查找用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        // 检查账号是否启用
        if (!user.getEnabled()) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        log.info("用户登录成功: {}", user.getUsername());
        return user;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("用户不存在");
        }
        
        User updatedUser = userRepository.save(user);
        log.info("用户信息更新成功: {}", updatedUser.getUsername());
        
        return updatedUser;
    }
    
    @Override
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setAvatar(avatarUrl);
        return userRepository.save(user);
    }
    
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户 {} 修改密码成功", user.getUsername());
        return true;
    }
    
    @Override
    public User setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        
        log.info("用户 {} 状态更新为: {}", user.getUsername(), enabled ? "启用" : "禁用");
        return updatedUser;
    }
    
    @Override
    public User updateReputation(Long userId, int delta) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        int newReputation = Math.max(0, user.getReputation() + delta);
        user.setReputation(newReputation);
        
        return userRepository.save(user);
    }
    
    @Override
    public List<User> getTopUsers(int limit) {
        return userRepository.findTopByReputation(limit);
    }
    
    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findAll(pageable); // 简化实现，可以后续改进
    }
    
    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        
        userRepository.deleteById(userId);
        log.info("用户ID {} 已被删除", userId);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public User changeUserRole(Long userId, User.UserRole newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        User.UserRole oldRole = user.getRole();
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        
        log.info("用户 {} 角色从 {} 更改为 {}", user.getUsername(), oldRole, newRole);
        return updatedUser;
    }
    
    @Override
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    @Override
    public Map<String, Long> getUserStats() {
        List<User> allUsers = userRepository.findAll();
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", (long) allUsers.size());
        stats.put("adminCount", allUsers.stream().filter(u -> u.getRole() == User.UserRole.ADMIN).count());
        stats.put("moderatorCount", allUsers.stream().filter(u -> u.getRole() == User.UserRole.MODERATOR).count());
        stats.put("regularUserCount", allUsers.stream().filter(u -> u.getRole() == User.UserRole.USER).count());
        stats.put("enabledUsers", allUsers.stream().filter(User::getEnabled).count());
        stats.put("disabledUsers", allUsers.stream().filter(u -> !u.getEnabled()).count());
        
        return stats;
    }
    
    // ================ 签到功能实现 ================
    
    @Override
    @Transactional
    public Map<String, Object> dailyCheckin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        LocalDate today = LocalDate.now();
        log.info("用户 {} (ID:{}) 尝试签到，今天日期: {}", user.getUsername(), userId, today);
        
        // 检查今天是否已签到
        Optional<UserCheckin> todayCheckin = userCheckinRepository.findByUserIdAndCheckinDate(userId, today);
        
        if (todayCheckin.isPresent()) {
            UserCheckin existingCheckin = todayCheckin.get();
            log.warn("用户 {} (ID:{}) 已于 {} 签到，记录ID: {}", 
                    user.getUsername(), userId, existingCheckin.getCheckinDate(), existingCheckin.getId());
            throw new RuntimeException("今天已经签到过了");
        }
        
        // 获取最近的签到记录，计算连续签到天数
        Optional<UserCheckin> lastCheckin = userCheckinRepository.findFirstByUserIdOrderByCheckinDateDesc(userId);
        
        int consecutiveDays = 1;
        
        if (lastCheckin.isPresent()) {
            UserCheckin lastCheckinRecord = lastCheckin.get();
            
            LocalDate lastCheckinDate = lastCheckinRecord.getCheckinDate();
            long daysBetween = ChronoUnit.DAYS.between(lastCheckinDate, today);
            
            if (daysBetween == 1) {
                // 连续签到
                consecutiveDays = lastCheckinRecord.getConsecutiveDays() + 1;
            } else if (daysBetween > 1) {
                // 中断了，重新开始
                consecutiveDays = 1;
            } else if (daysBetween == 0) {
                // 同一天，说明已经签到过了（这种情况已经在上面检查过了）
                log.error("检测到同一天签到，但之前检查显示无记录！userId: {}, today: {}", userId, today);
                throw new RuntimeException("今天已经签到过了");
            }
        }
        
        // 计算奖励声望值（基础10分，连续签到额外奖励）
        int reputationReward = 10;
        if (consecutiveDays >= 7 && consecutiveDays < 30) {
            reputationReward += 5; // 连续7天额外5分
        } else if (consecutiveDays >= 30) {
            reputationReward += 15; // 连续30天额外15分
        }
        
        try {
            // 创建签到记录
            UserCheckin checkin = new UserCheckin();
            checkin.setUser(user);
            checkin.setCheckinDate(today);
            checkin.setConsecutiveDays(consecutiveDays);
            checkin.setReputationEarned(reputationReward);
            
            // 保存签到记录
            UserCheckin savedCheckin = userCheckinRepository.save(checkin);
            
            // 更新用户声望值
            user.setReputation(user.getReputation() + reputationReward);
            userRepository.save(user);
            
            log.info("用户 {} 签到成功，连续{}天，获得{}声望", user.getUsername(), consecutiveDays, reputationReward);
            
            // 返回签到结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "签到成功！");
            result.put("consecutiveDays", consecutiveDays);
            result.put("reputationEarned", reputationReward);
            result.put("totalReputation", user.getReputation());
            result.put("checkinDate", today);
            
            return result;
            
        } catch (Exception e) {
            log.error("签到过程中发生错误，userId: {}, error: {}", userId, e.getMessage(), e);
            // 检查是否是唯一约束冲突
            if (e.getCause() != null && e.getCause().getMessage() != null && 
                e.getCause().getMessage().contains("Duplicate entry")) {
                throw new RuntimeException("今天已经签到过了");
            }
            throw new RuntimeException("签到失败，请稍后重试");
        }
    }
    
    @Override
    public boolean hasCheckinToday(Long userId) {
        LocalDate today = LocalDate.now();
        // 使用与签到逻辑相同的查询方法，确保一致性
        Optional<UserCheckin> todayCheckin = userCheckinRepository.findByUserIdAndCheckinDate(userId, today);
        return todayCheckin.isPresent();
    }
    
    @Override
    public Map<String, Object> getUserCheckinStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 基本统计
        Long totalCheckinDays = userCheckinRepository.getTotalCheckinDaysByUserId(userId);
        Integer maxConsecutiveDays = userCheckinRepository.getMaxConsecutiveDaysByUserId(userId);
        
        // 本月签到天数
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        Long monthlyCheckins = userCheckinRepository.getMonthlyCheckinDays(userId, startOfMonth, endOfMonth);
        
        // 是否今天已签到
        boolean hasCheckedInToday = hasCheckinToday(userId);
        
        // 当前连续签到天数
        int currentConsecutiveDays = 0;
        Optional<UserCheckin> lastCheckin = userCheckinRepository.findFirstByUserIdOrderByCheckinDateDesc(userId);
        if (lastCheckin.isPresent()) {
            LocalDate lastCheckinDate = lastCheckin.get().getCheckinDate();
            LocalDate today = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(lastCheckinDate, today);
            
            if (daysBetween == 0 || (daysBetween == 1 && !hasCheckedInToday)) {
                currentConsecutiveDays = lastCheckin.get().getConsecutiveDays();
            }
        }
        
        stats.put("totalCheckinDays", totalCheckinDays != null ? totalCheckinDays : 0L);
        stats.put("maxConsecutiveDays", maxConsecutiveDays != null ? maxConsecutiveDays : 0);
        stats.put("currentConsecutiveDays", currentConsecutiveDays);
        stats.put("monthlyCheckins", monthlyCheckins != null ? monthlyCheckins : 0L);
        stats.put("hasCheckedInToday", hasCheckedInToday);
        
        return stats;
    }
    
    @Override
    public List<UserCheckin> getUserCheckinHistory(Long userId) {
        return userCheckinRepository.findAllByUserIdOrderByCheckinDateDesc(userId);
    }
    
    @Override
    @Transactional
    public boolean deleteTodayCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<UserCheckin> todayCheckin = userCheckinRepository.findByUserIdAndCheckinDate(userId, today);
        
        if (todayCheckin.isPresent()) {
            UserCheckin checkin = todayCheckin.get();
            int reputationToDeduct = checkin.getReputationEarned();
            
            // 删除签到记录
            userCheckinRepository.delete(checkin);
            
            // 恢复用户声望值
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setReputation(Math.max(0, user.getReputation() - reputationToDeduct));
                userRepository.save(user);
            }
            
            log.info("用户 {} 的今日签到记录已删除，恢复 {} 声望", userId, reputationToDeduct);
            return true;
        }
        
        return false;
    }
} 