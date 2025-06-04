package com.qaplatform.decorator;

import com.qaplatform.model.Question;

/**
 * 问题装饰器抽象类
 * 装饰器模式：为问题对象动态添加额外功能
 */
public abstract class QuestionDecorator {
    
    protected Question question;
    
    public QuestionDecorator(Question question) {
        this.question = question;
    }
    
    /**
     * 装饰问题对象
     * @return 装饰后的问题描述
     */
    public abstract String decorate();
    
    /**
     * 获取原始问题对象
     * @return 问题对象
     */
    public Question getQuestion() {
        return question;
    }
} 