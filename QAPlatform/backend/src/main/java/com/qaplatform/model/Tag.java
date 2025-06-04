package com.qaplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

/**
 * 标签实体类
 */
@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"questions"})
@ToString(exclude = {"questions"})
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false)
    private Integer usageCount = 0;  // 使用次数
    
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Question> questions = new HashSet<>();
    
    // 更新使用次数
    public void updateUsageCount() {
        this.usageCount = questions.size();
    }
} 