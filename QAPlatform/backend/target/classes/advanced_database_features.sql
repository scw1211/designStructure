-- =============================================
-- 高级数据库功能实现 SQL 脚本
-- =============================================

-- 1. 创建用户统计汇总视图
DROP VIEW IF EXISTS v_user_statistics;
CREATE VIEW v_user_statistics AS
SELECT 
    u.id as user_id,
    u.username,
    u.email,
    u.reputation,
    COALESCE(q.question_count, 0) as question_count,
    COALESCE(a.answer_count, 0) as answer_count,
    COALESCE(aa.accepted_count, 0) as accepted_answer_count,
    COALESCE(uv.total_votes, 0) as total_votes_received,
    CASE 
        WHEN u.reputation >= 10000 THEN 'Expert'
        WHEN u.reputation >= 5000 THEN 'Advanced'
        WHEN u.reputation >= 1000 THEN 'Intermediate'
        WHEN u.reputation >= 100 THEN 'Beginner'
        ELSE 'Newbie'
    END as user_level,
    u.created_at,
    DATEDIFF(NOW(), u.created_at) as days_since_joined
FROM users u
LEFT JOIN (
    SELECT author_id, COUNT(*) as question_count 
    FROM questions 
    GROUP BY author_id
) q ON u.id = q.author_id
LEFT JOIN (
    SELECT author_id, COUNT(*) as answer_count 
    FROM answers 
    GROUP BY author_id
) a ON u.id = a.author_id
LEFT JOIN (
    SELECT author_id, COUNT(*) as accepted_count 
    FROM answers 
    WHERE accepted = true 
    GROUP BY author_id
) aa ON u.id = aa.author_id
LEFT JOIN (
    SELECT 
        q.author_id,
        SUM(COALESCE(q.votes, 0)) + SUM(COALESCE(a_votes.vote_sum, 0)) as total_votes
    FROM questions q
    LEFT JOIN (
        SELECT question_id, SUM(COALESCE(vote_count, 0)) as vote_sum
        FROM answers
        GROUP BY question_id
    ) a_votes ON q.id = a_votes.question_id
    GROUP BY q.author_id
) uv ON u.id = uv.author_id;

-- 2. 创建热门问题推荐视图
DROP VIEW IF EXISTS v_trending_questions;
CREATE VIEW v_trending_questions AS
SELECT 
    q.id,
    q.title,
    q.content,
    q.author_id,
    u.username as author_name,
    q.view_count,
    q.answer_count,
    COALESCE(q.votes, 0) as votes,
    q.created_at,
    -- 热度计算：浏览量*0.1 + 回答数*2 + 投票数*3 + 时间衰减
    (COALESCE(q.view_count, 0) * 0.1 + 
     COALESCE(q.answer_count, 0) * 2 + 
     COALESCE(q.votes, 0) * 3) * 
    (1 - LEAST(DATEDIFF(NOW(), q.created_at) / 30.0, 0.8)) as trending_score,
    
    -- 标签聚合
    GROUP_CONCAT(t.name ORDER BY t.name SEPARATOR ', ') as tags
FROM questions q
JOIN users u ON q.author_id = u.id
LEFT JOIN question_tags qt ON q.id = qt.question_id
LEFT JOIN tags t ON qt.tag_id = t.id
WHERE q.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY q.id, q.title, q.content, q.author_id, u.username, q.view_count, q.answer_count, q.votes, q.created_at
HAVING trending_score > 1
ORDER BY trending_score DESC;

-- 3. 创建专家用户识别视图
DROP VIEW IF EXISTS v_expert_users;
CREATE VIEW v_expert_users AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.reputation,
    us.question_count,
    us.answer_count,
    us.accepted_answer_count,
    us.total_votes_received,
    us.user_level,
    -- 专家评分算法
    (COALESCE(us.accepted_answer_count, 0) * 10 + 
     COALESCE(us.total_votes_received, 0) * 2 + 
     u.reputation * 0.1) as expert_score,
    
    -- 活跃度：最近30天的活动
    COALESCE(recent.recent_activity, 0) as recent_activity_count,
    
    -- 专业领域（最常用的标签）
    COALESCE(top_tags.expertise_areas, 'General') as expertise_areas
