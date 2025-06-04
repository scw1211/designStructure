package com.qaplatform.decorator;

import com.qaplatform.model.Question;

/**
 * 热门问题装饰器
 * 为浏览量高的问题添加热门标记
 */
public class HotQuestionDecorator extends QuestionDecorator {
    
    private static final int HOT_THRESHOLD = 100;  // 热门问题的浏览量阈值
    
    public HotQuestionDecorator(Question question) {
        super(question);
    }
    
    @Override
    public String decorate() {
        if (question.getViewCount() >= HOT_THRESHOLD) {
            return "🔥【热门】" + question.getTitle();
        }
        return question.getTitle();
    }
    
    /**
     * 判断是否为热门问题
     * @return true表示是热门问题
     */
    public boolean isHot() {
        return question.getViewCount() >= HOT_THRESHOLD;
    }
} 