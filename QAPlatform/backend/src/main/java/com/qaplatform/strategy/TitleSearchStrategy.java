package com.qaplatform.strategy;

import com.qaplatform.model.Question;
import com.qaplatform.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * 标题搜索策略
 * 只在问题标题中搜索关键词
 */
@Component
public class TitleSearchStrategy implements SearchStrategy {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Override
    public Page<Question> search(String keyword, Pageable pageable) {
        return questionRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }
    
    @Override
    public String getStrategyName() {
        return "TITLE_SEARCH";
    }
} 