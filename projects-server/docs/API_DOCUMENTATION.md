# PromptFlow API 文档

## 1. 概述

PromptFlow 是一个完整的用户认证和会话管理系统，支持多种登录方式和设备管理。

### 基础信息
- **基础URL**: `http://localhost:8080/api`
- **认证方式**: Token认证 (通过请求头传递)
- **认证头**: `Authorization: Bearer {{access_token}}`
- **响应格式**: JSON
- **错误处理**: 统一错误响应格式

### 统一响应格式
> **注意**：所有API（包括新增的分类管理接口）都必须遵循此统一响应格式。后端控制器应返回 `ApiResponse<T>` 对象。

```json
{
  "code": 200, // 成功为200，失败见错误码
  "message": "操作成功", // 或错误描述
  "data": { ... } // 业务数据，失败时为null
}
```

### 错误响应格式
```json
{
  "code": "ERROR_CODE",
  "message": "错误消息",
  "data": null
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
  "code": 201,
  "message": "注册成功",
  "data": {
    "userId": "user_id",
    "username": "user123",
    "email": "user@example.com",
    "displayName": "用户昵称",
    "userType": "REGISTERED",
    "roles": ["USER"]
  }
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
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "access_token",
    "refreshToken": "refresh_token",
    "expiresIn": 3600,
    "user": {
      "userId": "user_id",
      "username": "user123",
      "email": "user@example.com",
      "displayName": "用户昵称",
      "userType": "REGISTERED",
      "roles": ["USER"]
    }
  }
}
```

### 2.3 Token刷新

**POST** `/auth/refresh`

**请求体:**
```json
{
  "refreshToken": "session_refresh_token"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token",
    "expiresIn": 3600,
    "user": {
      "userId": "user_id",
      "username": "user123",
      "email": "user@example.com",
      "displayName": "用户昵称",
      "userType": "REGISTERED",
      "roles": ["USER"]
    }
  }
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
  "code": 200,
  "message": "三方登录成功",
  "data": {
    "userId": "user_id",
    "username": "user123",
    "email": "user@example.com",
    "displayName": "用户昵称",
    "userType": "OAUTH",
    "roles": ["USER"]
  }
}
```

### 3.2 获取三方登录关联

**GET** `/oauth/connections`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取三方登录关联成功",
  "data": [
    {
      "provider": "GITHUB",
      "providerUserId": "github_user_id",
      "createdAt": "2024-01-01T00:00:00"
    }
  ]
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
  "code": 200,
  "message": "解绑三方登录成功",
  "data": null
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
  "code": 201,
  "message": "会话创建成功",
  "data": {
    "sessionId": "session_id",
    "accessToken": "access_token",
    "refreshToken": "refresh_token",
    "expiresAt": "2024-01-01T00:00:00",
    "deviceInfo": {
      "deviceId": "device_unique_id",
      "deviceType": "IOS",
      "os": "iOS 17.0",
      "browser": null,
      "appVersion": "1.0.0"
    }
  }
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
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "sessionId": "new_session_id",
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token",
    "expiresAt": "2024-01-02T00:00:00",
    "deviceInfo": {
      "deviceId": "device_unique_id",
      "deviceType": "IOS",
      "os": "iOS 17.0",
      "browser": null,
      "appVersion": "1.0.0"
    }
  }
}
```

### 4.3 获取会话列表

**GET** `/sessions`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取会话列表成功",
  "data": [
    {
      "sessionId": "session_id",
      "accessToken": "access_token",
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
  ]
}
```

### 4.4 获取活跃会话

**GET** `/sessions/active`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取活跃会话成功",
  "data": [
    {
      "sessionId": "session_id",
      "accessToken": "access_token",
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
  ]
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
  "code": 200,
  "message": "会话失效成功",
  "data": null
}
```

### 4.6 失效所有会话

**DELETE** `/sessions`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "所有会话失效成功",
  "data": null
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
  "code": 200,
  "message": "其他会话失效成功",
  "data": null
}
```

### 4.8 获取会话统计