FROM users u
JOIN v_user_statistics us ON u.id = us.user_id
LEFT JOIN (
    SELECT 
        author_id,
        COUNT(*) as recent_activity
    FROM (
        SELECT author_id, created_at FROM questions WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        UNION ALL
        SELECT author_id, created_at FROM answers WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    ) recent_acts
    GROUP BY author_id
) recent ON u.id = recent.author_id
LEFT JOIN (
    SELECT 
        user_id,
        SUBSTRING_INDEX(GROUP_CONCAT(tag_name ORDER BY tag_count DESC SEPARATOR ', '), ', ', 3) as expertise_areas
    FROM (
        SELECT 
            q.author_id as user_id,
            t.name as tag_name,
            COUNT(*) as tag_count
        FROM questions q
        JOIN question_tags qt ON q.id = qt.question_id
        JOIN tags t ON qt.tag_id = t.id
        GROUP BY q.author_id, t.name
        ORDER BY q.author_id, tag_count DESC
    ) user_tags
    GROUP BY user_id
) top_tags ON u.id = top_tags.user_id
WHERE u.reputation >= 500 OR us.accepted_answer_count >= 5
ORDER BY expert_score DESC;

-- 4. 创建标签统计分析视图
DROP VIEW IF EXISTS v_tag_statistics;
CREATE VIEW v_tag_statistics AS
SELECT 
    t.id,
    t.name,
    t.description,
    t.usage_count,
    COALESCE(qt_stats.question_count, 0) as actual_question_count,
    COALESCE(qt_stats.avg_views, 0) as avg_question_views,
    COALESCE(qt_stats.avg_answers, 0) as avg_answers_per_question,
    COALESCE(qt_stats.total_votes, 0) as total_tag_votes,
    
    -- 标签热度评分
    (COALESCE(qt_stats.question_count, 0) * 2 + 
     COALESCE(qt_stats.total_votes, 0) * 0.5 + 
     COALESCE(qt_stats.avg_views, 0) * 0.1) as popularity_score,
    
    -- 标签活跃度（最近30天）
    COALESCE(recent_usage.recent_questions, 0) as recent_questions,
    
    -- 顶级贡献者
    COALESCE(top_contributor.top_contributor_name, 'None') as top_contributor
FROM tags t
LEFT JOIN (
    SELECT 
        qt.tag_id,
        COUNT(DISTINCT q.id) as question_count,
        AVG(COALESCE(q.view_count, 0)) as avg_views,
        AVG(COALESCE(q.answer_count, 0)) as avg_answers,
        SUM(COALESCE(q.votes, 0)) as total_votes
    FROM question_tags qt
    JOIN questions q ON qt.question_id = q.id
    GROUP BY qt.tag_id
) qt_stats ON t.id = qt_stats.tag_id
LEFT JOIN (
    SELECT 
        qt.tag_id,
        COUNT(*) as recent_questions
    FROM question_tags qt
    JOIN questions q ON qt.question_id = q.id
    WHERE q.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    GROUP BY qt.tag_id
) recent_usage ON t.id = recent_usage.tag_id
LEFT JOIN (
    SELECT 
        qt.tag_id,
        u.username as top_contributor_name
    FROM question_tags qt
    JOIN questions q ON qt.question_id = q.id
    JOIN users u ON q.author_id = u.id
    GROUP BY qt.tag_id, u.id, u.username
    HAVING COUNT(*) = (
        SELECT MAX(question_count) 
        FROM (
            SELECT COUNT(*) as question_count
            FROM question_tags qt2
            JOIN questions q2 ON qt2.question_id = q2.id
            WHERE qt2.tag_id = qt.tag_id
            GROUP BY q2.author_id
        ) max_counts
    )
) top_contributor ON t.id = top_contributor.tag_id
ORDER BY popularity_score DESC;

-- 5. 创建活跃度统计视图
DROP VIEW IF EXISTS v_activity_statistics;
CREATE VIEW v_activity_statistics AS
SELECT 
    DATE(created_at) as date,
    'question' as activity_type,
    COUNT(*) as count,
    AVG(COALESCE(view_count, 0)) as avg_engagement
FROM questions 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 90 DAY)
GROUP BY DATE(created_at)

UNION ALL

SELECT 
    DATE(created_at) as date,
    'answer' as activity_type,
    COUNT(*) as count,
    AVG(COALESCE(vote_count, 0)) as avg_engagement
FROM answers 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 90 DAY)
GROUP BY DATE(created_at)

ORDER BY date DESC, activity_type;

-- 6. 创建徽章定义表
CREATE TABLE IF NOT EXISTS badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(20) DEFAULT 'bronze',
    category ENUM('question', 'answer', 'vote', 'participation', 'social', 'moderation') DEFAULT 'participation',
    requirement_type ENUM('count', 'score', 'ratio', 'special') DEFAULT 'count',
    requirement_value INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入徽章数据
INSERT IGNORE INTO badges (name, description, color, category, requirement_type, requirement_value) VALUES
-- 提问徽章
('好奇宝宝', '提出第1个问题', 'bronze', 'question', 'count', 1),
('问题达人', '提出10个问题', 'silver', 'question', 'count', 10),
('问题专家', '提出50个问题', 'gold', 'question', 'count', 50),
('问题大师', '提出100个问题', 'platinum', 'question', 'count', 100),

