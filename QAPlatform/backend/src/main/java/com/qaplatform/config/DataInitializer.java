package com.qaplatform.config;

import com.qaplatform.model.Tag;
import com.qaplatform.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化配置
 * 应用启动时自动创建默认数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeTags();
    }

    /**
     * 初始化默认标签
     */
    private void initializeTags() {
        // 检查是否已有标签数据，避免重复创建
        if (tagRepository.count() > 0) {
            System.out.println("标签数据已存在，跳过初始化");
            return;
        }

        System.out.println("开始初始化默认标签数据...");

        // 创建默认标签
        String[][] defaultTags = {
            {"Java", "Java编程语言相关问题"},
            {"Spring", "Spring框架相关问题"},
            {"MySQL", "MySQL数据库相关问题"},
            {"前端", "前端开发相关问题"},
            {"Vue", "Vue.js框架相关问题"},
            {"JavaScript", "JavaScript编程语言相关问题"},
            {"Python", "Python编程语言相关问题"},
            {"算法", "算法和数据结构相关问题"},
            {"数据库", "数据库设计和优化相关问题"},
            {"网络", "计算机网络相关问题"},
            {"Linux", "Linux系统相关问题"},
            {"Git", "Git版本控制相关问题"},
            {"API", "API设计和开发相关问题"},
            {"微服务", "微服务架构相关问题"},
            {"Redis", "Redis缓存相关问题"},
            {"Docker", "Docker容器化相关问题"},
            {"安全", "信息安全相关问题"},
            {"性能优化", "系统性能优化相关问题"},
            {"移动开发", "移动应用开发相关问题"},
            {"机器学习", "机器学习和AI相关问题"}
        };

        for (String[] tagData : defaultTags) {
            Tag tag = new Tag();
            tag.setName(tagData[0]);
            tag.setDescription(tagData[1]);
            tag.setUsageCount(0);
            tagRepository.save(tag);
        }

        System.out.println("默认标签数据初始化完成，共创建 " + defaultTags.length + " 个标签");
    }
} 