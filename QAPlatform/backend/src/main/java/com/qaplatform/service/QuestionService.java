package com.qaplatform.service;

import com.qaplatform.model.Question;
import com.qaplatform.model.User;
import com.qaplatform.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * 问题服务接口
 * 定义问题相关的业务逻辑
 */
public interface QuestionService {
    
    /**
     * 创建问题
     * @param title 标题
     * @param content 内容
     * @param authorId 作者ID
     * @param tagNames 标签名称列表
     * @param images 图片文件列表
     * @return 创建的问题
     */
    Question createQuestion(String title, String content, Long authorId, List<String> tagNames, List<MultipartFile> images);
    
    /**
     * 根据ID查找问题
     * @param id 问题ID
     * @return 问题对象
     */
    Optional<Question> findById(Long id);
    
    /**
     * 获取所有问题（分页）
     * @param pageable 分页参数
     * @return 问题分页
     */
    Page<Question> getAllQuestions(Pageable pageable);
    
    /**
     * 更新问题
     * @param questionId 问题ID
     * @param title 新标题
     * @param content 新内容
     * @param tagNames 新标签列表
     * @return 更新后的问题
     */
    Question updateQuestion(Long questionId, String title, String content, List<String> tagNames);
    
    /**
     * 删除问题
     * @param questionId 问题ID
     * @param userId 操作用户ID
     */
    void deleteQuestion(Long questionId, Long userId);
    
    /**
     * 关闭问题
     * @param questionId 问题ID
     * @param userId 操作用户ID
     * @return 更新后的问题
     */
    Question closeQuestion(Long questionId, Long userId);
    
    /**
     * 标记问题为已解决
     * @param questionId 问题ID
     * @param userId 操作用户ID
     * @return 更新后的问题
     */
    Question markAsSolved(Long questionId, Long userId);
    
    /**
     * 增加浏览次数
     * @param questionId 问题ID
     */
    void incrementViewCount(Long questionId);
    
    /**
     * 关注/取消关注问题
     * @param questionId 问题ID
     * @param userId 用户ID
     * @param follow true表示关注，false表示取消关注
     * @return 更新后的问题
     */
    Question toggleFollow(Long questionId, Long userId, boolean follow);
    
    /**
     * 搜索问题
     * @param keyword 关键词
     * @param strategy 搜索策略
     * @param pageable 分页参数
     * @return 问题分页
     */
    Page<Question> searchQuestions(String keyword, String strategy, Pageable pageable);
    
    /**
     * 获取热门问题
     * @param pageable 分页参数
     * @return 问题分页
     */
    Page<Question> getHotQuestions(Pageable pageable);
    
    /**
     * 获取未回答的问题
     * @param pageable 分页参数
     * @return 问题分页
     */
    Page<Question> getUnansweredQuestions(Pageable pageable);
    
    /**
     * 根据标签获取问题
     * @param tagName 标签名
     * @param pageable 分页参数
     * @return 问题分页
     */
    Page<Question> getQuestionsByTag(String tagName, Pageable pageable);
    
    /**
     * 获取用户的问题
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 问题分页
     */
    Page<Question> getUserQuestions(Long userId, Pageable pageable);
    
    /**
     * 根据作者ID获取问题
     * @param authorId 作者ID
     * @param pageable 分页参数
     * @return 问题分页
     */
    Page<Question> findByAuthorId(Long authorId, Pageable pageable);
    
    /**
     * 统计作者的问题数量
     * @param authorId 作者ID
     * @return 问题数量
     */
    long countByAuthorId(Long authorId);
    
    /**
     * 问题投票
     * @param questionId 问题ID
     * @param userId 用户ID
     * @param vote 投票类型：1点赞，-1踩，0取消
     * @return 投票结果
     */
    Map<String, Object> voteQuestion(Long questionId, Long userId, int vote);
    
    /**
     * 检查是否是问题作者
     * @param questionId 问题ID
     * @param userId 用户ID
     * @return 是否是作者
     */
    boolean isQuestionAuthor(Long questionId, Long userId);
    
    /**
     * 获取问题投票状态
     * @param questionId 问题ID
     * @param userId 用户ID
     * @return 投票状态
     */
    Map<String, Object> getVoteStatus(Long questionId, Long userId);
    
    // ========== 管理员功能 ==========
    
    /**
     * 重新开启问题
     * @param questionId 问题ID
     * @param userId 操作用户ID
     * @return 更新后的问题
     */
    Question reopenQuestion(Long questionId, Long userId);
    
    /**
     * 获取问题总数
     * @return 问题总数
     */
    long getTotalQuestions();
    
    /**
     * 获取已解决问题数
     * @return 已解决问题数
     */
    long getSolvedQuestions();
    
    /**
     * 获取已关闭问题数
     * @return 已关闭问题数
     */
    long getClosedQuestions();
    
    /**
     * 获取未回答问题数
     * @return 未回答问题数
     */
    long getUnansweredQuestionsCount();
    
    /**
     * 获取问题统计信息
     * @return 问题统计Map
     */
    Map<String, Long> getQuestionStats();
} 