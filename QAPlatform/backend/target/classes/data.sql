-- 插入用户徽章示例数据
INSERT INTO user_badges (user_id, badge_name, badge_description, badge_level, badge_icon, earned_at, earned_reason, created_at) VALUES
(1, '新手上路', '成功注册并完成第一个问题', 'BRONZE', '🎯', NOW(), '注册并提问', NOW()),
(1, '积极回答者', '回答了10个问题', 'SILVER', '💬', NOW(), '回答问题达到10个', NOW()),
(2, '问题专家', '提出了高质量问题', 'GOLD', '🏆', NOW(), '问题获得高赞', NOW()),
(2, '热心助人', '帮助其他用户解决问题', 'SILVER', '🤝', NOW(), '回答被采纳', NOW()),
(3, '知识分享者', '分享了有价值的知识', 'BRONZE', '📚', NOW(), '发布优质内容', NOW()),
(4, '社区贡献者', '为社区做出贡献', 'SILVER', '🌟', NOW(), '积极参与讨论', NOW()),
(5, '技术大牛', '在技术领域表现出色', 'GOLD', '🚀', NOW(), '技术回答获得高评价', NOW()); 