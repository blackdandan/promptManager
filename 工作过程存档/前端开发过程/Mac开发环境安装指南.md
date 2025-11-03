# Mac开发环境安装指南

## 1. 系统环境检查

### 1.1 检查当前系统版本
```bash
# 检查macOS版本
sw_vers

# 检查系统架构
uname -m

# 检查可用磁盘空间
df -h
```

### 1.2 推荐系统配置
- **macOS**: 12.0 (Monterey) 或更高版本
- **处理器**: Intel 或 Apple Silicon (M1/M2)
- **内存**: 8GB (推荐16GB)
- **存储**: 至少20GB可用空间

## 2. 开发工具安装

### 2.1 Homebrew 包管理器安装

#### 安装 Homebrew
```bash
# 安装 Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 配置环境变量 (根据提示执行)
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"
```

#### 验证 Homebrew 安装
```bash
brew --version
```

### 2.2 Node.js 安装

#### 使用 Homebrew 安装 Node.js
```bash
# 安装 Node.js (包含 npm)
brew install node

# 验证安装
node --version
npm --version
```

#### 安装 Node 版本管理工具 (可选)
```bash
# 安装 nvm (Node Version Manager)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# 重新加载 shell 配置
source ~/.zshrc

# 安装指定版本的 Node.js
nvm install 18
nvm use 18
```

### 2.3 Git 安装和配置

#### 安装 Git
```bash
# 使用 Homebrew 安装 Git
brew install git

# 验证安装
git --version
```

#### 配置 Git 用户信息
```bash
git config --global user.name "您的姓名"
git config --global user.email "您的邮箱"
git config --global init.defaultBranch main
```

### 2.4 Visual Studio Code 安装

#### 使用 Homebrew 安装 VS Code
```bash
# 安装 VS Code
brew install --cask visual-studio-code

# 或从官网下载: https://code.visualstudio.com/
```

#### 配置 VS Code 命令行工具
```bash
# 安装 code 命令
code --install-extension

# 验证安装
code --version
```

## 3. 前端开发工具安装

### 3.1 安装项目构建工具
```bash
# 安装 Vite (现代前端构建工具)
npm install -g vite

# 安装 Vue CLI (可选)
npm install -g @vue/cli

# 验证安装
vite --version
vue --version
```

### 3.2 安装包管理器 (可选)
```bash
# 安装 yarn (替代 npm)
npm install -g yarn

# 验证安装
yarn --version
```

## 4. VS Code 扩展安装

### 4.1 必需扩展
```bash
# Vue 开发扩展
code --install-extension Vue.volar
code --install-extension Vue.vscode-typescript-vue-plugin

# TypeScript 支持
code --install-extension ms-vscode.vscode-typescript-next

# ESLint 代码检查
code --install-extension dbaeumer.vscode-eslint

# Prettier 代码格式化
code --install-extension esbenp.prettier-vscode

# Git 增强
code --install-extension eamodio.gitlens

# 自动重命名标签
code --install-extension formulahendry.auto-rename-tag

# 路径智能提示
code --install-extension christian-kohler.path-intellisense
```

### 4.2 推荐扩展
```bash
# 图标主题
code --install-extension pkief.material-icon-theme

# 颜色高亮
code --install-extension naumovs.color-highlight

# 括号对着色
code --install-extension CoenraadS.bracket-pair-colorizer-2

# 错误透镜
code --install-extension usernamehw.errorlens
```

## 5. 浏览器环境配置

### 5.1 安装推荐浏览器
```bash
# 安装 Chrome
brew install --cask google-chrome

# 安装 Firefox (可选)
brew install --cask firefox
```

### 5.2 安装浏览器开发工具
1. 打开 Chrome 浏览器
2. 访问 Chrome 网上应用店
3. 搜索并安装以下扩展：
   - Vue.js devtools
   - React Developer Tools (如需要)
   - Redux DevTools (如需要)

## 6. 项目环境配置

### 6.1 创建项目目录
```bash
# 进入项目根目录
cd /Users/blackdandan/VSCode/promptManager

# 进入前端项目目录
cd projects-web
```

### 6.2 初始化项目依赖
```bash
# 使用 npm 初始化项目
npm init -y

# 或使用 yarn
yarn init -y
```

### 6.3 安装项目依赖
根据项目技术栈安装相应依赖：

```bash
# Vue 3 + TypeScript 项目依赖
npm install vue@next @vitejs/plugin-vue typescript
npm install vue-router@next pinia axios
npm install element-plus @element-plus/icons-vue

# 开发依赖
npm install --save-dev @vue/tsconfig vite vue-tsc
npm install --save-dev eslint @vue/eslint-config-typescript
npm install --save-dev sass
```

## 7. 环境验证

### 7.1 环境检查脚本
创建环境验证脚本：

```bash
#!/bin/bash
echo "=== Mac 前端开发环境验证 ==="

echo "1. macOS 版本: $(sw_vers -productVersion)"
echo "2. 系统架构: $(uname -m)"
echo "3. Homebrew 版本: $(brew --version | head -n1)"
echo "4. Node.js 版本: $(node --version)"
echo "5. npm 版本: $(npm --version)"
echo "6. Git 版本: $(git --version)"
echo "7. VS Code 版本: $(code --version | head -n1)"

# 检查关键工具
which vite > /dev/null && echo "8. Vite: 已安装" || echo "8. Vite: 未安装"
which vue > /dev/null && echo "9. Vue CLI: 已安装" || echo "9. Vue CLI: 未安装"

echo "=== 环境验证完成 ==="
```

### 7.2 运行验证脚本
```bash
# 给脚本执行权限
chmod +x env-check.sh

# 运行验证
./env-check.sh
```

## 8. 常见问题解决

### 8.1 Homebrew 安装问题
```bash
# 如果 Homebrew 安装失败，尝试：
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 配置环境变量
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"
```

### 8.2 权限问题
```bash
# 修复 npm 全局安装权限
mkdir ~/.npm-global
npm config set prefix '~/.npm-global'

# 添加到 PATH
echo 'export PATH=~/.npm-global/bin:$PATH' >> ~/.zprofile
source ~/.zprofile
```

### 8.3 端口占用
```bash
# 查看端口占用
lsof -i :3000

# 终止占用进程
kill -9 <PID>
```

### 8.4 Node.js 版本问题
```bash
# 使用 nvm 管理 Node 版本
nvm install 18
nvm use 18
nvm alias default 18
```

## 9. 下一步操作

### 9.1 环境配置完成检查
- [ ] Homebrew 安装完成
- [ ] Node.js 18+ 安装完成
- [ ] Git 安装完成
- [ ] VS Code 安装完成
- [ ] 必需扩展安装完成
- [ ] 项目依赖安装完成

### 9.2 开始项目开发
```bash
# 进入项目目录
cd projects-web

# 启动开发服务器
npm run dev

# 访问 http://localhost:3000 确认应用正常启动
```

---
**文档版本**: 1.0  
**更新人**: 前端开发工程师  
**更新日期**: 第3周  
**系统环境**: macOS  
**下次更新**: 项目结构搭建完成后
