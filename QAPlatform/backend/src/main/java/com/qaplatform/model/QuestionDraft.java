package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题草稿实体类
 * 存储用户在提问页面的草稿内容
 */
@Entity
@Table(name = "question_drafts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"author"})
@ToString(exclude = {"author"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QuestionDraft {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 200)
    private String title;  // 草稿标题
    
    @Column(columnDefinition = "TEXT")
    private String content;  // 草稿内容
    
    @Column(columnDefinition = "JSON")
    private String tags;  // 标签列表，以JSON格式存储
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "email"})
    private User author;  // 草稿作者
    
    @Column(nullable = false)
    private Boolean autoSaved = false;  // 是否自动保存
    
    @Column(length = 100)
    private String draftName;  // 草稿名称（用户自定义）
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 构造函数 - 创建新草稿
     */
    public QuestionDraft(String title, String content, String tags, User author) {
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.author = author;
        this.autoSaved = true;
    }
    
    /**
     * 构造函数 - 创建命名草稿
     */
    public QuestionDraft(String title, String content, String tags, User author, String draftName) {
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.author = author;
        this.draftName = draftName;
        this.autoSaved = false;
    }
    
    /**
     * 检查草稿是否为空
     */
    public boolean isEmpty() {
        return (title == null || title.trim().isEmpty()) && 
               (content == null || content.trim().isEmpty()) &&
               (tags == null || tags.trim().equals("[]"));
    }
    
    /**
     * 获取草稿摘要（用于显示列表）
     */
    public String getSummary() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        } else if (content != null && !content.trim().isEmpty()) {
            String summary = content.trim();
            return summary.length() > 50 ? summary.substring(0, 47) + "..." : summary;
        }
        return "空草稿";
    }
} 