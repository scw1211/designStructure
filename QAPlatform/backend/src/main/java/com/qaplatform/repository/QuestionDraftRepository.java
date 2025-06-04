package com.qaplatform.repository;

import com.qaplatform.model.QuestionDraft;
import com.qaplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 问题草稿Repository接口
 */
@Repository
public interface QuestionDraftRepository extends JpaRepository<QuestionDraft, Long> {
    
    /**
     * 根据作者查找草稿
     */
    List<QuestionDraft> findByAuthor(User author);
    
    /**
     * 根据作者ID查找草稿
     */
    List<QuestionDraft> findByAuthorId(Long authorId);
    
    /**
     * 根据作者ID查找草稿（分页）
     */
    Page<QuestionDraft> findByAuthorId(Long authorId, Pageable pageable);
    
    /**
     * 根据作者ID按更新时间倒序查找草稿
     */
    @Query("SELECT qd FROM QuestionDraft qd WHERE qd.author.id = :authorId ORDER BY qd.updatedAt DESC")
    List<QuestionDraft> findByAuthorIdOrderByUpdatedAtDesc(@Param("authorId") Long authorId);
    
    /**
     * 根据草稿名称查找草稿
     */
    List<QuestionDraft> findByDraftName(String draftName);
    
    /**
     * 根据作者和草稿名称查找草稿
     */
    Optional<QuestionDraft> findByAuthorIdAndDraftName(Long authorId, String draftName);
    
    /**
     * 根据自动保存状态查找草稿
     */
    List<QuestionDraft> findByAutoSaved(Boolean autoSaved);
    
    /**
     * 根据作者ID和自动保存状态查找草稿
     */
    List<QuestionDraft> findByAuthorIdAndAutoSaved(Long authorId, Boolean autoSaved);
    
    /**
     * 查找用户最新的自动保存草稿
     */
    @Query("SELECT qd FROM QuestionDraft qd WHERE qd.author.id = :authorId AND qd.autoSaved = true ORDER BY qd.updatedAt DESC LIMIT 1")
    Optional<QuestionDraft> findLatestAutoSavedDraft(@Param("authorId") Long authorId);
    
    /**
     * 统计用户的草稿数量
     */
    @Query("SELECT COUNT(qd) FROM QuestionDraft qd WHERE qd.author.id = :authorId")
    Long countByAuthorId(@Param("authorId") Long authorId);
    
    /**
     * 根据时间范围查找草稿
     */
    @Query("SELECT qd FROM QuestionDraft qd WHERE qd.createdAt BETWEEN :startDate AND :endDate")
    List<QuestionDraft> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * 删除指定时间之前的自动保存草稿
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM QuestionDraft qd WHERE qd.autoSaved = true AND qd.updatedAt < :cutoffDate")
    void deleteOldAutoSavedDrafts(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 根据标题模糊查询用户的草稿
     */
    @Query("SELECT qd FROM QuestionDraft qd WHERE qd.author.id = :authorId AND " +
           "(qd.title LIKE %:keyword% OR qd.content LIKE %:keyword%)")
    List<QuestionDraft> searchUserDrafts(@Param("authorId") Long authorId, @Param("keyword") String keyword);
    
    /**
     * 查找空草稿（标题和内容都为空或null）
     */
    @Query("SELECT qd FROM QuestionDraft qd WHERE qd.author.id = :authorId AND " +
           "(qd.title IS NULL OR qd.title = '') AND " +
           "(qd.content IS NULL OR qd.content = '')")
    List<QuestionDraft> findEmptyDrafts(@Param("authorId") Long authorId);
    
    /**
     * 批量删除用户的空草稿
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM QuestionDraft qd WHERE qd.author.id = :authorId AND " +
           "(qd.title IS NULL OR qd.title = '') AND " +
           "(qd.content IS NULL OR qd.content = '')")
    void deleteEmptyDrafts(@Param("authorId") Long authorId);
} 