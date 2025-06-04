# 问答平台系统 - QA Platform

## 项目简介

这是一个基于Spring Boot + Vue3的现代化问答平台系统，作为《软件设计模式与体系结构》和《数据库原理》课程设计项目。系统实现了完整的问答功能，包括用户管理、问题发布、回答、评论、标签、图片上传等功能，并充分运用了多种设计模式。

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **API文档**: Swagger/OpenAPI 3.0
- **构建工具**: Maven

### 前端技术
- **框架**: Vue 3.3.8
- **构建工具**: Vite 5.0
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **语言**: TypeScript

## 设计模式应用

### 1. MVC模式
- **Model**: 实体类（User, Question, Answer等）
- **View**: Vue前端页面
- **Controller**: REST API控制器

### 2. 观察者模式
- **应用场景**: 问题更新通知系统
- **实现**: `QuestionObserver`接口和`NotificationService`
- **功能**: 当问题有新回答、被解决或关闭时，自动通知关注者

### 3. 策略模式
- **应用场景**: 搜索功能
- **实现**: `SearchStrategy`接口及其实现类
- **策略类型**:
  - `TitleSearchStrategy`: 标题搜索
  - `FullTextSearchStrategy`: 全文搜索

### 4. 工厂模式
- **应用场景**: 用户创建
- **实现**: `UserFactory`接口及其实现类
- **工厂类型**:
  - `RegularUserFactory`: 创建普通用户
  - `AdminUserFactory`: 创建管理员用户

### 5. 装饰器模式
- **应用场景**: 问题展示增强
- **实现**: `QuestionDecorator`抽象类
- **装饰器类型**:
  - `HotQuestionDecorator`: 热门问题标记
  - `FeaturedQuestionDecorator`: 精华问题标记

### 6. 外观模式
- **应用场景**: Spring Boot启动类
- **实现**: `QaPlatformApplication`主类提供统一的系统启动入口

### 7. 单例模式
- **应用场景**: Spring Bean管理
- **实现**: 所有Service、Repository、Controller默认为单例

### 8. 代理模式
- **应用场景**: Spring AOP事务管理
- **实现**: `@Transactional`注解实现事务代理

## 功能特性

### 用户管理
- 用户注册/登录
- 个人信息管理
- 头像上传
- 声望系统
- 用户角色（普通用户/管理员/版主）

### 问答功能
- 发布问题（支持Markdown格式）
- 图片上传
- 问题标签
- 回答问题
- 采纳答案
- 点赞功能
- 评论功能

### 搜索与发现
- 多策略搜索（标题/全文）
- 热门问题
- 未回答问题
- 标签分类
- 用户排行榜

### 通知系统
- 问题更新通知
- 新回答提醒
- 问题状态变更通知

## 数据库设计

### 主要数据表
1. **users** - 用户表
2. **questions** - 问题表
3. **answers** - 回答表
4. **tags** - 标签表
5. **comments** - 评论表
6. **question_images** - 问题图片表
7. **question_tags** - 问题标签关联表
8. **question_followers** - 问题关注表
9. **answer_votes** - 回答点赞表

## 项目结构

```
QAPlatform/
├── backend/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/qaplatform/
│   │       ├── controller/     # 控制器层
│   │       ├── service/        # 服务层
│   │       ├── dao/           # 数据访问层
│   │       ├── model/         # 实体模型
│   │       ├── config/        # 配置类
│   │       ├── observer/      # 观察者模式
│   │       ├── strategy/      # 策略模式
│   │       ├── factory/       # 工厂模式
│   │       └── decorator/     # 装饰器模式
│   └── src/main/resources/
│       └── application.properties
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── views/            # 页面组件
│   │   ├── components/       # 通用组件
│   │   ├── router/           # 路由配置
│   │   ├── store/            # 状态管理
│   │   ├── api/              # API接口
│   │   └── utils/            # 工具函数
│   └── package.json
└── docs/                      # 文档目录
    └── database_init.sql      # 数据库初始化脚本
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 后端启动
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 数据库初始化
1. 创建MySQL数据库
2. 执行`docs/database_init.sql`脚本
3. 默认管理员账号：admin/admin123

## API文档

启动后端服务后，访问以下地址查看API文档：
- http://localhost:8080/api/swagger-ui.html

## 项目特色

1. **完整的MVC架构**: 清晰的分层设计，便于维护和扩展
2. **多种设计模式应用**: 充分体现软件设计模式的实际应用
3. **现代化技术栈**: 使用最新的Spring Boot 3和Vue 3
4. **丰富的功能**: 涵盖问答平台的核心功能
5. **良好的用户体验**: 响应式设计，界面美观
6. **安全性考虑**: JWT认证、密码加密、权限控制
7. **可扩展性**: 模块化设计，易于添加新功能

## 作者

软件设计模式与数据库原理课程设计项目

## 许可证

MIT License 