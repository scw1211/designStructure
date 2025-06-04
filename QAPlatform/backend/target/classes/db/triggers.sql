-- =============================================
-- QA平台触发器
-- =============================================

DELIMITER //

-- 1. 问题创建后自动记录活动日志和更新统计
CREATE TRIGGER tr_question_after_insert
AFTER INSERT ON questions
FOR EACH ROW
BEGIN
    -- 记录活动日志
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id, reputation)
    VALUES (NEW.author_id, 'ASK_QUESTION', CONCAT('提问: ', NEW.title), 'question', NEW.id, 5);
    
    -- 更新用户声望
    UPDATE users SET reputation = reputation + 5 WHERE id = NEW.author_id;
    
    -- 更新标签使用次数
    UPDATE tags t 
    SET usage_count = usage_count + 1 
    WHERE t.id IN (
        SELECT tag_id FROM question_tags WHERE question_id = NEW.id
    );
    
    -- 异步调用统计更新（这里使用事件来模拟异步）
    INSERT INTO user_statistics_update_queue (user_id, created_at) 
    VALUES (NEW.author_id, NOW()) 
    ON DUPLICATE KEY UPDATE created_at = NOW();
END//

-- 2. 问题更新时记录活动
CREATE TRIGGER tr_question_after_update
AFTER UPDATE ON questions
FOR EACH ROW
BEGIN
    -- 如果内容或标题发生变化，记录编辑活动
    IF OLD.title != NEW.title OR OLD.content != NEW.content THEN
        INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
        VALUES (NEW.author_id, 'EDIT_QUESTION', CONCAT('编辑问题: ', NEW.title), 'question', NEW.id);
    END IF;
    
    -- 如果问题被标记为已解决，给作者额外声望奖励
    IF OLD.solved = 0 AND NEW.solved = 1 THEN
        UPDATE users SET reputation = reputation + 15 WHERE id = NEW.author_id;
        
        INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id, reputation)
        VALUES (NEW.author_id, 'QUESTION_SOLVED', CONCAT('问题被解决: ', NEW.title), 'question', NEW.id, 15);
    END IF;
END//

-- 3. 回答创建后自动处理
CREATE TRIGGER tr_answer_after_insert
AFTER INSERT ON answers
FOR EACH ROW
BEGIN
    -- 记录活动日志
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id, reputation)
    VALUES (NEW.author_id, 'ANSWER_QUESTION', '回答了问题', 'answer', NEW.id, 10);
    
    -- 更新用户声望
    UPDATE users SET reputation = reputation + 10 WHERE id = NEW.author_id;
    
    -- 更新问题的回答数量
    UPDATE questions SET answer_count = answer_count + 1 WHERE id = NEW.question_id;
    
    -- 更新用户统计队列
    INSERT INTO user_statistics_update_queue (user_id, created_at) 
    VALUES (NEW.author_id, NOW()) 
    ON DUPLICATE KEY UPDATE created_at = NOW();
END//

-- 4. 回答被采纳时的处理
CREATE TRIGGER tr_answer_after_update
AFTER UPDATE ON answers
FOR EACH ROW
BEGIN
    -- 如果回答被采纳
    IF OLD.accepted = 0 AND NEW.accepted = 1 THEN
        -- 给回答者声望奖励
        UPDATE users SET reputation = reputation + 25 WHERE id = NEW.author_id;
        
        -- 记录活动日志
        INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id, reputation)
        VALUES (NEW.author_id, 'ACCEPT_ANSWER', '回答被采纳', 'answer', NEW.id, 25);
        
        -- 标记问题为已解决
        UPDATE questions SET solved = 1 WHERE id = NEW.question_id;
        
        -- 更新统计
        INSERT INTO user_statistics_update_queue (user_id, created_at) 
        VALUES (NEW.author_id, NOW()) 
        ON DUPLICATE KEY UPDATE created_at = NOW();
    END IF;
    
    -- 如果回答被取消采纳
    IF OLD.accepted = 1 AND NEW.accepted = 0 THEN
        -- 扣除声望
        UPDATE users SET reputation = reputation - 25 WHERE id = NEW.author_id;
        
        -- 检查问题是否还有其他被采纳的回答，如果没有则标记为未解决
        IF NOT EXISTS(SELECT 1 FROM answers WHERE question_id = NEW.question_id AND accepted = 1 AND id != NEW.id) THEN
            UPDATE questions SET solved = 0 WHERE id = NEW.question_id;
        END IF;
    END IF;
END//

-- 5. 投票触发器（问题投票）
CREATE TRIGGER tr_question_vote_after_insert
AFTER INSERT ON question_votes
FOR EACH ROW
BEGIN
    -- 更新问题投票数
    UPDATE questions SET votes = votes + 1 WHERE id = NEW.question_id;
    
    -- 给问题作者声望奖励
    UPDATE users u 
    JOIN questions q ON u.id = q.author_id 
    SET u.reputation = u.reputation + 2 
    WHERE q.id = NEW.question_id;
    
    -- 记录活动日志
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (NEW.user_id, 'VOTE_UP', '给问题点赞', 'question', NEW.question_id);
END//

CREATE TRIGGER tr_question_vote_after_delete
AFTER DELETE ON question_votes
FOR EACH ROW
BEGIN
    -- 更新问题投票数
    UPDATE questions SET votes = votes - 1 WHERE id = OLD.question_id;
    
    -- 扣除问题作者声望
    UPDATE users u 
    JOIN questions q ON u.id = q.author_id 
    SET u.reputation = u.reputation - 2 
    WHERE q.id = OLD.question_id;
    
    -- 记录活动日志
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (OLD.user_id, 'CANCEL_VOTE', '取消给问题点赞', 'question', OLD.question_id);
END//

