package com.qaplatform.controller;

import com.qaplatform.model.QuestionImage;
import com.qaplatform.service.QuestionImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 问题图片控制器
 */
@RestController
@RequestMapping("/questions")
@CrossOrigin(origins = "*")
@Tag(name = "问题图片管理", description = "问题图片上传、获取、删除等接口")
@Slf4j
public class QuestionImageController {
    
    @Autowired
    private QuestionImageService questionImageService;
    
    /**
     * 上传问题图片
     */
    @PostMapping("/{questionId}/images")
    @Operation(summary = "上传问题图片")
    public ResponseEntity<?> uploadQuestionImage(
            @PathVariable Long questionId,
            @RequestParam("file") MultipartFile file) {
        try {
            QuestionImage uploadedImage = questionImageService.uploadQuestionImage(questionId, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "图片上传成功");
            response.put("image", createImageResponse(uploadedImage));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("上传问题图片失败: questionId={}, error={}", questionId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "图片上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 批量上传问题图片
     */
    @PostMapping("/{questionId}/images/batch")
    @Operation(summary = "批量上传问题图片")
    public ResponseEntity<?> uploadQuestionImages(
            @PathVariable Long questionId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            List<QuestionImage> uploadedImages = questionImageService.uploadQuestionImages(questionId, files);
            
            List<Map<String, Object>> imageResponses = uploadedImages.stream()
                .map(this::createImageResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "图片批量上传成功");
            response.put("images", imageResponses);
            response.put("count", uploadedImages.size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("批量上传问题图片失败: questionId={}, error={}", questionId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "图片批量上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取问题的所有图片
     */
    @GetMapping("/{questionId}/images")
    @Operation(summary = "获取问题的所有图片")
    public ResponseEntity<?> getQuestionImages(@PathVariable Long questionId) {
        try {
            List<QuestionImage> images = questionImageService.getQuestionImages(questionId);
            
            List<Map<String, Object>> imageResponses = images.stream()
                .map(this::createImageResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("images", imageResponses);
            response.put("count", images.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取问题图片失败: questionId={}, error={}", questionId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取图片失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取图片文件
     */
    @GetMapping("/images/{imageId}/file")
    @Operation(summary = "获取图片文件")
    public ResponseEntity<byte[]> getImageFile(@PathVariable Long imageId) {
        try {
            Optional<QuestionImage> imageOpt = questionImageService.getImageById(imageId);
            if (imageOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            QuestionImage image = imageOpt.get();
            byte[] imageData = questionImageService.getImageData(image.getFilePath());
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header("Content-Disposition", "inline; filename=\"" + image.getFileName() + "\"")
                .body(imageData);
                
        } catch (Exception e) {
            log.error("获取图片文件失败: imageId={}, error={}", imageId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除问题图片
     */
    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "删除问题图片")
    public ResponseEntity<?> deleteQuestionImage(
            @PathVariable Long imageId,
            @RequestParam Long userId) {
        try {
            questionImageService.deleteQuestionImage(imageId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "图片删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除问题图片失败: imageId={}, userId={}, error={}", imageId, userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "删除图片失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 批量删除问题的所有图片
     */
    @DeleteMapping("/{questionId}/images")
    @Operation(summary = "批量删除问题的所有图片")
    public ResponseEntity<?> deleteQuestionImages(
            @PathVariable Long questionId,
            @RequestParam Long userId) {
        try {
            questionImageService.deleteQuestionImages(questionId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "问题所有图片删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量删除问题图片失败: questionId={}, userId={}, error={}", questionId, userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "批量删除图片失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取图片详细信息
     */
    @GetMapping("/images/{imageId}")
    @Operation(summary = "获取图片详细信息")
    public ResponseEntity<?> getImageInfo(@PathVariable Long imageId) {
        try {
            Optional<QuestionImage> imageOpt = questionImageService.getImageById(imageId);
            if (imageOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "图片不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("image", createImageResponse(imageOpt.get()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取图片信息失败: imageId={}, error={}", imageId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取图片信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 检查用户是否有权限修改图片
     */
    @GetMapping("/images/{imageId}/permission")
    @Operation(summary = "检查用户是否有权限修改图片")
    public ResponseEntity<?> checkImagePermission(
            @PathVariable Long imageId,
            @RequestParam Long userId) {
        try {
            boolean hasPermission = questionImageService.hasPermissionToModify(imageId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasPermission", hasPermission);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检查图片权限失败: imageId={}, userId={}, error={}", imageId, userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "检查权限失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 创建图片响应对象
     */
    private Map<String, Object> createImageResponse(QuestionImage image) {
        Map<String, Object> imageResponse = new HashMap<>();
        imageResponse.put("id", image.getId());
        imageResponse.put("fileName", image.getFileName());
        imageResponse.put("fileSize", image.getFileSize());
        imageResponse.put("contentType", image.getContentType());
        imageResponse.put("uploadedAt", image.getUploadedAt());
        imageResponse.put("url", "/questions/images/" + image.getId() + "/file");
        imageResponse.put("questionId", image.getQuestion().getId());
        
        // 添加文件大小的友好显示
        imageResponse.put("fileSizeFormatted", formatFileSize(image.getFileSize()));
        
        return imageResponse;
    }
    
    /**
     * 格式化文件大小显示
     */
    private String formatFileSize(Long size) {
        if (size == null) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB"};
        double fileSize = size.doubleValue();
        int unitIndex = 0;
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", fileSize, units[unitIndex]);
    }
} 