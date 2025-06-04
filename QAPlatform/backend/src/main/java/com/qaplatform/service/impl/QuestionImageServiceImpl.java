package com.qaplatform.service.impl;

import com.qaplatform.model.Question;
import com.qaplatform.model.QuestionImage;
import com.qaplatform.model.User;
import com.qaplatform.repository.QuestionImageRepository;
import com.qaplatform.repository.QuestionRepository;
import com.qaplatform.repository.UserRepository;
import com.qaplatform.service.QuestionImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 问题图片服务实现类
 */
@Service
@Transactional
@Slf4j
public class QuestionImageServiceImpl implements QuestionImageService {
    
    @Autowired
    private QuestionImageRepository questionImageRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${file.upload-dir:./uploads/}")
    private String uploadDir;
    
    // 允许的图片类型
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };
    
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    @Override
    public QuestionImage uploadQuestionImage(Long questionId, MultipartFile file) {
        // 验证问题是否存在
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在，ID: " + questionId));
        
        // 验证文件
        validateImageFile(file);
        
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir + "questions");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = "question_" + questionId + "_" + UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // 创建数据库记录
            QuestionImage questionImage = new QuestionImage();
            questionImage.setQuestion(question);
            questionImage.setFileName(originalFilename);
            questionImage.setFilePath("questions/" + newFilename);
            questionImage.setContentType(file.getContentType());
            questionImage.setFileSize(file.getSize());
            
            QuestionImage savedImage = questionImageRepository.save(questionImage);
            
            log.info("问题图片上传成功: questionId={}, filename={}", questionId, newFilename);
            return savedImage;
            
        } catch (IOException e) {
            log.error("问题图片上传失败: questionId={}, error={}", questionId, e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<QuestionImage> uploadQuestionImages(Long questionId, List<MultipartFile> files) {
        List<QuestionImage> uploadedImages = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                QuestionImage uploadedImage = uploadQuestionImage(questionId, file);
                uploadedImages.add(uploadedImage);
            }
        }
        
        return uploadedImages;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QuestionImage> getQuestionImages(Long questionId) {
        return questionImageRepository.findByQuestionId(questionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<QuestionImage> getImageById(Long imageId) {
        return questionImageRepository.findById(imageId);
    }
    
    @Override
    public void deleteQuestionImage(Long imageId, Long userId) {
        QuestionImage image = questionImageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("图片不存在，ID: " + imageId));
        
        // 验证权限
        if (!hasPermissionToModify(imageId, userId)) {
            throw new RuntimeException("没有权限删除此图片");
        }
        
        try {
            // 删除文件
            Path filePath = Paths.get(uploadDir + image.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // 删除数据库记录
            questionImageRepository.delete(image);
            
            log.info("问题图片删除成功: imageId={}, userId={}", imageId, userId);
            
        } catch (IOException e) {
            log.error("删除图片文件失败: imageId={}, error={}", imageId, e.getMessage());
            // 即使文件删除失败，也删除数据库记录
            questionImageRepository.delete(image);
        }
    }
    
    @Override
    public void deleteQuestionImages(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在，ID: " + questionId));
        
        // 验证权限：只有问题作者、管理员或版主可以删除
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!question.getAuthor().getId().equals(userId) && 
            !isAdminOrModerator(user)) {
            throw new RuntimeException("没有权限删除问题图片");
        }
        
        List<QuestionImage> images = questionImageRepository.findByQuestionId(questionId);
        
        for (QuestionImage image : images) {
            try {
                // 删除文件
                Path filePath = Paths.get(uploadDir + image.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                log.error("删除图片文件失败: filePath={}, error={}", image.getFilePath(), e.getMessage());
            }
        }
        
        // 批量删除数据库记录
        questionImageRepository.deleteAll(images);
        
        log.info("问题所有图片删除成功: questionId={}, count={}", questionId, images.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] getImageData(String filePath) {
        try {
            Path path = Paths.get(uploadDir + filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("文件不存在: " + filePath);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("读取图片文件失败: filePath={}, error={}", filePath, e.getMessage());
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermissionToModify(Long imageId, Long userId) {
        QuestionImage image = questionImageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 图片所属问题的作者可以修改
        if (image.getQuestion().getAuthor().getId().equals(userId)) {
            return true;
        }
        
        // 管理员和版主可以修改
        return isAdminOrModerator(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getTotalStorageSize() {
        Long totalSize = questionImageRepository.getTotalStorageSize();
        return totalSize != null ? totalSize : 0L;
    }
    
    @Override
    public int cleanupOrphanedImages() {
        List<QuestionImage> allImages = questionImageRepository.findAll();
        int cleanupCount = 0;
        
        for (QuestionImage image : allImages) {
            // 检查关联的问题是否还存在
            if (!questionRepository.existsById(image.getQuestion().getId())) {
                try {
                    // 删除文件
                    Path filePath = Paths.get(uploadDir + image.getFilePath());
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                    
                    // 删除数据库记录
                    questionImageRepository.delete(image);
                    cleanupCount++;
                    
                } catch (IOException e) {
                    log.error("清理孤立图片失败: imageId={}, error={}", image.getId(), e.getMessage());
                }
            }
        }
        
        log.info("清理孤立图片完成，清理数量: {}", cleanupCount);
        return cleanupCount;
    }
    
    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过 5MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        boolean validType = false;
        if (contentType != null) {
            for (String allowedType : ALLOWED_IMAGE_TYPES) {
                if (contentType.equals(allowedType)) {
                    validType = true;
                    break;
                }
            }
        }
        
        if (!validType) {
            throw new RuntimeException("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * 检查用户是否是管理员或版主
     */
    private boolean isAdminOrModerator(User user) {
        return user.getRole().name().equals("ADMIN") || user.getRole().name().equals("MODERATOR");
    }
} 