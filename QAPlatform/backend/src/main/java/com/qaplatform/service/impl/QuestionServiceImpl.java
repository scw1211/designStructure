package com.qaplatform.service.impl;

import com.qaplatform.model.Question;
import com.qaplatform.model.User;
import com.qaplatform.model.Tag;
import com.qaplatform.service.QuestionService;
import com.qaplatform.service.QuestionImageService;
import com.qaplatform.repository.QuestionRepository;
import com.qaplatform.repository.UserRepository;
import com.qaplatform.repository.TagRepository;
import com.qaplatform.strategy.SearchContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * 问题服务实现类
 */
@Service
@Transactional
@Slf4j
public class QuestionServiceImpl implements QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private SearchContext searchContext;
    
    @Autowired
    private QuestionImageService questionImageService;
    
    @Override
    public Question createQuestion(String title, String content, Long authorId, List<String> tagNames, List<MultipartFile> images) {
        // 查找作者
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 创建问题
        Question question = new Question();
        question.setTitle(title);
        question.setContent(content);
        question.setAuthor(author);
        
        // 处理标签
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
                tags.add(tag);
            }
            question.setTags(tags);
        }
        
        // 先保存问题获取ID
        Question savedQuestion = questionRepository.save(question);
        
        // 处理图片上传
        if (images != null && !images.isEmpty()) {
            try {
                questionImageService.uploadQuestionImages(savedQuestion.getId(), images);
                log.info("问题图片上传成功: questionId={}, count={}", savedQuestion.getId(), images.size());
            } catch (Exception e) {
                log.error("问题图片上传失败: questionId={}, error={}", savedQuestion.getId(), e.getMessage());
                // 图片上传失败不影响问题创建
            }
        }
        
        log.info("创建问题成功: {}", savedQuestion.getTitle());
        return savedQuestion;
    }
    
    @Override
    public Optional<Question> findById(Long id) {
        return questionRepository.findById(id);
    }
    
    @Override
    public Question updateQuestion(Long questionId, String title, String content, List<String> tagNames) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        question.setTitle(title);
        question.setContent(content);
        
        // 更新标签
        if (tagNames != null) {
            Set<Tag> newTags = new HashSet<>();
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
                newTags.add(tag);
            }
            question.setTags(newTags);
        }
        
        return questionRepository.save(question);
    }
    
    @Override
    public void deleteQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        // 检查权限：只有作者或管理员可以删除
        if (!question.getAuthor().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (user.getRole() != User.UserRole.ADMIN) {
                throw new RuntimeException("无权限删除此问题");
            }
        }
        
        questionRepository.delete(question);
        log.info("删除问题: {}", question.getTitle());
    }
    
    @Override
    public Question closeQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        // 检查权限
        if (!question.getAuthor().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (user.getRole() != User.UserRole.ADMIN) {
                throw new RuntimeException("无权限关闭此问题");
            }
        }
        
        question.setClosed(true);
        return questionRepository.save(question);
    }
    
    @Override
    public Question markAsSolved(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        // 只有作者可以标记为已解决
        if (!question.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("只有问题作者可以标记问题为已解决");
        }
        
        question.setSolved(true);
        return questionRepository.save(question);
    }
    
    @Override
    public void incrementViewCount(Long questionId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        question.incrementViewCount();
        questionRepository.save(question);
    }
    
    @Override
    public Question toggleFollow(Long questionId, Long userId, boolean follow) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (follow) {
            question.addFollower(user);
        } else {
            question.removeFollower(user);
        }
        
        return questionRepository.save(question);
    }
    
    @Override
    public Page<Question> searchQuestions(String keyword, String strategy, Pageable pageable) {
        return searchContext.search(keyword, strategy, pageable);
    }
    
    @Override
    public Page<Question> getHotQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }
    
    @Override
    public Page<Question> getUnansweredQuestions(Pageable pageable) {
        return questionRepository.findUnsolvedQuestions(pageable);
    }
    
    @Override
    public Page<Question> getQuestionsByTag(String tagName, Pageable pageable) {
        Optional<Tag> tagOpt = tagRepository.findByName(tagName);
        if (tagOpt.isEmpty()) {
            return Page.empty(pageable);
        }
        return questionRepository.findByTagId(tagOpt.get().getId(), pageable);
    }
    
    @Override
    public Page<Question> getUserQuestions(Long userId, Pageable pageable) {
        return questionRepository.findByAuthorId(userId, pageable);
    }
    
    @Override
    public Page<Question> findByAuthorId(Long authorId, Pageable pageable) {
        return questionRepository.findByAuthorId(authorId, pageable);
    }
    
    @Override
    public long countByAuthorId(Long authorId) {
        return questionRepository.countByAuthorId(authorId);
    }
    
    @Override
    public Page<Question> getAllQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }
    
    @Override
    public Map<String, Object> voteQuestion(Long questionId, Long userId, int vote) {
        Map<String, Object> result = new HashMap<>();
        
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户之前的投票状态
        boolean hasVoted = question.getVoters().contains(user);
        
        if (vote == 1) {
            // 点赞
            if (!hasVoted) {
                question.addVote(user);
                result.put("message", "点赞成功");
                result.put("userVote", 1);
            } else {
                // 取消点赞
                question.removeVote(user);
                result.put("message", "取消点赞");
                result.put("userVote", 0);
            }
        } else if (vote == -1) {
            // 由于当前模型只支持点赞，暂时不处理踩
            if (hasVoted) {
                question.removeVote(user);
                result.put("message", "取消点赞");
                result.put("userVote", 0);
            } else {
                result.put("message", "暂不支持踩");
                result.put("userVote", 0);
            }
        }
        
        questionRepository.save(question);
        result.put("voteCount", question.getVotes());
        
        return result;
    }
    
    @Override
    public boolean isQuestionAuthor(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        return question.getAuthor().getId().equals(userId);
    }
    
    @Override
    public Map<String, Object> getVoteStatus(Long questionId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        // TODO: 实现具体的投票状态逻辑
        result.put("userVote", 0); // 0表示未投票，1表示点赞，-1表示踩
        return result;
    }
    
    // ========== 管理员功能实现 ==========
    
    @Override
    public Question reopenQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        // 检查权限：只有管理员可以重新开启问题
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getRole() != User.UserRole.ADMIN && user.getRole() != User.UserRole.MODERATOR) {
            throw new RuntimeException("无权限重新开启此问题");
        }
        
        question.setClosed(false);
        Question reopenedQuestion = questionRepository.save(question);
        log.info("重新开启问题: {}", question.getTitle());
        
        return reopenedQuestion;
    }
    
    @Override
    public long getTotalQuestions() {
        return questionRepository.count();
    }
    
    @Override
    public long getSolvedQuestions() {
        return questionRepository.countBySolvedTrue();
    }
    
    @Override
    public long getClosedQuestions() {
        return questionRepository.countByClosedTrue();
    }
    
    @Override
    public long getUnansweredQuestionsCount() {
        return questionRepository.countByAnswerCountEquals(0);
    }
    
    @Override
    public Map<String, Long> getQuestionStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalQuestions", getTotalQuestions());
        stats.put("solvedQuestions", getSolvedQuestions());
        stats.put("closedQuestions", getClosedQuestions());
        stats.put("unansweredQuestions", getUnansweredQuestionsCount());
        stats.put("openQuestions", getTotalQuestions() - getClosedQuestions());
        
        return stats;
    }
} 