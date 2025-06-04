package com.qaplatform.repository;

import com.qaplatform.model.QuestionImage;
import com.qaplatform.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题图片Repository接口
 */
@Repository
public interface QuestionImageRepository extends JpaRepository<QuestionImage, Long> {
    
    /**
     * 根据问题查找图片
     */
    List<QuestionImage> findByQuestion(Question question);
    
    /**
     * 根据问题ID查找图片
     */
    List<QuestionImage> findByQuestionId(Long questionId);
    
    /**
     * 根据文件名查找图片
     */
    List<QuestionImage> findByFileName(String fileName);
    
    /**
     * 根据文件路径查找图片
     */
    QuestionImage findByFilePath(String filePath);
    
    /**
     * 根据内容类型查找图片
     */
    List<QuestionImage> findByContentType(String contentType);
    
    /**
     * 根据时间范围查找图片
     */
    @Query("SELECT qi FROM QuestionImage qi WHERE qi.uploadedAt BETWEEN :startDate AND :endDate")
    List<QuestionImage> findByUploadDateRange(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据文件大小范围查找图片
     */
    @Query("SELECT qi FROM QuestionImage qi WHERE qi.fileSize BETWEEN :minSize AND :maxSize")
    List<QuestionImage> findByFileSizeRange(@Param("minSize") Long minSize, 
                                           @Param("maxSize") Long maxSize);
    
    /**
     * 统计问题的图片数量
     */
    @Query("SELECT COUNT(qi) FROM QuestionImage qi WHERE qi.question.id = :questionId")
    Long countByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 获取大文件（超过指定大小）
     */
    @Query("SELECT qi FROM QuestionImage qi WHERE qi.fileSize > :maxSize ORDER BY qi.fileSize DESC")
    List<QuestionImage> findLargeFiles(@Param("maxSize") Long maxSize);
    
    /**
     * 统计总存储大小
     */
    @Query("SELECT SUM(qi.fileSize) FROM QuestionImage qi")
    Long getTotalStorageSize();
} 