# PromptFlow API 文档

## 1. 概述

PromptFlow 是一个完整的用户认证和会话管理系统，支持多种登录方式和设备管理。

### 基础信息
- **基础URL**: `http://localhost:8080/api`
- **认证方式**: Token认证 (通过请求头传递)
- **响应格式**: JSON
- **错误处理**: 统一错误响应格式

### 统一响应格式
```json
{
  "success": true,
  "data": { ... },
  "message": "操作成功",
  "error": null
}
```

### 错误响应格式
```json
{
  "success": false,
  "data": null,
  "message": "错误描述",
  "error": {
    "code": "ERROR_CODE",
    "message": "错误消息",
    "details": "详细错误信息"
  }
}
```

## 2. 认证接口

### 2.1 用户注册

**POST** `/auth/register`

**请求体:**
```json
{
  "username": "user123",
  "email": "user@example.com",
  "password": "password123",
  "displayName": "用户昵称"
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "userId": "user_id",
    "username": "user123",
    "email": "user@example.com",
    "displayName": "用户昵称",
    "userType": "REGISTERED",
    "roles": ["USER"]
  },
  "message": "注册成功"
}
```

### 2.2 用户登录

**POST** `/auth/login`

**请求体:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "userId": "user_id",
    "username": "user123",
    "email": "user@example.com",
    "displayName": "用户昵称",
    "userType": "REGISTERED",
    "roles": ["USER"]
  },
  "message": "登录成功"
}
```

## 3. 三方登录接口

### 3.1 三方登录回调

**POST** `/oauth/callback/{provider}`

**路径参数:**
- `provider`: 三方登录提供商 (GITHUB, GOOGLE, WECHAT, APPLE)

**请求体:**
```json
{
  "providerUserId": "provider_user_id",
  "email": "user@example.com",
  "username": "user123",
  "avatarUrl": "https://example.com/avatar.jpg",
  "profileData": {
    "name": "用户名称",
    "location": "用户位置"
  },
  "accessToken": "access_token",
  "refreshToken": "refresh_token",
  "expiresAt": "2024-01-01T00:00:00"
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "userId": "user_id",
    "username": "user123",
    "email": "user@example.com",
    "displayName": "用户昵称",
    "userType": "OAUTH",
    "roles": ["USER"]
  },
  "message": "三方登录成功"
}
```

### 3.2 获取三方登录关联

**GET** `/oauth/connections`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "success": true,
  "data": [
    {
      "provider": "GITHUB",
      "providerUserId": "github_user_id",
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "message": "获取三方登录关联成功"
}
```

### 3.3 解绑三方登录

**DELETE** `/oauth/connections/{provider}`

**请求头:**
- `X-User-ID`: 用户ID

**路径参数:**
- `provider`: 三方登录提供商

**响应:**
```json
{
  "success": true,
  "data": null,
  "message": "解绑三方登录成功"
}
```

## 4. 会话管理接口

### 4.1 创建会话

**POST** `/sessions`

**请求头:**
- `X-User-ID`: 用户ID

**请求体:**
```json
{
  "deviceInfo": {
    "deviceId": "device_unique_id",
    "deviceType": "IOS",
    "os": "iOS 17.0",
    "browser": null,
    "appVersion": "1.0.0"
  },
  "tokenExpiryHours": 24
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "sessionId": "session_id",
    "token": "session_token",
    "refreshToken": "refresh_token",
    "expiresAt": "2024-01-01T00:00:00",
    "deviceInfo": {
      "deviceId": "device_unique_id",
      "deviceType": "IOS",
      "os": "iOS 17.0",
      "browser": null,
      "appVersion": "1.0.0"
    }
  },
  "message": "会话创建成功"
}
```

### 4.2 刷新Token

**POST** `/sessions/refresh`

**请求体:**
```json
{
  "refreshToken": "refresh_token",
  "tokenExpiryHours": 24
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "sessionId": "new_session_id",
    "token": "new_session_token",
    "refreshToken": "new_refresh_token",
    "expiresAt": "2024-01-02T00:00:00",
    "deviceInfo": {
      "deviceId": "device_unique_id",
      "deviceType": "IOS",
      "os": "iOS 17.0",
      "browser": null,
      "appVersion": "1.0.0"
    }
  },
  "message": "Token刷新成功"
}
```

### 4.3 获取会话列表

