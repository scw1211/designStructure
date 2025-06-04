-- MySQL数据库初始化脚本
-- 创建qa_platform数据库

CREATE DATABASE IF NOT EXISTS qa_platform 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE qa_platform;

-- 显示创建结果
SELECT 'qa_platform数据库创建成功！' as message;
SHOW DATABASES LIKE 'qa_platform';

-- 确认字符集
SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'qa_platform'; 