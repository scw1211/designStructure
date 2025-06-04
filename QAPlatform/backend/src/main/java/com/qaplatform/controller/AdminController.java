package com.qaplatform.controller;

import com.qaplatform.model.User;
import com.qaplatform.model.Question;
import com.qaplatform.model.Answer;
import com.qaplatform.service.UserService;
import com.qaplatform.service.QuestionService;
import com.qaplatform.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 处理管理员权限相关的功能
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员功能", description = "管理员权限相关的API接口")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
@Slf4j
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private AnswerService answerService;
    
    // ========== 用户管理 ==========
    
    /**
     * 获取所有用户（管理员视图）
     */
    @GetMapping("/users")
    @Operation(summary = "获取所有用户", description = "管理员获取所有用户列表，包含详细信息")
    public ResponseEntity<Page<User>> getAllUsersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String role) {
        
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<User> users;
            if (!search.isEmpty()) {
                users = userService.searchUsers(search, pageable);
            } else {
                users = userService.searchUsers("", pageable);
            }
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 更改用户角色
     */
    @PutMapping("/users/{userId}/role")
    @Operation(summary = "更改用户角色", description = "管理员和版主可以更改用户角色")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeRoleRequest request) {
        
        try {
            // 更新角色
            User.UserRole newRole = User.UserRole.valueOf(request.getNewRole().toUpperCase());
            User updatedUser = userService.changeUserRole(userId, newRole);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色更新成功");
            response.put("user", updatedUser);
            
            log.info("用户角色更新: {} -> {}", updatedUser.getUsername(), newRole);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更改用户角色失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 启用/禁用用户
     */
    @PutMapping("/users/{userId}/status")
    @Operation(summary = "启用/禁用用户", description = "管理员可以启用或禁用用户账号")
    public ResponseEntity<?> changeUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeStatusRequest request) {
        
        try {
            // 更新状态
            User updatedUser = userService.setUserEnabled(userId, request.getEnabled());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", request.getEnabled() ? "用户已启用" : "用户已禁用");
            response.put("user", updatedUser);
            
            log.info("用户状态更新: {} -> {}", 
                updatedUser.getUsername(), request.getEnabled() ? "启用" : "禁用");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更改用户状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "管理员可以删除用户")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        
        try {
            // 删除用户
            userService.deleteUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户已删除");
            
            log.info("用户删除: ID {}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除用户失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    // ========== 问题管理 ==========
    
    /**
     * 获取所有问题（管理员视图）
     */
    @GetMapping("/questions")
    @Operation(summary = "获取所有问题", description = "管理员获取所有问题列表")
    public ResponseEntity<Page<Question>> getAllQuestionsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status) {
        
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Question> questions;
            if (!search.isEmpty()) {
                questions = questionService.searchQuestions(search, "all", pageable);
            } else {
                questions = questionService.getAllQuestions(pageable);
            }
            
            return ResponseEntity.ok(questions);
            
        } catch (Exception e) {
            log.error("获取问题列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 关闭/开启问题
     */
    @PutMapping("/questions/{questionId}/status")
    @Operation(summary = "关闭/开启问题", description = "管理员可以关闭或开启问题")
    public ResponseEntity<?> changeQuestionStatus(
            @PathVariable Long questionId,
            @Valid @RequestBody ChangeQuestionStatusRequest request) {
        
        try {
            Question question = questionService.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));
            
            Question updatedQuestion;
            if (request.getClosed()) {
                // 关闭问题 - 需要传入管理员用户ID，这里暂时使用1
                updatedQuestion = questionService.closeQuestion(questionId, 1L);
            } else {
                // 开启问题 - 使用已实现的reopenQuestion方法
                updatedQuestion = questionService.reopenQuestion(questionId, 1L);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", request.getClosed() ? "问题已关闭" : "问题已开启");
            response.put("question", updatedQuestion);
            
            log.info("问题状态更新: ID {} -> {}", questionId, request.getClosed() ? "关闭" : "开启");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更改问题状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 删除问题
     */
    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "删除问题", description = "管理员可以删除问题")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        
        try {
            // 删除问题 - 需要传入管理员用户ID，这里暂时使用1
            questionService.deleteQuestion(questionId, 1L);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "问题已删除");
            
            log.info("问题删除: ID {}", questionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除问题失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    // ========== 回答管理 ==========
    
    /**
     * 获取所有回答（管理员视图）
     */
    @GetMapping("/answers")
    @Operation(summary = "获取所有回答", description = "管理员获取所有回答列表")
    public ResponseEntity<?> getAllAnswersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Long questionId) {
        
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Answer> answers;
            if (questionId != null) {
                answers = answerService.getAnswersByQuestion(questionId, sortBy, pageable);
            } else {
                // 获取所有回答 - 使用已实现的getAllAnswers方法
                answers = answerService.getAllAnswers(pageable);
            }
            
            return ResponseEntity.ok(answers);
            
        } catch (Exception e) {
            log.error("获取回答列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除回答
     */
    @DeleteMapping("/answers/{answerId}")
    @Operation(summary = "删除回答", description = "管理员可以删除回答")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long answerId) {
        
        try {
            // 使用已实现的deleteAnswer方法
            answerService.deleteAnswer(answerId, 1L);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "回答已删除");
            
            log.info("回答删除: ID {}", answerId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除回答失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    // ========== 系统统计 ==========
    
    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取系统统计", description = "获取系统运行统计信息")
    public ResponseEntity<?> getSystemStats() {
        try {
            Map<String, Long> userStats = userService.getUserStats();
            
            // 添加问题和回答的统计信息
            Map<String, Object> allStats = new HashMap<>(userStats);
            
            // 获取问题统计
            Map<String, Long> questionStats = questionService.getQuestionStats();
            allStats.putAll(questionStats);
            
            // 获取回答统计
            Map<String, Long> answerStats = answerService.getAnswerStats();
            allStats.putAll(answerStats);
            
            return ResponseEntity.ok(allStats);
            
        } catch (Exception e) {
            log.error("获取系统统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ========== 请求对象类 ==========
    
    @Data
    public static class ChangeRoleRequest {
        @NotBlank(message = "新角色不能为空")
        private String newRole;
        
        private String reason; // 可选的操作原因
    }
    
    @Data
    public static class ChangeStatusRequest {
        @NotNull(message = "状态不能为空")
        private Boolean enabled;
        
        private String reason; // 可选的操作原因
    }
    
    @Data
    public static class ChangeQuestionStatusRequest {
        @NotNull(message = "状态不能为空")
        private Boolean closed;
        
        private String reason; // 可选的操作原因
    }
} 