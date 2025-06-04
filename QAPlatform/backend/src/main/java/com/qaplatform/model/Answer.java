package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 回答实体类
 */
@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"author", "question", "voters"})
@ToString(exclude = {"author", "question", "voters"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private Integer voteCount = 0;  // 点赞数
    
    @Column(nullable = false)
    private Boolean accepted = false;  // 是否被采纳
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 投票关系 - 使用JoinTable配置
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "answer_votes",
        joinColumns = @JoinColumn(name = "answer_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> voters = new HashSet<>();
    
    // 添加投票
    public void addVote(User user) {
        voters.add(user);
        this.voteCount = voters.size();
    }
    
    // 取消投票
    public void removeVote(User user) {
        voters.remove(user);
        this.voteCount = voters.size();
    }
}
