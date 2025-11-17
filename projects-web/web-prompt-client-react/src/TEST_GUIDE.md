# 🧪 API 测试指南

后端已启动！现在可以测试真实的 API 对接了。

## 📋 测试清单

### ✅ 第一步：测试用户注册和登录

#### 1. **注册新账号**

1. 刷新页面回到登录界面
2. 点击"使用邮箱密码登录"按钮
3. 切换到"注册"标签
4. 填写表单：
   ```
   用户名: testuser
   邮箱: test@example.com
   密码: password123
   显示名称: 测试用户
   ```
5. 点击"注册"按钮

**预期结果：**
- ✅ 看到"注册成功！请登录"提示
- ✅ 自动切换到登录标签
- ✅ 邮箱和密码已自动填充

#### 2. **登录账号**

1. 在登录标签下
2. 邮箱：`test@example.com`
3. 密码：`password123`
4. 点击"登录"按钮

**预期结果：**
- ✅ 看到"登录成功！"提示
- ✅ 进入主界面
- ✅ 右上角显示您的用户名

---

### ✅ 第二步：测试 Prompt 管理

#### 1. **创建 Prompt**

1. 点击左侧"+ 新建 Prompt"按钮
2. 填写表单：
   ```
   标题: 测试API对接
   内容: 这是一个测试{variable}的Prompt
   分类: 测试
   标签: API, 测试
   ```
3. 点击"保存"

**预期结果：**
- ✅ 看到"Prompt 创建成功！"提示
- ✅ 在列表中看到新创建的 Prompt
- ✅ **打开浏览器 DevTools → Network** 可以看到：
  - `POST http://localhost:8080/api/prompts` 请求
  - 状态码：201 Created

#### 2. **编辑 Prompt**

1. 点击刚创建的 Prompt 进入详情页
2. 点击"编辑"按钮
3. 修改标题为："测试API对接 - 已修改"
4. 点击"保存"

**预期结果：**
- ✅ 看到"Prompt 更新成功！"提示
- ✅ 标题已更新
- ✅ Network 中看到 `PUT http://localhost:8080/api/prompts/{id}`

#### 3. **收藏 Prompt**

1. 在详情页点击星标图标
2. 或在列表中点击星标

**预期结果：**
- ✅ 看到"已添加到收藏"提示
- ✅ 星标变为黄色
- ✅ Network 中看到 `POST http://localhost:8080/api/prompts/{id}/favorite`

#### 4. **删除 Prompt**

1. 在详情页点击"删除"按钮
2. 确认删除

**预期结果：**
- ✅ 看到"Prompt 删除成功！"提示
- ✅ 返回列表页
- ✅ Prompt 已从列表中消失
- ✅ Network 中看到 `DELETE http://localhost:8080/api/prompts/{id}`

---

### ✅ 第三步：测试数据同步

#### 1. **刷新页面**

1. 按 `F5` 或点击浏览器刷新按钮
2. 等待页面加载

**预期结果：**
- ✅ 自动登录（无需重新输入密码）
- ✅ 看到之前创建的 Prompt（从服务器加载）
- ✅ Network 中看到 `GET http://localhost:8080/api/prompts`

#### 2. **测试多端同步**

1. 打开另一个浏览器窗口（隐私模式）
2. 登录同一个账号
3. 查看 Prompt 列表

**预期结果：**
- ✅ 两个窗口看到相同的数据
- ✅ 在一个窗口创建 Prompt
- ✅ 刷新另一个窗口能看到新数据

---

### ✅ 第四步：测试错误处理

#### 1. **测试网络错误**

1. 停止后端服务
2. 尝试创建一个 Prompt

**预期结果：**
- ✅ 看到错误提示（如"创建失败: Failed to fetch"）
- ✅ 数据不会丢失

#### 2. **测试 Token 过期**

1. 手动清除 localStorage 中的 `access_token`
2. 尝试创建 Prompt

**预期结果：**
- ✅ 自动尝试刷新 Token
- ✅ 或提示需要重新登录

---

## 🔍 调试技巧

### 1. **查看 Network 请求**

打开浏览器 DevTools (F12) → Network 标签：

**成功的请求应该显示：**
```
POST /api/prompts          201 Created
GET  /api/prompts          200 OK
PUT  /api/prompts/1        200 OK
DELETE /api/prompts/1      204 No Content
POST /api/prompts/1/favorite 200 OK
```

### 2. **查看请求详情**

点击任一请求，查看：
- **Headers** - 确认 `Authorization: Bearer {token}` 存在
- **Payload** - 查看发送的数据
- **Response** - 查看返回的数据

### 3. **查看 LocalStorage**

DevTools → Application → Local Storage：

**真实用户应该看到：**
```
access_token: "eyJhbGc..."  (真实 JWT Token)
refresh_token: "..."
user: {"userId":"xxx","username":"testuser",...}
```

**游客用户会看到：**
```
access_token: "guest_token_..."
guest_prompts: [...]  (本地数据)
```

---

## ⚠️ 常见问题

### 问题 1: 注册失败 - "邮箱已存在"

**原因：** 该邮箱已经注册过了

**解决：**
- 使用不同的邮箱地址
- 或直接登录

### 问题 2: 创建 Prompt 失败 - "401 Unauthorized"

**原因：** Token 无效或已过期

**解决：**
1. 退出登录
2. 重新登录
3. 检查后端是否正确验证 Token

### 问题 3: CORS 错误

```
Access to fetch at 'http://localhost:8080/api/...' has been blocked by CORS policy
```

**解决：** 后端需要添加 CORS 配置：
```java
@CrossOrigin(origins = "*") // 允许所有来源（开发环境）
```

### 问题 4: 数据不同步

**检查：**
1. 确认使用的是真实用户登录（不是游客模式）
2. 查看 Network 是否有请求发出
3. 检查请求是否返回正确数据

---

## ✅ 验证成功的标志

如果一切正常，您应该能：

- ✅ 注册新账号
- ✅ 登录和退出
- ✅ 创建、编辑、删除 Prompt
- ✅ 收藏功能正常
- ✅ 刷新页面数据不丢失
- ✅ 多个浏览器窗口数据同步
- ✅ Network 中看到所有 API 请求

**当所有功能都正常时，恭喜您！前后端对接成功！** 🎉

---

## 🚀 下一步

1. **完善功能**
   - 实现文件夹管理 API
   - 添加搜索排序功能
   - 实现分享功能

2. **优化性能**
   - 添加分页加载
   - 实现虚拟滚动
   - 优化 API 调用频率

3. **准备部署**
   - 配置生产环境 API 地址
   - 实现 HTTPS
   - 添加错误监控

---

**祝测试顺利！** 🎊
