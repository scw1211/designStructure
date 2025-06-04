package com.qaplatform.repository;

import com.qaplatform.model.Answer;
import com.qaplatform.model.Question;
import com.qaplatform.model.User;
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
 * 回答Repository接口
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    /**
     * 根据问题查找回答
     */
    List<Answer> findByQuestion(Question question);
    
    /**
     * 根据问题ID查找回答
     */
    List<Answer> findByQuestionId(Long questionId);
    
    /**
     * 根据问题ID查找回答（分页）
     */
    Page<Answer> findByQuestionId(Long questionId, Pageable pageable);
    
    /**
     * 根据问题ID查找回答（分页，预加载author）
     */
    @Query("SELECT a FROM Answer a JOIN FETCH a.author WHERE a.question.id = :questionId")
    List<Answer> findByQuestionIdWithAuthor(@Param("questionId") Long questionId);
    
    /**
     * 根据问题ID查找回答（分页，预加载author）- 支持排序
     */
    @Query(value = "SELECT a FROM Answer a JOIN FETCH a.author WHERE a.question.id = :questionId ORDER BY " +
           "CASE WHEN :sort = 'votes' THEN a.voteCount END DESC, " +
           "CASE WHEN :sort = 'oldest' THEN a.createdAt END ASC, " +
           "CASE WHEN :sort = 'newest' THEN a.createdAt END DESC")
    List<Answer> findByQuestionIdWithAuthorSorted(@Param("questionId") Long questionId, @Param("sort") String sort);
    
    /**
     * 根据问题ID查找回答（预加载author和question）- 支持排序
     */
    @Query(value = "SELECT a FROM Answer a JOIN FETCH a.author JOIN FETCH a.question WHERE a.question.id = :questionId ORDER BY " +
           "CASE WHEN :sort = 'votes' THEN a.voteCount END DESC, " +
           "CASE WHEN :sort = 'oldest' THEN a.createdAt END ASC, " +
           "CASE WHEN :sort = 'newest' THEN a.createdAt END DESC")
    List<Answer> findByQuestionIdWithAuthorAndQuestionSorted(@Param("questionId") Long questionId, @Param("sort") String sort);
    
    /**
     * 根据作者查找回答
     */
    List<Answer> findByAuthor(User author);
    
    /**
     * 根据作者ID查找回答
     */
    List<Answer> findByAuthorId(Long authorId);
    
    /**
     * 根据作者ID查找回答（分页）
     */
    Page<Answer> findByAuthorId(Long authorId, Pageable pageable);
    
    /**
     * 根据作者ID查找回答（分页，预加载问题和作者信息）
     */
    @Query("SELECT a FROM Answer a JOIN FETCH a.question q JOIN FETCH a.author WHERE a.author.id = :authorId")
    List<Answer> findByAuthorIdWithQuestionAndAuthor(@Param("authorId") Long authorId);
    
    /**
     * 根据作者ID查找回答（分页，预加载问题和作者信息，支持排序）
     */
    @Query(value = "SELECT a FROM Answer a JOIN FETCH a.question q JOIN FETCH a.author WHERE a.author.id = :authorId ORDER BY " +
           "CASE WHEN :sortBy = 'voteCount' THEN a.voteCount END DESC, " +
           "CASE WHEN :sortDirection = 'asc' THEN a.createdAt END ASC, " +
           "CASE WHEN :sortDirection = 'desc' THEN a.createdAt END DESC")
    List<Answer> findByAuthorIdWithQuestionAndAuthorSorted(@Param("authorId") Long authorId, 
                                                          @Param("sortBy") String sortBy, 
                                                          @Param("sortDirection") String sortDirection);
    
    /**
     * 根据问题ID和采纳状态查找回答
     */
    List<Answer> findByQuestionIdAndAccepted(Long questionId, Boolean accepted);
    
    /**
     * 查找问题的被采纳回答
     */
    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId AND a.accepted = true")
    Optional<Answer> findAcceptedAnswerByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 根据问题ID按点赞数排序查找回答
     */
    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId ORDER BY a.voteCount DESC, a.createdAt ASC")
    List<Answer> findByQuestionIdOrderByVoteCountDesc(@Param("questionId") Long questionId);
    
    /**
     * 根据问题ID按时间排序查找回答
     */
    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId ORDER BY a.createdAt ASC")
    List<Answer> findByQuestionIdOrderByCreatedAtAsc(@Param("questionId") Long questionId);
    
    /**
     * 统计问题的回答数量
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question.id = :questionId")
    Long countByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 统计用户的回答数量
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.author.id = :userId")
    Long countByAuthorId(@Param("userId") Long userId);
    
    /**
     * 统计用户的被采纳回答数量
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.author.id = :userId AND a.accepted = true")
    Long countAcceptedAnswersByAuthorId(@Param("userId") Long userId);
    
    /**
     * 根据时间范围查找回答
     */
    @Query("SELECT a FROM Answer a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Answer> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取用户点赞的回答
     */
    @Query("SELECT a FROM Answer a JOIN a.voters v WHERE v.id = :userId")
    List<Answer> findVotedAnswersByUserId(@Param("userId") Long userId);
    
    /**
     * 检查用户是否对回答点赞
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Answer a JOIN a.voters v WHERE a.id = :answerId AND v.id = :userId")
    Boolean isAnswerVotedByUser(@Param("answerId") Long answerId, @Param("userId") Long userId);
    
    /**
     * 获取高分回答（点赞数大于阈值）
     */
    @Query("SELECT a FROM Answer a WHERE a.voteCount >= :minVotes ORDER BY a.voteCount DESC")
    List<Answer> findHighScoredAnswers(@Param("minVotes") int minVotes);
    
    /**
     * 根据内容模糊查询回答
     */
    @Query("SELECT a FROM Answer a WHERE a.content LIKE %:keyword%")
    List<Answer> searchByKeyword(@Param("keyword") String keyword);
    
    // ========== 管理员查询方法 ==========
    
    /**
     * 获取所有回答（管理员视图，预加载作者和问题信息）
     */
    @Query("SELECT a FROM Answer a JOIN FETCH a.author JOIN FETCH a.question ORDER BY a.createdAt DESC")
    Page<Answer> findAllWithAuthorAndQuestion(Pageable pageable);
    
    /**
     * 统计已采纳的回答数量
     */
    long countByAcceptedTrue();
    
    // ========== 高级分析查询方法 ==========
    
    /**
     * 统计某个时间之后创建的回答数量
     */
    long countByCreatedAtAfter(LocalDateTime date);
    
    /**
     * 统计某个时间范围内创建的回答数量
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计某个作者被采纳的回答数量
     */
    long countByAuthorIdAndAccepted(Long authorId, Boolean accepted);
} 