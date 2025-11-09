<template>
  <div class="settings-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">PromptManager</h2>
      </div>
      
      <div class="sidebar-menu">
        <div class="menu-item">
          <el-icon><i-ep-HomeFilled /></el-icon>
          <span @click="$router.push('/')">首页</span>
        </div>
        <div class="menu-item">
          <el-icon><i-ep-Document /></el-icon>
          <span @click="$router.push('/prompts')">我的 Prompt</span>
        </div>
        <div class="menu-item">
          <el-icon><i-ep-Star /></el-icon>
          <span>收藏</span>
        </div>
        <div class="menu-item">
          <el-icon><i-ep-Folder /></el-icon>
          <span>文件夹</span>
        </div>
        <div class="menu-item">
          <el-icon><i-ep-PriceTag /></el-icon>
          <span>标签</span>
        </div>
        <div class="menu-item active">
          <el-icon><i-ep-Setting /></el-icon>
          <span>设置</span>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="header">
        <div class="header-left">
          <h1 class="page-title">设置</h1>
        </div>
        <div class="header-actions">
          <el-avatar size="small" :src="userStore.getUserInfo?.avatarUrl" />
        </div>
      </div>

      <div class="content">
        <div class="settings-tabs">
          <el-tabs v-model="activeTab" type="card">
            <el-tab-pane label="账户设置" name="account">
              <div class="tab-content">
                <el-card class="setting-card">
                  <template #header>
                    <div class="card-header">
                      <span>个人信息</span>
                    </div>
                  </template>
                  <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
                    <el-form-item label="用户名" prop="username">
                      <el-input v-model="userForm.username" placeholder="请输入用户名" />
                    </el-form-item>
                    <el-form-item label="昵称" prop="displayName">
                      <el-input v-model="userForm.displayName" placeholder="请输入昵称" />
                    </el-form-item>
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                    </el-form-item>
                    <el-form-item label="头像">
                      <div class="avatar-upload">
                        <el-avatar :size="80" :src="userForm.avatarUrl" />
                        <el-upload
                          class="avatar-uploader"
                          action="#"
                          :show-file-list="false"
                          :before-upload="beforeAvatarUpload"
                        >
                          <el-button type="primary" size="small">更换头像</el-button>
                        </el-upload>
                      </div>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="handleUpdateProfile">保存更改</el-button>
                    </el-form-item>
                  </el-form>
                </el-card>

                <el-card class="setting-card">
                  <template #header>
                    <div class="card-header">
                      <span>密码修改</span>
                    </div>
                  </template>
                  <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                    <el-form-item label="当前密码" prop="currentPassword">
                      <el-input v-model="passwordForm.currentPassword" type="password" placeholder="请输入当前密码" show-password />
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                      <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                    </el-form-item>
                    <el-form-item label="确认密码" prop="confirmPassword">
                      <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
                    </el-form-item>
                  </el-form>
                </el-card>
              </div>
            </el-tab-pane>

            <el-tab-pane label="偏好设置" name="preferences">
              <div class="tab-content">
                <el-card class="setting-card">
                  <template #header>
                    <div class="card-header">
                      <span>界面设置</span>
                    </div>
                  </template>
                  <div class="preference-item">
                    <div class="preference-label">主题模式</div>
                    <div class="preference-control">
                      <el-radio-group v-model="preferences.theme">
                        <el-radio label="light">浅色</el-radio>
                        <el-radio label="dark">深色</el-radio>
                        <el-radio label="auto">跟随系统</el-radio>
                      </el-radio-group>
                    </div>
                  </div>
                  <div class="preference-item">
                    <div class="preference-label">语言</div>
                    <div class="preference-control">
                      <el-select v-model="preferences.language" placeholder="选择语言">
                        <el-option label="简体中文" value="zh-CN" />
                        <el-option label="English" value="en-US" />
                      </el-select>
                    </div>
                  </div>
                  <div class="preference-item">
                    <div class="preference-label">默认页面</div>
                    <div class="preference-control">
                      <el-select v-model="preferences.defaultPage" placeholder="选择默认页面">
                        <el-option label="首页" value="home" />
                        <el-option label="我的 Prompt" value="prompts" />
                        <el-option label="收藏" value="favorites" />
                      </el-select>
                    </div>
                  </div>
                </el-card>

                <el-card class="setting-card">
                  <template #header>
                    <div class="card-header">
                      <span>通知设置</span>
                    </div>
                  </template>
                  <div class="preference-item">
                    <div class="preference-label">邮件通知</div>
                    <div class="preference-control">
                      <el-switch v-model="preferences.emailNotifications" />
                    </div>
                  </div>
                  <div class="preference-item">
                    <div class="preference-label">新功能提醒</div>
                    <div class="preference-control">
                      <el-switch v-model="preferences.featureNotifications" />
                    </div>
                  </div>
                  <div class="preference-item">
                    <div class="preference-label">安全提醒</div>
                    <div class="preference-control">
                      <el-switch v-model="preferences.securityNotifications" />
                    </div>
                  </div>
                </el-card>
              </div>
            </el-tab-pane>

            <el-tab-pane label="数据管理" name="data">
              <div class="tab-content">
                <el-card class="setting-card">
                  <template #header>
                    <div class="card-header">
                      <span>数据导出</span>
                    </div>
                  </template>
                  <div class="data-section">
                    <p class="data-description">导出您的 Prompt 数据，包括所有 Prompt 内容、标签和设置。</p>
                    <div class="export-options">
                      <el-radio-group v-model="exportFormat">
                        <el-radio label="json">JSON 格式</el-radio>
                        <el-radio label="csv">CSV 格式</el-radio>
                        <el-radio label="markdown">Markdown 格式</el-radio>
                      </el-radio-group>
                    </div>
                    <el-button type="primary" @click="handleExportData" :loading="exporting">
                      <el-icon><i-ep-Download /></el-icon>
                      导出数据
                    </el-button>
                  </div>
                </el-card>

                <el-card class="setting-card">
                  <template #header>
                    <div class="card-header">
                      <span>数据导入</span>
                    </div>
                  </template>
                  <div class="data-section">
                    <p class="data-description">从文件导入 Prompt 数据，支持 JSON 格式。</p>
                    <el-upload
                      class="import-upload"
                      action="#"
                      :show-file-list="false"
                      :before-upload="beforeImportUpload"
                      accept=".json"
                    >
                      <el-button type="primary">
                        <el-icon><i-ep-Upload /></el-icon>
                        选择文件
                      </el-button>
                    </el-upload>
                  </div>
                </el-card>

                <el-card class="setting-card danger-zone">
                  <template #header>
                    <div class="card-header">
                      <span>危险操作</span>
                    </div>
                  </template>
                  <div class="danger-section">
                    <p class="danger-description">这些操作不可逆，请谨慎操作。</p>
                    <div class="danger-actions">
                      <el-button type="danger" @click="handleClearData">
                        <el-icon><i-ep-Delete /></el-icon>
                        清空所有数据
                      </el-button>
                      <el-button type="danger" @click="handleDeleteAccount">
                        <el-icon><i-ep-User /></el-icon>
                        删除账户
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const activeTab = ref('account')
const exporting = ref(false)
const exportFormat = ref('json')
const userFormRef = ref()
const passwordFormRef = ref()

