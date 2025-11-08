# Windows 11 开发环境完整安装指南

## 概述

本指南将帮助您在 Windows 11 系统上安装完整的开发环境，包括：
- MongoDB 6.0+
- Redis 7.0+
- RabbitMQ 3.12+
- Postman (API测试工具)
- MongoDB Compass (MongoDB图形化管理工具)
- Redis Desktop Manager (Redis图形化管理工具)

## 1. MongoDB 6.0+ 安装

### 1.1 下载 MongoDB Community Server

1. 访问 MongoDB 官网：https://www.mongodb.com/try/download/community
2. 选择以下选项：
   - **Version**: 选择最新稳定版（如 7.0.x）
   - **Platform**: Windows x64
   - **Package**: MSI
3. 点击 "Download" 下载安装包

### 1.2 安装 MongoDB

1. 双击下载的 `.msi` 文件
2. 选择 "Complete" 完整安装
3. 选择安装路径（推荐默认路径）
4. 勾选 "Install MongoDB as a Service"
5. 配置服务设置：
   - **Service Name**: MongoDB
   - **Data Directory**: `C:\data\db`（会自动创建）
   - **Log Directory**: `C:\data\log`
6. 点击 "Install" 开始安装
7. 安装完成后，点击 "Finish"

### 1.3 验证 MongoDB 安装

```cmd
# 打开命令提示符或 PowerShell
mongod --version

# 启动 MongoDB 服务（如果未自动启动）
net start MongoDB

# 连接到 MongoDB
mongo
# 或使用新版本命令
mongosh

# 在 MongoDB shell 中验证
> db.version()
> show dbs
> exit
```

### 1.4 配置环境变量（可选）

如果 `mongod` 和 `mongo` 命令无法识别，需要手动添加环境变量：
1. 右键点击"此电脑" → "属性" → "高级系统设置"
2. 点击"环境变量"
3. 在"系统变量"中找到"Path"，点击"编辑"
4. 添加 MongoDB 的 bin 目录路径：
   - `C:\Program Files\MongoDB\Server\7.0\bin`
5. 点击"确定"保存

## 2. Redis 7.0+ 安装

### 2.1 使用 Chocolatey 安装（推荐）

1. 首先安装 Chocolatey（如果未安装）：
```powershell
# 以管理员身份运行 PowerShell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

2. 安装 Redis：
```powershell
# 以管理员身份运行
choco install redis-64
```

### 2.2 手动安装 Redis

1. 访问 Redis Windows 版本：https://github.com/microsoftarchive/redis/releases
2. 下载最新版本的 `.msi` 安装包
3. 双击安装包，按照向导完成安装
4. 选择安装路径（推荐默认）
5. 勾选 "Add Redis installation folder to PATH"

### 2.3 启动 Redis 服务

```cmd
# 启动 Redis 服务
redis-server --service-start

# 停止 Redis 服务
redis-server --service-stop

# 查看服务状态
redis-server --service-status
```

### 2.4 验证 Redis 安装

```cmd
# 连接到 Redis
redis-cli

# 测试连接
127.0.0.1:6379> ping
PONG

# 设置测试键值
127.0.0.1:6379> set test:key "Hello Redis"
OK

# 获取值
127.0.0.1:6379> get test:key
"Hello Redis"

# 退出
127.0.0.1:6379> exit
```

## 3. RabbitMQ 3.12+ 安装

### 3.1 安装 Erlang/OTP

RabbitMQ 需要 Erlang 运行时环境：

1. 访问 Erlang 下载页面：https://www.erlang.org/downloads
2. 下载 Windows 64-bit 安装包
3. 运行安装程序，按照向导完成安装
4. 验证 Erlang 安装：
```cmd
erl
1> halt().
```

### 3.2 安装 RabbitMQ

1. 访问 RabbitMQ 下载页面：https://www.rabbitmq.com/download.html
2. 下载 Windows 安装包（.exe）
3. 运行安装程序
4. 选择安装路径（推荐默认）
5. 安装完成后，RabbitMQ 服务会自动启动

### 3.3 启用管理插件

```cmd
# 以管理员身份运行命令提示符
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.x\sbin"

# 启用管理插件
rabbitmq-plugins enable rabbitmq_management
```

### 3.4 验证 RabbitMQ 安装

1. 打开浏览器访问：http://localhost:15672
2. 使用默认账号登录：
   - **用户名**: `guest`
   - **密码**: `guest`
3. 如果无法访问，重启 RabbitMQ 服务：
```cmd
# 停止服务
rabbitmq-service stop

