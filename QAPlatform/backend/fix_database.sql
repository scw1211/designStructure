-- 完整修复用户签到表的约束问题
USE qaplatform;

-- 1. 查看当前表结构
SHOW CREATE TABLE user_checkins;

-- 2. 删除今天的所有签到记录
DELETE FROM user_checkins WHERE checkin_date = CURDATE();

-- 3. 删除所有可能的旧约束
ALTER TABLE user_checkins DROP INDEX IF EXISTS checkin_date;
ALTER TABLE user_checkins DROP INDEX IF EXISTS UK_jvr8mgaasuhfyrpa3872mpdod;

-- 4. 重新创建表（如果需要）
DROP TABLE IF EXISTS user_checkins_backup;
CREATE TABLE user_checkins_backup AS SELECT * FROM user_checkins;

DROP TABLE user_checkins;

CREATE TABLE user_checkins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    consecutive_days INT NOT NULL DEFAULT 1,
    reputation_earned INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_checkin_date (user_id, checkin_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. 恢复数据（排除今天的重复记录）
INSERT INTO user_checkins (user_id, checkin_date, consecutive_days, reputation_earned, created_at)
SELECT user_id, checkin_date, consecutive_days, reputation_earned, created_at 
FROM user_checkins_backup 
WHERE checkin_date != CURDATE();

-- 6. 查看修复结果
SELECT * FROM user_checkins ORDER BY checkin_date DESC LIMIT 10;

-- 7. 删除备份表
DROP TABLE user_checkins_backup; 