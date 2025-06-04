package com.qaplatform.observer;

import com.qaplatform.model.Question;
import com.qaplatform.model.Answer;
import com.qaplatform.model.User;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * 通知服务
 * 实现观察者模式，负责发送各种通知
 */
@Service
@Slf4j
public class NotificationService implements QuestionObserver {
    
    @Override
    @Async  // 异步发送通知，不阻塞主流程
    public void onNewAnswer(Question question, Answer newAnswer) {
        Set<User> followers = question.getFollowers();
        
        for (User follower : followers) {
            // 跳过回答者自己
            if (follower.getId().equals(newAnswer.getAuthor().getId())) {
                continue;
            }
            
            // 发送通知（这里可以是邮件、站内信、推送等）
            sendNotification(follower, 
                String.format("您关注的问题《%s》有了新回答", question.getTitle()),
                String.format("用户 %s 回答了您关注的问题", newAnswer.getAuthor().getUsername())
            );
        }
        
        // 通知问题作者
        if (!question.getAuthor().getId().equals(newAnswer.getAuthor().getId())) {
            sendNotification(question.getAuthor(),
                String.format("您的问题《%s》有了新回答", question.getTitle()),
                String.format("用户 %s 回答了您的问题", newAnswer.getAuthor().getUsername())
            );
        }
    }
    
    @Override
    @Async
    public void onQuestionSolved(Question question) {
        Set<User> followers = question.getFollowers();
        
        for (User follower : followers) {
            sendNotification(follower,
                String.format("您关注的问题《%s》已被解决", question.getTitle()),
                "问题已被标记为已解决"
            );
        }
    }
    
    @Override
    @Async
    public void onQuestionClosed(Question question) {
        Set<User> followers = question.getFollowers();
        
        for (User follower : followers) {
            sendNotification(follower,
                String.format("您关注的问题《%s》已被关闭", question.getTitle()),
                "问题已被关闭，不再接受新的回答"
            );
        }
    }
    
    /**
     * 发送通知的具体实现
     * 在实际项目中，这里可以集成邮件、短信、推送等多种通知方式
     */
    private void sendNotification(User user, String subject, String content) {
        log.info("发送通知给用户 {}: {} - {}", user.getUsername(), subject, content);
        
        // TODO: 这里可以扩展其他通知方式
        // 1. 站内信
        // 2. WebSocket实时推送
        // 3. 手机App推送
        // 4. 短信通知
    }
} 