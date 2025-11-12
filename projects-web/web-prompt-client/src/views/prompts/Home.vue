<template>
  <div class="home-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">PromptManager</h2>
      </div>
      
      <div class="sidebar-menu">
        <div class="menu-item active">
          <el-icon><i-ep-HomeFilled /></el-icon>
          <span>首页</span>
        </div>
        <div class="menu-item">
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
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><i-ep-Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">25</div>
              <div class="stat-label">总 Prompt</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><i-ep-Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">8</div>
              <div class="stat-label">收藏</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><i-ep-View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">156</div>
              <div class="stat-label">使用次数</div>
            </div>
          </div>
        </div>

        <div class="recent-prompts">
          <div class="section-header">
            <h3>最近使用的 Prompt</h3>
            <el-link type="primary" @click="$router.push('/prompts')">查看全部</el-link>
          </div>
          
          <div class="prompts-grid">
            <div class="prompt-card" v-for="prompt in recentPrompts" :key="prompt.id">
              <div class="prompt-header">
                <h4 class="prompt-title">{{ prompt.title }}</h4>
                <el-button text class="favorite-btn">
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const searchQuery = ref('')

// 模拟最近使用的 Prompt 数据
const recentPrompts = ref([
  {
    id: '1',
    title: '代码审查助手',
    description: '帮助审查代码质量和最佳实践',
    tags: ['编程', '代码审查', '最佳实践'],
    isFavorite: true,
    usageCount: 23,
    lastUsedAt: '2小时前'
  },
  {
    id: '2',
    title: '文章写作助手',
    description: '协助撰写高质量的技术文章',
    tags: ['写作', '内容创作', '技术'],
    isFavorite: false,
    usageCount: 15,
    lastUsedAt: '1天前'
  },
  {
    id: '3',
    title: '数据分析报告',
    description: '生成数据分析报告和洞察',
    tags: ['数据分析', '报告', '洞察'],
    isFavorite: true,
    usageCount: 8,
    lastUsedAt: '3天前'
  },
  {
    id: '4',
    title: '产品需求文档',
    description: '编写清晰的产品需求文档',
    tags: ['产品', '文档', '需求'],
    isFavorite: false,
    usageCount: 12,
    lastUsedAt: '1周前'
  }
])
</script>

<style scoped>
.home-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f9fafb;
  margin: 0;
  padding: 0;
  overflow: hidden;

  .sidebar {
    width: 240px;
    background: white;
    border-right: 1px solid #e5e7eb;
    padding: 20px 0;

    .sidebar-header {
      padding: 0 20px 20px;
      border-bottom: 1px solid #e5e7eb;
      margin-bottom: 20px;

      .sidebar-title {
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
      }
    }

    .sidebar-menu {
      padding: 0 12px;

      .menu-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        border-radius: 8px;
        color: #6b7280;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background-color: #f3f4f6;
          color: #374151;
        }

        &.active {
          background-color: #eff6ff;
          color: #2563eb;
          font-weight: 500;
        }
      }
    }
  }

  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;

    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px 24px;
      background: white;
      border-bottom: 1px solid #e5e7eb;

      .search-bar {
        width: 400px;
      }

      .header-actions {
        display: flex;
        align-items: center;
        gap: 16px;
      }
    }

    .content {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
      box-sizing: border-box;

      .stats-cards {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 20px;
        margin-bottom: 32px;

        .stat-card {
          background: white;
          border-radius: 12px;
          padding: 24px;
          display: flex;
          align-items: center;
          gap: 16px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          border: 1px solid #e5e7eb;

          .stat-icon {
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

          .stat-info {
            flex: 1;

            .stat-number {
              font-size: 24px;
              font-weight: 600;
              color: #1f2937;
              line-height: 1;
            }

            .stat-label {
              font-size: 14px;
              color: #6b7280;
              margin-top: 4px;
            }
          }
        }
      }

      .recent-prompts {
        .section-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 20px;

          h3 {
            font-size: 18px;
            font-weight: 600;
            color: #1f2937;
            margin: 0;
          }
        }

        .prompts-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
          gap: 20px;

          .prompt-card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            border: 1px solid #e5e7eb;
            transition: all 0.2s;
            cursor: pointer;

            &:hover {
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
              transform: translateY(-2px);
            }

            .prompt-header {
              display: flex;
              align-items: flex-start;
              justify-content: space-between;
              margin-bottom: 12px;

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

              .usage-count {
                font-weight: 500;
              }

              .last-used {
                font-size: 11px;
              }
            }
          }
        }
      }
    }
  }
}
</style>
