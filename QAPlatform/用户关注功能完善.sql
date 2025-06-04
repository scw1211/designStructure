-- 用户关注功能完善SQL脚本
-- 创建用户关注表

-- 1. 用户关注关系表
CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL COMMENT '关注者ID',
    following_id BIGINT NOT NULL COMMENT '被关注者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_follow (follower_id, following_id),
    INDEX idx_follower (follower_id),
    INDEX idx_following (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- 2. 创建视图：用户统计信息
CREATE OR REPLACE VIEW user_stats_view AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.reputation,
    COALESCE(q_count.question_count, 0) as question_count,
    COALESCE(a_count.answer_count, 0) as answer_count,
    COALESCE(a_accepted.accepted_answers, 0) as accepted_answers,
    COALESCE(followers.followers_count, 0) as followers_count,
    COALESCE(following.following_count, 0) as following_count
FROM users u
LEFT JOIN (
    SELECT author_id, COUNT(*) as question_count 
    FROM questions 
    GROUP BY author_id
) q_count ON u.id = q_count.author_id
LEFT JOIN (
    SELECT author_id, COUNT(*) as answer_count 
    FROM answers 
    GROUP BY author_id
) a_count ON u.id = a_count.author_id
LEFT JOIN (
    SELECT author_id, COUNT(*) as accepted_answers 
    FROM answers 
    WHERE accepted = true 
    GROUP BY author_id
) a_accepted ON u.id = a_accepted.author_id
LEFT JOIN (
    SELECT following_id, COUNT(*) as followers_count 
    FROM user_follows 
    GROUP BY following_id
) followers ON u.id = followers.following_id
LEFT JOIN (
    SELECT follower_id, COUNT(*) as following_count 
    FROM user_follows 
    GROUP BY follower_id
) following ON u.id = following.follower_id;

-- 3. 为提高查询性能，创建一些有用的索引
ALTER TABLE answers ADD INDEX idx_author_accepted (author_id, accepted);
ALTER TABLE questions ADD INDEX idx_author_created (author_id, created_at);

-- 4. 插入一些测试关注数据（可选）
-- INSERT INTO user_follows (follower_id, following_id) VALUES 
-- (2, 1),  -- 用户2关注用户1
-- (1, 2);  -- 用户1关注用户2

-- 查看创建的表
SHOW TABLES LIKE '%follow%';

-- 查看用户统计视图的结构
DESCRIBE user_stats_view; 