-- 修复用户签到表的约束问题
USE qaplatform;

-- 1. 删除今天的所有签到记录（因为约束错误导致的问题）
DELETE FROM user_checkins WHERE checkin_date = CURDATE();

-- 2. 删除旧的错误约束（如果存在）
ALTER TABLE user_checkins DROP INDEX IF EXISTS checkin_date;

-- 3. 添加正确的复合唯一约束
ALTER TABLE user_checkins ADD CONSTRAINT uk_user_checkin_date UNIQUE (user_id, checkin_date);

-- 4. 查看表结构确认
SHOW CREATE TABLE user_checkins;

-- 5. 查看当前的签到记录
SELECT * FROM user_checkins ORDER BY checkin_date DESC LIMIT 10; 