# 启动服务
rabbitmq-service start
```

## 4. Postman 安装

### 4.1 下载和安装

1. 访问 Postman 官网：https://www.postman.com/downloads/
2. 点击 "Download for Windows"
3. 运行下载的 `.exe` 文件
4. 按照安装向导完成安装
5. 启动 Postman，创建账户或直接跳过

### 4.2 基本使用

1. 创建新的请求
2. 设置请求方法（GET、POST 等）
3. 输入 API 端点 URL
4. 添加请求头和请求体
5. 点击 "Send" 发送请求

## 5. MongoDB Compass 安装

### 5.1 下载和安装

1. 访问 MongoDB Compass 下载页面：https://www.mongodb.com/try/download/compass
2. 选择 Windows 版本，下载 `.msi` 安装包
3. 运行安装程序，按照向导完成安装
4. 启动 MongoDB Compass

### 5.2 配置连接

1. 首次启动时，点击 "New Connection"
2. 输入连接字符串：`mongodb://localhost:27017`
3. 点击 "Connect" 连接
4. 创建项目数据库：
   - 点击 "Create Database"
   - 数据库名：`user_db`
   - 集合名：`users`
   - 点击 "Create Database"

## 6. Redis Desktop Manager 安装

### 6.1 下载和安装

1. 访问 Redis Desktop Manager 官网：https://redisdesktop.com/
2. 点击 "Download for Windows"
3. 运行安装程序，按照向导完成安装
4. 启动 Redis Desktop Manager

### 6.2 配置连接

1. 点击 "Connect to Redis Server"
2. 配置连接信息：
   - **Name**: Local Redis
   - **Host**: localhost
   - **Port**: 6379
3. 点击 "Test Connection" 测试连接
4. 点击 "OK" 保存连接

## 7. 环境验证

### 7.1 验证所有服务

```cmd
# 验证 MongoDB
mongosh --eval "db.version()"

# 验证 Redis
redis-cli ping

# 验证 RabbitMQ
# 访问 http://localhost:15672 确认管理界面可访问
```

### 7.2 创建项目测试数据

```javascript
// MongoDB 测试数据
use user_db
db.users.insertOne({
  email: "test@example.com",
  username: "testuser",
  createdAt: new Date()
})

// Redis 测试数据
redis-cli set "user:session:123" "{\"userId\":\"123\",\"username\":\"testuser\"}"
redis-cli get "user:session:123"
```

## 8. 故障排除

### 8.1 端口冲突问题

如果遇到端口被占用：

```cmd
# 查看端口占用情况
netstat -ano | findstr :27017  # MongoDB
netstat -ano | findstr :6379   # Redis
netstat -ano | findstr :5672   # RabbitMQ AMQP
netstat -ano | findstr :15672  # RabbitMQ 管理界面

# 终止占用进程（谨慎使用）
taskkill /PID <进程ID> /F
```

### 8.2 服务启动问题

```cmd
# 检查服务状态
sc query MongoDB
sc query Redis
sc query RabbitMQ

# 启动服务
net start MongoDB
net start Redis
rabbitmq-service start

# 重启服务
net stop MongoDB && net start MongoDB
net stop Redis && net start Redis
rabbitmq-service stop && rabbitmq-service start
```

### 8.3 权限问题

- 确保以管理员身份运行命令提示符
- 检查防火墙设置，确保相关端口未被阻止
- 确认安装目录有足够的读写权限

## 9. 开发环境配置

### 9.1 Spring Boot 应用配置

在您的 Spring Boot 应用中配置连接：

```yaml
# application.yml
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

### 9.2 项目数据库初始化

```javascript
// 在 MongoDB 中创建项目所需数据库和集合
use user_db
db.createCollection("users")
db.createCollection("oauth_connections")
db.createCollection("user_sessions")

use business_db  
db.createCollection("prompts")
db.createCollection("tags")
db.createCollection("sync_records")
```

## 10. 下一步操作

安装完成后，您可以：

1. **验证所有服务正常运行**
2. **配置开发环境**：在 IDE 中设置项目
3. **测试连接**：确保应用能正常连接所有服务
4. **开始开发**：基于配置好的环境进行项目开发

## 附录：常用命令参考

### MongoDB 命令
```cmd
# 启动服务
net start MongoDB

# 停止服务  
net stop MongoDB

# 连接到 MongoDB
mongosh
```

### Redis 命令
```cmd
# 启动服务
redis-server --service-start

# 停止服务
redis-server --service-stop

# 连接到 Redis
redis-cli
```

### RabbitMQ 命令
```cmd
# 服务管理
rabbitmq-service start
rabbitmq-service stop
rabbitmq-service status

# 插件管理
rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins list
```

---
**文档版本**：1.0  
**编制人**：项目经理  
**编制日期**：2025年10月30日  
**适用系统**：Windows 11
