# 05-Redis安装指南

## 1. Redis安装完成状态

### 1.1 Redis Server
- **版本**：Redis 8.2.2
- **安装方式**：Homebrew
- **服务状态**：已启动并运行
- **端口**：6379
- **数据目录**：`/opt/homebrew/var/db/redis/`

### 1.2 RedisInsight (GUI管理工具)
- **版本**：最新版
- **安装方式**：Homebrew Cask
- **位置**：`/Applications/Redis Insight.app`

## 2. Redis服务管理

### 2.1 服务管理命令
```bash
# 启动Redis服务
brew services start redis

# 停止Redis服务
brew services stop redis

# 重启Redis服务
brew services restart redis

# 查看服务状态
brew services list

# 查看Redis进程
ps aux | grep redis
```

### 2.2 命令行连接
```bash
# 连接到Redis
redis-cli

# 测试连接
redis-cli ping

# 查看Redis信息
redis-cli info

# 查看Redis配置
redis-cli config get *
```

## 3. RedisInsight配置

### 3.1 首次配置
1. 打开Redis Insight应用
2. 点击"Add Redis Database"
3. 配置连接信息：
   - **Host**：`localhost` 或 `127.0.0.1`
   - **Port**：`6379`
   - **Name**：`Local Redis`（可选）
4. 点击"Add Redis Database"连接

### 3.2 连接验证
- 在RedisInsight中查看数据库信息
- 查看内存使用情况
- 测试键值操作

## 4. Redis基本操作

### 4.1 键值操作
```bash
# 连接到Redis
redis-cli

# 设置键值
SET user:session:123 "user_data"
SET cache:prompts:list "prompt_data"

# 获取值
GET user:session:123

# 设置过期时间（秒）
SETEX user:session:123 3600 "user_data"

# 删除键
DEL user:session:123

# 查看所有键
KEYS *

# 退出
exit
```

### 4.2 项目相关Redis键设计
```bash
# 用户会话缓存
SET user:session:{userId} "{sessionData}"

# Prompt缓存
SET cache:prompts:{userId} "{promptsData}"

# 限流计数器
INCR rate_limit:{userId}
EXPIRE rate_limit:{userId} 60

# 验证码缓存
SET verification:{email} "{code}"
EXPIRE verification:{email} 300
```

## 5. Redis配置

### 5.1 配置文件位置
```bash
# Redis配置文件
/opt/homebrew/etc/redis.conf

# 数据目录
/opt/homebrew/var/db/redis/

# 日志文件
/opt/homebrew/var/log/redis.log
```

### 5.2 重要配置项
```conf
# 绑定地址
bind 127.0.0.1

# 端口
port 6379

# 持久化配置
save 900 1
save 300 10
save 60 10000

# 内存策略
maxmemory 256mb
maxmemory-policy allkeys-lru

# 日志级别
loglevel notice
```

## 6. 项目Redis使用场景

### 6.1 用户会话管理
```kotlin
// 存储用户会话
redisTemplate.opsForValue().set(
    "user:session:${userId}", 
    sessionData, 
    Duration.ofHours(1)
)

// 获取用户会话
val session = redisTemplate.opsForValue().get("user:session:${userId}")
```

### 6.2 数据缓存
```kotlin
// 缓存用户Prompt列表
redisTemplate.opsForValue().set(
    "cache:prompts:${userId}",
    promptsData,
    Duration.ofMinutes(30)
)

// 获取缓存数据
val cachedPrompts = redisTemplate.opsForValue().get("cache:prompts:${userId}")
```

### 6.3 限流控制
```kotlin
// 用户操作限流
val key = "rate_limit:${userId}:${action}"
val count = redisTemplate.opsForValue().increment(key)

if (count == 1L) {
    redisTemplate.expire(key, Duration.ofMinutes(1))
}

if (count > 10) {
    throw RateLimitExceededException()
}
```

## 7. 故障排除

### 7.1 常见问题

#### 问题1：无法连接Redis
```bash
# 检查服务状态
brew services list

# 检查端口占用
lsof -i :6379

# 重启服务
brew services restart redis
```

#### 问题2：内存不足
```bash
# 查看内存使用
redis-cli info memory

# 清理所有数据
redis-cli flushall

# 清理当前数据库
redis-cli flushdb
```

#### 问题3：配置问题
```bash
# 检查配置文件
cat /opt/homebrew/etc/redis.conf

# 重新加载配置
redis-cli config rewrite
```

### 7.2 性能监控
```bash
# 查看Redis状态
redis-cli info

# 查看内存使用
redis-cli info memory

# 查看客户端连接
redis-cli info clients

# 查看持久化状态
redis-cli info persistence
```

## 8. 下一步操作

### 8.1 开发环境配置
- 在Spring Boot应用中配置Redis连接
- 实现用户会话缓存
- 配置数据缓存策略

### 8.2 测试Redis功能
```bash
# 测试基本操作
redis-cli SET test:key "Hello Redis"
redis-cli GET test:key
redis-cli DEL test:key

# 测试项目相关功能
redis-cli SET user:session:test "{\"userId\":\"123\",\"username\":\"testuser\"}"
redis-cli GET user:session:test
```

### 8.3 集成到项目
- 在用户服务中集成Redis会话管理
- 在业务服务中集成数据缓存
- 配置Redis连接池和序列化

---
**文档版本**：1.0  
**编制人**：项目经理  
**编制日期**：第1周  
**相关文档**：
- [03-实施阶段](./03-实施阶段.md)
- [04-MongoDB安装指南](./04-MongoDB安装指南.md)
