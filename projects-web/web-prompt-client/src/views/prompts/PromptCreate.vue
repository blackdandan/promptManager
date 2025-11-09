<template>
  <div class="prompt-create-container">
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
          <h1 class="page-title">新建 Prompt</h1>
        </div>
        <div class="header-actions">
          <el-button @click="handleSaveDraft">
            <el-icon><i-ep-Document /></el-icon>
            保存草稿
          </el-button>
          <el-button type="primary" @click="handleCreate" :loading="loading">
            <el-icon><i-ep-Check /></el-icon>
            创建 Prompt
          </el-button>
          <el-avatar size="small" :src="userStore.getUserInfo?.avatarUrl" />
        </div>
      </div>

      <div class="content">
        <div class="create-form">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入 Prompt 标题"
                size="large"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="请输入 Prompt 描述"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 200px">
                <el-option label="开发" value="development" />
                <el-option label="写作" value="writing" />
                <el-option label="设计" value="design" />
                <el-option label="产品" value="product" />
                <el-option label="营销" value="marketing" />
                <el-option label="客服" value="customer-service" />
                <el-option label="教育" value="education" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>

            <el-form-item label="文件夹" prop="folderId">
              <el-select v-model="form.folderId" placeholder="选择文件夹" style="width: 200px">
                <el-option label="默认文件夹" value="default" />
                <el-option label="工作相关" value="work" />
                <el-option label="个人项目" value="personal" />
                <el-option label="学习资料" value="learning" />
              </el-select>
            </el-form-item>

            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="form.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入标签"
                style="width: 100%"
              >
                <el-option
                  v-for="tag in availableTags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="Prompt 内容" prop="content">
              <div class="content-editor">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="12"
                  placeholder="请输入 Prompt 内容"
                  resize="none"
                />
                <div class="editor-tips">
                  <p><strong>提示：</strong></p>
                  <ul>
                    <li>使用 {{variable}} 格式定义变量</li>
                    <li>提供清晰的指令和上下文</li>
                    <li>包含期望的输出格式</li>
                    <li>考虑边界情况和错误处理</li>
                  </ul>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="变量定义" v-if="hasVariables">
              <div class="variables-section">
                <div class="variable-item" v-for="variable in extractedVariables" :key="variable.name">
                  <div class="variable-header">
                    <span class="variable-name">{{ variable.name }}</span>
                    <el-button text size="small" @click="removeVariable(variable.name)">
                      <el-icon><i-ep-Close /></el-icon>
                    </el-button>
                  </div>
                  <div class="variable-form">
                    <el-form-item label="类型" style="margin-bottom: 12px">
                      <el-select v-model="variable.type" placeholder="选择类型" style="width: 120px">
                        <el-option label="文本" value="string" />
                        <el-option label="数字" value="number" />
                        <el-option label="布尔值" value="boolean" />
                        <el-option label="代码" value="code" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="描述">
                      <el-input
                        v-model="variable.description"
                        placeholder="变量描述"
                        size="small"
                      />
                    </el-form-item>
                    <el-form-item label="默认值">
                      <el-input
                        v-model="variable.defaultValue"
                        placeholder="默认值"
                        size="small"
                      />
                    </el-form-item>
                  </div>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="公开设置">
              <el-switch
                v-model="form.isPublic"
                active-text="公开"
                inactive-text="私有"
              />
              <div class="setting-tips">
                <p>公开的 Prompt 可以被其他用户查看和使用</p>
              </div>
            </el-form-item>

            <el-form-item label="收藏状态">
              <el-switch
                v-model="form.isFavorite"
                active-text="收藏"
                inactive-text="不收藏"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { usePromptStore } from '../../stores/prompt'

const router = useRouter()
const userStore = useUserStore()
const promptStore = usePromptStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  title: '',
  description: '',
  content: '',
  category: '',
  folderId: 'default',
  tags: [],
  isPublic: false,
  isFavorite: false
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 3, max: 100, message: '标题长度在 3 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入 Prompt 内容', trigger: 'blur' },
    { min: 10, message: 'Prompt 内容不能少于 10 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

const availableTags = ref(['编程', '写作', '数据分析', '产品', '设计', '营销', '客服', '教育'])

// 提取变量
const extractedVariables = ref([])

const hasVariables = computed(() => {
  return extractedVariables.value.length > 0
})

// 监听内容变化，提取变量
watch(() => form.content, (newContent) => {
  extractVariables(newContent)
})

const extractVariables = (content) => {
  const variableRegex = /\{\{([^{}]+)\}\}/g
  const matches = [...content.matchAll(variableRegex)]
  const variables = matches.map(match => match[1].trim())
  
  // 去重
  const uniqueVariables = [...new Set(variables)]
  
  // 更新变量列表
  extractedVariables.value = uniqueVariables.map(name => {
    const existing = extractedVariables.value.find(v => v.name === name)
    return existing || {
      name,
      type: 'string',
      description: '',
      defaultValue: ''
    }
  })
}

const removeVariable = (variableName) => {
  // 从内容中移除变量
  form.content = form.content.replace(new RegExp(`\\{\\{${variableName}\\}\\}`, 'g'), '')
  // 从变量列表中移除
  extractedVariables.value = extractedVariables.value.filter(v => v.name !== variableName)
}

const handleSaveDraft = () => {
  ElMessage.info('草稿已保存')
}

const handleCreate = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    // 模拟创建成功
    const newPrompt = {
      id: 'prompt_' + Date.now(),
      ...form,
      usageCount: 0,
      lastUsedAt: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      variables: extractedVariables.value
    }

    promptStore.addPrompt(newPrompt)
    ElMessage.success('Prompt 创建成功')
    router.push('/prompts')
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败，请检查表单')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.prompt-create-container {
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
  gap: 16px;
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
  gap: 12px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.create-form {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 32px;
  border: 1px solid #e5e7eb;
}

.content-editor {
  position: relative;
}

.editor-tips {
  margin-top: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.editor-tips p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #1f2937;
}

.editor-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #6b7280;
  font-size: 13px;
}

.editor-tips li {
  margin-bottom: 4px;
}

.variables-section {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.variable-item {
  border-bottom: 1px solid #e5e7eb;
  padding: 16px;
}

.variable-item:last-child {
  border-bottom: none;
}

.variable-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.variable-name {
  font-weight: 600;
  color: #1f2937;
  background: #eff6ff;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: monospace;
}

.variable-form {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.setting-tips {
  margin-top: 8px;
}

.setting-tips p {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}
</style>
