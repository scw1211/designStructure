package com.qaplatform.repository;

import com.qaplatform.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 标签Repository接口
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 根据名称查找标签
     */
    Optional<Tag> findByName(String name);
    
    /**
     * 检查标签名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据名称模糊查询标签
     */
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword% OR t.description LIKE %:keyword%")
    List<Tag> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 根据使用次数排序获取标签
     */
    List<Tag> findAllByOrderByUsageCountDesc();
    
    /**
     * 获取使用次数前N的标签
     */
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC LIMIT :limit")
    List<Tag> findTopByUsageCount(@Param("limit") int limit);
    
    /**
     * 根据名称列表查找标签
     */
    List<Tag> findByNameIn(List<String> names);
    
    /**
     * 获取使用次数大于指定值的标签
     */
    @Query("SELECT t FROM Tag t WHERE t.usageCount > :minUsage ORDER BY t.usageCount DESC")
    List<Tag> findByUsageCountGreaterThan(@Param("minUsage") int minUsage);
    
    /**
     * 根据首字母查找标签
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(:prefix%)")
    List<Tag> findByNameStartingWith(@Param("prefix") String prefix);
    
    /**
     * 统计标签总数
     */
    @Query("SELECT COUNT(t) FROM Tag t")
    Long getTotalTagCount();
    
    /**
     * 获取未使用的标签（使用次数为0）
     */
    @Query("SELECT t FROM Tag t WHERE t.usageCount = 0")
    List<Tag> findUnusedTags();
    
    // ========== 高级分析查询方法 ==========
    
    /**
     * 获取热门标签（按使用次数排序，分页）
     */
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findTopTags(Pageable pageable);
} 