**GET** `/sessions/stats`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取会话统计成功",
  "data": {
    "totalSessions": 5,
    "activeSessions": 3,
    "deviceTypes": {
      "IOS": 2,
      "ANDROID": 1
    },
    "lastActivity": "2024-01-01T12:00:00"
  }
}
```

## 5. 业务接口

### 5.1 获取用户Prompt列表

**GET** `/prompts`

**请求头:**
- `X-User-Id`: 用户ID

**查询参数:**
- `page`: 页码 (默认: 0)
- `size`: 每页数量 (默认: 20)
- `search`: 搜索关键词
- `tags`: 标签筛选 (数组)
- `category`: 分类筛选
- `isFavorite`: 是否收藏
- `folderId`: 文件夹ID (传入 `root` 可查询无文件夹的Prompt)
- `sort`: 排序字段 (例如 `lastUsedAt,desc`)

**响应:**
```json
{
  "code": 200,
  "message": "获取Prompt列表成功",
  "data": {
    "content": [
      {
        "id": "prompt_id",
        "userId": "user_id",
        "title": "Prompt标题",
        "content": "Prompt内容",
        "description": "描述",
        "tags": ["标签1", "标签2"],
        "category": "分类",
        "isPublic": true,
        "isFavorite": false,
        "usageCount": 5,
        "folderId": "folder_id",
        "status": "ACTIVE",
        "lastUsedAt": "2024-01-01T00:00:00",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": { ... }
    },
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true
  }
}
```

### 5.2 获取单个Prompt

**GET** `/prompts/{id}`

**请求头:**
- `X-User-Id`: 用户ID

**路径参数:**
- `id`: Prompt ID

**响应:**
```json
{
  "code": 200,
  "message": "获取Prompt成功",
  "data": {
    "id": "prompt_id",
    "userId": "user_id",
    "title": "Prompt标题",
    "content": "Prompt内容",
    "description": "描述",
    "tags": ["标签1", "标签2"],
    "category": "分类",
    "isPublic": true,
    "isFavorite": false,
    "usageCount": 5,
    "folderId": "folder_id",
    "status": "ACTIVE",
    "lastUsedAt": "2024-01-01T00:00:00",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 5.3 创建Prompt

**POST** `/prompts`

**请求头:**
- `X-User-Id`: 用户ID

**请求体:**
```json
{
  "title": "Prompt标题",
  "content": "Prompt内容",
  "description": "描述",
  "tags": ["标签1", "标签2"],
  "category": "分类",
  "isPublic": true,
  "folderId": "folder_id"
}
```

**响应:**
```json
{
  "code": 201,
  "message": "创建Prompt成功",
  "data": {
    "id": "prompt_id",
    "userId": "user_id",
    "title": "Prompt标题",
    "content": "Prompt内容",
    "description": "描述",
    "tags": ["标签1", "标签2"],
    "category": "分类",
    "isPublic": true,
    "isFavorite": false,
    "usageCount": 0,
    "folderId": "folder_id",
    "status": "ACTIVE",
    "lastUsedAt": null,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 5.4 更新Prompt

**PUT** `/prompts/{id}`

**请求头:**
- `X-User-Id`: 用户ID

**路径参数:**
- `id`: Prompt ID

**请求体:**
```json
{
  "title": "更新后的标题",
  "content": "更新后的内容",
  "description": "更新后的描述",
  "tags": ["新标签1", "新标签2"],
  "category": "新分类",
  "isPublic": false,
  "isFavorite": true,
  "folderId": "新文件夹ID",
  "status": "INACTIVE"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "更新Prompt成功",
  "data": {
    "id": "prompt_id",
    "userId": "user_id",
    "title": "更新后的标题",
    "content": "更新后的内容",
    "description": "更新后的描述",
    "tags": ["新标签1", "新标签2"],
    "category": "新分类",
    "isPublic": false,
    "isFavorite": true,
    "usageCount": 5,
    "folderId": "新文件夹ID",
    "status": "INACTIVE",
    "lastUsedAt": "2024-01-01T00:00:00",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-02T00:00:00"
  }
}
```

### 5.5 删除Prompt

**DELETE** `/prompts/{id}`

**请求头:**
- `X-User-Id`: 用户ID

**路径参数:**
- `id`: Prompt ID

**响应:**
```json
{
  "code": 204,
  "message": "删除Prompt成功",
  "data": null
}
```

### 5.6 切换收藏状态

**POST** `/prompts/{id}/favorite`

**请求头:**
- `X-User-Id`: 用户ID

**路径参数:**
- `id`: Prompt ID

**响应:**
```json
{
  "code": 200,
  "message": "切换收藏状态成功",
  "data": {
    "id": "prompt_id",
    "userId": "user_id",
    "title": "Prompt标题",
    "content": "Prompt内容",
    "description": "描述",
    "tags": ["标签1", "标签2"],
    "category": "分类",
    "isPublic": true,
    "isFavorite": true,
    "usageCount": 5,
    "folderId": "folder_id",
    "status": "ACTIVE",
    "lastUsedAt": "2024-01-01T00:00:00",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 5.7 获取公开Prompt列表

**GET** `/prompts/public`

**查询参数:**
- `page`: 页码 (默认: 0)
- `size`: 每页数量 (默认: 20)
- `search`: 搜索关键词
- `tags`: 标签筛选 (数组)

**响应:**
```json
{
  "code": 200,
  "message": "获取公开Prompt列表成功",
  "data": {
    "content": [
      {
        "id": "prompt_id",
        "userId": "user_id",
        "title": "Prompt标题",
        "content": "Prompt内容",
        "description": "描述",
        "tags": ["标签1", "标签2"],
        "category": "分类",
        "isPublic": true,
        "isFavorite": false,
        "usageCount": 5,
        "folderId": "folder_id",
        "status": "ACTIVE",
        "lastUsedAt": "2024-01-01T00:00:00",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "pageable": { ... },
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### 5.8 获取用户标签列表

**GET** `/prompts/tags`

**请求头:**
- `X-User-Id`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取标签列表成功",
  "data": ["标签1", "标签2", "标签3"]
}
```

### 5.9 获取用户统计信息

**GET** `/prompts/stats`

**请求头:**
- `X-User-Id`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取统计信息成功",
  "data": {
    "totalPrompts": 50,
    "favoritePrompts": 10,
    "publicPrompts": 15,
    "totalUsage": 120,
    "mostUsedTags": ["AI", "写作", "编程"]
  }
}
```

### 5.10 使用Prompt

**POST** `/prompts/{id}/use`

**请求头:**
- `X-User-Id`: 用户ID

**路径参数:**
- `id`: Prompt ID

**响应:**
```json
{
  "code": 200,
  "message": "更新使用次数成功",
  "data": null
}
```

## 6. 会员服务接口

### 6.1 创建免费会员

**POST** `/membership/free`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 201,
  "message": "免费会员创建成功",
  "data": {
    "membershipId": "membership_id",
    "userId": "user_id",
    "planType": "FREE",
    "status": "ACTIVE",
    "features": {
      "promptLimit": 100,
      "storageLimit": 1024,
      "exportEnabled": true
    },
    "usageLimits": {
      "prompts": 100,
      "storage": 1024
    },
    "currentUsage": {
      "prompts": 0,
      "storage": 0
    },
    "currentPeriodStart": "2024-01-01T00:00:00",
    "currentPeriodEnd": "2024-02-01T00:00:00"
  }
}
```

### 6.2 获取会员信息

**GET** `/membership`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取会员信息成功",
  "data": {
    "membershipId": "membership_id",
    "userId": "user_id",
    "planType": "FREE",
    "status": "ACTIVE",
    "features": {
      "promptLimit": 100,
      "storageLimit": 1024,
      "exportEnabled": true
    },
    "usageLimits": {
      "prompts": 100,
      "storage": 1024
    },
    "currentUsage": {
      "prompts": 25,
      "storage": 256
    },
    "currentPeriodStart": "2024-01-01T00:00:00",
    "currentPeriodEnd": "2024-02-01T00:00:00"
  }
}
```

### 6.3 升级会员

**POST** `/membership/upgrade`

**请求头:**
- `X-User-ID`: 用户ID

**请求体:**
```json
{
  "planId": "premium_monthly",
  "planType": "PREMIUM",
  "billingCycle": "MONTHLY",
  "amount": 2999
}
```

**响应:**
```json
{
  "code": 200,
  "message": "会员升级成功",
  "data": {
    "membershipId": "membership_id",
    "userId": "user_id",
    "planType": "PREMIUM",
    "status": "ACTIVE",
    "features": {
      "promptLimit": 1000,
      "storageLimit": 10240,
      "exportEnabled": true,
      "prioritySupport": true
    },
    "usageLimits": {
      "prompts": 1000,
      "storage": 10240
    },
    "currentUsage": {
      "prompts": 25,
      "storage": 256
    },
    "currentPeriodStart": "2024-01-01T00:00:00",
    "currentPeriodEnd": "2024-02-01T00:00:00"
  }
}
```

### 6.4 取消会员

**POST** `/membership/cancel`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "会员取消成功",
  "data": {
    "membershipId": "membership_id",
    "userId": "user_id",
    "planType": "FREE",
    "status": "CANCELLED",
    "features": {
      "promptLimit": 100,
      "storageLimit": 1024,
      "exportEnabled": true
    },
    "usageLimits": {
      "prompts": 100,
      "storage": 1024
    },
    "currentUsage": {
      "prompts": 25,
      "storage": 256
    },
    "currentPeriodStart": "2024-01-01T00:00:00",
    "currentPeriodEnd": "2024-02-01T00:00:00"
  }
}
```

### 6.5 更新使用量

**POST** `/membership/usage`

**请求头:**
- `X-User-ID`: 用户ID

**请求体:**
```json
{
  "feature": "prompts",
  "usage": 1
}
```

**响应:**
```json
{
  "code": 200,
  "message": "使用量更新成功",
  "data": {
    "membershipId": "membership_id",
    "userId": "user_id",
    "planType": "FREE",
    "status": "ACTIVE",
    "features": {
      "promptLimit": 100,
      "storageLimit": 1024,
      "exportEnabled": true
    },
    "usageLimits": {
      "prompts": 100,
      "storage": 1024
    },
    "currentUsage": {
      "prompts": 26,
      "storage": 256
    },
    "currentPeriodStart": "2024-01-01T00:00:00",
    "currentPeriodEnd": "2024-02-01T00:00:00"
  }
}
```

### 6.6 检查功能访问权限

**GET** `/membership/check-access/{feature}`

**请求头:**
- `X-User-ID`: 用户ID

**路径参数:**
- `feature`: 功能名称

**响应:**
```json
{
  "code": 200,
  "message": "功能访问权限检查成功",
  "data": {
    "feature": "export",
    "hasAccess": true
  }
}
```

### 6.7 获取使用统计

**GET** `/membership/stats`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取使用统计成功",
  "data": {
    "totalUsage": 120,
    "promptUsage": 25,
    "storageUsage": 256,
    "remainingPrompts": 75,
    "remainingStorage": 768
  }
}
```

## 7. 分类管理接口

### 7.1 获取分类列表

**GET** `/categories`

**请求头:**
- `X-User-ID`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取分类列表成功",
  "data": [
    {
      "id": "category_id_1",
      "userId": "user_id",
      "name": "通用",
      "sortOrder": 0,
      "isSystem": true,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    },
    {
      "id": "category_id_2",
      "userId": "user_id",
      "name": "写作",
      "sortOrder": 1,
      "isSystem": false,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ]
}
```

### 7.2 创建分类

**POST** `/categories`

**请求头:**
- `X-User-ID`: 用户ID

**请求体:**
```json
{
  "name": "新分类名称"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "创建分类成功",
  "data": {
    "id": "new_category_id",
    "userId": "user_id",
    "name": "新分类名称",
    "sortOrder": 2,
    "isSystem": false,
    "createdAt": "2024-01-02T00:00:00",
    "updatedAt": "2024-01-02T00:00:00"
  }
}
```

### 7.3 更新分类

**PUT** `/categories/{id}`

**请求头:**
- `X-User-ID`: 用户ID

**路径参数:**
- `id`: 分类ID

**请求体:**
```json
{
  "name": "更新后的分类名称"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "更新分类成功",
  "data": {
    "id": "category_id",
    "userId": "user_id",
    "name": "更新后的分类名称",
    "sortOrder": 2,
    "isSystem": false,
    "createdAt": "2024-01-02T00:00:00",
    "updatedAt": "2024-01-02T00:00:00"
  }
}
```

### 7.4 删除分类

**DELETE** `/categories/{id}`

**请求头:**
- `X-User-ID`: 用户ID

**路径参数:**
- `id`: 分类ID

**响应:**
```json
{
  "code": 200,
  "message": "删除分类成功",
  "data": null
}
```

## 8. 错误码说明

### 8.1 认证相关错误码
- `AUTH_001`: 认证失败
- `AUTH_002`: Token过期
- `AUTH_003`: 三方登录失败
- `AUTH_004`: 游客升级失败

### 8.2 验证相关错误码
- `VALIDATION_001`: 参数验证失败

### 8.3 资源相关错误码
- `NOT_FOUND_001`: 用户不存在
- `NOT_FOUND_002`: Prompt不存在
- `NOT_FOUND_003`: 会话不存在

### 8.4 冲突相关错误码
- `CONFLICT_001`: 邮箱已存在
- `CONFLICT_002`: 用户名已存在
- `CONFLICT_003`: 会员已存在

### 8.5 权限相关错误码
- `ACCESS_DENIED_001`: 无权访问
- `ACCESS_DENIED_002`: 权限不足

### 8.6 系统相关错误码
- `SYSTEM_001`: 系统错误
- `SYSTEM_002`: 数据库错误

### 8.7 会员相关错误码
- `MEMBERSHIP_001`: 会员不存在
- `MEMBERSHIP_002`: 会员已过期
- `MEMBERSHIP_003`: 会员升级失败
- `MEMBERSHIP_004`: 会员取消失败
- `MEMBERSHIP_005`: 使用量更新失败

## 8. 设备类型说明

### 8.1 支持的设备类型
- `WEB`: Web端
- `ANDROID`: Android端
- `IOS`: iOS端

## 9. 三方登录提供商

### 9.1 支持的提供商
- `GITHUB`: GitHub登录
- `GOOGLE`: Google登录
- `WECHAT`: 微信登录
- `APPLE`: Apple登录

## 10. 用户类型说明

### 10.1 用户类型
- `REGISTERED`: 注册用户
- `GUEST`: 游客
- `OAUTH`: 三方登录用户

## 11. 使用示例

### 11.1 完整登录流程
1. 用户注册或登录
2. 创建会话
3. 使用Token访问受保护接口
4. Token过期时使用refreshToken刷新

### 11.2 三方登录流程
1. 前端跳转到三方登录页面
2. 三方登录成功后回调到后端
3. 后端处理三方登录并返回用户信息
4. 创建会话

### 11.3 多设备管理
1. 用户可以在多个设备上同时登录
2. 可以查看和管理所有活跃会话
3. 可以单独或批量失效会话

## 13. 用户服务接口 (补充)

### 13.1 更新用户资料

**PUT** `/users/profile`

**请求头:**
- `X-User-Id`: 用户ID

**请求体:**
```json
{
  "displayName": "新的昵称",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "用户资料更新成功",
  "data": {
    "userId": "user_id",
    "username": "user123",
    "email": "user@example.com",
    "displayName": "新的昵称",
    "userType": "REGISTERED",
    "roles": ["USER"]
  }
}
```

### 13.2 获取用户资料

**GET** `/users/profile`

**请求头:**
- `X-User-Id`: 用户ID

**响应:**
```json
{
  "code": 200,
  "message": "获取用户资料成功",
  "data": { ... }
}
```

## 14. 反馈接口

### 14.1 提交反馈

**POST** `/feedback`

**请求头:**
- `X-User-Id`: 用户ID

**请求体:**
```json
{
  "type": "suggestion", // bug, suggestion, other
  "content": "我有一个很好的建议...",
  "contact": "user@example.com"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "反馈提交成功",
  "data": null
}
```

## 12. 安全说明

### 12.1 Token安全
- Token有过期时间
- 支持Token刷新
- 支持会话失效

### 12.2 密码安全
- 使用BCrypt加密存储
- 支持密码强度验证

### 12.3 三方登录安全
- 支持三方登录关联管理
- 防止重复关联
- 支持安全解绑

---

**文档版本**: 2.0  
**最后更新**: 2025年10月  
**维护者**: PromptFlow开发团队