const userForm = reactive({
  username: '',
  displayName: '',
  email: '',
  avatarUrl: ''
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const preferences = reactive({
  theme: 'light',
  language: 'zh-CN',
  defaultPage: 'home',
  emailNotifications: true,
  featureNotifications: true,
  securityNotifications: true
})

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  
  // 模拟上传成功
  const reader = new FileReader()
  reader.onload = (e) => {
    userForm.avatarUrl = e.target.result
    ElMessage.success('头像上传成功')
  }
  reader.readAsDataURL(file)
  
  return false // 阻止自动上传
}

const beforeImportUpload = (file) => {
  const isJSON = file.type === 'application/json'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJSON) {
    ElMessage.error('只能上传 JSON 格式的文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过 5MB!')
    return false
  }
  
  // 模拟导入成功
  ElMessage.success('数据导入成功')
  return false
}

const handleUpdateProfile = async () => {
  if (!userFormRef.value) return

  try {
    await userFormRef.value.validate()
    // 模拟更新成功
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('更新失败:', error)
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    // 模拟密码修改成功
    ElMessage.success('密码修改成功')
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('密码修改失败:', error)
  }
}

const handleExportData = async () => {
  exporting.value = true
  try {
    // 模拟导出过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success(`数据已导出为 ${exportFormat.value.toUpperCase()} 格式`)
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const handleClearData = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将清空所有 Prompt 数据，且不可恢复。确定要继续吗？',
      '清空数据确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('数据已清空')
  } catch {
    // 用户取消操作
  }
}

const handleDeleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将永久删除您的账户和所有数据，且不可恢复。确定要继续吗？',
      '删除账户确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    ElMessage.success('账户已删除')
  } catch {
    // 用户取消操作
  }
}

onMounted(() => {
  // 加载用户数据
  const userInfo = userStore.getUserInfo
  if (userInfo) {
    Object.assign(userForm, {
      username: userInfo.username || '',
      displayName: userInfo.displayName || '',
      email: userInfo.email || '',
      avatarUrl: userInfo.avatarUrl || ''
    })
  }
})
</script>

<style scoped>
.settings-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f9fafb;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #e5e7eb;
  padding: 20px 0;
}

.sidebar-header {
  padding: 0 20px 20px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.sidebar-menu {
  padding: 0 12px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.menu-item:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.menu-item.active {
  background-color: #eff6ff;
  color: #2563eb;
  font-weight: 500;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  box-sizing: border-box;
}

.settings-tabs {
  max-width: 800px;
  margin: 0 auto;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-card {
  margin-bottom: 0;
}

.card-header {
  font-weight: 600;
  color: #1f2937;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.preference-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.preference-item:last-child {
  border-bottom: none;
}

.preference-label {
  font-weight: 500;
  color: #374151;
}

.preference-control {
  min-width: 200px;
}

.data-section {
  padding: 16px 0;
}

.data-description {
  color: #6b7280;
  margin-bottom: 16px;
}

.export-options {
  margin-bottom: 16px;
}

.import-upload {
  margin-top: 16px;
}

.danger-zone {
  border-color: #ef4444;
}

.danger-zone .card-header {
  color: #ef4444;
}

.danger-section {
  padding: 16px 0;
}

.danger-description {
  color: #6b7280;
  margin-bottom: 16px;
}

.danger-actions {
  display: flex;
  gap: 12px;
}
</style>
