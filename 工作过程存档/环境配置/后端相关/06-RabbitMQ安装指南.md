# 06-RabbitMQ安装指南

## 1. RabbitMQ是什么？

RabbitMQ是一个开源的消息代理软件，用于实现微服务之间的异步通信。在您的PromptFlow项目中，RabbitMQ将用于：

### 1.1 主要用途
- **异步任务处理**：处理耗时的操作，如数据同步、邮件发送
- **服务解耦**：微服务之间的松耦合通信
- **消息队列**：确保消息的可靠传递
- **流量削峰**：处理突发流量，避免系统过载

### 1.2 项目应用场景
- **数据同步**：多端Prompt数据同步
- **通知服务**：用户操作通知
- **缓存更新**：分布式缓存一致性
- **日志处理**：异步日志收集和处理

## 2. RabbitMQ安装完成状态

### 2.1 RabbitMQ Server
- **版本**：RabbitMQ 4.2.0
- **安装方式**：Homebrew
- **服务状态**：已启动并运行
- **端口**：5672 (AMQP), 15672 (管理界面)
- **数据目录**：`/opt/homebrew/var/lib/rabbitmq/mnesia/`

### 2.2 管理插件
- **管理界面**：已启用 (http://localhost:15672)
- **默认账号**：`guest` / `guest`
- **功能**：队列监控、消息管理、性能统计

## 3. RabbitMQ服务管理

### 3.1 服务管理命令
```bash
# 启动RabbitMQ服务
brew services start rabbitmq

# 停止RabbitMQ服务
brew services stop rabbitmq

# 重启RabbitMQ服务
brew services restart rabbitmq

# 查看服务状态
brew services list

# 查看RabbitMQ状态
rabbitmqctl status
```

### 3.2 插件管理
```bash
# 启用管理插件
rabbitmq-plugins enable rabbitmq_management

# 禁用插件
rabbitmq-plugins disable rabbitmq_management

# 查看已启用插件
rabbitmq-plugins list
```

## 4. RabbitMQ管理界面

### 4.1 访问管理界面
1. 打开浏览器访问：`http://localhost:15672`
2. 使用默认账号登录：
   - **用户名**：`guest`
   - **密码**：`guest`

### 4.2 管理界面功能
- **Overview**：系统概览和统计信息
- **Connections**：客户端连接管理
- **Channels**：信道管理
- **Exchanges**：交换机管理
- **Queues**：队列管理
- **Admin**：用户和权限管理

## 5. RabbitMQ基本概念

### 5.1 核心组件
```
Producer (生产者) → Exchange (交换机) → Queue (队列) → Consumer (消费者)
```

### 5.2 交换机类型
- **Direct**：直接路由，精确匹配Routing Key
- **Fanout**：广播，发送到所有绑定队列
- **Topic**：主题路由，模式匹配Routing Key
- **Headers**：头部路由，基于消息属性

## 6. 项目RabbitMQ配置

### 6.1 项目队列设计
```yaml
# 用户服务队列
user.sync.queue: 用户数据同步
user.notification.queue: 用户通知

# 业务服务队列
prompt.sync.queue: Prompt数据同步
prompt.cache.queue: 缓存更新
prompt.share.queue: 分享通知

# 系统队列
system.log.queue: 系统日志处理
system.metric.queue: 指标收集
```

### 6.2 Spring Boot配置
```yaml
# application.yml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    
    # 连接池配置
    connection-timeout: 10000
    template:
      retry:
        enabled: true
        initial-interval: 1000
        max-attempts: 3
        multiplier: 1.0
        max-interval: 10000
```

### 6.3 队列配置类
```kotlin
@Configuration
class RabbitMQConfig {

    // 用户数据同步队列
    @Bean
    fun userSyncQueue(): Queue {
        return Queue("user.sync.queue", true)
    }

    // Prompt数据同步队列
    @Bean
    fun promptSyncQueue(): Queue {
        return Queue("prompt.sync.queue", true)
    }

    // 交换机配置
    @Bean
    fun topicExchange(): TopicExchange {
        return TopicExchange("promptflow.topic")
    }

    // 绑定配置
    @Bean
    fun bindingUserSync(queue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with("user.sync.*")
    }
}
```

## 7. RabbitMQ消息处理

### 7.1 生产者示例
```kotlin
@Service
class MessageProducer(
    private val rabbitTemplate: RabbitTemplate
) {
    
    fun sendUserSyncMessage(userId: String, syncData: UserSyncData) {
        rabbitTemplate.convertAndSend(
            "promptflow.topic",
            "user.sync.${userId}",
            syncData
        )
    }
    
    fun sendPromptSyncMessage(promptId: String, syncData: PromptSyncData) {
        rabbitTemplate.convertAndSend(
            "promptflow.topic", 
            "prompt.sync.${promptId}",
            syncData
        )
    }
}
```

### 7.2 消费者示例
```kotlin
@Service
class MessageConsumer {
    
    @RabbitListener(queues = ["user.sync.queue"])
    fun handleUserSync(message: UserSyncData) {
        // 处理用户数据同步
        userService.syncUserData(message.userId, message.syncData)
    }
    
    @RabbitListener(queues = ["prompt.sync.queue"])
    fun handlePromptSync(message: PromptSyncData) {
        // 处理Prompt数据同步
        promptService.syncPromptData(message.promptId, message.syncData)
    }
}
```

## 8. 故障排除

### 8.1 常见问题

#### 问题1：无法连接RabbitMQ
```bash
# 检查服务状态
brew services list

# 检查端口占用
lsof -i :5672
lsof -i :15672

# 重启服务
brew services restart rabbitmq
```

#### 问题2：管理界面无法访问
```bash
# 启用管理插件
rabbitmq-plugins enable rabbitmq_management

# 重启服务
brew services restart rabbitmq
```

#### 问题3：内存不足
```bash
# 查看内存使用
rabbitmqctl status

# 清理队列
rabbitmqctl purge_queue queue_name
```

### 8.2 性能监控
```bash
# 查看节点状态
rabbitmqctl status

# 查看队列状态
rabbitmqctl list_queues

# 查看连接状态
rabbitmqctl list_connections

# 查看信道状态
rabbitmqctl list_channels
```

## 9. 下一步操作

### 9.1 开发环境配置
- 在Spring Boot应用中配置RabbitMQ连接
- 定义项目所需的队列和交换机
- 实现消息生产者和消费者

### 9.2 测试RabbitMQ功能
```bash
# 测试管理界面
open http://localhost:15672

# 测试消息发送（使用管理界面）
# 1. 登录管理界面
# 2. 进入Queues标签页
# 3. 点击队列名称
# 4. 在Publish message区域发送测试消息
```

### 9.3 集成到项目
- 在用户服务中集成异步通知
- 在业务服务中集成数据同步
- 配置消息重试和错误处理

## 10. RabbitMQ与IDE关系

### 10.1 IDE不会自带RabbitMQ
- RabbitMQ是一个独立的消息代理服务
- 需要单独安装和运行
- IDE只提供代码编辑和调试功能

### 10.2 开发环境要求
- **本地安装**：开发时在本地运行RabbitMQ
- **Docker**：也可以使用Docker容器运行
- **生产环境**：生产环境需要独立的RabbitMQ集群

---
**文档版本**：1.0  
**编制人**：项目经理  
**编制日期**：第1周  
**相关文档**：
- [03-实施阶段](./03-实施阶段.md)
- [04-MongoDB安装指南](./04-MongoDB安装指南.md)
- [05-Redis安装指南](./05-Redis安装指南.md)
