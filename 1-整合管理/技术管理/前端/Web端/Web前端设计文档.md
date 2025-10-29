# Web前端设计文档

## 1. 项目概述

### 1.1 项目目标
构建一个现代化的Prompt管理Web应用，提供良好的用户体验和响应式设计。

### 1.2 核心功能
- **用户认证**：注册、登录、三方登录、游客体验
- **Prompt管理**：增删改查、搜索、分类、收藏
- **标签管理**：标签创建、使用统计
- **数据同步**：多端数据同步
- **分享功能**：Prompt分享和复制

### 1.3 技术栈
- **框架**：Vue 3 + TypeScript
- **状态管理**：Pinia
- **UI组件**：Element Plus
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **构建工具**：Vite
- **包管理**：npm/yarn

## 2. 项目结构

### 2.1 目录结构
```
src/
├── components/           # 公共组件
│   ├── common/          # 通用组件
│   ├── layout/          # 布局组件
│   └── prompts/         # Prompt相关组件
├── views/               # 页面组件
│   ├── auth/            # 认证页面
│   ├── prompts/         # Prompt管理页面
│   └── settings/        # 设置页面
├── stores/              # 状态管理
├── services/            # API服务
├── utils/               # 工具函数
├── types/               # TypeScript类型定义
├── assets/              # 静态资源
└── styles/              # 样式文件
```

### 2.2 组件设计

#### 2.2.1 布局组件
```typescript
// Layout.vue
<template>
  <div class="app-layout">
    <AppHeader />
    <AppSidebar v-if="showSidebar" />
    <main class="main-content">
      <router-view />
    </main>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const showSidebar = computed(() => !route.meta.hideSidebar)
</script>
```

#### 2.2.2 Prompt列表组件
```typescript
// PromptList.vue
<template>
  <div class="prompt-list">
    <div class="list-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索Prompt..."
        @input="handleSearch"
      />
      <el-button type="primary" @click="handleCreate">
        新建Prompt
      </el-button>
    </div>
    
    <div class="list-content">
      <PromptCard
        v-for="prompt in prompts"
        :key="prompt.id"
        :prompt="prompt"
        @edit="handleEdit"
        @delete="handleDelete"
        @favorite="handleFavorite"
      />
    </div>
    
    <el-pagination
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { usePromptStore } from '@/stores/prompt'

const promptStore = usePromptStore()
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const prompts = computed(() => promptStore.prompts)

onMounted(() => {
  loadPrompts()
})

const loadPrompts = async () => {
  await promptStore.fetchPrompts({
    page: currentPage.value,
    limit: pageSize.value,
    search: searchKeyword.value
  })
  total.value = promptStore.total
}

const handleSearch = () => {
  currentPage.value = 1
  loadPrompts()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadPrompts()
}
</script>
```

## 3. 状态管理

### 3.1 用户状态管理
```typescript
// stores/user.ts
import { defineStore } from 'pinia'

interface User {
  id: string
  email: string
  username: string
  avatar: string
  userType: 'REGISTERED' | 'GUEST' | 'OAUTH'
}

interface UserState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: null,
    token: null,
    isAuthenticated: false
  }),

  getters: {
    isGuest: (state) => state.user?.userType === 'GUEST'
  },

  actions: {
    async login(credentials: { email: string; password: string }) {
      const response = await authService.login(credentials)
      this.token = response.token
      this.user = response.user
      this.isAuthenticated = true
      
      // 保存token到localStorage
      localStorage.setItem('token', response.token)
    },

    async guestLogin(deviceId: string) {
      const response = await authService.guestLogin(deviceId)
      this.token = response.token
      this.user = response.guest
      this.isAuthenticated = true
      
      localStorage.setItem('token', response.token)
    },

    async logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    },

    async refreshToken() {
      const response = await authService.refreshToken()
      this.token = response.token
      localStorage.setItem('token', response.token)
    }
  }
})
```

### 3.2 Prompt状态管理
```typescript
// stores/prompt.ts
import { defineStore } from 'pinia'

interface Prompt {
  id: string
  title: string
  content: string
  description?: string
  tags: string[]
  category: string
  variables: string[]
  useCount: number
  isFavorite: boolean
  createdAt: string
  updatedAt: string
}

interface PromptState {
  prompts: Prompt[]
  currentPrompt: Prompt | null
  total: number
  loading: boolean
}

export const usePromptStore = defineStore('prompt', {
  state: (): PromptState => ({
    prompts: [],
    currentPrompt: null,
    total: 0,
    loading: false
  }),

  actions: {
    async fetchPrompts(params: {
      page: number
      limit: number
      search?: string
      category?: string
      tags?: string[]
    }) {
      this.loading = true
      try {
        const response = await promptService.getPrompts(params)
        this.prompts = response.prompts
        this.total = response.pagination.total
      } finally {
        this.loading = false
      }
    },

    async createPrompt(promptData: Omit<Prompt, 'id' | 'createdAt' | 'updatedAt'>) {
      const response = await promptService.createPrompt(promptData)
      this.prompts.unshift(response.prompt)
      return response.prompt
    },

    async updatePrompt(id: string, promptData: Partial<Prompt>) {
      const response = await promptService.updatePrompt(id, promptData)
      const index = this.prompts.findIndex(p => p.id === id)
      if (index !== -1) {
        this.prompts[index] = response.prompt
      }
      return response.prompt
    },

    async deletePrompt(id: string) {
      await promptService.deletePrompt(id)
      this.prompts = this.prompts.filter(p => p.id !== id)
    }
  }
})
```

