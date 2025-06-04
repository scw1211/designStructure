package com.qaplatform.controller;

import com.qaplatform.model.Question;
import com.qaplatform.service.QuestionService;
import com.qaplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 问题控制器
 * 处理问题相关的HTTP请求
 */
@RestController
@RequestMapping("/questions")
@Tag(name = "问题管理", description = "问题相关操作")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
@Transactional(readOnly = true)
@Slf4j
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取问题列表
     */
    @GetMapping
    @Operation(summary = "获取问题列表")
    public ResponseEntity<?> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // 获取所有问题
            Page<Question> questions = questionService.getAllQuestions(pageable);
            
            // 创建简化的响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("content", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("first", questions.isFirst());
            response.put("last", questions.isLast());
            response.put("numberOfElements", questions.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取问题列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 根据ID获取问题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取问题详情")
    public ResponseEntity<Question> getQuestion(@PathVariable Long id) {
        Optional<Question> question = questionService.findById(id);
        if (question.isPresent()) {
            // 增加浏览次数
            questionService.incrementViewCount(id);
            return ResponseEntity.ok(question.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 创建新问题
     */
    @PostMapping
    @Operation(summary = "创建问题")
    @Transactional
    public ResponseEntity<?> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        try {
            Question question = questionService.createQuestion(
                request.getTitle(),
                request.getContent(),
                request.getAuthorId(),
                request.getTagNames(),
                null  // 暂时不处理图片
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "问题创建成功");
            response.put("question", question);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "问题创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 搜索问题
     */
    @GetMapping("/search")
    @Operation(summary = "搜索问题")
    public ResponseEntity<Page<Question>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "FULL_TEXT") String strategy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questions = questionService.searchQuestions(keyword, strategy, pageable);
        return ResponseEntity.ok(questions);
    }
    
    /**
     * 获取热门问题
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门问题")
    public ResponseEntity<Page<Question>> getHotQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questions = questionService.getHotQuestions(pageable);
        return ResponseEntity.ok(questions);
    }
    
    /**
     * 获取未回答的问题
     */
    @GetMapping("/unanswered")
    @Operation(summary = "获取未回答的问题")
    public ResponseEntity<Page<Question>> getUnansweredQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questions = questionService.getUnansweredQuestions(pageable);
        return ResponseEntity.ok(questions);
    }
    
    /**
     * 问题投票
     */
    @PostMapping("/{id}/vote")
    @Operation(summary = "问题投票")
    @Transactional
    public ResponseEntity<?> voteQuestion(
            @PathVariable Long id,
            @RequestBody VoteRequest request,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // 检查是否是自己的问题
            if (questionService.isQuestionAuthor(id, currentUserId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "不能给自己的问题投票");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 处理投票
            Map<String, Object> result = questionService.voteQuestion(id, currentUserId, request.getVote());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result.get("message"));
            response.put("voteCount", result.get("voteCount"));
            response.put("userVote", result.get("userVote"));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "投票失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取问题投票状态
     */
    @GetMapping("/{id}/vote")
    @Operation(summary = "获取问题投票状态")
    public ResponseEntity<?> getQuestionVoteStatus(
            @PathVariable Long id,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            Map<String, Object> status = questionService.getVoteStatus(id, currentUserId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取投票状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 关注/取消关注问题
     */
    @PostMapping("/{id}/follow")
    @Operation(summary = "关注问题", description = "用户关注或取消关注某个问题")
    public ResponseEntity<Map<String, Object>> toggleFollowQuestion(
            @PathVariable Long id,
            @RequestParam boolean follow) {
        
        try {
            Long userId = 1L; // 模拟当前用户ID，实际应从认证中获取
            
            Question question = questionService.toggleFollow(id, userId, follow);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("followed", follow);
            response.put("followersCount", question.getFollowers().size());
            response.put("message", follow ? "关注成功" : "取消关注成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("关注操作失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取问题关注者列表
     */
    @GetMapping("/{id}/followers")
    @Operation(summary = "获取问题关注者", description = "获取关注该问题的用户列表")
    public ResponseEntity<List<Map<String, Object>>> getQuestionFollowers(@PathVariable Long id) {
        try {
            Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("问题不存在"));
            
            List<Map<String, Object>> followers = question.getFollowers().stream()
                .map(user -> {
                    Map<String, Object> followerInfo = new HashMap<>();
                    followerInfo.put("id", user.getId());
                    followerInfo.put("username", user.getUsername());
                    followerInfo.put("avatar", user.getAvatar());
                    followerInfo.put("reputation", user.getReputation());
                    return followerInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            log.error("获取关注者列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加问题浏览次数
     */
    @PostMapping("/{id}/view")
    @Operation(summary = "增加浏览次数", description = "用户查看问题时增加浏览次数")
    @Transactional
    public ResponseEntity<Map<String, Object>> incrementViewCount(@PathVariable Long id) {
        try {
            questionService.incrementViewCount(id);
            
            Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("问题不存在"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("viewCount", question.getViewCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("增加浏览次数失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查用户是否关注了问题
     */
    @GetMapping("/{id}/follow-status")
    @Operation(summary = "检查关注状态", description = "检查当前用户是否关注了该问题")
    public ResponseEntity<Map<String, Object>> getFollowStatus(@PathVariable Long id) {
        try {
            Long userId = 1L; // 模拟当前用户ID
            
            Question question = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("问题不存在"));
            
            boolean isFollowing = question.getFollowers().stream()
                .anyMatch(user -> user.getId().equals(userId));
            
            Map<String, Object> response = new HashMap<>();
            response.put("isFollowing", isFollowing);
            response.put("followersCount", question.getFollowers().size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查关注状态失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("isFollowing", false);
            response.put("followersCount", 0);
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 创建问题请求DTO
     */
    @Data
    public static class CreateQuestionRequest {
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题长度不能超过200字符")
        private String title;
        
        @NotBlank(message = "内容不能为空")
        private String content;
        
        private Long authorId;
        
        private List<String> tagNames;
    }
    
    /**
     * 投票请求DTO
     */
    @Data
    public static class VoteRequest {
        private int vote; // 1 表示点赞, -1 表示踩, 0 表示取消投票
    }
} 