import { createRouter, createWebHistory } from 'vue-router'

// 页面组件导入
const Login = () => import('../views/auth/Login.vue')
const Register = () => import('../views/auth/Register.vue')
const Home = () => import('../views/prompts/Home.vue')
const PromptList = () => import('../views/prompts/PromptList.vue')
const PromptDetail = () => import('../views/prompts/PromptDetail.vue')
const PromptEdit = () => import('../views/prompts/PromptEdit.vue')
const PromptCreate = () => import('../views/prompts/PromptCreate.vue')
const TagManagement = () => import('../views/prompts/TagManagement.vue')
const Settings = () => import('../views/settings/Settings.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/prompts',
    name: 'PromptList',
    component: PromptList,
    meta: { requiresAuth: true }
  },
  {
    path: '/prompts/:id',
    name: 'PromptDetail',
    component: PromptDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/prompts/:id/edit',
    name: 'PromptEdit',
    component: PromptEdit,
    meta: { requiresAuth: true }
  },
  {
    path: '/prompts/new',
    name: 'PromptCreate',
    component: PromptCreate,
    meta: { requiresAuth: true }
  },
  {
    path: '/tags',
    name: 'TagManagement',
    component: TagManagement,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 认证检查
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('accessToken')
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
