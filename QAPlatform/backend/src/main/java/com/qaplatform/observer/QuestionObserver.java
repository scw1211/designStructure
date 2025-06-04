package com.qaplatform.observer;

import com.qaplatform.model.Question;
import com.qaplatform.model.Answer;

/**
 * 问题观察者接口
 * 实现观察者模式，当问题有新回答时通知关注者
 */
public interface QuestionObserver {
    
    /**
     * 当问题有新回答时通知
     * @param question 问题
     * @param newAnswer 新回答
     */
    void onNewAnswer(Question question, Answer newAnswer);
    
    /**
     * 当问题被解决时通知
     * @param question 问题
     */
    void onQuestionSolved(Question question);
    
    /**
     * 当问题被关闭时通知
     * @param question 问题
     */
    void onQuestionClosed(Question question);
} 