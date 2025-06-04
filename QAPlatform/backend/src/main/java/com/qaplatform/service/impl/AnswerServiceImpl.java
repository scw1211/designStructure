package com.qaplatform.service.impl;

import com.qaplatform.model.Answer;
import com.qaplatform.model.Question;
import com.qaplatform.model.User;
import com.qaplatform.repository.AnswerRepository;
import com.qaplatform.repository.QuestionRepository;
import com.qaplatform.repository.UserRepository;
import com.qaplatform.service.AnswerService;
import com.qaplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * 回答服务实现类
 */
@Service
@Transactional
@Slf4j
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public Page<Answer> getAnswersByQuestion(Long questionId, String sort, Pageable pageable) {
        // 使用预加载author和question的查询方法来避免懒加载异常
        List<Answer> answers = answerRepository.findByQuestionIdWithAuthorAndQuestionSorted(questionId, sort);
        
        // 手动实现分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), answers.size());
        
        List<Answer> pageContent = start > answers.size() ? 
            java.util.Collections.emptyList() : answers.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, answers.size());
    }

    @Override
    public Answer createAnswer(Long questionId, Answer answer) {
        // 查找问题
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            throw new RuntimeException("问题不存在");
        }
        
        Question question = questionOpt.get();
        
        // 设置回答的基本信息
        answer.setQuestion(question);
        answer.setVoteCount(0);
        answer.setAccepted(false);
        answer.setCreatedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());
        
        // 保存回答到数据库
        Answer savedAnswer = answerRepository.save(answer);
        
        // 更新问题的回答数量
        question.setAnswerCount(question.getAnswerCount() + 1);
        questionRepository.save(question);
        
        return savedAnswer;
    }

    @Override
    public Answer createAnswer(Long questionId, String content, Long authorId) {
        // 查找问题
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("问题不存在"));
        
        // 查找作者
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 创建回答
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setQuestion(question);
        answer.setAuthor(author);
        answer.setVoteCount(0);
        answer.setAccepted(false);
        answer.setCreatedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());
        
        // 保存回答到数据库
        Answer savedAnswer = answerRepository.save(answer);
        
        // 更新问题的回答数量
        question.setAnswerCount(question.getAnswerCount() + 1);
        questionRepository.save(question);
        
        return savedAnswer;
    }

    @Override
    public void voteAnswer(Long answerId, int vote) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            answer.setVoteCount(answer.getVoteCount() + vote);
            answerRepository.save(answer);
        }
    }

    @Override
    public void acceptAnswer(Long questionId, Long answerId) {
        // 首先检查问题是否已经有被采纳的回答
        Optional<Answer> existingAccepted = answerRepository.findAcceptedAnswerByQuestionId(questionId);
        if (existingAccepted.isPresent()) {
            throw new RuntimeException("该问题已经有被采纳的回答，不能重复采纳");
        }
        
        // 查找要采纳的回答
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (!answerOpt.isPresent()) {
            throw new RuntimeException("回答不存在");
        }
        
        Answer answer = answerOpt.get();
        
        // 验证回答属于指定的问题
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("回答不属于指定问题");
        }
        
        // 检查回答是否已经被采纳
        if (answer.getAccepted()) {
            throw new RuntimeException("该回答已被采纳");
        }
        
        // 设置为采纳
        answer.setAccepted(true);
        answer.setUpdatedAt(LocalDateTime.now());
        answerRepository.save(answer);
        
        // 更新问题状态为已解决
        Question question = answer.getQuestion();
        question.setSolved(true);
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);
        
        // 给回答者增加50声望
        User author = answer.getAuthor();
        userService.updateReputation(author.getId(), 50);
        log.info("用户 {} 的回答被采纳，获得50声望", author.getUsername());
    }

    @Override
    public Map<String, Object> voteAnswer(Long answerId, Long userId, int vote) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (!answerOpt.isPresent()) {
            throw new RuntimeException("回答不存在");
        }
        
        Answer answer = answerOpt.get();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户之前的投票状态
        boolean hasVoted = answer.getVoters().contains(user);
        
        if (vote == 1) {
            // 点赞
            if (!hasVoted) {
                answer.addVote(user);
                result.put("message", "点赞成功");
                result.put("userVote", 1);
            } else {
                // 取消点赞
                answer.removeVote(user);
                result.put("message", "取消点赞");
                result.put("userVote", 0);
            }
        } else if (vote == -1) {
            // 取消点赞
            if (hasVoted) {
                answer.removeVote(user);
                result.put("message", "取消点赞");
                result.put("userVote", 0);
            } else {
                result.put("message", "暂不支持踩");
                result.put("userVote", 0);
            }
        }
        
        answerRepository.save(answer);
        result.put("voteCount", answer.getVoteCount());
        
        return result;
    }
    
    @Override
    public boolean isAnswerAuthor(Long answerId, Long userId) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            return answerOpt.get().getAuthor().getId().equals(userId);
        }
        return false;
    }
    
    @Override
    public boolean isQuestionAuthor(Long questionId, Long userId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            return questionOpt.get().getAuthor().getId().equals(userId);
        }
        return false;
    }
    
    @Override
    public Map<String, Object> getVoteStatus(Long answerId, Long userId) {
        Map<String, Object> status = new HashMap<>();
        
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("回答不存在"));
        
        status.put("voteCount", answer.getVoteCount());
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                boolean hasVoted = answer.getVoters().contains(user);
                status.put("userVote", hasVoted ? 1 : 0);
                status.put("isAuthor", answer.getAuthor().getId().equals(userId));
            } else {
                status.put("userVote", 0);
                status.put("isAuthor", false);
            }
        } else {
            status.put("userVote", 0);
            status.put("isAuthor", false);
        }
        
        return status;
    }
    
    // ========== 管理员功能实现 ==========
    
    @Override
    public Page<Answer> getAllAnswers(Pageable pageable) {
        return answerRepository.findAllWithAuthorAndQuestion(pageable);
    }
    
    @Override
    public void deleteAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("回答不存在"));
        
        // 检查权限：只有回答作者或管理员可以删除
        if (!answer.getAuthor().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (user.getRole() != User.UserRole.ADMIN && user.getRole() != User.UserRole.MODERATOR) {
                throw new RuntimeException("无权限删除此回答");
            }
        }
        
        Question question = answer.getQuestion();
        
        // 如果删除的是已采纳的回答，需要更新问题状态
        if (answer.getAccepted()) {
            question.setSolved(false);
        }
        
        // 更新问题的回答数量
        question.setAnswerCount(Math.max(0, question.getAnswerCount() - 1));
        questionRepository.save(question);
        
        // 删除回答
        answerRepository.delete(answer);
    }
    
    @Override
    public long getTotalAnswers() {
        return answerRepository.count();
    }
    
    @Override
    public long getAcceptedAnswers() {
        return answerRepository.countByAcceptedTrue();
    }
    
    @Override
    public Map<String, Long> getAnswerStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalAnswers", getTotalAnswers());
        stats.put("acceptedAnswers", getAcceptedAnswers());
        stats.put("unacceptedAnswers", getTotalAnswers() - getAcceptedAnswers());
        
        return stats;
    }
} 