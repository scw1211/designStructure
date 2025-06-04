package com.qaplatform.controller;

import com.qaplatform.model.Answer;
import com.qaplatform.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
@CrossOrigin(origins = "*")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @GetMapping("/{questionId}/answers")
    public ResponseEntity<Page<Answer>> getAnswersByQuestion(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "newest") String sort,
            Pageable pageable) {
        Page<Answer> answers = answerService.getAnswersByQuestion(questionId, sort, pageable);
        return ResponseEntity.ok(answers);
    }

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<?> createAnswer(
            @PathVariable Long questionId,
            @RequestBody CreateAnswerRequest request) {
        try {
            Answer createdAnswer = answerService.createAnswer(questionId, request.getContent(), request.getAuthorId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "回答发布成功");
            response.put("answer", createdAnswer);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "回答发布失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/answers/{answerId}/vote")
    public ResponseEntity<?> voteAnswer(
            @PathVariable Long answerId,
            @RequestBody VoteRequest request,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // 检查是否是自己的回答
            if (answerService.isAnswerAuthor(answerId, currentUserId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "不能给自己的回答投票");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 处理投票
            Map<String, Object> result = answerService.voteAnswer(answerId, currentUserId, request.getVote());
            
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

    @PostMapping("/{questionId}/accept")
    public ResponseEntity<?> acceptAnswer(
            @PathVariable Long questionId,
            @RequestBody AcceptAnswerRequest request,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // 检查是否是问题作者
            if (!answerService.isQuestionAuthor(questionId, currentUserId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "只有问题作者才能采纳回答");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // 采纳回答
            answerService.acceptAnswer(questionId, request.getAnswerId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "回答已采纳");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "采纳失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/answers/{answerId}/vote-status")
    public ResponseEntity<?> getVoteStatus(
            @PathVariable Long answerId,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            Map<String, Object> status = answerService.getVoteStatus(answerId, currentUserId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取投票状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public static class VoteRequest {
        private int vote;
        
        public int getVote() { return vote; }
        public void setVote(int vote) { this.vote = vote; }
    }

    public static class AcceptAnswerRequest {
        private Long answerId;
        
        public Long getAnswerId() { return answerId; }
        public void setAnswerId(Long answerId) { this.answerId = answerId; }
    }

    public static class CreateAnswerRequest {
        private String content;
        private Long authorId;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getAuthorId() { return authorId; }
        public void setAuthorId(Long authorId) { this.authorId = authorId; }
    }
} 