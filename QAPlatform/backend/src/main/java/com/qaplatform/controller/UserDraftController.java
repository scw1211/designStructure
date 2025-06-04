package com.qaplatform.controller;

import com.qaplatform.model.QuestionDraft;
import com.qaplatform.model.User;
import com.qaplatform.repository.QuestionDraftRepository;
import com.qaplatform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户草稿控制器
 * 处理用户草稿相关的HTTP请求
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户草稿管理", description = "用户草稿相关操作")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
@Slf4j
public class UserDraftController {

    @Autowired
    private QuestionDraftRepository draftRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取用户草稿列表
     */
    @GetMapping("/drafts")
    @Operation(summary = "获取用户草稿列表", description = "获取当前用户的所有草稿")
    public ResponseEntity<?> getUserDrafts(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 默认用户ID（演示用）
            Long userId = currentUserId != null ? currentUserId : 1L;
            
            // 从数据库获取用户草稿
            List<QuestionDraft> drafts = draftRepository.findByAuthorIdOrderByUpdatedAtDesc(userId);
            
            // 转换为响应格式
            List<Map<String, Object>> draftList = new ArrayList<>();
            for (QuestionDraft draft : drafts) {
                Map<String, Object> draftMap = new HashMap<>();
                draftMap.put("id", draft.getId());
                draftMap.put("title", draft.getTitle());
                draftMap.put("content", draft.getContent());
                draftMap.put("tags", parseTagsFromJson(draft.getTags()));
                draftMap.put("draftName", draft.getDraftName());
                draftMap.put("autoSaved", draft.getAutoSaved());
                draftMap.put("summary", draft.getSummary());
                draftMap.put("createdAt", draft.getCreatedAt());
                draftMap.put("updatedAt", draft.getUpdatedAt());
                draftList.add(draftMap);
            }
            
            log.info("获取用户 {} 的草稿列表成功，共 {} 个草稿", userId, drafts.size());
            return ResponseEntity.ok(draftList);
            
        } catch (Exception e) {
            log.error("获取用户草稿失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取草稿失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 保存草稿
     */
    @PostMapping("/drafts")
    @Operation(summary = "保存草稿", description = "保存或更新用户草稿")
    public ResponseEntity<?> saveDraft(@RequestBody @Valid DraftRequest request,
                                     @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 默认用户ID（演示用）
            Long userId = currentUserId != null ? currentUserId : 1L;
            
            // 获取用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(error);
            }
            User user = userOpt.get();
            
            QuestionDraft draft;
            
            // 如果有ID，则更新现有草稿
            if (request.getId() != null) {
                Optional<QuestionDraft> existingDraft = draftRepository.findById(request.getId());
                if (existingDraft.isPresent() && existingDraft.get().getAuthor().getId().equals(userId)) {
                    draft = existingDraft.get();
                    draft.setTitle(request.getTitle());
                    draft.setContent(request.getContent());
                    draft.setTags(convertTagsToJson(request.getTags()));
                    draft.setDraftName(request.getDraftName());
                    draft.setAutoSaved(request.getAutoSaved() != null ? request.getAutoSaved() : true);
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "草稿不存在或无权限访问");
                    return ResponseEntity.badRequest().body(error);
                }
            } else {
                // 创建新草稿
                draft = new QuestionDraft();
                draft.setTitle(request.getTitle());
                draft.setContent(request.getContent());
                draft.setTags(convertTagsToJson(request.getTags()));
                draft.setAuthor(user);
                draft.setDraftName(request.getDraftName());
                draft.setAutoSaved(request.getAutoSaved() != null ? request.getAutoSaved() : true);
            }
            
            // 保存到数据库
            QuestionDraft savedDraft = draftRepository.save(draft);
            
            // 返回保存结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "草稿保存成功");
            result.put("draftId", savedDraft.getId());
            result.put("updatedAt", savedDraft.getUpdatedAt());
            
            log.info("用户 {} 保存草稿成功，草稿ID: {}", userId, savedDraft.getId());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "保存草稿失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除草稿
     */
    @DeleteMapping("/drafts/{draftId}")
    @Operation(summary = "删除草稿", description = "删除指定的草稿")
    public ResponseEntity<?> deleteDraft(@PathVariable Long draftId,
                                       @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 默认用户ID（演示用）
            Long userId = currentUserId != null ? currentUserId : 1L;
            
            Optional<QuestionDraft> draftOpt = draftRepository.findById(draftId);
            if (!draftOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "草稿不存在");
                return ResponseEntity.notFound().build();
            }
            
            QuestionDraft draft = draftOpt.get();
            if (!draft.getAuthor().getId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "无权限删除此草稿");
                return ResponseEntity.badRequest().body(error);
            }
            
            draftRepository.delete(draft);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "草稿删除成功");
            
            log.info("用户 {} 删除草稿成功，草稿ID: {}", userId, draftId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("删除草稿失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "删除草稿失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取单个草稿详情
     */
    @GetMapping("/drafts/{draftId}")
    @Operation(summary = "获取草稿详情", description = "获取指定草稿的详细信息")
    public ResponseEntity<?> getDraft(@PathVariable Long draftId,
                                    @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 默认用户ID（演示用）
            Long userId = currentUserId != null ? currentUserId : 1L;
            
            Optional<QuestionDraft> draftOpt = draftRepository.findById(draftId);
            if (!draftOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "草稿不存在");
                return ResponseEntity.notFound().build();
            }
            
            QuestionDraft draft = draftOpt.get();
            if (!draft.getAuthor().getId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "无权限访问此草稿");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> draftMap = new HashMap<>();
            draftMap.put("id", draft.getId());
            draftMap.put("title", draft.getTitle());
            draftMap.put("content", draft.getContent());
            draftMap.put("tags", parseTagsFromJson(draft.getTags()));
            draftMap.put("draftName", draft.getDraftName());
            draftMap.put("autoSaved", draft.getAutoSaved());
            draftMap.put("createdAt", draft.getCreatedAt());
            draftMap.put("updatedAt", draft.getUpdatedAt());
            
            return ResponseEntity.ok(draftMap);
            
        } catch (Exception e) {
            log.error("获取草稿详情失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取草稿详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 清理空草稿
     */
    @DeleteMapping("/drafts/cleanup")
    @Operation(summary = "清理空草稿", description = "删除用户的所有空草稿")
    public ResponseEntity<?> cleanupEmptyDrafts(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 默认用户ID（演示用）
            Long userId = currentUserId != null ? currentUserId : 1L;
            
            draftRepository.deleteEmptyDrafts(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "空草稿清理完成");
            
            log.info("用户 {} 清理空草稿完成", userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("清理空草稿失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "清理空草稿失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 将标签列表转换为JSON字符串
     */
    private String convertTagsToJson(List<String> tags) {
        try {
            if (tags == null || tags.isEmpty()) {
                return "[]";
            }
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            log.warn("转换标签为JSON失败", e);
            return "[]";
        }
    }

    /**
     * 将JSON字符串解析为标签列表
     */
    private List<String> parseTagsFromJson(String tagsJson) {
        try {
            if (tagsJson == null || tagsJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(tagsJson, List.class);
        } catch (Exception e) {
            log.warn("解析标签JSON失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 草稿请求DTO
     */
    @Data
    public static class DraftRequest {
        private Long id;  // 草稿ID（更新时使用）
        private String title;
        private String content;
        private List<String> tags;
        private String draftName;
        private Boolean autoSaved;
    }
} 