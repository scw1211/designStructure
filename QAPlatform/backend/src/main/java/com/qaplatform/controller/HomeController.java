package com.qaplatform.controller;

import com.qaplatform.repository.QuestionRepository;
import com.qaplatform.repository.UserRepository;
import com.qaplatform.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/")
@io.swagger.v3.oas.annotations.tags.Tag(name = "首页", description = "首页相关操作")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class HomeController {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取统计数据")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalQuestions", questionRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalAnswers", 0L); // 暂时设为0，等Answer实体创建后再更新
        stats.put("totalTags", tagRepository.count());
        return ResponseEntity.ok(stats);
    }
} 