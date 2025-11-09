<template>
  <div class="prompt-list-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">PromptManager</h2>
      </div>
      
      <div class="sidebar-menu">
        <div class="menu-item">
          <el-icon><i-ep-HomeFilled /></el-icon>
          <span @click="$router.push('/')">首页</span>
        </div>
        <div class="menu-item active">
          <el-icon><i-ep-Document /></el-icon>
          <span>我的 Prompt</span>
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
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索 Prompt..."
            size="large"
            prefix-icon="Search"
            @input="handleSearch"
          />
        </div>
        <div class="header-actions">
          <el-button type="primary" size="large" @click="$router.push('/prompts/new')">
            <el-icon><i-ep-Plus /></el-icon>
            新建 Prompt
          </el-button>
          <el-avatar size="small" :src="userStore.getUserInfo?.avatarUrl" />
        </div>
      </div>

      <div class="content">
        <div class="filters-section">
          <div class="filter-tabs">
            <el-radio-group v-model="activeTab" @change="handleTabChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="favorites">收藏</el-radio-button>
              <el-radio-button label="recent">最近使用</el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="filter-tags">
            <el-select
              v-model="selectedTags"
              multiple
              placeholder="选择标签"
              size="large"
              @change="handleTagFilter"
            >
              <el-option
                v-for="tag in availableTags"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </div>
        </div>

        <div class="prompts-section">
          <div class="section-header">
            <h3>我的 Prompt ({{ filteredPrompts.length }})</h3>
            <div class="sort-options">
              <el-select v-model="sortBy" placeholder="排序方式" size="small">
                <el-option label="最近使用" value="lastUsed" />
                <el-option label="创建时间" value="createdAt" />
                <el-option label="使用次数" value="usageCount" />
                <el-option label="标题" value="title" />
              </el-select>
            </div>
          </div>

          <div class="prompts-grid">
            <div 
              class="prompt-card" 
              v-for="prompt in filteredPrompts" 
              :key="prompt.id"
              @click="$router.push(`/prompts/${prompt.id}`)"
            >
              <div class="prompt-header">
                <h4 class="prompt-title">{{ prompt.title }}</h4>
                <el-button text class="favorite-btn" @click.stop="toggleFavorite(prompt.id)">
                  <el-icon :color="prompt.isFavorite ? '#fbbf24' : '#9ca3af'">
                    <i-ep-StarFilled />
                  </el-icon>
                </el-button>
              </div>
              <p class="prompt-description">{{ prompt.description }}</p>
              <div class="prompt-tags">
                <el-tag
                  v-for="tag in prompt.tags"
                  :key="tag"
                  size="small"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <div class="prompt-footer">
                <span class="usage-count">使用 {{ prompt.usageCount }} 次</span>
                <span class="last-used">{{ prompt.lastUsedAt }}</span>
              </div>
            </div>
          </div>

          <div class="empty-state" v-if="filteredPrompts.length === 0">
            <el-empty description="暂无 Prompt" :image-size="200">
              <el-button type="primary" @click="$router.push('/prompts/new')">
                创建第一个 Prompt
              </el-button>
            </el-empty>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { usePromptStore } from '../../stores/prompt'

const router = useRouter()
const userStore = useUserStore()
const promptStore = usePromptStore()

const searchQuery = ref('')
const activeTab = ref('all')
const selectedTags = ref([])
const sortBy = ref('lastUsed')

// 模拟数据
const availableTags = ref(['编程', '写作', '数据分析', '产品', '设计', '营销', '客服', '教育'])

const mockPrompts = ref([
  {
    id: '1',
    title: '代码审查助手',
    description: '帮助审查代码质量和最佳实践，提供详细的改进建议',
    tags: ['编程', '代码审查', '最佳实践'],
    isFavorite: true,
    usageCount: 23,
    lastUsedAt: '2小时前',
    createdAt: '2024-01-01',
    category: '开发'
  },
  {
    id: '2',
    title: '文章写作助手',
    description: '协助撰写高质量的技术文章，包括结构规划和内容优化',
    tags: ['写作', '内容创作', '技术'],
    isFavorite: false,
    usageCount: 15,
    lastUsedAt: '1天前',
    createdAt: '2024-01-02',
    category: '写作'
  },
  {
    id: '3',
    title: '数据分析报告',
    description: '生成数据分析报告和洞察，帮助理解数据趋势',
    tags: ['数据分析', '报告', '洞察'],
    isFavorite: true,
    usageCount: 8,
    lastUsedAt: '3天前',
    createdAt: '2024-01-03',
    category: '分析'
  },
  {
    id: '4',
    title: '产品需求文档',
    description: '编写清晰的产品需求文档，确保团队理解一致',
    tags: ['产品', '文档', '需求'],
    isFavorite: false,
    usageCount: 12,
    lastUsedAt: '1周前',
    createdAt: '2024-01-04',
    category: '产品'
  },
  {
    id: '5',
    title: 'UI设计评审',
    description: '提供UI设计评审和改进建议，确保用户体验',
    tags: ['设计', 'UI', '用户体验'],
    isFavorite: false,
    usageCount: 5,
    lastUsedAt: '2周前',
    createdAt: '2024-01-05',
    category: '设计'
  }
])

const filteredPrompts = computed(() => {
  let filtered = [...mockPrompts.value]

  // 搜索过滤
  if (searchQuery.value) {
    filtered = filtered.filter(prompt => 
      prompt.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      prompt.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // 标签过滤
  if (selectedTags.value.length > 0) {
    filtered = filtered.filter(prompt =>
      selectedTags.value.some(tag => prompt.tags.includes(tag))
    )
  }

  // 标签过滤
  if (activeTab.value === 'favorites') {
    filtered = filtered.filter(prompt => prompt.isFavorite)
  } else if (activeTab.value === 'recent') {
    filtered = filtered.filter(prompt => 
      prompt.lastUsedAt.includes('小时') || prompt.lastUsedAt.includes('天')
    )
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'lastUsed':
        return new Date(b.lastUsedAt) - new Date(a.lastUsedAt)
      case 'createdAt':
        return new Date(b.createdAt) - new Date(a.createdAt)
      case 'usageCount':
        return b.usageCount - a.usageCount
      case 'title':
        return a.title.localeCompare(b.title)
      default:
        return 0
    }
  })

  return filtered
})

const handleSearch = () => {
  // 搜索逻辑
}

const handleTabChange = () => {
  // 标签切换逻辑
}

const handleTagFilter = () => {
  // 标签过滤逻辑
}

const toggleFavorite = (promptId) => {
  const prompt = mockPrompts.value.find(p => p.id === promptId)
  if (prompt) {
    prompt.isFavorite = !prompt.isFavorite
  }
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.prompt-list-container {
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

.search-bar {
  width: 400px;
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
}

.filters-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tags {
  width: 200px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.sort-options {
  width: 120px;
}

.prompts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.prompt-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
  cursor: pointer;
}

.prompt-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.prompt-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.prompt-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.favorite-btn {
  padding: 4px;
  margin-left: 8px;
}

.prompt-description {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.prompt-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.prompt-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #9ca3af;
}

.usage-count {
  font-weight: 500;
}

.last-used {
  font-size: 11px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
