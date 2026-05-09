# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

儿童绘本阅读行为分析与成长跟踪系统 - 毕业设计项目，通过分析儿童阅读行为数据，提供个性化绘本推荐和成长跟踪。

## 构建与运行命令

### 后端 (Spring Boot 3 + Java 17)
```bash
# 需要 Java 17, Maven 3.8+
export JAVA_HOME=/d/java/jdk-17.0.11  # 根据实际 JDK 17 路径调整
cd backend
mvn spring-boot:run
```

后端地址: http://localhost:8080/api

### 前端 (Vue 3 + Vite)
```bash
cd frontend
npm install
npm run dev
```

前端地址: http://localhost:3000 (或下一个可用端口)

### 数据库初始化
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p picture_book_system < sql/extend_schema.sql
mysql -u root -p picture_book_system < sql/incremental_schema.sql
```

数据库: `picture_book_system`，MySQL 8.0 (用户: root, 密码: 123456)

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher | 123456 |
| 家长 | parent | 123456 |

## 架构说明

### 后端分层架构
- `controller/` - REST API 控制器 (使用 `@RequestMapping` 定义资源路径)
- `service/` + `service/impl/` - 业务逻辑层
- `mapper/` - MyBatis-Plus 数据访问层 (继承 `BaseMapper<T>`)
- `entity/` - 实体类，映射数据库表
- `dto/` - 数据传输对象，用于 API 输入
- `vo/` - 视图对象，用于 API 输出 (如 `Result<T>`)
- `config/` - Spring 配置类 (Security, Swagger, CORS)
- `security/` - JWT 认证、密码加密 (BCrypt)
- `task/` - 定时任务

### 前端结构
- `views/` - 页面组件 (通过 Vue Router 路由)
- `api/` - Axios API 调用 (路径与后端控制器对应)
- `stores/` - Pinia 状态管理
- `router/` - Vue Router 配置，包含角色权限守卫
- `utils/` - 工具类，包含请求拦截器和 JWT token 注入

### 关键设计模式
1. **API 路径**: 后端使用 `/api` 上下文路径，前端代理处理路由
2. **认证方式**: JWT token 放在 `Authorization: Bearer <token>` 请求头
3. **权限控制**: 三种角色 - ADMIN, TEACHER, PARENT，使用 `@PreAuthorize` 注解控制
4. **响应格式**: 所有 API 返回 `Result<T>`，包含 `code`, `message`, `data` 字段
5. **密码存储**: 使用 `PasswordEncoder` 进行 BCrypt 加密

## 核心业务逻辑

### 阅读行为分析
- `AnalysisService` 实现 K-Means 聚类，将读者分类为：专注型、跳跃型、兴趣导向型
- 使用 `commons-math3` 进行统计计算
- 计算专注度分数、阅读能力评分、认知阶段评估

### 成长跟踪
- `GrowthService` 生成周报/月报，包含 8 个维度评分
- 维度：词汇量、逻辑理解、阅读量、专注度、认知发展、社交情感、创造力、阅读习惯
- 生成成长里程碑、同龄对比、家长指导建议

### 推荐系统
- `RecommendationService` 使用多策略融合推荐：
  - 基于内容推荐 (兴趣匹配)
  - 协同过滤推荐 (同龄人行为)
  - 难度匹配推荐 (能力匹配)
  - 年龄段推荐

### 绘本分类
- 绘本属性：category (分类), ageRange (年龄段), knowledgeType (知识类型), artStyle (画风), difficultyLevel (难度等级)
- 支持多维度组合筛选

## 数据库说明

- 主键使用自增 `Long` 类型
- 数据库使用 snake_case，Java 实体使用 camelCase
- MyBatis-Plus 自动处理映射
- 核心表：`user`, `child`, `book`, `reading_log`, `behavior_analysis`, `growth_report`, `recommendation`, `reading_bookmark`, `reading_annotation`, `notification`

## 新增功能

### 儿童分析概览 (管理员专属)
- 路径: `/app/children-analysis`
- 前端: `frontend/src/views/children-analysis/index.vue`
- 后端接口: `/stats/children-analysis`, `/stats/children-ranking`
- 功能: 阅读类型分布饼图、专注度分布柱状图、能力评估堆叠图、阅读排行榜

### 通知系统
- 实体: `Notification.java`
- 服务: `NotificationService.java`
- 前端: `frontend/src/views/notification/`

## 项目相关文件

| 文件 | 说明 |
|------|------|
| `我的毕业设计.-2026版-带截图.doc` | 论文文档 (含系统截图) |
| `我的毕业设计.-2026版-带图表.doc` | 论文文档 (含流程图) |
| `screenshots/` | 系统截图目录 |
| `thesis_diagrams/` | 论文图表目录 (.drawio + .spec.yaml) |

## 已安装的 Skills

| Skill | 位置 | 用途 |
|-------|------|------|
| drawio | 用户级 | 生成流程图、架构图 |
| drawio-academic-skills | 用户级 | 学术论文图表 |
| grad-skill | 项目级 | 毕业论文写作规范 |

## 开发注意事项

1. **Java 版本**: 必须使用 JDK 17 (Spring Boot 3 要求)，默认 JAVA_HOME 可能是 JDK 8
2. **端口冲突**: 前端开发服务器会自动递增端口 (3000 → 3001 → 3002...)
3. **API 认证**: 大部分接口需要 JWT token，公开接口在 `SecurityConfig` 中配置
4. **跨域**: 后端已配置 CORS 允许前端来源
5. **图表编辑**: 使用 draw.io 网页版 (https://app.diagrams.net) 打开 `thesis_diagrams/*.drawio` 文件