-- 6. 投票触发器（回答投票）
CREATE TRIGGER tr_answer_vote_after_insert
AFTER INSERT ON answer_votes
FOR EACH ROW
BEGIN
    -- 更新回答投票数
    UPDATE answers SET vote_count = vote_count + 1 WHERE id = NEW.answer_id;
    
    -- 给回答作者声望奖励
    UPDATE users u 
    JOIN answers a ON u.id = a.author_id 
    SET u.reputation = u.reputation + 2 
    WHERE a.id = NEW.answer_id;
    
    -- 记录活动日志
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (NEW.user_id, 'VOTE_UP', '给回答点赞', 'answer', NEW.answer_id);
END//

CREATE TRIGGER tr_answer_vote_after_delete
AFTER DELETE ON answer_votes
FOR EACH ROW
BEGIN
    -- 更新回答投票数
    UPDATE answers SET vote_count = vote_count - 1 WHERE id = OLD.answer_id;
    
    -- 扣除回答作者声望
    UPDATE users u 
    JOIN answers a ON u.id = a.author_id 
    SET u.reputation = u.reputation - 2 
    WHERE a.id = OLD.answer_id;
    
    -- 记录活动日志
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (OLD.user_id, 'CANCEL_VOTE', '取消给回答点赞', 'answer', OLD.answer_id);
END//

-- 7. 用户关注问题触发器
CREATE TRIGGER tr_question_follow_after_insert
AFTER INSERT ON question_followers
FOR EACH ROW
BEGIN
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (NEW.user_id, 'FOLLOW_QUESTION', '关注了问题', 'question', NEW.question_id);
END//

CREATE TRIGGER tr_question_follow_after_delete
AFTER DELETE ON question_followers
FOR EACH ROW
BEGIN
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (OLD.user_id, 'UNFOLLOW_QUESTION', '取消关注问题', 'question', OLD.question_id);
END//

-- 8. 用户关注用户触发器
CREATE TRIGGER tr_user_follow_after_insert
AFTER INSERT ON user_follows
FOR EACH ROW
BEGIN
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (NEW.follower_id, 'FOLLOW_USER', '关注了用户', 'user', NEW.following_id);
    
    -- 更新统计队列
    INSERT INTO user_statistics_update_queue (user_id, created_at) 
    VALUES (NEW.follower_id, NOW()), (NEW.following_id, NOW())
    ON DUPLICATE KEY UPDATE created_at = NOW();
END//

CREATE TRIGGER tr_user_follow_after_delete
AFTER DELETE ON user_follows
FOR EACH ROW
BEGIN
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id)
    VALUES (OLD.follower_id, 'UNFOLLOW_USER', '取消关注用户', 'user', OLD.following_id);
    
    -- 更新统计队列
    INSERT INTO user_statistics_update_queue (user_id, created_at) 
    VALUES (OLD.follower_id, NOW()), (OLD.following_id, NOW())
    ON DUPLICATE KEY UPDATE created_at = NOW();
END//

-- 9. 徽章授予触发器
CREATE TRIGGER tr_user_badge_after_insert
AFTER INSERT ON user_badges
FOR EACH ROW
BEGIN
    DECLARE badge_name VARCHAR(100);
    
    -- 获取徽章名称
    SELECT name INTO badge_name FROM badges WHERE id = NEW.badge_id;
    
    -- 记录活动日志
    INSERT INTO activity_logs (user_id, activity_type, description, target_type, target_id, reputation)
    VALUES (NEW.user_id, 'BADGE_AWARDED', CONCAT('获得徽章: ', badge_name), 'badge', NEW.badge_id, 50);
    
    -- 给用户声望奖励
    UPDATE users SET reputation = reputation + 50 WHERE id = NEW.user_id;
END//

-- 10. 自动清理过期数据的触发器（通过定时任务表模拟）
CREATE TRIGGER tr_cleanup_old_logs
BEFORE INSERT ON activity_logs
FOR EACH ROW
BEGIN
    -- 每1000次插入时清理90天前的日志
    IF (SELECT COUNT(*) FROM activity_logs) % 1000 = 0 THEN
        DELETE FROM activity_logs 
        WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY)
        ORDER BY created_at 
        LIMIT 1000;
    END IF;
END//

DELIMITER ;

-- 创建用户统计更新队列表（用于异步处理统计更新）
CREATE TABLE IF NOT EXISTS user_statistics_update_queue (
    user_id BIGINT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建事件调度器来定期处理统计更新队列
SET GLOBAL event_scheduler = ON;

DELIMITER //

CREATE EVENT IF NOT EXISTS ev_update_user_statistics
ON SCHEDULE EVERY 5 MINUTE
DO
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_user_id BIGINT;
    DECLARE cur CURSOR FOR 
        SELECT user_id FROM user_statistics_update_queue 
        WHERE created_at <= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
        ORDER BY created_at LIMIT 100;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_user_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 调用存储过程更新用户统计
        CALL sp_update_user_statistics(v_user_id);
        
        -- 调用存储过程检查徽章
        CALL sp_award_badges(v_user_id);
        
        -- 从队列中删除已处理的记录
        DELETE FROM user_statistics_update_queue WHERE user_id = v_user_id;
    END LOOP;
    
    CLOSE cur;
END//

DELIMITER ; 