package com.qaplatform.strategy;

import com.qaplatform.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索策略上下文
 * 负责管理和执行不同的搜索策略
 */
@Component
public class SearchContext {
    
    private final Map<String, SearchStrategy> strategies;
    private SearchStrategy currentStrategy;
    
    @Autowired
    public SearchContext(List<SearchStrategy> strategyList) {
        // 将所有策略放入Map中，便于查找
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(SearchStrategy::getStrategyName, strategy -> strategy));
        
        // 默认使用全文搜索策略
        this.currentStrategy = strategies.get("FULL_TEXT_SEARCH");
    }
    
    /**
     * 设置搜索策略
     * @param strategyName 策略名称
     */
    public void setStrategy(String strategyName) {
        SearchStrategy strategy = strategies.get(strategyName);
        if (strategy != null) {
            this.currentStrategy = strategy;
        } else {
            throw new IllegalArgumentException("未知的搜索策略: " + strategyName);
        }
    }
    
    /**
     * 执行搜索
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    public Page<Question> executeSearch(String keyword, Pageable pageable) {
        return currentStrategy.search(keyword, pageable);
    }
    
    /**
     * 执行搜索（指定策略）
     * @param keyword 搜索关键词
     * @param strategyName 策略名称
     * @param pageable 分页参数
     * @return 搜索结果
     */
    public Page<Question> search(String keyword, String strategyName, Pageable pageable) {
        SearchStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            // 如果策略不存在，使用默认策略
            strategy = strategies.get("FULL_TEXT_SEARCH");
            if (strategy == null) {
                // 如果默认策略也不存在，使用第一个可用策略
                strategy = strategies.values().iterator().next();
            }
        }
        return strategy.search(keyword, pageable);
    }
    
    /**
     * 获取所有可用的策略名称
     * @return 策略名称列表
     */
    public List<String> getAvailableStrategies() {
        return strategies.keySet().stream().sorted().collect(Collectors.toList());
    }
} 