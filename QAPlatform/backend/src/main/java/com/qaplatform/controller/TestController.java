package com.qaplatform.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class TestController {

    @GetMapping("/questions")
    public Map<String, Object> getTestQuestions() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // 创建测试问题数据
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> question = new HashMap<>();
            question.put("id", i);
            question.put("title", "测试问题 " + i);
            question.put("content", "这是第 " + i + " 个测试问题的内容");
            question.put("viewCount", 10 + i);
            question.put("answerCount", i);
            question.put("solved", i % 2 == 0);
            question.put("createdAt", "2024-01-0" + i + "T10:00:00");
            
            Map<String, Object> author = new HashMap<>();
            author.put("id", i);
            author.put("username", "用户" + i);
            author.put("avatar", "/api/uploads/avatars/default.jpg");
            question.put("author", author);
            
            List<Map<String, Object>> tags = new ArrayList<>();
            Map<String, Object> tag = new HashMap<>();
            tag.put("id", i);
            tag.put("name", i % 2 == 0 ? "Java" : "Spring");
            tags.add(tag);
            question.put("tags", tags);
            
            questions.add(question);
        }
        
        response.put("content", questions);
        response.put("totalElements", 5);
        response.put("totalPages", 1);
        response.put("size", 5);
        response.put("number", 0);
        
        return response;
    }
    
    @GetMapping("/stats")
    public Map<String, Object> getTestStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuestions", 5);
        stats.put("totalAnswers", 10);
        stats.put("totalUsers", 3);
        stats.put("totalTags", 8);
        return stats;
    }
    
    @GetMapping("/tags")
    public List<Map<String, Object>> getTestTags() {
        List<Map<String, Object>> tags = new ArrayList<>();
        String[] tagNames = {"Java", "Spring", "MySQL", "Vue", "JavaScript", "Python", "React", "Node.js"};
        
        for (int i = 0; i < tagNames.length; i++) {
            Map<String, Object> tag = new HashMap<>();
            tag.put("id", i + 1);
            tag.put("name", tagNames[i]);
            tag.put("description", tagNames[i] + "相关问题");
            tag.put("usageCount", 5 + i);
            tags.add(tag);
        }
        
        return tags;
    }
} 