## 4. API服务

### 4.1 HTTP客户端配置
```typescript
// services/api.ts
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token过期，跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
```

### 4.2 认证服务
```typescript
// services/auth.ts
import api from './api'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  username?: string
}

export interface AuthResponse {
  success: boolean
  data: {
    user?: any
    guest?: any
    token: string
  }
  message: string
}

class AuthService {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/login', credentials)
    return response
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/register', userData)
    return response
  }

  async guestLogin(deviceId: string): Promise<AuthResponse> {
    const response = await api.post('/auth/guest', { deviceId })
    return response
  }

  async refreshToken(): Promise<AuthResponse> {
    const response = await api.post('/auth/refresh')
    return response
  }

  async guestUpgrade(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/guest/upgrade', userData)
    return response
  }
}

export const authService = new AuthService()
```

### 4.3 Prompt服务
```typescript
// services/prompt.ts
import api from './api'

export interface Prompt {
  id: string
  title: string
  content: string
  description?: string
  tags: string[]
  category: string
  variables: string[]
  useCount: number
  isFavorite: boolean
  createdAt: string
  updatedAt: string
}

export interface GetPromptsParams {
  page: number
  limit: number
  search?: string
  category?: string
  tags?: string[]
  sort?: string
}

class PromptService {
  async getPrompts(params: GetPromptsParams) {
    const response = await api.get('/prompts', { params })
    return response.data
  }

  async getPrompt(id: string) {
    const response = await api.get(`/prompts/${id}`)
    return response.data
  }

  async createPrompt(promptData: Omit<Prompt, 'id' | 'createdAt' | 'updatedAt'>) {
    const response = await api.post('/prompts', promptData)
    return response.data
  }

  async updatePrompt(id: string, promptData: Partial<Prompt>) {
    const response = await api.put(`/prompts/${id}`, promptData)
    return response.data
  }

  async deletePrompt(id: string) {
    const response = await api.delete(`/prompts/${id}`)
    return response.data
  }

  async favoritePrompt(id: string) {
    const response = await api.post(`/prompts/${id}/favorite`)
    return response.data
  }

  async usePrompt(id: string) {
    const response = await api.post(`/prompts/${id}/use`)
    return response.data
  }
}

export const promptService = new PromptService()
```

## 5. 路由配置

### 5.1 路由定义
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/prompts',
    name: 'Prompts',
    component: () => import('@/views/prompts/PromptList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/prompts/create',
    name: 'CreatePrompt',
    component: () => import('@/views/prompts/CreatePrompt.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/prompts/:id/edit',
    name: 'EditPrompt',
    component: () => import('@/views/prompts/EditPrompt.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/Settings.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.guestOnly && userStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
```

## 6. 样式设计

### 6.1 设计系统
```scss
// styles/variables.scss
$primary-color: #409EFF;
$success-color: #67C23A;
$warning-color: #E6A23C;
$danger-color: #F56C6C;
$info-color: #909399;

$text-primary: #303133;
$text-regular: #606266;
$text-secondary: #909399;
$text-placeholder: #C0C4CC;

$border-color: #DCDFE6;
$border-color-light: #E4E7ED;
$border-color-lighter: #EBEEF5;
$border-color-extra-light: #F2F6FC;

$background-color: #F5F7FA;
$background-color-light: #FAFAFA;
```

### 6.2 响应式设计
```scss
// styles/responsive.scss
$breakpoints: (
  'xs': 0,
  'sm': 576px,
  'md': 768px,
  'lg': 992px,
  'xl': 1200px
);

@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media (min-width: map-get($breakpoints, $breakpoint)) {
      @content;
    }
  } @else {
    @warn "Unknown breakpoint: #{$breakpoint}";
  }
}

.prompt-list {
  padding: 20px;
  
  @include respond-to('md') {
    padding: 30px;
  }
  
  @include respond-to('lg') {
    padding: 40px;
  }
}
```

## 7. 构建配置

### 7.1 Vite配置
```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: true
  }
})
```

### 7.2 环境变量配置
```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=PromptManager Dev

# .env.production
VITE_API_BASE_URL=https://api.promptmanager.com
VITE_APP_TITLE=PromptManager
```

## 8. 部署配置

### 8.1 Docker配置
```dockerfile
# Dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 8.2 Nginx配置
```nginx
# nginx.conf
server {
    listen 80;
    server_name localhost;
    
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://api-gateway:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---
**文档版本**：1.0  
**编制人**：项目经理  
**编制日期**：第1周  
**下次评审**：Web前端实现阶段开始前
