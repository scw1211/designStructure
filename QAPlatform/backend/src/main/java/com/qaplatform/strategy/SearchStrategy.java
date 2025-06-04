package com.qaplatform.strategy;

import com.qaplatform.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 搜索策略接口
 * 策略模式：定义不同的搜索算法
 */
public interface SearchStrategy {
    
    /**
     * 执行搜索
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<Question> search(String keyword, Pageable pageable);
    
    /**
     * 获取策略名称
     * @return 策略名称
     */
    String getStrategyName();
} 