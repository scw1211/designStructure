-- =============================================
-- QA平台存储过程
-- =============================================

DELIMITER //

-- 1. 更新用户统计信息的存储过程
CREATE OR REPLACE PROCEDURE sp_update_user_statistics(IN p_user_id BIGINT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 插入或更新用户统计信息
    INSERT INTO user_statistics (
        user_id, total_questions, total_answers, accepted_answers,
        total_votes_received, total_votes_given, questions_followed,
        followers_count, following_count, acceptance_rate,
        average_answer_score, last_active_date, updated_at
    )
    SELECT 
        p_user_id,
        COALESCE(q_stats.question_count, 0),
        COALESCE(a_stats.answer_count, 0),
        COALESCE(a_stats.accepted_count, 0),
        COALESCE(vote_stats.votes_received, 0),
        COALESCE(vote_stats.votes_given, 0),
        COALESCE(follow_stats.questions_followed, 0),
        COALESCE(follow_stats.followers_count, 0),
        COALESCE(follow_stats.following_count, 0),
        CASE 
            WHEN COALESCE(a_stats.answer_count, 0) > 0 
            THEN ROUND(COALESCE(a_stats.accepted_count, 0) * 100.0 / a_stats.answer_count, 2)
            ELSE 0 
        END,
        CASE 
            WHEN COALESCE(a_stats.answer_count, 0) > 0 
            THEN ROUND(COALESCE(vote_stats.answer_votes, 0) * 1.0 / a_stats.answer_count, 2)
            ELSE 0 
        END,
        COALESCE(activity_stats.last_active_date, NOW()),
        NOW()
    FROM (SELECT p_user_id as user_id) u
    LEFT JOIN (
        SELECT author_id, COUNT(*) as question_count
        FROM questions WHERE author_id = p_user_id
    ) q_stats ON u.user_id = q_stats.author_id
    LEFT JOIN (
        SELECT 
            author_id,
            COUNT(*) as answer_count,
            SUM(CASE WHEN accepted = 1 THEN 1 ELSE 0 END) as accepted_count
        FROM answers WHERE author_id = p_user_id
    ) a_stats ON u.user_id = a_stats.author_id
    LEFT JOIN (
        SELECT 
            p_user_id as user_id,
            COALESCE(qv.question_votes, 0) + COALESCE(av.answer_votes, 0) as votes_received,
            COALESCE(given_votes.votes_given, 0) as votes_given,
            COALESCE(av.answer_votes, 0) as answer_votes
        FROM (SELECT p_user_id as user_id) dummy
        LEFT JOIN (
            SELECT SUM(votes) as question_votes
            FROM questions WHERE author_id = p_user_id
        ) qv ON 1=1
        LEFT JOIN (
            SELECT SUM(vote_count) as answer_votes
            FROM answers WHERE author_id = p_user_id
        ) av ON 1=1
        LEFT JOIN (
            SELECT 
                (SELECT COUNT(*) FROM question_votes WHERE user_id = p_user_id) +
                (SELECT COUNT(*) FROM answer_votes WHERE user_id = p_user_id) as votes_given
        ) given_votes ON 1=1
    ) vote_stats ON u.user_id = vote_stats.user_id
    LEFT JOIN (
        SELECT 
            p_user_id as user_id,
            COALESCE(qf.questions_followed, 0) as questions_followed,
            COALESCE(uf.following_count, 0) as following_count,
            COALESCE(uf_followers.followers_count, 0) as followers_count
        FROM (SELECT p_user_id as user_id) dummy
        LEFT JOIN (
            SELECT COUNT(*) as questions_followed
            FROM question_followers WHERE user_id = p_user_id
        ) qf ON 1=1
        LEFT JOIN (
            SELECT COUNT(*) as following_count
            FROM user_follows WHERE follower_id = p_user_id
        ) uf ON 1=1
        LEFT JOIN (
            SELECT COUNT(*) as followers_count
            FROM user_follows WHERE following_id = p_user_id
        ) uf_followers ON 1=1
    ) follow_stats ON u.user_id = follow_stats.user_id
    LEFT JOIN (
        SELECT MAX(created_at) as last_active_date
        FROM activity_logs WHERE user_id = p_user_id
    ) activity_stats ON 1=1
    ON DUPLICATE KEY UPDATE
        total_questions = VALUES(total_questions),
        total_answers = VALUES(total_answers),
        accepted_answers = VALUES(accepted_answers),
        total_votes_received = VALUES(total_votes_received),
        total_votes_given = VALUES(total_votes_given),
        questions_followed = VALUES(questions_followed),
        followers_count = VALUES(followers_count),
        following_count = VALUES(following_count),
        acceptance_rate = VALUES(acceptance_rate),
        average_answer_score = VALUES(average_answer_score),
        last_active_date = VALUES(last_active_date),
        updated_at = VALUES(updated_at);
    
    COMMIT;
END//

-- 2. 自动奖励徽章的存储过程
CREATE OR REPLACE PROCEDURE sp_award_badges(IN p_user_id BIGINT)
BEGIN
    DECLARE v_question_count INT DEFAULT 0;
    DECLARE v_answer_count INT DEFAULT 0;
    DECLARE v_accepted_count INT DEFAULT 0;
    DECLARE v_votes_received INT DEFAULT 0;
    DECLARE v_acceptance_rate DECIMAL(5,2) DEFAULT 0;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 获取用户统计信息
    SELECT 
        total_questions, total_answers, accepted_answers, 
        total_votes_received, acceptance_rate
    INTO 
        v_question_count, v_answer_count, v_accepted_count,
        v_votes_received, v_acceptance_rate
    FROM user_statistics 
    WHERE user_id = p_user_id;
    
    -- 提问相关徽章
    IF v_question_count >= 1 AND NOT EXISTS(
        SELECT 1 FROM user_badges ub 
        JOIN badges b ON ub.badge_id = b.id 
        WHERE ub.user_id = p_user_id AND b.name = 'Curious'
    ) THEN
        INSERT INTO user_badges (user_id, badge_id)
        SELECT p_user_id, id FROM badges WHERE name = 'Curious';
    END IF;
    
    IF v_question_count >= 10 AND NOT EXISTS(
        SELECT 1 FROM user_badges ub 
        JOIN badges b ON ub.badge_id = b.id 
        WHERE ub.user_id = p_user_id AND b.name = 'Inquisitive'
    ) THEN
        INSERT INTO user_badges (user_id, badge_id)
        SELECT p_user_id, id FROM badges WHERE name = 'Inquisitive';
    END IF;
    
    -- 回答相关徽章
    IF v_answer_count >= 1 AND NOT EXISTS(
        SELECT 1 FROM user_badges ub 
        JOIN badges b ON ub.badge_id = b.id 
        WHERE ub.user_id = p_user_id AND b.name = 'Helper'
    ) THEN
        INSERT INTO user_badges (user_id, badge_id)
        SELECT p_user_id, id FROM badges WHERE name = 'Helper';
    END IF;
    
    IF v_accepted_count >= 10 AND NOT EXISTS(
        SELECT 1 FROM user_badges ub 
        JOIN badges b ON ub.badge_id = b.id 
        WHERE ub.user_id = p_user_id AND b.name = 'Teacher'
    ) THEN
        INSERT INTO user_badges (user_id, badge_id)
        SELECT p_user_id, id FROM badges WHERE name = 'Teacher';
    END IF;
    
    -- 专家徽章
    IF v_accepted_count >= 50 AND v_acceptance_rate >= 80 AND NOT EXISTS(
        SELECT 1 FROM user_badges ub 
        JOIN badges b ON ub.badge_id = b.id 
        WHERE ub.user_id = p_user_id AND b.name = 'Expert'
    ) THEN
        INSERT INTO user_badges (user_id, badge_id)
        SELECT p_user_id, id FROM badges WHERE name = 'Expert';
    END IF;
    
    -- 投票相关徽章
    IF v_votes_received >= 100 AND NOT EXISTS(
        SELECT 1 FROM user_badges ub 
        JOIN badges b ON ub.badge_id = b.id 
        WHERE ub.user_id = p_user_id AND b.name = 'Popular'
    ) THEN
        INSERT INTO user_badges (user_id, badge_id)
        SELECT p_user_id, id FROM badges WHERE name = 'Popular';
    END IF;
    
    COMMIT;
END//

-- 3. 计算问题热度分数的存储过程
CREATE OR REPLACE PROCEDURE sp_calculate_trending_score(IN p_question_id BIGINT, OUT p_score DECIMAL(10,2))
BEGIN
    DECLARE v_votes INT DEFAULT 0;
    DECLARE v_answers INT DEFAULT 0;
    DECLARE v_views INT DEFAULT 0;
    DECLARE v_solved BOOLEAN DEFAULT FALSE;
    DECLARE v_hours_old INT DEFAULT 0;
    DECLARE v_base_score DECIMAL(10,2) DEFAULT 0;
    DECLARE v_time_decay DECIMAL(10,4) DEFAULT 0;
    
    -- 获取问题数据
    SELECT votes, answer_count, view_count, solved,
           TIMESTAMPDIFF(HOUR, created_at, NOW())
    INTO v_votes, v_answers, v_views, v_solved, v_hours_old
    FROM questions WHERE id = p_question_id;
    
    -- 计算基础分数
    SET v_base_score = v_votes * 10 + v_answers * 5 + v_views * 0.1;
    IF v_solved THEN
        SET v_base_score = v_base_score + 20;
    END IF;
    
    -- 时间衰减因子（一周为衰减周期）
    SET v_time_decay = EXP(-v_hours_old / 168.0);
    
    -- 最终分数
    SET p_score = v_base_score * v_time_decay;
END//

-- 4. 全文搜索存储过程
CREATE OR REPLACE PROCEDURE sp_search_content(
    IN p_keyword VARCHAR(500),
    IN p_search_type ENUM('questions', 'answers', 'all'),
    IN p_limit INT,
    IN p_offset INT
)
BEGIN
    IF p_search_type = 'questions' THEN
        SELECT 
            q.id, q.title, q.content, q.view_count, q.answer_count, 
            q.votes, q.created_at, u.username as author,
            MATCH(q.title, q.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE) as relevance
        FROM questions q
        JOIN users u ON q.author_id = u.id
        WHERE MATCH(q.title, q.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE)
        ORDER BY relevance DESC, q.created_at DESC
        LIMIT p_offset, p_limit;
        
    ELSEIF p_search_type = 'answers' THEN
        SELECT 
            a.id, a.content, a.vote_count, a.accepted, a.created_at,
            u.username as author, q.title as question_title,
            MATCH(a.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE) as relevance
        FROM answers a
        JOIN users u ON a.author_id = u.id
        JOIN questions q ON a.question_id = q.id
        WHERE MATCH(a.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE)
        ORDER BY relevance DESC, a.created_at DESC
        LIMIT p_offset, p_limit;
        
    ELSE
        -- 搜索所有内容
        (SELECT 
            'question' as type, q.id, q.title as title, q.content, 
            q.view_count as views, q.votes, q.created_at, u.username as author,
            MATCH(q.title, q.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE) as relevance
        FROM questions q
        JOIN users u ON q.author_id = u.id
        WHERE MATCH(q.title, q.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE))
        UNION ALL
        (SELECT 
            'answer' as type, a.id, q.title, a.content, 
            0 as views, a.vote_count as votes, a.created_at, u.username as author,
            MATCH(a.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE) as relevance
        FROM answers a
        JOIN users u ON a.author_id = u.id
        JOIN questions q ON a.question_id = q.id
        WHERE MATCH(a.content) AGAINST(p_keyword IN NATURAL LANGUAGE MODE))
        ORDER BY relevance DESC, created_at DESC
        LIMIT p_offset, p_limit;
    END IF;
END//

-- 5. 生成推荐问题的存储过程
CREATE OR REPLACE PROCEDURE sp_get_recommended_questions(
    IN p_user_id BIGINT,
    IN p_limit INT
)
BEGIN
    -- 基于用户的标签偏好和活动历史推荐问题
    SELECT DISTINCT
        q.id, q.title, q.content, q.view_count, q.answer_count,
        q.votes, q.created_at, u.username as author,
        (
            -- 基于标签匹配的分数
            COALESCE(tag_score.score, 0) * 0.4 +
            -- 基于作者声望的分数
            (u.reputation / 1000.0) * 0.2 +
            -- 基于问题质量的分数
            (q.votes * 2 + q.view_count * 0.01) * 0.3 +
            -- 时间新鲜度分数
            EXP(-TIMESTAMPDIFF(HOUR, q.created_at, NOW()) / 72.0) * 0.1
        ) as recommendation_score
    FROM questions q
    JOIN users u ON q.author_id = u.id
    LEFT JOIN (
        -- 计算基于用户标签偏好的分数
        SELECT 
            qt.question_id,
            SUM(
                CASE 
                    WHEN user_tags.answer_count > 0 THEN user_tags.answer_count * 2
                    WHEN user_tags.question_count > 0 THEN user_tags.question_count
                    ELSE 0
                END
            ) as score
        FROM question_tags qt
        JOIN (
            -- 用户在各标签上的活动统计
            SELECT 
                t.id as tag_id,
                COALESCE(uq.question_count, 0) as question_count,
                COALESCE(ua.answer_count, 0) as answer_count
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
        ) user_tags ON qt.tag_id = user_tags.tag_id
        GROUP BY qt.question_id
    ) tag_score ON q.id = tag_score.question_id
    WHERE q.closed = 0 
      AND q.author_id != p_user_id
      AND q.id NOT IN (
          -- 排除用户已经关注的问题
          SELECT question_id FROM question_followers WHERE user_id = p_user_id
      )
    ORDER BY recommendation_score DESC
    LIMIT p_limit;
END//

DELIMITER ; 