package com.qaplatform.strategy;

import com.qaplatform.model.Question;
import com.qaplatform.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * 全文搜索策略
 * 在问题的标题、内容和标签中搜索关键词
 */
@Component
public class FullTextSearchStrategy implements SearchStrategy {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Override
    public Page<Question> search(String keyword, Pageable pageable) {
        return questionRepository.searchQuestions(keyword, pageable);
    }
    
    @Override
    public String getStrategyName() {
        return "FULL_TEXT_SEARCH";
    }
} 