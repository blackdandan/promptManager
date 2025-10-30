# PromptFlow Server

PromptFlow项目的后端服务，基于Spring Boot 3.4.0和Kotlin构建。

## 项目结构

```
prompt-server/
├── src/main/kotlin/com/promptflow/
│   ├── PromptServerApplication.kt          # 主应用类
│   ├── user/                              # 用户服务模块
│   │   ├── domain/model/                  # 领域模型
│   │   └── infrastructure/repository/     # 数据访问层
│   ├── business/                          # 业务服务模块
│   │   ├── domain/model/                  # 领域模型
│   │   ├── infrastructure/repository/     # 数据访问层
│   │   ├── application/service/           # 业务逻辑层
│   │   └── interfaces/rest/               # 接口层
│   └── common/                            # 公共模块
└── src/main/resources/
    └── application.yml                    # 应用配置
```

## 技术栈

- **框架**: Spring Boot 3.4.0
- **语言**: Kotlin 2.0.20
- **数据库**: MongoDB 8.2.1
- **缓存**: Redis 8.2.2
- **消息队列**: RabbitMQ 4.2.0
- **构建工具**: Gradle Kotlin DSL
- **Java版本**: 21

## 功能特性

### 用户服务
- 用户注册和登录
- 用户信息管理
- 会话管理
- 权限控制

### Prompt管理
- Prompt增删改查
- 标签管理
- 搜索和过滤
- 收藏功能
- 公开分享
- 使用统计

### 数据同步
- 多端数据同步
- 实时更新
- 冲突解决

## 快速开始

### 环境要求

- Java 21+
- MongoDB 8.2+
- Redis 8.2+
- RabbitMQ 4.2+

### 安装依赖

```bash
cd projects/prompt-server
./gradlew build
```

### 运行应用

```bash
./gradlew bootRun
```

应用将在 `http://localhost:8080/api` 启动。

### API文档

启动后访问: `http://localhost:8080/api/swagger-ui.html`

## API接口

### Prompt管理

#### 获取用户Prompt列表
```
GET /api/prompts
Headers:
  X-User-Id: {userId}
Query Parameters:
  search: 搜索关键词
  tags: 标签过滤
  category: 分类过滤
  isFavorite: 收藏过滤
  page: 页码
  size: 每页大小
```

#### 创建Prompt
```
POST /api/prompts
Headers:
  X-User-Id: {userId}
Body:
{
  "title": "Prompt标题",
  "content": "Prompt内容",
  "description": "描述",
  "tags": ["标签1", "标签2"],
  "category": "分类",
  "isPublic": false,
  "folderId": "文件夹ID"
}
```

#### 更新Prompt
```
PUT /api/prompts/{id}
Headers:
  X-User-Id: {userId}
Body:
{
  "title": "新标题",
  "content": "新内容",
  "description": "新描述",
  "tags": ["新标签"],
  "category": "新分类",
  "isPublic": true,
  "folderId": "新文件夹ID"
}
```

#### 删除Prompt
```
DELETE /api/prompts/{id}
Headers:
  X-User-Id: {userId}
```

#### 切换收藏状态
```
POST /api/prompts/{id}/favorite
Headers:
  X-User-Id: {userId}
```

#### 获取公开Prompt
```
GET /api/prompts/public
Query Parameters:
  search: 搜索关键词
  tags: 标签过滤
  page: 页码
  size: 每页大小
```

#### 获取用户标签
```
GET /api/prompts/tags
Headers:
  X-User-Id: {userId}
```

#### 获取用户统计
```
GET /api/prompts/stats
Headers:
  X-User-Id: {userId}
```

## 配置说明

### 数据库配置
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/user_db
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 应用配置
```yaml
server:
  port: 8080
  servlet:
    context-path: /api
app:
  jwt:
    secret: promptflow-secret-key-change-in-production
    expiration: 86400000
```

## 开发指南

### 添加新功能

1. 在对应模块创建领域模型
2. 实现Repository接口
3. 编写Service业务逻辑
4. 创建Controller接口
5. 添加DTO映射

### 测试

```bash
./gradlew test
```

### 打包

```bash
./gradlew bootJar
```

## 部署

### Docker部署

```dockerfile
FROM openjdk:21-jdk-slim
COPY build/libs/prompt-server.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 环境变量配置

```bash
export SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/user_db
export SPRING_REDIS_HOST=redis
export SPRING_RABBITMQ_HOST=rabbitmq
```

## 监控和日志

- 应用日志: `/var/log/prompt-server/application.log`
- 健康检查: `http://localhost:8080/api/actuator/health`
- 指标监控: `http://localhost:8080/api/actuator/metrics`

## 许可证

MIT License
