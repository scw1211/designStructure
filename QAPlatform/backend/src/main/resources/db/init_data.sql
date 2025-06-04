-- =============================================
-- QA平台初始化数据
-- =============================================

-- 1. 初始化徽章数据
INSERT INTO badges (name, description, icon_url, type, level, required_count, active) VALUES
-- 提问相关徽章
('Curious', '提出了第一个问题', '/icons/badges/curious.png', 'QUESTION', 'BRONZE', 1, 1),
('Inquisitive', '提出了10个问题', '/icons/badges/inquisitive.png', 'QUESTION', 'SILVER', 10, 1),
('Question Master', '提出了50个问题', '/icons/badges/question_master.png', 'QUESTION', 'GOLD', 50, 1),
('Popular Question', '问题获得了100次点赞', '/icons/badges/popular_question.png', 'QUESTION', 'GOLD', 100, 1),

-- 回答相关徽章
('Helper', '提供了第一个回答', '/icons/badges/helper.png', 'ANSWER', 'BRONZE', 1, 1),
('Teacher', '有10个回答被采纳', '/icons/badges/teacher.png', 'ANSWER', 'SILVER', 10, 1),
('Guru', '有50个回答被采纳', '/icons/badges/guru.png', 'ANSWER', 'GOLD', 50, 1),
('Expert', '回答采纳率达到80%且有50个被采纳的回答', '/icons/badges/expert.png', 'ANSWER', 'PLATINUM', 50, 1),
('Scholar', '有100个回答被采纳', '/icons/badges/scholar.png', 'ANSWER', 'DIAMOND', 100, 1),

-- 投票相关徽章
('Supporter', '给出了第一个赞', '/icons/badges/supporter.png', 'VOTE', 'BRONZE', 1, 1),
('Good Judge', '投票100次', '/icons/badges/good_judge.png', 'VOTE', 'SILVER', 100, 1),
('Popular', '获得了100个赞', '/icons/badges/popular.png', 'VOTE', 'SILVER', 100, 1),
('Superstar', '获得了500个赞', '/icons/badges/superstar.png', 'VOTE', 'GOLD', 500, 1),
('Celebrity', '获得了1000个赞', '/icons/badges/celebrity.png', 'VOTE', 'PLATINUM', 1000, 1),

-- 参与相关徽章
('Active Member', '连续7天活跃', '/icons/badges/active_member.png', 'PARTICIPATION', 'BRONZE', 7, 1),
('Dedicated', '连续30天活跃', '/icons/badges/dedicated.png', 'PARTICIPATION', 'SILVER', 30, 1),
('Veteran', '连续100天活跃', '/icons/badges/veteran.png', 'PARTICIPATION', 'GOLD', 100, 1),
('Enthusiast', '访问网站100天', '/icons/badges/enthusiast.png', 'PARTICIPATION', 'SILVER', 100, 1),

-- 社交相关徽章
('Social', '关注了第一个问题', '/icons/badges/social.png', 'SOCIAL', 'BRONZE', 1, 1),
('Follower', '关注了10个问题', '/icons/badges/follower.png', 'SOCIAL', 'SILVER', 10, 1),
('Networking', '关注了第一个用户', '/icons/badges/networking.png', 'SOCIAL', 'BRONZE', 1, 1),
('Influential', '有50个粉丝', '/icons/badges/influential.png', 'SOCIAL', 'GOLD', 50, 1),

-- 管理相关徽章
('Editor', '编辑了第一个帖子', '/icons/badges/editor.png', 'MODERATION', 'BRONZE', 1, 1),
('Reviewer', '审核了100个帖子', '/icons/badges/reviewer.png', 'MODERATION', 'SILVER', 100, 1),
('Moderator', '处理了500个举报', '/icons/badges/moderator.png', 'MODERATION', 'GOLD', 500, 1);

-- 2. 创建一些高级索引
-- 复合索引优化查询性能
CREATE INDEX idx_questions_author_created ON questions(author_id, created_at);
CREATE INDEX idx_questions_solved_votes ON questions(solved, votes DESC);
CREATE INDEX idx_answers_question_votes ON answers(question_id, vote_count DESC);
CREATE INDEX idx_answers_author_accepted ON answers(author_id, accepted, created_at);
CREATE INDEX idx_activity_logs_type_date ON activity_logs(activity_type, created_at);
CREATE INDEX idx_user_statistics_reputation ON user_statistics(total_votes_received, acceptance_rate);