**GET** `/sessions`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "success": true,
  "data": [
    {
      "sessionId": "session_id",
      "token": "session_token",
      "refreshToken": "refresh_token",
      "expiresAt": "2024-01-01T00:00:00",
      "deviceInfo": {
        "deviceId": "device_unique_id",
        "deviceType": "IOS",
        "os": "iOS 17.0",
        "browser": null,
        "appVersion": "1.0.0"
      },
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "message": "获取会话列表成功"
}
```

### 4.4 获取活跃会话

**GET** `/sessions/active`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "success": true,
  "data": [
    {
      "sessionId": "session_id",
      "token": "session_token",
      "refreshToken": "refresh_token",
      "expiresAt": "2024-01-01T00:00:00",
      "deviceInfo": {
        "deviceId": "device_unique_id",
        "deviceType": "IOS",
        "os": "iOS 17.0",
        "browser": null,
        "appVersion": "1.0.0"
      },
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "message": "获取活跃会话成功"
}
```

### 4.5 失效单个会话

**DELETE** `/sessions/{sessionId}`

**请求头:**
- `X-User-ID`: 用户ID

**路径参数:**
- `sessionId`: 会话ID

**响应:**
```json
{
  "success": true,
  "data": null,
  "message": "会话失效成功"
}
```

### 4.6 失效所有会话

**DELETE** `/sessions`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "success": true,
  "data": null,
  "message": "所有会话失效成功"
}
```

### 4.7 失效其他会话

**DELETE** `/sessions/others`

**请求头:**
- `X-User-ID`: 用户ID
- `X-Session-ID`: 当前会话ID

**响应:**
```json
{
  "success": true,
  "data": null,
  "message": "其他会话失效成功"
}
```

### 4.8 获取会话统计

**GET** `/sessions/stats`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "success": true,
  "data": {
    "totalSessions": 5,
    "activeSessions": 3,
    "deviceTypes": {
      "IOS": 2,
      "ANDROID": 1
    },
    "lastActivity": "2024-01-01T12:00:00"
  },
  "message": "获取会话统计成功"
}
```

## 5. 业务接口

### 5.1 获取Prompt列表

**GET** `/prompts`

**请求头:**
- `X-User-ID`: 用户ID

**查询参数:**
- `page`: 页码 (默认: 1)
- `limit`: 每页数量 (默认: 20)
- `search`: 搜索关键词
- `tags`: 标签筛选 (逗号分隔)

**响应:**
```json
{
  "success": true,
  "data": {
    "prompts": [
      {
        "id": "prompt_id",
        "title": "Prompt标题",
        "content": "Prompt内容",
        "tags": ["标签1", "标签2"],
        "isPublic": true,
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 100,
      "pages": 5
    }
  },
  "message": "获取Prompt列表成功"
}
```

### 5.2 创建Prompt

**POST** `/prompts`

**请求头:**
- `X-User-ID`: 用户ID

**请求体:**
```json
{
  "title": "Prompt标题",
  "content": "Prompt内容",
  "tags": ["标签1", "标签2"],
  "isPublic": true
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "id": "prompt_id",
    "title": "Prompt标题",
    "content": "Prompt内容",
    "tags": ["标签1", "标签2"],
    "isPublic": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "message": "创建Prompt成功"
}
```

## 6. 错误码说明

### 6.1 认证相关错误码
- `AUTH_001`: 认证失败
- `AUTH_002`: Token过期
- `AUTH_003`: 三方登录失败
- `AUTH_004`: 游客升级失败

### 6.2 验证相关错误码
- `VALIDATION_001`: 参数验证失败

### 6.3 资源相关错误码
- `NOT_FOUND_001`: 用户不存在
- `NOT_FOUND_002`: Prompt不存在

### 6.4 冲突相关错误码
- `CONFLICT_001`: 邮箱已存在
- `CONFLICT_002`: 用户名已存在

### 6.5 权限相关错误码
- `ACCESS_DENIED_001`: 无权访问
- `ACCESS_DENIED_002`: 权限不足

### 6.6 系统相关错误码
- `SYSTEM_001`: 系统错误
- `SYSTEM_002`: 数据库错误

## 7. 设备类型说明

### 7.1 支持的设备类型
- `WEB`: Web端
- `ANDROID`: Android端
- `IOS`: iOS端

## 8. 三方登录提供商

### 8.1 支持的提供商
- `GITHUB`: GitHub登录
- `GOOGLE`: Google登录
- `WECHAT`: 微信登录
- `APPLE`: Apple登录

## 9. 用户类型说明

### 9.1 用户类型
- `REGISTERED`: 注册用户
- `GUEST`: 游客
- `OAUTH`: 三方登录用户

## 10. 使用示例

### 10.1 完整登录流程
1. 用户注册或登录
2. 创建会话
3. 使用Token访问受保护接口
4. Token过期时使用refreshToken刷新

### 10.2 三方登录流程
1. 前端跳转到三方登录页面
2. 三方登录成功后回调到后端
3. 后端处理三方登录并返回用户信息
4. 创建会话

### 10.3 多设备管理
1. 用户可以在多个设备上同时登录
2. 可以查看和管理所有活跃会话
3. 可以单独或批量失效会话

## 11. 安全说明

### 11.1 Token安全
- Token有过期时间
- 支持Token刷新
- 支持会话失效

### 11.2 密码安全
- 使用BCrypt加密存储
- 支持密码强度验证

### 11.3 三方登录安全
- 支持三方登录关联管理
- 防止重复关联
- 支持安全解绑

---

**文档版本**: 1.0  
**最后更新**: 2024年1月  
**维护者**: PromptFlow开发团队
