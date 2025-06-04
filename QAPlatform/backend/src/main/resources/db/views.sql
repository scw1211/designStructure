-- =============================================
-- QA平台数据库视图和高级功能
-- =============================================

-- 1. 用户统计视图
CREATE OR REPLACE VIEW v_user_statistics AS
SELECT 
    u.id as user_id,
    u.username,
    u.email,
    u.reputation,
    u.role,
    u.created_at as user_created_at,
    COALESCE(q_stats.question_count, 0) as total_questions,
    COALESCE(a_stats.answer_count, 0) as total_answers,
    COALESCE(a_stats.accepted_count, 0) as accepted_answers,
    COALESCE(vote_stats.votes_received, 0) as total_votes_received,
    COALESCE(vote_stats.votes_given, 0) as total_votes_given,
    COALESCE(follow_stats.questions_followed, 0) as questions_followed,
    COALESCE(follow_stats.users_following, 0) as users_following,
    COALESCE(follow_stats.followers_count, 0) as followers_count,
    COALESCE(badge_stats.badges_count, 0) as badges_count,
    CASE 
        WHEN COALESCE(a_stats.answer_count, 0) > 0 
        THEN ROUND(COALESCE(a_stats.accepted_count, 0) * 100.0 / a_stats.answer_count, 2)
        ELSE 0 
    END as acceptance_rate,
    CASE 
        WHEN COALESCE(a_stats.answer_count, 0) > 0 
        THEN ROUND(COALESCE(vote_stats.answer_votes, 0) * 1.0 / a_stats.answer_count, 2)
        ELSE 0 
    END as average_answer_score,
    COALESCE(activity_stats.last_active_date, u.created_at) as last_active_date,
    COALESCE(activity_stats.activity_count_7d, 0) as activity_count_7d,
    COALESCE(activity_stats.activity_count_30d, 0) as activity_count_30d
