package com.qaplatform.service;

import com.qaplatform.model.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface AnswerService {
    Page<Answer> getAnswersByQuestion(Long questionId, String sort, Pageable pageable);
    Answer createAnswer(Long questionId, Answer answer);
    Answer createAnswer(Long questionId, String content, Long authorId);
    void voteAnswer(Long answerId, int vote);
    Map<String, Object> voteAnswer(Long answerId, Long userId, int vote);
    void acceptAnswer(Long questionId, Long answerId);
    boolean isAnswerAuthor(Long answerId, Long userId);
    boolean isQuestionAuthor(Long questionId, Long userId);
    Map<String, Object> getVoteStatus(Long answerId, Long userId);
    
    // ========== 管理员功能 ==========
    
    /**
     * 获取所有回答（管理员视图）
     * @param pageable 分页参数
     * @return 回答分页
     */
    Page<Answer> getAllAnswers(Pageable pageable);
    
    /**
     * 删除回答
     * @param answerId 回答ID
     * @param userId 操作用户ID
     */
    void deleteAnswer(Long answerId, Long userId);
    
    /**
     * 获取回答总数
     * @return 回答总数
     */
    long getTotalAnswers();
    
    /**
     * 获取已采纳回答数
     * @return 已采纳回答数
     */
    long getAcceptedAnswers();
    
    /**
     * 获取回答统计信息
     * @return 回答统计Map
     */
    Map<String, Long> getAnswerStats();
} 