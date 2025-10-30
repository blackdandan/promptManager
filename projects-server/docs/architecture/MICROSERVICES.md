# PromptFlow 微服务架构设计

## 1. 微服务拆分方案

### 1.1 服务划分
```
promptflow-system/
├── user-service/          # 用户服务 (端口: 8081)
├── business-service/      # 业务服务 (端口: 8082)
├── gateway-service/       # API网关 (端口: 8080)
├── discovery-service/     # 服务发现 (端口: 8761)
└── config-service/        # 配置中心 (端口: 8888)
```

### 1.2 服务职责

#### User Service (用户服务)
- 用户注册、登录、认证
- 用户信息管理
- 权限控制
- 会话管理

#### Business Service (业务服务)
- Prompt增删改查
- 标签管理
- 搜索和过滤
- 数据同步

#### Gateway Service (API网关)
- 请求路由
- 认证拦截
- 限流控制
- 负载均衡

#### Discovery Service (服务发现)
- 服务注册
- 健康检查
- 服务发现

#### Config Service (配置中心)
- 统一配置管理
- 动态配置更新
- 配置版本控制

## 2. 当前架构分析

### 2.1 当前状态
当前项目是单体架构，包含：
- 用户模块 (`com.promptflow.user`)
- 业务模块 (`com.promptflow.business`)

### 2.2 微服务改造计划

#### 阶段1: 模块化重构
- 将单体应用拆分为独立模块
- 建立清晰的模块边界
- 定义服务间通信协议

#### 阶段2: 服务独立部署
- 每个模块独立打包部署
- 引入服务发现机制
- 配置API网关

#### 阶段3: 数据分离
- 数据库按服务拆分
- 实现数据一致性
- 配置数据同步

## 3. 微服务通信设计

### 3.1 同步通信 (REST API)
```kotlin
// 用户服务 -> 业务服务
GET /api/business/prompts?userId={userId}
POST /api/business/prompts
```

### 3.2 异步通信 (消息队列)
```kotlin
// 用户注册事件
user.registered.queue
// Prompt创建事件
prompt.created.queue
// 数据同步事件
data.sync.queue
```

### 3.3 服务发现
```yaml
# Eureka配置
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## 4. 数据设计

### 4.1 数据库拆分
- **User Service**: `user_db` (用户相关表)
- **Business Service**: `business_db` (业务相关表)

### 4.2 数据一致性
- 使用Saga模式处理分布式事务
- 事件驱动架构保证最终一致性
- 补偿机制处理失败场景

## 5. 安全设计

### 5.1 认证流程
```
客户端 -> Gateway -> User Service -> JWT Token
```

### 5.2 服务间认证
- 使用JWT Token进行服务间认证
- API Gateway负责Token验证
- 服务间使用服务账号认证

## 6. 部署架构

### 6.1 开发环境
```yaml
version: '3.8'
services:
  discovery-service:
    image: promptflow/discovery:latest
    ports: ["8761:8761"]
    
  gateway-service:
    image: promptflow/gateway:latest
    ports: ["8080:8080"]
    depends_on: [discovery-service]
    
  user-service:
    image: promptflow/user:latest
    ports: ["8081:8081"]
    depends_on: [discovery-service]
    
  business-service:
    image: promptflow/business:latest
    ports: ["8082:8082"]
    depends_on: [discovery-service]
```

### 6.2 生产环境
- Kubernetes集群部署
- 服务自动扩缩容
- 监控和日志收集
- 故障自动恢复

## 7. 监控和运维

### 7.1 监控指标
- 服务健康状态
- 请求响应时间
- 错误率统计
- 资源使用情况

### 7.2 日志收集
- 集中式日志管理
- 分布式追踪
- 性能分析

## 8. 迁移策略

### 8.1 渐进式迁移
1. 模块化重构现有代码
2. 引入服务发现机制
3. 逐步拆分服务
4. 数据迁移和同步

### 8.2 回滚方案
- 保持单体版本作为备份
- 分阶段验证功能
- 快速回滚机制

## 9. 下一步行动

### 短期目标 (1-2周)
- [ ] 完成模块化重构
- [ ] 引入Spring Cloud组件
- [ ] 配置服务发现
- [ ] 实现API Gateway

### 中期目标 (3-4周)
- [ ] 服务独立部署
- [ ] 数据库拆分
- [ ] 消息队列集成
- [ ] 监控系统搭建

### 长期目标 (5-8周)
- [ ] 生产环境部署
- [ ] 性能优化
- [ ] 安全加固
- [ ] 自动化运维
