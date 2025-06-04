package com.qaplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 问题实体类
 */
@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"author", "answers", "tags", "followers", "images"})
@ToString(exclude = {"author", "answers", "tags", "followers", "images"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private Integer viewCount = 0;  // 浏览次数
    
    @Column(nullable = false)
    private Integer answerCount = 0;  // 回答数量
    
    @Column(nullable = false)
    private Boolean solved = false;  // 是否已解决
    
    @Column(nullable = false)
    private Boolean closed = false;  // 是否关闭
    
    @Column(nullable = false)
    private Integer votes = 0;  // 投票数（点赞数）
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "email"})
    private User author;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Answer> answers = new HashSet<>();
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
        name = "question_tags",
        joinColumns = @JoinColumn(name = "question_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "question_followers",
        joinColumns = @JoinColumn(name = "question_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> followers = new HashSet<>();  // 关注该问题的用户
    
    @ManyToMany
    @JoinTable(
        name = "question_votes",
        joinColumns = @JoinColumn(name = "question_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> voters = new HashSet<>();  // 给问题投票的用户
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<QuestionImage> images = new HashSet<>();  // 问题相关图片
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 添加标签
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getQuestions().add(this);
    }
    
    // 移除标签
    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getQuestions().remove(this);
    }
    
    // 添加关注者
    public void addFollower(User user) {
        followers.add(user);
        user.getFollowedQuestions().add(this);
    }
    
    // 移除关注者
    public void removeFollower(User user) {
        followers.remove(user);
        user.getFollowedQuestions().remove(this);
    }
    
    // 增加浏览次数
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    // 更新回答数量
    public void updateAnswerCount() {
        this.answerCount = answers.size();
    }
    
    // 添加投票
    public void addVote(User user) {
        voters.add(user);
        user.getVotedQuestions().add(this);
        this.votes = voters.size();
    }
    
    // 取消投票
    public void removeVote(User user) {
        voters.remove(user);
        user.getVotedQuestions().remove(this);
        this.votes = voters.size();
    }
} 