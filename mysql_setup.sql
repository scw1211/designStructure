-- MySQL 8.0 数据库设置脚本
-- 创建问答平台数据库
CREATE DATABASE IF NOT EXISTS qa_platform 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE qa_platform;

-- 显示数据库信息
SELECT DATABASE() as current_database;
SELECT VERSION() as mysql_version;

-- 显示字符集设置
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%'; 