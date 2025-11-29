# Android端开发计划与进度追踪

## 1. 项目概述

本项目旨在构建 Prompt Manager 的 Android 客户端，提供高效的 Prompt 管理体验，支持多端数据同步和离线使用。

### 1.1 技术栈
- **语言**: Kotlin
- **UI框架**: Jetpack Compose (Material 3)
- **架构**: Clean Architecture + MVVM
- **依赖注入**: Dagger Hilt
- **异步处理**: Coroutines + Flow
- **网络请求**: Retrofit + OkHttp + Moshi
- **本地存储**: Room Database
- **图片加载**: Coil
- **导航**: Navigation Compose

### 1.2 架构设计
采用 **按功能分包 (Package by Feature)** 结合 **分层架构** 的策略：
- **Presentation Layer**: UI (Compose) + ViewModel
- **Domain Layer**: UseCases + Repository Interfaces (纯 Kotlin)
- **Data Layer**: Repository Implementations + Data Sources (API/DB)

---

## 2. 开发进度追踪

### 阶段一：项目初始化与基础架构 [进行中]
- [x] **项目骨架搭建**
    - [x] 创建 Gradle 构建文件 (`build.gradle.kts`, `libs.versions.toml`)
    - [x] 配置应用清单 (`AndroidManifest.xml`)
    - [x] 创建基础资源文件 (`strings.xml`, `themes.xml`, `ic_launcher`)
    - [x] 创建 Application 入口 (`PromptManagerApplication`)
    - [x] 创建主 Activity (`MainActivity`)
- [ ] **核心架构实现**
    - [ ] **Design System (设计系统)**
        - [x] 定义颜色 (`Color.kt`) - *基于 Figma 数据*
        - [x] 定义字体 (`Type.kt`) - *基于 Figma 数据*
        - [ ] 定义主题 (`Theme.kt`)
        - [ ] 封装基础组件 (Button, TextField)
    - [ ] **依赖注入 (DI)**
        - [ ] 配置 Hilt 模块 (`NetworkModule`, `DatabaseModule`)
    - [ ] **网络层**
        - [ ] 定义 API 接口 (`AuthService`, `PromptService`)
        - [ ] 配置 Retrofit 客户端
    - [ ] **持久层**
        - [ ] 定义 Room 实体 (`PromptEntity`, `UserEntity`)
        - [ ] 创建 DAO 接口
        - [ ] 配置 Room Database

### 阶段二：用户认证模块 [完成]
- [x] **UI 实现**
    - [x] 登录页面 (`LoginScreen`) - *高度还原 Figma，支持邮箱/三方登录*
    - [x] 注册页面 (`RegisterScreen`) - *支持邮箱注册*
    - [x] 实现国际化支持 (En/Zh) - *全量覆盖*
    - [x] 添加邮箱/密码登录表单
- [ ] **逻辑实现**
    - [ ] `AuthRepository` 实现 (登录/注册/Token管理)
    - [ ] `AuthViewModel` 实现
    - [ ] Token 持久化 (DataStore)

### 阶段三：Prompt 管理核心功能 [完成]
- [x] **UI 实现**
    - [x] Prompt 列表页 (`PromptListScreen`) - *支持搜索、过滤、卡片列表*
    - [x] 主界面底部导航 (`MainScreen`) - *支持多 Tab 切换，自定义图标*
    - [x] 侧边栏导航 (`DrawerContent`) - *支持多级文件夹目录*
    - [x] 创建/编辑页 (`PromptEditorScreen`) - *支持表单输入*
    - [ ] Prompt 详情页 (`PromptDetailScreen`) - *暂由编辑页承担*
- [ ] **逻辑实现**
    - [ ] `PromptRepository` 实现 (CRUD)
    - [ ] 离线缓存逻辑
    - [ ] 搜索与筛选功能

### 阶段四：数据同步与优化 [待开始]
- [ ] **数据同步**
    - [ ] 实现增量同步算法
    - [ ] 处理数据冲突
- [ ] **优化**
    - [ ] UI/UX 细节优化
    - [ ] 性能调优

---

## 3. 设计规范备忘 (Figma)
- **主色调**: 渐变蓝紫 (`#2B7FFF` -> `#9810FA`)
- **背景**: 白色或浅灰 (`#ECEEF2`)
- **字体**: 系统默认 (Roboto/Arial), 标题 22sp, 正文 16sp/14sp
