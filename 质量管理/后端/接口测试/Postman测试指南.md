# Postman 接口测试指南

## 概述
本文档提供使用 Postman 进行后端接口测试的完整指南，包括环境配置、集合管理、测试脚本编写等。

## 环境配置

### 1. 基础环境变量
```json
{
  "base_url": "http://localhost:8080",
  "gateway_url": "http://localhost:8765",
  "auth_token": "{{bearer_token}}"
}
```

### 2. 服务端点
- **网关服务**: `{{gateway_url}}`
- **用户服务**: `{{base_url}}:8081`
- **会员服务**: `{{base_url}}:8082`
- **业务服务**: `{{base_url}}:8083`

## 测试集合结构

### 1. 认证测试集合
- 用户注册
- 用户登录
- Token 刷新
- 用户登出

### 2. 用户管理测试集合
- 获取用户信息
- 更新用户信息
- 删除用户
- 用户列表查询

### 3. 会员管理测试集合
- 会员注册
- 会员信息查询
- 会员等级管理
- 会员积分操作

### 4. 业务服务测试集合
- 业务数据创建
- 业务数据查询
- 业务数据更新
- 业务数据删除

## 测试脚本示例

### 认证测试脚本
```javascript
// 登录后设置全局变量
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has access token", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.accessToken).to.not.be.undefined;
    pm.environment.set("bearer_token", jsonData.data.accessToken);
});
```

### 数据验证脚本
```javascript
// 验证响应数据结构
pm.test("Response structure validation", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
    pm.expect(jsonData.code).to.equal(200);
});
```

## 测试执行流程

### 1. 环境准备
1. 启动所有微服务
2. 配置 Postman 环境变量
3. 导入测试集合

### 2. 测试执行顺序
1. 认证测试 → 获取 Token
2. 用户管理测试
3. 会员管理测试  
4. 业务服务测试

### 3. 测试报告
- 使用 Newman 生成 HTML 报告
- 集成到 CI/CD 流水线
- 性能测试报告

## 常见问题

### 1. Token 过期处理
- 设置自动刷新机制
- 使用 Pre-request Script 检查 Token 有效性

### 2. 数据依赖
- 使用测试数据工厂
- 清理测试数据
- 数据隔离策略

### 3. 环境切换
- 开发环境
- 测试环境  
- 生产环境
