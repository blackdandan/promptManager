<template>
  <div class="prompt-detail-container">
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
      </div>
    </div>

    <div class="main-content">
      <div class="header">
        <div class="header-left">
          <el-button text @click="$router.back()">
            <el-icon><i-ep-ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
        <div class="header-actions">
          <el-button @click="handleEdit">
            <el-icon><i-ep-Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="primary" @click="handleCopy">
            <el-icon><i-ep-CopyDocument /></el-icon>
            复制内容
          </el-button>
          <el-button :type="currentPrompt.isFavorite ? 'warning' : 'default'" @click="toggleFavorite">
            <el-icon><i-ep-StarFilled /></el-icon>
            {{ currentPrompt.isFavorite ? '取消收藏' : '收藏' }}
          </el-button>
          <el-avatar size="small" :src="userStore.getUserInfo?.avatarUrl" />
        </div>
      </div>

      <div class="content">
        <div class="prompt-detail">
          <div class="prompt-header">
            <h1 class="prompt-title">{{ currentPrompt.title }}</h1>
            <div class="prompt-meta">
              <span class="usage-count">使用 {{ currentPrompt.usageCount }} 次</span>
              <span class="last-used">最后使用: {{ currentPrompt.lastUsedAt }}</span>
              <span class="created-at">创建于: {{ currentPrompt.createdAt }}</span>
            </div>
          </div>

          <div class="prompt-description">
            <h3>描述</h3>
            <p>{{ currentPrompt.description }}</p>
          </div>

          <div class="prompt-tags">
            <h3>标签</h3>
            <div class="tags-list">
              <el-tag
                v-for="tag in currentPrompt.tags"
                :key="tag"
                size="medium"
                type="info"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div class="prompt-content">
            <h3>Prompt 内容</h3>
            <div class="content-editor">
              <el-input
                v-model="currentPrompt.content"
                type="textarea"
                :rows="12"
                placeholder="Prompt 内容"
                readonly
              />
            </div>
            <div class="content-actions">
              <el-button type="primary" @click="handleCopyContent">
                <el-icon><i-ep-CopyDocument /></el-icon>
                复制内容
              </el-button>
              <el-button @click="handleUse">
                <el-icon><i-ep-Promotion /></el-icon>
                使用 Prompt
              </el-button>
            </div>
          </div>

          <div class="prompt-variables" v-if="currentPrompt.variables && currentPrompt.variables.length > 0">
            <h3>变量</h3>
            <div class="variables-list">
              <div class="variable-item" v-for="variable in currentPrompt.variables" :key="variable.name">
                <div class="variable-info">
                  <span class="variable-name">{{ variable.name }}</span>
                  <span class="variable-type">{{ variable.type }}</span>
                </div>
                <el-input
                  v-model="variable.value"
                  :placeholder="variable.description"
                  size="small"
                />
              </div>
            </div>
          </div>

          <div class="prompt-stats">
            <h3>使用统计</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ currentPrompt.usageCount }}</div>
                <div class="stat-label">总使用次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ currentPrompt.successRate }}%</div>
                <div class="stat-label">成功率</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ currentPrompt.avgResponseTime }}s</div>
                <div class="stat-label">平均响应时间</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ currentPrompt.favoriteCount }}</div>
                <div class="stat-label">收藏次数</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentPrompt = ref({
  id: '1',
  title: '代码审查助手',
  description: '帮助审查代码质量和最佳实践，提供详细的改进建议。这个 Prompt 可以分析代码结构、识别潜在问题、提出优化建议。',
  content: `你是一个专业的代码审查助手。请帮我审查以下代码：

代码语言: {{language}}
代码内容:
\`\`\`{{language}}
{{code}}
\`\`\`

请从以下几个方面进行审查：
1. 代码质量和可读性
2. 性能优化建议
3. 安全性问题
4. 最佳实践遵循情况
5. 潜在的 bug 和错误

请提供详细的审查报告和改进建议。`,
  tags: ['编程', '代码审查', '最佳实践', '开发'],
  isFavorite: true,
  usageCount: 23,
  lastUsedAt: '2024-01-09 14:30',
  createdAt: '2024-01-01',
  category: '开发',
  successRate: 95,
  avgResponseTime: 2.3,
  favoriteCount: 8,
  variables: [
    {
      name: 'language',
      type: 'string',
      description: '编程语言',
      value: 'JavaScript'
    },
    {
      name: 'code',
      type: 'text',
      description: '要审查的代码',
      value: ''
    }
  ]
})

const handleEdit = () => {
  router.push(`/prompts/${currentPrompt.value.id}/edit`)
}

const handleCopy = () => {
  // 复制整个 Prompt 信息
  ElMessage.success('Prompt 信息已复制')
}

const handleCopyContent = () => {
  // 复制 Prompt 内容
  navigator.clipboard.writeText(currentPrompt.value.content)
  ElMessage.success('Prompt 内容已复制到剪贴板')
}

const handleUse = () => {
  // 使用 Prompt 的逻辑
  currentPrompt.value.usageCount++
  ElMessage.success('Prompt 已使用，使用次数 +1')
}

const toggleFavorite = () => {
  currentPrompt.value.isFavorite = !currentPrompt.value.isFavorite
  if (currentPrompt.value.isFavorite) {
    currentPrompt.value.favoriteCount++
    ElMessage.success('已添加到收藏')
  } else {
    currentPrompt.value.favoriteCount--
    ElMessage.success('已取消收藏')
  }
}

onMounted(() => {
  // 根据路由参数加载 Prompt 数据
  const promptId = route.params.id
  console.log('加载 Prompt ID:', promptId)
})
</script>

<style scoped>
.prompt-detail-container {
  display: flex;
  min-height: 100vh;
  background-color: #f9fafb;
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

.header-left {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.prompt-detail {
  max-width: 800px;
  margin: 0 auto;
}

.prompt-header {
  margin-bottom: 32px;
}

.prompt-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.prompt-meta {
  display: flex;
  gap: 24px;
  color: #6b7280;
  font-size: 14px;
}

.prompt-description,
.prompt-tags,
.prompt-content,
.prompt-variables,
.prompt-stats {
  margin-bottom: 32px;
}

.prompt-description h3,
.prompt-tags h3,
.prompt-content h3,
.prompt-variables h3,
.prompt-stats h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.prompt-description p {
  color: #4b5563;
  line-height: 1.6;
  font-size: 16px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.content-editor {
  margin-bottom: 16px;
}

.content-actions {
  display: flex;
  gap: 12px;
}

.variables-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.variable-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.variable-info {
  display: flex;
  flex-direction: column;
  min-width: 120px;
}

.variable-name {
  font-weight: 600;
  color: #1f2937;
}

.variable-type {
  font-size: 12px;
  color: #6b7280;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}
</style>
