<template>
  <div class="tag-management-container">
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
        <div class="menu-item active">
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
            placeholder="搜索标签..."
            size="large"
            prefix-icon="Search"
            @input="handleSearch"
          />
        </div>
        <div class="header-actions">
          <el-button type="primary" size="large" @click="showCreateDialog = true">
            <el-icon><i-ep-Plus /></el-icon>
            新建标签
          </el-button>
          <el-avatar size="small" :src="userStore.getUserInfo?.avatarUrl" />
        </div>
      </div>

      <div class="content">
        <div class="tags-overview">
          <div class="overview-card">
            <div class="overview-icon">
              <el-icon><i-ep-PriceTag /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-number">{{ tags.length }}</div>
              <div class="overview-label">总标签数</div>
            </div>
          </div>
          <div class="overview-card">
            <div class="overview-icon">
              <el-icon><i-ep-Document /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-number">{{ totalPrompts }}</div>
              <div class="overview-label">关联 Prompt</div>
            </div>
          </div>
          <div class="overview-card">
            <div class="overview-icon">
              <el-icon><i-ep-View /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-number">{{ totalUsage }}</div>
              <div class="overview-label">总使用次数</div>
            </div>
          </div>
        </div>

        <div class="tags-section">
          <div class="section-header">
            <h3>标签管理</h3>
            <div class="sort-options">
              <el-select v-model="sortBy" placeholder="排序方式" size="small">
                <el-option label="使用频率" value="usage" />
                <el-option label="名称" value="name" />
                <el-option label="创建时间" value="createdAt" />
              </el-select>
            </div>
          </div>

          <div class="tags-grid">
            <div class="tag-card" v-for="tag in filteredTags" :key="tag.id">
              <div class="tag-header">
                <div class="tag-info">
                  <h4 class="tag-name">{{ tag.name }}</h4>
                  <span class="tag-count">{{ tag.promptCount }} 个 Prompt</span>
                </div>
                <div class="tag-actions">
                  <el-button text @click="handleEditTag(tag)">
                    <el-icon><i-ep-Edit /></el-icon>
                  </el-button>
                  <el-button text @click="handleDeleteTag(tag)" v-if="tag.promptCount === 0">
                    <el-icon><i-ep-Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="tag-stats">
                <div class="stat-item">
                  <span class="stat-label">使用次数</span>
                  <span class="stat-value">{{ tag.usageCount }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">创建时间</span>
                  <span class="stat-value">{{ tag.createdAt }}</span>
                </div>
              </div>
              <div class="tag-preview">
                <div class="preview-title">关联 Prompt 预览</div>
                <div class="preview-list">
                  <span 
                    class="preview-item" 
                    v-for="prompt in tag.recentPrompts" 
                    :key="prompt.id"
                    @click="$router.push(`/prompts/${prompt.id}`)"
                  >
                    {{ prompt.title }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="empty-state" v-if="filteredTags.length === 0">
            <el-empty description="暂无标签" :image-size="200">
              <el-button type="primary" @click="showCreateDialog = true">
                创建第一个标签
              </el-button>
            </el-empty>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑标签对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingTag ? '编辑标签' : '新建标签'"
      width="500px"
    >
      <el-form :model="tagForm" :rules="tagRules" ref="tagFormRef" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input
            v-model="tagForm.name"
            placeholder="请输入标签名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="tagForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入标签描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="tagForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTag" :loading="loading">
          {{ editingTag ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const searchQuery = ref('')
const sortBy = ref('usage')
const showCreateDialog = ref(false)
const editingTag = ref(null)
const loading = ref(false)
const tagFormRef = ref()

const tagForm = reactive({
  name: '',
  description: '',
  color: '#409EFF'
})

const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 20, message: '标签名称长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

// 模拟标签数据
const tags = ref([
  {
    id: '1',
    name: '编程',
    description: '与编程相关的 Prompt',
    color: '#3B82F6',
    promptCount: 15,
    usageCount: 156,
    createdAt: '2024-01-01',
    recentPrompts: [
      { id: '1', title: '代码审查助手' },
      { id: '2', title: '算法优化' },
      { id: '3', title: 'API 设计' }
    ]
  },
  {
    id: '2',
    name: '写作',
    description: '协助写作的 Prompt',
    color: '#10B981',
    promptCount: 8,
    usageCount: 89,
    createdAt: '2024-01-02',
    recentPrompts: [
      { id: '4', title: '文章写作助手' },
      { id: '5', title: '邮件模板' }
    ]
  },
  {
    id: '3',
    name: '数据分析',
    description: '数据分析和报告生成',
    color: '#F59E0B',
    promptCount: 6,
    usageCount: 45,
    createdAt: '2024-01-03',
    recentPrompts: [
      { id: '6', title: '数据分析报告' },
      { id: '7', title: '图表生成' }
    ]
  },
  {
    id: '4',
    name: '产品',
    description: '产品管理和需求分析',
    color: '#EF4444',
    promptCount: 5,
    usageCount: 32,
    createdAt: '2024-01-04',
    recentPrompts: [
      { id: '8', title: '产品需求文档' },
      { id: '9', title: '用户故事' }
    ]
  },
  {
    id: '5',
    name: '设计',
    description: 'UI/UX 设计相关',
    color: '#8B5CF6',
    promptCount: 4,
    usageCount: 28,
    createdAt: '2024-01-05',
    recentPrompts: [
      { id: '10', title: 'UI设计评审' },
      { id: '11', title: '设计系统' }
    ]
  }
])

const filteredTags = computed(() => {
  let filtered = [...tags.value]

  // 搜索过滤
  if (searchQuery.value) {
    filtered = filtered.filter(tag => 
      tag.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      tag.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'usage':
        return b.usageCount - a.usageCount
      case 'name':
        return a.name.localeCompare(b.name)
      case 'createdAt':
        return new Date(b.createdAt) - new Date(a.createdAt)
      default:
        return 0
    }
  })

  return filtered
})

const totalPrompts = computed(() => {
  return tags.value.reduce((sum, tag) => sum + tag.promptCount, 0)
})

const totalUsage = computed(() => {
  return tags.value.reduce((sum, tag) => sum + tag.usageCount, 0)
})

const handleSearch = () => {
  // 搜索逻辑
}

const handleEditTag = (tag) => {
  editingTag.value = tag
  Object.assign(tagForm, {
    name: tag.name,
    description: tag.description,
    color: tag.color
  })
  showCreateDialog.value = true
}

const handleDeleteTag = async (tag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签 "${tag.name}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    tags.value = tags.value.filter(t => t.id !== tag.id)
    ElMessage.success('标签删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleSaveTag = async () => {
  if (!tagFormRef.value) return

  try {
    await tagFormRef.value.validate()
    loading.value = true

    if (editingTag.value) {
      // 更新标签
      const index = tags.value.findIndex(t => t.id === editingTag.value.id)
      if (index !== -1) {
        Object.assign(tags.value[index], {
          name: tagForm.name,
          description: tagForm.description,
          color: tagForm.color
        })
      }
      ElMessage.success('标签更新成功')
    } else {
      // 创建新标签
      const newTag = {
        id: 'tag_' + Date.now(),
        name: tagForm.name,
        description: tagForm.description,
        color: tagForm.color,
        promptCount: 0,
        usageCount: 0,
        createdAt: new Date().toISOString().split('T')[0],
        recentPrompts: []
      }
      tags.value.push(newTag)
      ElMessage.success('标签创建成功')
    }

    showCreateDialog.value = false
    resetTagForm()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

const resetTagForm = () => {
  tagForm.name = ''
  tagForm.description = ''
  tagForm.color = '#409EFF'
  editingTag.value = null
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.tag-management-container {
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

.tags-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2B7FFF 0%, #9810FA 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.overview-info {
  flex: 1;
}

.overview-number {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1;
}

.overview-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
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

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.tag-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.tag-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.tag-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.tag-info {
  flex: 1;
}

.tag-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.tag-count {
  font-size: 14px;
  color: #6b7280;
}

.tag-actions {
  display: flex;
  gap: 4px;
}

.tag-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.tag-preview {
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-item {
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.preview-item:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
