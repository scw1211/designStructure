package com.qaplatform.repository;

import com.qaplatform.model.Question;
import com.qaplatform.model.User;
import com.qaplatform.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 问题Repository接口
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * 根据作者查找问题
     */
    List<Question> findByAuthor(User author);
    
    /**
     * 根据作者ID查找问题（分页）
     */
    Page<Question> findByAuthorId(Long authorId, Pageable pageable);
    
    /**
     * 根据解决状态查找问题
     */
    List<Question> findBySolved(Boolean solved);
    
    /**
     * 根据关闭状态查找问题
     */
    List<Question> findByClosed(Boolean closed);
    
    /**
     * 根据标题模糊查询（忽略大小写）
     */
    Page<Question> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * 全文搜索 - 在标题、内容和标签中搜索
     */
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN q.tags t WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Question> searchQuestions(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据标题模糊查询
     */
    @Query("SELECT q FROM Question q WHERE q.title LIKE %:keyword% OR q.content LIKE %:keyword%")
    List<Question> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 根据标题模糊查询（分页）
     */
    @Query("SELECT q FROM Question q WHERE q.title LIKE %:keyword% OR q.content LIKE %:keyword%")
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据标签查找问题
     */
    @Query("SELECT DISTINCT q FROM Question q JOIN q.tags t WHERE t IN :tags")
    List<Question> findByTagsIn(@Param("tags") List<Tag> tags);
    
    /**
     * 根据标签ID查找问题
     */
    @Query("SELECT DISTINCT q FROM Question q JOIN q.tags t WHERE t.id = :tagId")
    List<Question> findByTagId(@Param("tagId") Long tagId);
    
    /**
     * 根据标签ID查找问题（分页）
     */
    @Query("SELECT DISTINCT q FROM Question q JOIN q.tags t WHERE t.id = :tagId")
    Page<Question> findByTagId(@Param("tagId") Long tagId, Pageable pageable);
    
    /**
     * 根据浏览次数排序获取热门问题
     */
    List<Question> findAllByOrderByViewCountDesc();
    
    /**
     * 根据创建时间排序获取最新问题
     */
    List<Question> findAllByOrderByCreatedAtDesc();
    
    /**
     * 根据回答数量排序获取问题
     */
    List<Question> findAllByOrderByAnswerCountDesc();
    
    /**
     * 获取未解决的问题
     */
    @Query("SELECT q FROM Question q WHERE q.solved = false AND q.closed = false ORDER BY q.createdAt DESC")
    List<Question> findUnsolvedQuestions();
    
    /**
     * 获取未解决的问题（分页）
     */
    @Query("SELECT q FROM Question q WHERE q.solved = false AND q.closed = false ORDER BY q.createdAt DESC")
    Page<Question> findUnsolvedQuestions(Pageable pageable);
    
    /**
     * 根据时间范围查找问题
     */
    @Query("SELECT q FROM Question q WHERE q.createdAt BETWEEN :startDate AND :endDate")
    List<Question> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取用户关注的问题
     */
    @Query("SELECT q FROM Question q JOIN q.followers f WHERE f.id = :userId")
    List<Question> findFollowedQuestionsByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户关注的问题（分页）
     */
    @Query("SELECT q FROM Question q JOIN q.followers f WHERE f.id = :userId")
    Page<Question> findFollowedQuestionsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 获取热门问题（根据浏览量和回答数）
     */
    @Query("SELECT q FROM Question q WHERE q.closed = false ORDER BY (q.viewCount + q.answerCount * 2) DESC")
    List<Question> findHotQuestions(Pageable pageable);
    
    /**
     * 统计用户的问题数量
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.author.id = :userId")
    Long countByAuthorId(@Param("userId") Long userId);
    
    /**
     * 使用JOIN FETCH获取问题及其关联的作者和标签（避免懒加载问题）
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.author " +
           "LEFT JOIN FETCH q.tags " +
           "ORDER BY q.createdAt DESC")
    List<Question> findAllWithAuthorAndTags();
    
    /**
     * 使用JOIN FETCH获取问题及其关联的作者和标签（分页）
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.author " +
           "LEFT JOIN FETCH q.tags " +
           "ORDER BY q.createdAt DESC")
    Page<Question> findAllWithAuthorAndTags(Pageable pageable);
    
    /**
     * 根据ID使用JOIN FETCH获取问题详情
     */
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.author " +
           "LEFT JOIN FETCH q.tags " +
           "WHERE q.id = :id")
    Optional<Question> findByIdWithAuthorAndTags(@Param("id") Long id);
    
    // ========== 管理员统计查询 ==========
    
    /**
     * 统计已解决的问题数量
     */
    long countBySolvedTrue();
    
    /**
     * 统计已关闭的问题数量
     */
    long countByClosedTrue();
    
    /**
     * 统计未回答的问题数量（回答数为0）
     */
    long countByAnswerCountEquals(int answerCount);
    
    /**
     * 统计开放的问题数量（未关闭且未解决）
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.closed = false AND q.solved = false")
    long countOpenQuestions();
    
    /**
     * 统计某个状态的问题数量
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.closed = :closed AND q.solved = :solved")
    long countByStatus(@Param("closed") boolean closed, @Param("solved") boolean solved);
    
    // ========== 高级分析查询方法 ==========
    
    /**
     * 统计某个时间之后创建的问题数量
     */
    long countByCreatedAtAfter(LocalDateTime date);
    
    /**
     * 统计某个时间范围内创建的问题数量
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取热门趋势问题（最近一周，按浏览量和回答数排序）
     */
    @Query("SELECT q FROM Question q WHERE q.createdAt > :weekAgo AND q.closed = false " +
           "ORDER BY (q.viewCount + q.answerCount * 3) DESC")
    List<Question> findTrendingQuestions(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);
    
    /**
     * 统计包含特定标签的问题数量
     */
    @Query("SELECT COUNT(DISTINCT q) FROM Question q JOIN q.tags t WHERE t = :tag")
    long countByTagsContaining(@Param("tag") Tag tag);
    
    /**
     * 统计某个标签下已回答的问题数量
     */
    @Query("SELECT COUNT(q) FROM Question q JOIN q.tags t WHERE t.id = :tagId AND q.answerCount > 0")
    long countAnsweredQuestionsByTag(@Param("tagId") Long tagId);
} 