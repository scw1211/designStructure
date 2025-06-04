package com.qaplatform.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 在线用户状态管理服务
 */
@Service
public class OnlineUserService {
    
    // 在线用户映射表 <用户ID, 最后活跃时间>
    private final Map<Long, LocalDateTime> onlineUsers = new ConcurrentHashMap<>();
    
    // 定义用户离线时间阈值（5分钟）
    private static final int OFFLINE_THRESHOLD_MINUTES = 5;
    
    // 定时清理离线用户的调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public OnlineUserService() {
        // 每分钟清理一次离线用户
        scheduler.scheduleAtFixedRate(this::cleanupOfflineUsers, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * 更新用户在线状态
     * @param userId 用户ID
     */
    public void updateUserActivity(Long userId) {
        if (userId != null) {
            onlineUsers.put(userId, LocalDateTime.now());
        }
    }
    
    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        
        LocalDateTime lastActivity = onlineUsers.get(userId);
        if (lastActivity == null) {
            return false;
        }
        
        return lastActivity.isAfter(LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES));
    }
    
    /**
     * 获取在线用户数量
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return getOnlineUserIds().size();
    }
    
    /**
     * 获取所有在线用户ID
     * @return 在线用户ID集合
     */
    public Set<Long> getOnlineUserIds() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES);
        return onlineUsers.entrySet().stream()
            .filter(entry -> entry.getValue().isAfter(threshold))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
    
    /**
     * 手动设置用户离线
     * @param userId 用户ID
     */
    public void setUserOffline(Long userId) {
        onlineUsers.remove(userId);
    }
    
    /**
     * 清理离线用户
     */
    private void cleanupOfflineUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES);
        onlineUsers.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
    }
    
    /**
     * 获取在线用户统计信息
     * @return 统计信息
     */
    public Map<String, Object> getOnlineStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("onlineCount", getOnlineUserCount());
        stats.put("onlineUserIds", getOnlineUserIds());
        stats.put("lastUpdate", LocalDateTime.now());
        return stats;
    }
} 