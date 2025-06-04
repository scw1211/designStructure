package com.qaplatform.decorator;

import com.qaplatform.model.Question;

/**
 * 精华问题装饰器
 * 为高质量的问题（已解决且有多个高赞回答）添加精华标记
 */
public class FeaturedQuestionDecorator extends QuestionDecorator {
    
    private static final int MIN_ANSWERS = 3;  // 最少回答数
    private static final int MIN_TOTAL_VOTES = 10;  // 回答总赞数阈值
    
    public FeaturedQuestionDecorator(Question question) {
        super(question);
    }
    
    @Override
    public String decorate() {
        if (isFeatured()) {
            return "⭐【精华】" + question.getTitle();
        }
        return question.getTitle();
    }
    
    /**
     * 判断是否为精华问题
     * @return true表示是精华问题
     */
    public boolean isFeatured() {
        if (!question.getSolved() || question.getAnswerCount() < MIN_ANSWERS) {
            return false;
        }
        
        // 计算所有回答的总赞数
        int totalVotes = question.getAnswers().stream()
            .mapToInt(answer -> answer.getVoteCount())
            .sum();
        
        return totalVotes >= MIN_TOTAL_VOTES;
    }
} 