-- 分区索引（如果数据量很大）
-- ALTER TABLE activity_logs PARTITION BY RANGE (YEAR(created_at)) (
--     PARTITION p2023 VALUES LESS THAN (2024),
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 3. 创建一些实用的存储函数
DELIMITER //

-- 计算用户等级的函数
CREATE FUNCTION fn_get_user_level(p_reputation INT) 
RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE user_level VARCHAR(20);
    
    CASE
        WHEN p_reputation < 50 THEN SET user_level = 'Newbie';
        WHEN p_reputation < 200 THEN SET user_level = 'Beginner';
        WHEN p_reputation < 500 THEN SET user_level = 'Contributor';
        WHEN p_reputation < 1000 THEN SET user_level = 'Regular';
        WHEN p_reputation < 2000 THEN SET user_level = 'Trusted';
        WHEN p_reputation < 5000 THEN SET user_level = 'Expert';
        WHEN p_reputation < 10000 THEN SET user_level = 'Elite';
        ELSE SET user_level = 'Legend';
    END CASE;
    
    RETURN user_level;
END//

-- 计算两个用户的相似度函数
CREATE FUNCTION fn_calculate_user_similarity(p_user1_id BIGINT, p_user2_id BIGINT)
RETURNS DECIMAL(5,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE similarity DECIMAL(5,2) DEFAULT 0.0;
    DECLARE common_tags INT DEFAULT 0;
    DECLARE total_tags INT DEFAULT 0;
    DECLARE common_questions INT DEFAULT 0;
    
    -- 基于共同标签计算相似度
    SELECT COUNT(DISTINCT t1.tag_id) INTO common_tags
    FROM (
        SELECT DISTINCT qt.tag_id
        FROM questions q
        JOIN question_tags qt ON q.id = qt.question_id
        WHERE q.author_id = p_user1_id
        UNION
        SELECT DISTINCT qt.tag_id
        FROM answers a
        JOIN questions q ON a.question_id = q.id
        JOIN question_tags qt ON q.id = qt.question_id
        WHERE a.author_id = p_user1_id
    ) t1
    INNER JOIN (
        SELECT DISTINCT qt.tag_id
        FROM questions q
        JOIN question_tags qt ON q.id = qt.question_id
        WHERE q.author_id = p_user2_id
        UNION
        SELECT DISTINCT qt.tag_id
        FROM answers a
        JOIN questions q ON a.question_id = q.id
        JOIN question_tags qt ON q.id = qt.question_id
        WHERE a.author_id = p_user2_id
    ) t2 ON t1.tag_id = t2.tag_id;
    
    SELECT COUNT(DISTINCT tag_id) INTO total_tags
    FROM (
        SELECT DISTINCT qt.tag_id
        FROM questions q
        JOIN question_tags qt ON q.id = qt.question_id
        WHERE q.author_id IN (p_user1_id, p_user2_id)
        UNION
        SELECT DISTINCT qt.tag_id
        FROM answers a
        JOIN questions q ON a.question_id = q.id
        JOIN question_tags qt ON q.id = qt.question_id
        WHERE a.author_id IN (p_user1_id, p_user2_id)
    ) all_tags;
    
    -- 基于共同关注的问题
    SELECT COUNT(*) INTO common_questions
    FROM question_followers qf1
    JOIN question_followers qf2 ON qf1.question_id = qf2.question_id
    WHERE qf1.user_id = p_user1_id AND qf2.user_id = p_user2_id;
    
    -- 计算综合相似度
    IF total_tags > 0 THEN
        SET similarity = (common_tags * 1.0 / total_tags) * 70 + (common_questions * 2.0) + 10;
    ELSE
        SET similarity = common_questions * 2.0;
    END IF;
    
    -- 限制在0-100之间
    IF similarity > 100 THEN SET similarity = 100; END IF;
    
    RETURN similarity;
END//

-- 获取问题推荐分数的函数
CREATE FUNCTION fn_get_recommendation_score(p_question_id BIGINT, p_user_id BIGINT)
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE score DECIMAL(10,2) DEFAULT 0.0;
    DECLARE tag_match_score DECIMAL(8,2) DEFAULT 0.0;
    DECLARE author_reputation INT DEFAULT 0;
    DECLARE question_quality DECIMAL(8,2) DEFAULT 0.0;
    DECLARE time_factor DECIMAL(5,4) DEFAULT 0.0;
    DECLARE hours_old INT DEFAULT 0;
    
    -- 获取问题基本信息
    SELECT 
        u.reputation,
        q.votes * 2 + q.view_count * 0.01,
        TIMESTAMPDIFF(HOUR, q.created_at, NOW())
    INTO author_reputation, question_quality, hours_old
    FROM questions q
    JOIN users u ON q.author_id = u.id
    WHERE q.id = p_question_id;
    
    -- 计算标签匹配分数
    SELECT COALESCE(SUM(user_tag_activity.activity_score), 0) INTO tag_match_score
    FROM question_tags qt
    JOIN (
        SELECT 
            t.id as tag_id,
            COALESCE(uq.question_count * 1, 0) + COALESCE(ua.answer_count * 2, 0) as activity_score
        FROM tags t
        LEFT JOIN (
            SELECT qt.tag_id, COUNT(*) as question_count
            FROM questions q
            JOIN question_tags qt ON q.id = qt.question_id
            WHERE q.author_id = p_user_id
            GROUP BY qt.tag_id
        ) uq ON t.id = uq.tag_id
        LEFT JOIN (
            SELECT qt.tag_id, COUNT(*) as answer_count
            FROM answers a
            JOIN questions q ON a.question_id = q.id
            JOIN question_tags qt ON q.id = qt.question_id
            WHERE a.author_id = p_user_id
            GROUP BY qt.tag_id
        ) ua ON t.id = ua.tag_id
    ) user_tag_activity ON qt.tag_id = user_tag_activity.tag_id
    WHERE qt.question_id = p_question_id;
    
    -- 时间衰减因子
    SET time_factor = EXP(-hours_old / 72.0);
    
    -- 综合计算推荐分数
    SET score = (
        tag_match_score * 0.4 +
        (author_reputation / 1000.0) * 0.2 +
        question_quality * 0.3 +
        time_factor * 10
    );
    
    RETURN score;
END//

DELIMITER ;

-- 4. 创建一些有用的视图
-- 用户活跃度排行榜
CREATE OR REPLACE VIEW v_user_activity_leaderboard AS
SELECT 
    u.id,
    u.username,
    u.avatar,
    u.reputation,
    fn_get_user_level(u.reputation) as user_level,
    us.total_questions,
    us.total_answers,
    us.accepted_answers,
    us.acceptance_rate,
    us.total_votes_received,
    us.badges_count,
    us.activity_count_7d,
    us.activity_count_30d,
    -- 活跃度分数
    (
        us.total_questions * 2 +
        us.total_answers * 3 +
        us.accepted_answers * 5 +
        us.total_votes_received * 1 +
        us.badges_count * 10 +
        us.activity_count_7d * 2 +
        us.activity_count_30d * 1
    ) as activity_score
FROM users u
JOIN v_user_statistics us ON u.id = us.user_id
WHERE u.enabled = 1
ORDER BY activity_score DESC;

-- 问题质量评分视图
CREATE OR REPLACE VIEW v_question_quality_scores AS
SELECT 
    q.id,
    q.title,
    q.view_count,
    q.answer_count,
    q.votes,
    q.solved,
    q.created_at,
    u.username as author,
    u.reputation as author_reputation,
    -- 质量分数计算
    (
        q.votes * 10 +
        q.answer_count * 5 +
        q.view_count * 0.1 +
        CASE WHEN q.solved = 1 THEN 20 ELSE 0 END +
        CASE WHEN LENGTH(q.content) > 100 THEN 5 ELSE 0 END +
        (u.reputation / 100.0)
    ) as quality_score,
    GROUP_CONCAT(t.name ORDER BY t.name) as tags
FROM questions q
JOIN users u ON q.author_id = u.id
LEFT JOIN question_tags qt ON q.id = qt.question_id
LEFT JOIN tags t ON qt.tag_id = t.id
WHERE q.closed = 0
GROUP BY q.id, q.title, q.view_count, q.answer_count, q.votes, 
         q.solved, q.created_at, u.username, u.reputation
ORDER BY quality_score DESC;

-- 每日统计仪表板视图
CREATE OR REPLACE VIEW v_daily_dashboard AS
SELECT 
    CURDATE() as date,
    (SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()) as new_users_today,
    (SELECT COUNT(*) FROM questions WHERE DATE(created_at) = CURDATE()) as questions_today,
    (SELECT COUNT(*) FROM answers WHERE DATE(created_at) = CURDATE()) as answers_today,
    (SELECT COUNT(*) FROM activity_logs WHERE DATE(created_at) = CURDATE() AND activity_type = 'LOGIN') as logins_today,
    (SELECT COUNT(DISTINCT user_id) FROM activity_logs WHERE DATE(created_at) = CURDATE()) as active_users_today,
    (SELECT COUNT(*) FROM questions WHERE solved = 1 AND DATE(updated_at) = CURDATE()) as solved_today,
    (SELECT AVG(vote_count) FROM answers WHERE DATE(created_at) = CURDATE()) as avg_answer_votes_today,
    (SELECT COUNT(*) FROM user_badges WHERE DATE(created_at) = CURDATE()) as badges_awarded_today;

-- 5. 创建性能监控视图
CREATE OR REPLACE VIEW v_performance_metrics AS
SELECT 
    'Questions' as table_name,
    COUNT(*) as total_records,
    AVG(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) * COUNT(*) as recent_activity
FROM questions
UNION ALL
SELECT 
    'Answers' as table_name,
    COUNT(*) as total_records,
    AVG(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) * COUNT(*) as recent_activity
FROM answers
UNION ALL
SELECT 
    'Users' as table_name,
    COUNT(*) as total_records,
    AVG(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) * COUNT(*) as recent_activity
FROM users
UNION ALL
SELECT 
    'Activity Logs' as table_name,
    COUNT(*) as total_records,
    AVG(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) * COUNT(*) as recent_activity
FROM activity_logs; 