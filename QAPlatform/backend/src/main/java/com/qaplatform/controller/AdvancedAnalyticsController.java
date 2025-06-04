package com.qaplatform.controller;

import com.qaplatform.model.*;
import com.qaplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "http://localhost:5174")
public class AdvancedAnalyticsController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private AnswerRepository answerRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private UserBadgeRepository userBadgeRepository;

    // 平台概览统计
    @GetMapping("/platform-overview")
    public ResponseEntity<Map<String, Object>> getPlatformOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 基础统计
        long totalUsers = userRepository.count();
        long totalQuestions = questionRepository.count();
        long totalAnswers = answerRepository.count();
        long totalTags = tagRepository.count();
        
        // 今日统计
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayQuestions = questionRepository.countByCreatedAtAfter(today);
        long todayAnswers = answerRepository.countByCreatedAtAfter(today);
        long todayUsers = userRepository.countByCreatedAtAfter(today);
        
        // 活跃度统计
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long activeUsers = userRepository.countActiveUsers(weekAgo);
        double answerRate = totalQuestions > 0 ? (double) totalAnswers / totalQuestions : 0;
        
        overview.put("totalUsers", totalUsers);
        overview.put("totalQuestions", totalQuestions);
        overview.put("totalAnswers", totalAnswers);
        overview.put("totalTags", totalTags);
        overview.put("todayQuestions", todayQuestions);
        overview.put("todayAnswers", todayAnswers);
        overview.put("todayUsers", todayUsers);
        overview.put("activeUsers", activeUsers);
        overview.put("answerRate", Math.round(answerRate * 100.0) / 100.0);
        
        return ResponseEntity.ok(overview);
    }

    // 专家用户列表
    @GetMapping("/expert-users")
    public ResponseEntity<List<Map<String, Object>>> getExpertUsers(
            @RequestParam(defaultValue = "3") int limit) {
        
        List<User> topUsers = userRepository.findTopUsersByReputation(PageRequest.of(0, limit));
        
        List<Map<String, Object>> experts = topUsers.stream().map(user -> {
            Map<String, Object> expert = new HashMap<>();
            expert.put("id", user.getId());
            expert.put("username", user.getUsername());
            expert.put("avatar", user.getAvatar());
            expert.put("reputation", user.getReputation());
            expert.put("bio", user.getBio());
            
            // 获取用户统计
            long questionCount = questionRepository.countByAuthorId(user.getId());
            long answerCount = answerRepository.countByAuthorId(user.getId());
            long acceptedAnswers = answerRepository.countByAuthorIdAndAccepted(user.getId(), true);
            
            expert.put("questionCount", questionCount);
            expert.put("answerCount", answerCount);
            expert.put("acceptedAnswers", acceptedAnswers);
            expert.put("acceptanceRate", answerCount > 0 ? Math.round((double) acceptedAnswers / answerCount * 100.0) / 100.0 : 0);
            
            return expert;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(experts);
    }

    // 热门问题推荐
    @GetMapping("/trending-questions")
    public ResponseEntity<List<Map<String, Object>>> getTrendingQuestions(
            @RequestParam(defaultValue = "10") int limit) {
        
        // 获取最近7天的热门问题（按浏览量和回答数排序）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Question> trendingQuestions = questionRepository.findTrendingQuestions(weekAgo, PageRequest.of(0, limit));
        
        List<Map<String, Object>> trending = trendingQuestions.stream().map(question -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", question.getId());
            item.put("title", question.getTitle());
            item.put("content", question.getContent().length() > 200 ? 
                    question.getContent().substring(0, 200) + "..." : question.getContent());
            item.put("viewCount", question.getViewCount());
            item.put("answerCount", question.getAnswerCount());
            item.put("createdAt", question.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
            // 获取作者信息
            if (question.getAuthor() != null) {
                Map<String, Object> author = new HashMap<>();
                author.put("id", question.getAuthor().getId());
                author.put("username", question.getAuthor().getUsername());
                author.put("avatar", question.getAuthor().getAvatar());
                item.put("author", author);
            }
            
            // 获取标签
            if (question.getTags() != null) {
                List<Map<String, Object>> tags = question.getTags().stream().map(tag -> {
                    Map<String, Object> tagMap = new HashMap<>();
                    tagMap.put("id", tag.getId());
                    tagMap.put("name", tag.getName());
                    return tagMap;
                }).collect(Collectors.toList());
                item.put("tags", tags);
            }
            
            return item;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(trending);
    }

    // 标签统计分析
    @GetMapping("/tag-statistics")
    public ResponseEntity<List<Map<String, Object>>> getTagStatistics(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<Tag> popularTags = tagRepository.findTopTags(PageRequest.of(0, limit));
        
        List<Map<String, Object>> tagStats = popularTags.stream().map(tag -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("id", tag.getId());
            stat.put("name", tag.getName());
            stat.put("description", tag.getDescription());
            stat.put("usageCount", tag.getUsageCount());
            
            // 获取该标签的问题统计
            long questionCount = questionRepository.countByTagsContaining(tag);
            long answeredQuestions = questionRepository.countAnsweredQuestionsByTag(tag.getId());
            double answerRate = questionCount > 0 ? (double) answeredQuestions / questionCount : 0;
            
            stat.put("questionCount", questionCount);
            stat.put("answeredQuestions", answeredQuestions);
            stat.put("answerRate", Math.round(answerRate * 100.0) / 100.0);
            
            return stat;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(tagStats);
    }

    // 徽章统计
    @GetMapping("/badge-statistics")
    public ResponseEntity<Map<String, Object>> getBadgeStatistics() {
        Map<String, Object> badgeStats = new HashMap<>();
        
        // 徽章类型分布
        Map<String, Integer> badgeTypeCount = new HashMap<>();
        badgeTypeCount.put("BRONZE", 0);
        badgeTypeCount.put("SILVER", 0);
        badgeTypeCount.put("GOLD", 0);
        
        // 获取所有用户徽章并分类统计
        List<UserBadge> allBadges = userBadgeRepository.findAll();
        Map<String, Long> badgeCount = allBadges.stream()
                .collect(Collectors.groupingBy(badge -> {
                    // 根据徽章名称判断类型
                    String badgeName = badge.getBadgeName().toLowerCase();
                    if (badgeName.contains("gold") || badgeName.contains("expert") || badgeName.contains("guru")) {
                        return "GOLD";
                    } else if (badgeName.contains("silver") || badgeName.contains("good") || badgeName.contains("nice")) {
                        return "SILVER";
                    } else {
                        return "BRONZE";
                    }
                }, Collectors.counting()));
        
        badgeTypeCount.put("BRONZE", badgeCount.getOrDefault("BRONZE", 0L).intValue());
        badgeTypeCount.put("SILVER", badgeCount.getOrDefault("SILVER", 0L).intValue());
        badgeTypeCount.put("GOLD", badgeCount.getOrDefault("GOLD", 0L).intValue());
        
        // 最受欢迎的徽章
        List<Map<String, Object>> popularBadges = allBadges.stream()
                .collect(Collectors.groupingBy(UserBadge::getBadgeName, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> badge = new HashMap<>();
                    badge.put("name", entry.getKey());
                    badge.put("count", entry.getValue());
                    return badge;
                })
                .collect(Collectors.toList());
        
        // 拥有徽章最多的用户
        List<Map<String, Object>> topBadgeUsers = allBadges.stream()
                .collect(Collectors.groupingBy(UserBadge::getUserId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> user = new HashMap<>();
                    Optional<User> userOpt = userRepository.findById(entry.getKey());
                    if (userOpt.isPresent()) {
                        User u = userOpt.get();
                        user.put("id", u.getId());
                        user.put("username", u.getUsername());
                        user.put("avatar", u.getAvatar());
                        user.put("badgeCount", entry.getValue());
                    }
                    return user;
                })
                .filter(user -> user.containsKey("username"))
                .collect(Collectors.toList());
        
        badgeStats.put("totalBadges", allBadges.size());
        badgeStats.put("badgeTypeDistribution", badgeTypeCount);
        badgeStats.put("popularBadges", popularBadges);
        badgeStats.put("topBadgeUsers", topBadgeUsers);
        
        return ResponseEntity.ok(badgeStats);
    }

    // 活动时间线
    @GetMapping("/activity-timeline")
    public ResponseEntity<List<Map<String, Object>>> getActivityTimeline(
            @RequestParam(defaultValue = "7") int days) {
        
        List<Map<String, Object>> timeline = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            Map<String, Object> dayActivity = new HashMap<>();
            dayActivity.put("date", dayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            // 统计当天活动
            long questions = questionRepository.countByCreatedAtBetween(dayStart, dayEnd);
            long answers = answerRepository.countByCreatedAtBetween(dayStart, dayEnd);
            long newUsers = userRepository.countByCreatedAtBetween(dayStart, dayEnd);
            
            dayActivity.put("questions", questions);
            dayActivity.put("answers", answers);
            dayActivity.put("newUsers", newUsers);
            dayActivity.put("totalActivity", questions + answers + newUsers);
            
            timeline.add(dayActivity);
        }
        
        return ResponseEntity.ok(timeline);
    }
    
    // 用户统计信息
    @GetMapping("/user-statistics/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable Long userId) {
        Map<String, Object> userStats = new HashMap<>();
        
        try {
            // 获取用户基本信息
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // 统计用户数据
            long totalQuestions = questionRepository.countByAuthorId(userId);
            long totalAnswers = answerRepository.countByAuthorId(userId);
            long acceptedAnswers = answerRepository.countByAuthorIdAndAccepted(userId, true);
            long badgesCount = userBadgeRepository.countByUserId(userId);
            
            // 计算接受率
            double acceptanceRate = totalAnswers > 0 ? (double) acceptedAnswers / totalAnswers * 100 : 0;
            
            userStats.put("userId", userId);
            userStats.put("username", user.getUsername());
            userStats.put("reputation", user.getReputation());
            userStats.put("totalQuestions", totalQuestions);
            userStats.put("totalAnswers", totalAnswers);
            userStats.put("acceptedAnswers", acceptedAnswers);
            userStats.put("acceptanceRate", Math.round(acceptanceRate * 100.0) / 100.0);
            userStats.put("badgesCount", badgesCount);
            userStats.put("joinDate", user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            return ResponseEntity.ok(userStats);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 排行榜
    @GetMapping("/leaderboard/{type}")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @PathVariable String type,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<User> users;
        
        switch (type.toLowerCase()) {
            case "reputation":
                users = userRepository.findTopUsersByReputation(PageRequest.of(0, limit));
                break;
            case "answers":
                // 按回答数排序 - 使用基础查询
                users = userRepository.findAll().stream()
                    .sorted((a, b) -> {
                        long aAnswers = answerRepository.countByAuthorId(a.getId());
                        long bAnswers = answerRepository.countByAuthorId(b.getId());
                        return Long.compare(bAnswers, aAnswers);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
                break;
            case "questions":
                // 按问题数排序 - 使用基础查询
                users = userRepository.findAll().stream()
                    .sorted((a, b) -> {
                        long aQuestions = questionRepository.countByAuthorId(a.getId());
                        long bQuestions = questionRepository.countByAuthorId(b.getId());
                        return Long.compare(bQuestions, aQuestions);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
                break;
            default:
                users = userRepository.findTopUsersByReputation(PageRequest.of(0, limit));
        }
        
        List<Map<String, Object>> leaderboard = users.stream().map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("reputation", user.getReputation());
            userInfo.put("bio", user.getBio());
            
            // 根据排行榜类型添加相应统计
            switch (type.toLowerCase()) {
                case "answers":
                    userInfo.put("totalAnswers", answerRepository.countByAuthorId(user.getId()));
                    break;
                case "questions":
                    userInfo.put("totalQuestions", questionRepository.countByAuthorId(user.getId()));
                    break;
                default:
                    userInfo.put("totalQuestions", questionRepository.countByAuthorId(user.getId()));
                    userInfo.put("totalAnswers", answerRepository.countByAuthorId(user.getId()));
            }
            
            return userInfo;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(leaderboard);
    }
} 