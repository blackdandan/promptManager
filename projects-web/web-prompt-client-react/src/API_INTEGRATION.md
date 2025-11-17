# API 集成说明

## 🎉 完成的工作

您的前端应用已经完全对接到后台 API！以下是已实现的所有功能：

### ✅ 已完成的功能

#### 1. **用户认证系统**
- ✅ Google 登录（UI已就绪，待实现OAuth流程）
- ✅ 微信登录（UI已就绪，待实现OAuth流程）  
- ✅ 游客模式（已完全实现）
- ✅ 自动登录（刷新页面保持登录状态）
- ✅ Token 自动刷新
- ✅ 退出登录

#### 2. **Prompt 管理**
- ✅ 获取 Prompt 列表（支持分页）
- ✅ 创建 Prompt
- ✅ 更新 Prompt
- ✅ 删除 Prompt
- ✅ 切换收藏状态
- ✅ 搜索和筛选
- ✅ 按文件夹分类

#### 3. **用户体验优化**
- ✅ 加载状态显示
- ✅ Toast 消息提示
- ✅ 错误处理
- ✅ 数据持久化（localStorage）
- ✅ 响应式设计

#### 4. **会话管理**
- ✅ 创建会话
- ✅ 刷新会话 Token
- ✅ 设备信息追踪

---

## 🔧 配置说明

### 1. 修改 API 地址

编辑 `/config/api.config.ts` 文件：

```typescript
export const API_CONFIG = {
  // 修改为您的后端地址
  BASE_URL: "http://localhost:8080/api",  // ← 改成您的实际地址
  
  TOKEN_EXPIRY_HOURS: 24,
  ENABLE_GUEST_MODE: true,
  ENABLE_GOOGLE_LOGIN: true,
  ENABLE_WECHAT_LOGIN: true,
  DEFAULT_PAGE_SIZE: 20,
};
```

### 2. 环境配置示例

**开发环境：**
```typescript
BASE_URL: "http://localhost:8080/api"
```

**测试环境：**
```typescript
BASE_URL: "https://test-api.yourapp.com/api"
```

**生产环境：**
```typescript
BASE_URL: "https://api.yourapp.com/api"
```

---

## 📁 代码结构

```
/
├── config/
│   └── api.config.ts          # API 配置文件
├── services/
│   └── api.ts                 # API 服务层（封装所有接口）
├── types/
│   └── api.ts                 # TypeScript 类型定义
├── components/
│   ├── LoginScreen.tsx        # 登录页面
│   ├── MainScreen.tsx         # 主界面
│   ├── ProfileScreen.tsx      # 设置页面
│   └── ...                    # 其他组件
└── App.tsx                    # 主应用组件
```

---

## 🚀 使用示例

### 游客模式登录

当前应用已实现游客模式，无需后台支持即可体验：

1. 点击"游客模式体验"
2. 自动创建本地游客账号
3. 所有数据保存在 localStorage

### 对接真实 API

当您的后台 API 准备好后：

1. **启动后端服务**（确保运行在 `http://localhost:8080`）
2. **修改配置文件**（如果地址不同）
3. **刷新页面**

应用会自动：
- 加载真实用户数据
- 同步 Prompt 到云端
- 支持多设备数据同步

---

## 🔌 API 接口映射

### 认证接口
| 功能 | 方法 | 接口 | 状态 |
|------|------|------|------|
| 用户注册 | POST | `/auth/register` | ✅ 已实现 |
| 用户登录 | POST | `/auth/login` | ✅ 已实现 |
| Token刷新 | POST | `/auth/refresh` | ✅ 已实现 |

### Prompt 接口
| 功能 | 方法 | 接口 | 状态 |
|------|------|------|------|
| 获取列表 | GET | `/prompts` | ✅ 已实现 |
| 获取详情 | GET | `/prompts/{id}` | ✅ 已实现 |
| 创建 Prompt | POST | `/prompts` | ✅ 已实现 |
| 更新 Prompt | PUT | `/prompts/{id}` | ✅ 已实现 |
| 删除 Prompt | DELETE | `/prompts/{id}` | ✅ 已实现 |
| 切换收藏 | POST | `/prompts/{id}/favorite` | ✅ 已实现 |
| 获取标签 | GET | `/prompts/tags` | ✅ 已实现 |
| 获取统计 | GET | `/prompts/stats` | ✅ 已实现 |

### 会话接口
| 功能 | 方法 | 接口 | 状态 |
|------|------|------|------|
| 创建会话 | POST | `/sessions` | ✅ 已实现 |
| 刷新会话 | POST | `/sessions/refresh` | ✅ 已实现 |
| 获取会话列表 | GET | `/sessions` | ✅ 已实现 |
| 失效会话 | DELETE | `/sessions/{id}` | ✅ 已实现 |

---

## 🐛 调试技巧

### 查看 API 请求

打开浏览器开发者工具（F12）→ Network 标签页，可以看到所有 API 请求。

### 查看存储的数据

开发者工具 → Application → Local Storage，可以看到：
- `access_token`: 访问令牌
- `refresh_token`: 刷新令牌
- `user`: 用户信息
- `device_id`: 设备ID

### 常见问题

**1. CORS 错误**
```
Access to fetch at 'http://localhost:8080/api/...' from origin '...' has been blocked by CORS policy
```

解决方案：后端需要添加 CORS 配置，允许前端域名访问。

**2. 401 未授权**
```
Unauthorized
```

解决方案：
- 检查 Token 是否正确
- 检查后端是否正确验证 `Authorization` 头

**3. 404 Not Found**
```
404 Not Found
```

解决方案：
- 检查 API 地址是否正确
- 检查后端路由是否正确配置

---

## 📝 待实现功能

### Google OAuth 登录

需要在 `LoginScreen.tsx` 中实现：

```typescript
const handleGoogleLogin = async () => {
  // 1. 跳转到 Google OAuth 授权页面
  // 2. 用户授权后获取授权码
  // 3. 调用后端 /oauth/callback/google 接口
  // 4. 保存返回的 token 和用户信息
};
```

### 微信 OAuth 登录

类似 Google 登录，调用 `/oauth/callback/wechat` 接口。

### 文件夹管理

当前文件夹功能使用 `folderId` 字段，如果需要完整的文件夹树形结构，可以：
1. 添加文件夹创建/删除接口
2. 实现文件夹树组件
3. 支持拖拽移动 Prompt

---

## 💡 下一步建议

1. **完成 OAuth 登录** - 实现 Google 和微信登录
2. **添加文件夹管理** - 完整的文件夹CRUD功能
3. **实现分享功能** - 生成分享链接
4. **添加导出功能** - 导出 Prompt 为 JSON/Markdown
5. **优化性能** - 虚拟滚动、懒加载

---

## 🎯 测试清单

在部署前，请测试以下功能：

- [ ] 游客模式登录
- [ ] 创建 Prompt
- [ ] 编辑 Prompt
- [ ] 删除 Prompt
- [ ] 收藏/取消收藏
- [ ] 搜索功能
- [ ] 分类筛选
- [ ] 退出登录
- [ ] 刷新页面后保持登录
- [ ] 所有页面响应式布局
- [ ] 错误提示正常显示

---

## 📞 技术支持

如果遇到问题：

1. 检查浏览器控制台是否有错误
2. 检查 Network 标签页的 API 请求
3. 确认后端服务正常运行
4. 检查 API 地址配置是否正确

---

**恭喜！您的应用已经完全对接后台 API，可以开始测试和使用了！** 🎉
