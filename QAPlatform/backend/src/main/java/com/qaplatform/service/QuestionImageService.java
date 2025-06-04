package com.qaplatform.service;

import com.qaplatform.model.QuestionImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 问题图片服务接口
 */
public interface QuestionImageService {
    
    /**
     * 上传问题图片
     * @param questionId 问题ID
     * @param file 图片文件
     * @return 上传的图片信息
     */
    QuestionImage uploadQuestionImage(Long questionId, MultipartFile file);
    
    /**
     * 批量上传问题图片
     * @param questionId 问题ID
     * @param files 图片文件列表
     * @return 上传的图片信息列表
     */
    List<QuestionImage> uploadQuestionImages(Long questionId, List<MultipartFile> files);
    
    /**
     * 获取问题的所有图片
     * @param questionId 问题ID
     * @return 图片列表
     */
    List<QuestionImage> getQuestionImages(Long questionId);
    
    /**
     * 根据ID获取图片
     * @param imageId 图片ID
     * @return 图片信息
     */
    Optional<QuestionImage> getImageById(Long imageId);
    
    /**
     * 删除问题图片
     * @param imageId 图片ID
     * @param userId 操作用户ID
     */
    void deleteQuestionImage(Long imageId, Long userId);
    
    /**
     * 批量删除问题图片
     * @param questionId 问题ID
     * @param userId 操作用户ID
     */
    void deleteQuestionImages(Long questionId, Long userId);
    
    /**
     * 获取图片文件数据
     * @param filePath 文件路径
     * @return 文件字节数组
     */
    byte[] getImageData(String filePath);
    
    /**
     * 验证用户是否有权限操作图片
     * @param imageId 图片ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionToModify(Long imageId, Long userId);
    
    /**
     * 获取图片总存储大小
     * @return 总大小（字节）
     */
    Long getTotalStorageSize();
    
    /**
     * 清理孤立的图片文件（没有关联问题的图片）
     * @return 清理的文件数量
     */
    int cleanupOrphanedImages();
} 