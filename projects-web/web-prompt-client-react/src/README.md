# Prompt Manager - 多平台 Prompt 管理工具

一个现代化的 Prompt 管理工具，支持多端同步、离线使用，帮助您高效管理和使用 AI 提示词。

## ✨ 核心功能

### 📝 Prompt 管理
- ✅ 增删改查 Prompt
- ✅ 模板变量化（支持 `{变量名}` 格式）
- ✅ 文件夹管理
- ✅ 标签系统（最多 5 个标签）
- ✅ 搜索和筛选
- ✅ 一键复制和分享

### 🔐 用户认证
- ✅ Google 登录（开发中）
- ✅ 微信登录（开发中）
- ✅ 游客模式（已实现）
- ✅ 自动登录
- ✅ 多端数据同步

### 🎨 用户体验
- ✅ 简洁易用的界面
- ✅ 响应式设计
- ✅ 暗色模式支持
- ✅ 离线数据缓存
- ✅ 实时数据同步

## 🚀 快速开始

### 1. 游客模式体验

无需配置，直接使用：

1. 打开应用
2. 点击"游客模式体验"
3. 开始管理您的 Prompt

> **提示：** 游客模式下数据仅保存在本地浏览器中。

### 2. 对接后台 API

如果您有自己的后台服务：

#### 步骤 1：修改 API 配置

编辑 `/config/api.config.ts`：

```typescript
export const API_CONFIG = {
  BASE_URL: "http://your-api-url.com/api",  // 改成您的 API 地址
  TOKEN_EXPIRY_HOURS: 24,
  ENABLE_GUEST_MODE: true,
};
```

#### 步骤 2：启动后端服务

确保您的后端服务运行在配置的地址上。

#### 步骤 3：刷新页面

应用会自动连接到您的后端 API。

## 📁 项目结构

```
/
├── config/               # 配置文件
│   └── api.config.ts    # API 配置
├── services/            # 服务层
│   └── api.ts          # API 封装
├── types/              # TypeScript 类型
│   └── api.ts         # API 类型定义
├── components/         # React 组件
│   ├── LoginScreen.tsx
│   ├── MainScreen.tsx
│   ├── CreatePromptScreen.tsx
│   └── ...
├── App.tsx            # 主应用
└── README.md          # 本文件
```

## 🔌 API 文档

详细的 API 接口文档请查看：[API_INTEGRATION.md](./API_INTEGRATION.md)

## 🛠️ 技术栈

- **前端框架：** React 18 + TypeScript
- **样式方案：** Tailwind CSS v4.0
- **UI 组件：** shadcn/ui
- **状态管理：** React Hooks
- **HTTP 请求：** Fetch API
- **图标库：** Lucide React

## 📱 功能说明

### Prompt 模板变量

支持使用 `{变量名}` 格式定义变量：

```
请将以下文章改写为{tone}风格，字数控制在{length}字左右：

{content}
```

### 文件夹管理

- 支持多级文件夹结构
- 格式：`工作/写作/文章`
- 可通过侧边栏快速筛选

### 标签系统

- 每个 Prompt 最多支持 5 个标签
- 支持按标签搜索和筛选
- 自动标签建议

## 🐛 常见问题

### 1. 无法连接到后台 API

**问题：** 出现 CORS 错误或网络错误

**解决方案：**
- 检查 `/config/api.config.ts` 中的 `BASE_URL` 是否正确
- 确认后端服务正在运行
- 检查后端是否配置了 CORS

### 2. 数据丢失

**问题：** 清除浏览器数据后 Prompt 丢失

**解决方案：**
- 游客模式数据仅存储在本地
- 使用 Google 或微信登录以同步到云端
- 定期导出备份数据

### 3. Token 过期

**问题：** 需要重新登录

**解决方案：**
- Token 默认 24 小时有效
- 应用会自动刷新 Token
- 如果仍然失败，请重新登录

## 🔒 数据安全

- ✅ 所有 API 请求使用 HTTPS（生产环境）
- ✅ Token 安全存储在 localStorage
- ✅ 自动 Token 刷新机制
- ✅ 支持退出登录清除本地数据

## 📝 开发计划

### v1.1（计划中）
- [ ] Google OAuth 登录
- [ ] 微信 OAuth 登录
- [ ] 完整的文件夹管理
- [ ] Prompt 分享功能

### v1.2（计划中）
- [ ] 导出/导入功能
- [ ] Prompt 模板市场
- [ ] 团队协作功能
- [ ] Android 客户端

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

---

**开发团队：** PromptFlow 团队  
**最后更新：** 2025 年 11 月  
**版本：** v1.0.0