FROM users u
LEFT JOIN (
    SELECT 
        author_id,
        COUNT(*) as question_count,
        SUM(votes) as question_votes
    FROM questions 
    GROUP BY author_id
) q_stats ON u.id = q_stats.author_id
LEFT JOIN (
    SELECT 
        author_id,
        COUNT(*) as answer_count,
        SUM(CASE WHEN accepted = 1 THEN 1 ELSE 0 END) as accepted_count,
        SUM(vote_count) as answer_votes
    FROM answers 
    GROUP BY author_id
) a_stats ON u.id = a_stats.author_id
LEFT JOIN (
    SELECT 
        u.id as user_id,
        COALESCE(qv.question_votes, 0) + COALESCE(av.answer_votes, 0) as votes_received,
        COALESCE(given_votes.votes_given, 0) as votes_given,
        COALESCE(av.answer_votes, 0) as answer_votes
    FROM users u
    LEFT JOIN (
        SELECT q.author_id, SUM(q.votes) as question_votes
        FROM questions q GROUP BY q.author_id
    ) qv ON u.id = qv.author_id
    LEFT JOIN (
        SELECT a.author_id, SUM(a.vote_count) as answer_votes
        FROM answers a GROUP BY a.author_id
    ) av ON u.id = av.author_id
    LEFT JOIN (
        SELECT 
            u.id as user_id,
            (SELECT COUNT(*) FROM question_votes qv WHERE qv.user_id = u.id) +
            (SELECT COUNT(*) FROM answer_votes av WHERE av.user_id = u.id) as votes_given
        FROM users u
    ) given_votes ON u.id = given_votes.user_id
) vote_stats ON u.id = vote_stats.user_id
LEFT JOIN (
    SELECT 
        u.id as user_id,
        COALESCE(qf.questions_followed, 0) as questions_followed,
        COALESCE(uf.users_following, 0) as users_following,
        COALESCE(uf_followers.followers_count, 0) as followers_count
    FROM users u
    LEFT JOIN (
        SELECT user_id, COUNT(*) as questions_followed
        FROM question_followers GROUP BY user_id
    ) qf ON u.id = qf.user_id
    LEFT JOIN (
        SELECT follower_id, COUNT(*) as users_following
        FROM user_follows GROUP BY follower_id
    ) uf ON u.id = uf.follower_id
    LEFT JOIN (
        SELECT following_id, COUNT(*) as followers_count
        FROM user_follows GROUP BY following_id
    ) uf_followers ON u.id = uf_followers.following_id
) follow_stats ON u.id = follow_stats.user_id
LEFT JOIN (
    SELECT user_id, COUNT(*) as badges_count
    FROM user_badges GROUP BY user_id
) badge_stats ON u.id = badge_stats.user_id
LEFT JOIN (
    SELECT 
        user_id,
        MAX(created_at) as last_active_date,
        SUM(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) as activity_count_7d,
        SUM(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as activity_count_30d
    FROM activity_logs 
    GROUP BY user_id
) activity_stats ON u.id = activity_stats.user_id;

-- 2. 热门问题视图
CREATE OR REPLACE VIEW v_trending_questions AS
SELECT 
    q.id,
    q.title,
    q.content,
    q.view_count,
    q.answer_count,
    q.votes,
    q.solved,
    q.created_at,
    q.updated_at,
    u.username as author_username,
    u.reputation as author_reputation,
    -- 计算热度分数（基于浏览量、回答数、投票数和时间衰减）
    (
        q.votes * 10 + 
        q.answer_count * 5 + 
        q.view_count * 0.1 +
        CASE WHEN q.solved = 1 THEN 20 ELSE 0 END
    ) * EXP(-TIMESTAMPDIFF(HOUR, q.created_at, NOW()) / 168.0) as trending_score,
    GROUP_CONCAT(t.name ORDER BY t.name) as tags
FROM questions q
JOIN users u ON q.author_id = u.id
LEFT JOIN question_tags qt ON q.id = qt.question_id
LEFT JOIN tags t ON qt.tag_id = t.id
WHERE q.closed = 0
GROUP BY q.id, q.title, q.content, q.view_count, q.answer_count, 
         q.votes, q.solved, q.created_at, q.updated_at, u.username, u.reputation
HAVING trending_score > 0
ORDER BY trending_score DESC;

-- 3. 专家用户视图
CREATE OR REPLACE VIEW v_expert_users AS
SELECT 
    u.id,
    u.username,
    u.reputation,
    u.avatar,
    vs.total_questions,
    vs.total_answers,
    vs.accepted_answers,
    vs.acceptance_rate,
    vs.total_votes_received,
    vs.badges_count,
    vs.average_answer_score,
    -- 专业度评分
    (
        vs.accepted_answers * 20 +
        vs.total_votes_received * 2 +
        vs.badges_count * 10 +
        CASE WHEN vs.acceptance_rate >= 80 THEN 50 ELSE 0 END +
        CASE WHEN vs.total_answers >= 50 THEN 30 ELSE 0 END
    ) as expertise_score,
    GROUP_CONCAT(DISTINCT t.name ORDER BY tag_stats.answer_count DESC LIMIT 5) as top_tags
FROM users u
JOIN v_user_statistics vs ON u.id = vs.user_id
LEFT JOIN (
    SELECT 
        a.author_id,
        t.id as tag_id,
        t.name as tag_name,
        COUNT(*) as answer_count
    FROM answers a
    JOIN questions q ON a.question_id = q.id
    JOIN question_tags qt ON q.id = qt.question_id
    JOIN tags t ON qt.tag_id = t.id
    GROUP BY a.author_id, t.id, t.name
) tag_stats ON u.id = tag_stats.author_id
LEFT JOIN tags t ON tag_stats.tag_id = t.id
WHERE vs.total_answers >= 10 AND vs.acceptance_rate >= 50
GROUP BY u.id, u.username, u.reputation, u.avatar, vs.total_questions, 
         vs.total_answers, vs.accepted_answers, vs.acceptance_rate, 
         vs.total_votes_received, vs.badges_count, vs.average_answer_score
ORDER BY expertise_score DESC;

-- 4. 标签统计视图
CREATE OR REPLACE VIEW v_tag_statistics AS
SELECT 
    t.id,
    t.name,
    t.description,
    t.usage_count,
    COUNT(DISTINCT q.id) as question_count,
    COUNT(DISTINCT a.id) as answer_count,
    COUNT(DISTINCT q.author_id) as unique_askers,
    COUNT(DISTINCT a.author_id) as unique_answerers,
    AVG(q.votes) as avg_question_votes,
    AVG(a.vote_count) as avg_answer_votes,
    SUM(CASE WHEN q.solved = 1 THEN 1 ELSE 0 END) as solved_questions,
    CASE 
        WHEN COUNT(DISTINCT q.id) > 0 
        THEN ROUND(SUM(CASE WHEN q.solved = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT q.id), 2)
        ELSE 0 
    END as solve_rate,
    MAX(q.created_at) as last_question_date
FROM tags t
LEFT JOIN question_tags qt ON t.id = qt.tag_id
LEFT JOIN questions q ON qt.question_id = q.id
LEFT JOIN answers a ON q.id = a.question_id
GROUP BY t.id, t.name, t.description, t.usage_count
ORDER BY t.usage_count DESC;

-- 5. 活跃度统计视图
CREATE OR REPLACE VIEW v_activity_statistics AS
SELECT 
    DATE(al.created_at) as activity_date,
    COUNT(*) as total_activities,
    COUNT(DISTINCT al.user_id) as active_users,
    SUM(CASE WHEN al.activity_type = 'ASK_QUESTION' THEN 1 ELSE 0 END) as questions_asked,
    SUM(CASE WHEN al.activity_type = 'ANSWER_QUESTION' THEN 1 ELSE 0 END) as answers_given,
    SUM(CASE WHEN al.activity_type = 'VOTE_UP' THEN 1 ELSE 0 END) as votes_cast,
    SUM(CASE WHEN al.activity_type = 'VIEW_QUESTION' THEN 1 ELSE 0 END) as questions_viewed,
    SUM(CASE WHEN al.activity_type = 'LOGIN' THEN 1 ELSE 0 END) as logins
FROM activity_logs al
WHERE al.created_at >= DATE_SUB(NOW(), INTERVAL 90 DAY)
GROUP BY DATE(al.created_at)
ORDER BY activity_date DESC;

-- 6. 创建索引优化查询性能
CREATE INDEX idx_questions_trending ON questions(created_at, votes, view_count, answer_count);
CREATE INDEX idx_answers_author_votes ON answers(author_id, vote_count, accepted);
CREATE INDEX idx_activity_logs_user_date ON activity_logs(user_id, created_at, activity_type);
CREATE INDEX idx_user_statistics_composite ON user_statistics(total_answers, acceptance_rate, total_votes_received);
CREATE INDEX idx_question_tags_composite ON question_tags(question_id, tag_id);
CREATE INDEX idx_user_badges_composite ON user_badges(user_id, badge_id);

-- 7. 全文搜索索引
ALTER TABLE questions ADD FULLTEXT(title, content);
ALTER TABLE answers ADD FULLTEXT(content);
ALTER TABLE tags ADD FULLTEXT(name, description); 