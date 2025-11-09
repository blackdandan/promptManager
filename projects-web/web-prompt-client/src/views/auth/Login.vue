<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="logo-section">
          <div class="logo-circle">
            <span class="logo-text">P</span>
          </div>
          <h1 class="app-title">Prompt Manager</h1>
          <p class="app-subtitle">管理你的AI提示词，随时随地使用</p>
        </div>
      </div>

      <div class="login-right">
        <div class="login-card">
          <div class="login-options">
            <el-button class="oauth-button" size="large">
              <el-icon><i-ep-Chrome /></el-icon>
              使用Google账号登录
            </el-button>
            <el-button class="oauth-button" size="large">
              <el-icon><i-ep-ChatDotRound /></el-icon>
              使用微信账号登录
            </el-button>
            <el-button class="guest-button" size="large" @click="handleGuestLogin">
              <el-icon><i-ep-User /></el-icon>
              游客模式体验
            </el-button>
          </div>

          <div class="guest-notice">
            <p>游客模式下数据仅保存在本地</p>
          </div>

          <div class="login-divider">
            <span class="divider-line"></span>
            <span class="divider-text">或</span>
            <span class="divider-line"></span>
          </div>

          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                size="large"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <span>支持多端同步 · 安全可靠</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin'
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 模拟登录成功
    const mockUserData = {
      user: {
        userId: 'user_123',
        username: loginForm.username,
        email: `${loginForm.username}@example.com`,
        displayName: '管理员',
        userType: 'REGISTERED',
        roles: ['USER']
      },
      accessToken: 'mock_access_token',
      refreshToken: 'mock_refresh_token'
    }

    userStore.login(mockUserData)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const handleGuestLogin = () => {
  const guestUserData = {
    user: {
      userId: 'guest_123',
      username: 'guest',
      email: 'guest@example.com',
      displayName: '游客用户',
      userType: 'GUEST',
      roles: ['GUEST']
    },
    accessToken: 'guest_access_token',
    refreshToken: 'guest_refresh_token'
  }

  userStore.login(guestUserData)
  ElMessage.success('游客模式登录成功')
  router.push('/')
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #2B7FFF 0%, #9810FA 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.login-content {
  display: flex;
  width: 100%;
  height: 100%;
  background: white;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #2B7FFF 0%, #9810FA 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo-section {
  text-align: center;
}

.logo-circle {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30px;
  backdrop-filter: blur(10px);
}

.logo-text {
  font-size: 32px;
  font-weight: bold;
}

.app-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 16px 0;
  line-height: 1.2;
}

.app-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  line-height: 1.5;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.oauth-button {
  width: 100%;
  height: 48px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #374151;
  font-size: 14px;
}

.oauth-button:hover {
  border-color: #d1d5db;
  background: #f9fafb;
}

.guest-button {
  width: 100%;
  height: 48px;
  border: 1px solid #2B7FFF;
  background: transparent;
  color: #2B7FFF;
  font-size: 14px;
}

.guest-button:hover {
  background: #2B7FFF;
  color: white;
}

.guest-notice {
  text-align: center;
  margin-bottom: 20px;
}

.guest-notice p {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.login-divider {
  display: flex;
  align-items: center;
  margin: 30px 0;
  gap: 12px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}

.divider-text {
  color: #6b7280;
  font-size: 14px;
  white-space: nowrap;
}

.login-form {
  margin-bottom: 30px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  background: linear-gradient(135deg, #2B7FFF 0%, #9810FA 100%);
  border: none;
}

.login-button:hover {
  opacity: 0.9;
}

.login-footer {
  text-align: center;
  color: #6b7280;
  font-size: 12px;
  margin-top: 20px;
}
</style>
