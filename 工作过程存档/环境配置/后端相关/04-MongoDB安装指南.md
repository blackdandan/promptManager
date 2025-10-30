# 04-MongoDB安装指南

## 1. MongoDB Community Server 安装步骤

### 1.1 下载MongoDB Community Server

1. 访问：https://www.mongodb.com/try/download/community
2. 选择您的操作系统（macOS）
3. 选择版本：推荐选择最新稳定版（如7.0.x）
4. 选择包类型：
   - **推荐**：选择 `.tgz` 包（手动安装，更灵活）
   - 或者选择 `.pkg` 包（图形化安装）

### 1.2 安装MongoDB

#### 方法一：使用 .pkg 包（推荐新手）
```bash
# 1. 双击下载的 .pkg 文件
# 2. 按照安装向导完成安装
# 3. MongoDB会自动安装到 /usr/local/bin/
# 4. 数据目录：/usr/local/var/mongodb/
# 5. 日志目录：/usr/local/var/log/mongodb/
```

#### 方法二：使用 .tgz 包（推荐开发者）
```bash
# 1. 解压下载的 .tgz 文件
tar -zxvf mongodb-macos-x86_64-7.0.x.tgz

# 2. 移动到系统目录
sudo mv mongodb-macos-x86_64-7.0.x /usr/local/mongodb

# 3. 添加到PATH环境变量
echo 'export PATH="/usr/local/mongodb/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 4. 创建数据目录
sudo mkdir -p /usr/local/var/mongodb
sudo mkdir -p /usr/local/var/log/mongodb

# 5. 设置权限
sudo chown `whoami` /usr/local/var/mongodb
sudo chown `whoami` /usr/local/var/log/mongodb
```

### 1.3 启动MongoDB服务

#### 方法一：使用Homebrew（如果已安装）
```bash
# 安装MongoDB
brew tap mongodb/brew
brew install mongodb-community

# 启动服务
brew services start mongodb-community

# 停止服务
brew services stop mongodb-community
```

#### 方法二：手动启动
```bash
# 启动MongoDB服务
mongod --dbpath /usr/local/var/mongodb --logpath /usr/local/var/log/mongodb/mongo.log --fork

# 或者使用配置文件启动
mongod --config /usr/local/etc/mongod.conf
```

### 1.4 验证MongoDB安装
```bash
# 连接到MongoDB
mongo

# 或者使用新版本命令
mongosh

# 在MongoDB shell中执行
> db.version()
> show dbs
> exit
```

## 2. MongoDB Compass 安装步骤

### 2.1 下载MongoDB Compass

1. 访问：https://www.mongodb.com/try/download/compass
2. 选择您的操作系统（macOS）
3. 下载 `.dmg` 文件

### 2.2 安装MongoDB Compass

```bash
# 1. 双击下载的 .dmg 文件
# 2. 将MongoDB Compass拖拽到Applications文件夹
# 3. 在Launchpad或Applications中找到并启动MongoDB Compass
```

### 2.3 配置MongoDB Compass

1. **首次启动配置**：
   - 连接字符串：`mongodb://localhost:27017`
   - 点击"Connect"连接

2. **创建数据库**：
   - 点击"Create Database"
   - 数据库名：`user_db`
   - 集合名：`users`
   - 点击"Create Database"

3. **创建项目所需数据库**：
   ```javascript
   // 用户服务数据库
   use user_db
   
   // 业务服务数据库  
   use business_db
   ```

## 3. 项目数据库初始化

### 3.1 使用MongoDB Shell创建数据库和集合

```bash
# 连接到MongoDB
mongosh

# 创建用户服务数据库和集合
use user_db
db.createCollection("users")
db.createCollection("oauth_connections") 
db.createCollection("user_sessions")
db.createCollection("guest_sessions")
db.createCollection("memberships")
db.createCollection("subscriptions")
db.createCollection("orders")

# 创建业务服务数据库和集合
use business_db
db.createCollection("prompts")
db.createCollection("tags")
db.createCollection("sync_records")
db.createCollection("folders")
db.createCollection("shares")

# 验证创建结果
show dbs
show collections
```

### 3.2 使用MongoDB Compass创建数据库

1. 打开MongoDB Compass
2. 连接到 `mongodb://localhost:27017`
3. 点击"Create Database"
4. 输入数据库名：`user_db`
5. 输入集合名：`users`
6. 重复步骤创建其他数据库和集合

## 4. 常用MongoDB命令

### 4.1 服务管理命令
```bash
# 启动MongoDB服务
brew services start mongodb-community

# 停止MongoDB服务
brew services stop mongodb-community

# 重启MongoDB服务
brew services restart mongodb-community

# 查看服务状态
brew services list
```

### 4.2 数据库操作命令
```bash
# 连接到MongoDB
mongosh

# 显示所有数据库
show dbs

# 切换数据库
use user_db

# 显示当前数据库的所有集合
show collections

# 删除数据库
db.dropDatabase()

# 删除集合
db.collection_name.drop()
```

### 4.3 数据操作命令
```javascript
// 插入文档
db.users.insertOne({
  email: "test@example.com",
  username: "testuser",
  createdAt: new Date()
})

// 查询文档
db.users.find()
db.users.findOne({email: "test@example.com"})

// 更新文档
db.users.updateOne(
  {email: "test@example.com"},
  {$set: {username: "updateduser"}}
)

// 删除文档
db.users.deleteOne({email: "test@example.com"})
```

## 5. 故障排除

### 5.1 常见问题

#### 问题1：端口被占用
```bash
# 检查27017端口是否被占用
lsof -i :27017

# 如果被占用，杀死进程
kill -9 <PID>
```

#### 问题2：权限问题
```bash
# 确保数据目录有正确权限
sudo chown -R `whoami` /usr/local/var/mongodb
sudo chown -R `whoami` /usr/local/var/log/mongodb
```

#### 问题3：无法启动服务
```bash
# 查看日志文件
tail -f /usr/local/var/log/mongodb/mongo.log

# 重新创建数据目录
rm -rf /usr/local/var/mongodb/*
mongod --dbpath /usr/local/var/mongodb --repair
```

### 5.2 配置文件示例

创建配置文件 `/usr/local/etc/mongod.conf`：
```yaml
systemLog:
  destination: file
  path: /usr/local/var/log/mongodb/mongo.log
  logAppend: true

storage:
  dbPath: /usr/local/var/mongodb
  journal:
    enabled: true

net:
  bindIp: 127.0.0.1
  port: 27017

processManagement:
  fork: true
```

## 6. 下一步操作

安装完成后，您可以：
1. 验证MongoDB服务是否正常运行
2. 使用MongoDB Compass连接和查看数据库
3. 开始后端服务的开发
4. 配置Spring Boot应用连接MongoDB

---
**文档版本**：1.0  
**编制人**：项目经理  
**编制日期**：第1周  
**相关文档**：[03-实施阶段](./03-实施阶段.md)
