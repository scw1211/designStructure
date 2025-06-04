-- 创建问答平台数据库
CREATE DATABASE IF NOT EXISTS qa_platform 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE qa_platform;

-- 显示数据库信息
SELECT DATABASE() as current_database;
SHOW TABLES; 