-- 回答徽章
('初出茅庐', '回答第1个问题', 'bronze', 'answer', 'count', 1),
('热心助人', '回答10个问题', 'silver', 'answer', 'count', 10),
('回答专家', '回答50个问题', 'gold', 'answer', 'count', 50),
('回答大师', '回答100个问题', 'platinum', 'answer', 'count', 100),

-- 投票徽章
('支持者', '投出第1票', 'bronze', 'vote', 'count', 1),
('评判官', '投出50票', 'silver', 'vote', 'count', 50),
('公正使者', '投出200票', 'gold', 'vote', 'count', 200),

-- 参与徽章
('新人报到', '注册账号', 'bronze', 'participation', 'special', 0),
('活跃用户', '连续7天活跃', 'silver', 'participation', 'special', 7),
('忠实用户', '连续30天活跃', 'gold', 'participation', 'special', 30),

-- 社交徽章
('受欢迎', '获得10个赞', 'silver', 'social', 'count', 10),
('万人迷', '获得100个赞', 'gold', 'social', 'count', 100),

-- 管理徽章
('维护者', '编辑第1个帖子', 'bronze', 'moderation', 'count', 1),
('审核员', '进行第1次审核', 'silver', 'moderation', 'count', 1);

-- 7. 创建用户徽章关联表
CREATE TABLE IF NOT EXISTS user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_badge (user_id, badge_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE
);

-- 8. 创建活动日志表
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type ENUM('question_posted', 'answer_posted', 'vote_cast', 'comment_posted', 'badge_earned', 'login', 'profile_updated') NOT NULL,
    target_type ENUM('question', 'answer', 'comment', 'user', 'badge') NULL,
    target_id BIGINT NULL,
    description TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_activity (user_id, created_at),
    INDEX idx_activity_type (activity_type, created_at)
);

-- 9. 创建用户统计表
CREATE TABLE IF NOT EXISTS user_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    question_count INT DEFAULT 0,
    answer_count INT DEFAULT 0,
    accepted_answer_count INT DEFAULT 0,
    vote_count INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    reputation_this_month INT DEFAULT 0,
    reputation_this_week INT DEFAULT 0,
    consecutive_days_active INT DEFAULT 0,
    last_activity_date DATE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 10. 初始化现有用户的统计数据
INSERT INTO user_statistics (user_id, question_count, answer_count, accepted_answer_count, last_activity_date)
SELECT 
    u.id,
    COALESCE(q.count, 0),
    COALESCE(a.count, 0),
    COALESCE(aa.count, 0),
    GREATEST(
        COALESCE(q.last_date, '1970-01-01'),
        COALESCE(a.last_date, '1970-01-01')
    )
FROM users u
LEFT JOIN (
    SELECT author_id, COUNT(*) as count, MAX(DATE(created_at)) as last_date
    FROM questions GROUP BY author_id
) q ON u.id = q.author_id
LEFT JOIN (
    SELECT author_id, COUNT(*) as count, MAX(DATE(created_at)) as last_date
    FROM answers GROUP BY author_id
) a ON u.id = a.author_id
LEFT JOIN (
    SELECT author_id, COUNT(*) as count
    FROM answers WHERE accepted = true GROUP BY author_id
) aa ON u.id = aa.author_id
ON DUPLICATE KEY UPDATE
    question_count = VALUES(question_count),
    answer_count = VALUES(answer_count),
    accepted_answer_count = VALUES(accepted_answer_count),
    last_activity_date = VALUES(last_activity_date);

-- 11. 为现有用户颁发基础徽章
INSERT IGNORE INTO user_badges (user_id, badge_id)
SELECT u.id, b.id
FROM users u
CROSS JOIN badges b
WHERE b.name = '新人报到';

-- 为有问题的用户颁发相应徽章
INSERT IGNORE INTO user_badges (user_id, badge_id)
SELECT us.user_id, b.id
FROM user_statistics us
CROSS JOIN badges b
WHERE b.name = '好奇宝宝' AND us.question_count >= 1;

INSERT IGNORE INTO user_badges (user_id, badge_id)
SELECT us.user_id, b.id
FROM user_statistics us
CROSS JOIN badges b
WHERE b.name = '问题达人' AND us.question_count >= 10;

-- 为有回答的用户颁发相应徽章
INSERT IGNORE INTO user_badges (user_id, badge_id)
SELECT us.user_id, b.id
FROM user_statistics us
CROSS JOIN badges b
WHERE b.name = '初出茅庐' AND us.answer_count >= 1;

INSERT IGNORE INTO user_badges (user_id, badge_id)
SELECT us.user_id, b.id
FROM user_statistics us
CROSS JOIN badges b
WHERE b.name = '热心助人' AND us.answer_count >= 10;

-- 完成高级数据库功能初始化
SELECT '高级数据库功能初始化完成！' as status; 