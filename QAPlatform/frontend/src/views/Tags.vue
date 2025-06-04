<template>
  <div class="tags-page">
    <div class="container">
      <div class="page-header">
        <h1>标签</h1>
        <p class="page-desc">浏览所有标签，找到您感兴趣的技术话题</p>
      </div>

      <!-- 搜索标签 -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="搜索标签..."
          size="large"
          @input="filterTags"
          class="search-input"
        >
          <template #prepend>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 标签统计 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ totalTags }}</div>
                <div class="stat-label">总标签数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ totalQuestions }}</div>
                <div class="stat-label">相关问题</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ todayQuestions }}</div>
                <div class="stat-label">今日新问题</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 标签分类 -->
      <div class="categories-section">
        <el-tabs v-model="activeCategory" @tab-click="handleTabClick">
          <el-tab-pane label="全部标签" name="all" />
          <el-tab-pane label="热门标签" name="popular" />
          <el-tab-pane label="编程语言" name="language" />
          <el-tab-pane label="框架库" name="framework" />
          <el-tab-pane label="数据库" name="database" />
          <el-tab-pane label="工具" name="tools" />
        </el-tabs>
      </div>

      <!-- 标签列表 -->
      <div class="tags-section">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="6" animated />
        </div>
        
        <div v-else-if="filteredTags.length === 0" class="empty-state">
          <el-empty description="未找到相关标签" />
        </div>
        
        <div v-else class="tags-grid">
          <el-card 
            v-for="tag in paginatedTags" 
            :key="tag.id" 
            class="tag-card"
            @click="goToTagQuestions(tag.id)"
          >
            <div class="tag-content">
              <div class="tag-header">
                <el-tag :type="getTagType(tag.category)" effect="plain" size="large">
                  {{ tag.name }}
                </el-tag>
                <span class="question-count">{{ tag.questionCount }} 个问题</span>
              </div>
              
              <p class="tag-description">{{ tag.description || '暂无描述' }}</p>
              
              <div class="tag-meta">
                <span class="used-today">今日使用: {{ tag.usedToday || 0 }}</span>
                <span class="last-used">最后使用: {{ formatTime(tag.lastUsed) }}</span>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div v-if="filteredTags.length > pageSize" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredTags.length"
            layout="total, prev, pager, next, jumper"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const activeCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(20)
const tags = ref<any[]>([])

// 统计数据
const totalTags = computed(() => tags.value.length)
const totalQuestions = computed(() => tags.value.reduce((sum, tag) => sum + tag.questionCount, 0))
const todayQuestions = computed(() => tags.value.reduce((sum, tag) => sum + (tag.usedToday || 0), 0))

// 过滤标签
const filteredTags = computed(() => {
  let result = tags.value

  // 按分类过滤
  if (activeCategory.value !== 'all') {
    result = result.filter(tag => tag.category === activeCategory.value)
  }

  // 按搜索关键词过滤
  if (searchQuery.value) {
    result = result.filter(tag => 
      tag.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (tag.description && tag.description.toLowerCase().includes(searchQuery.value.toLowerCase()))
    )
  }

  return result
})

// 分页标签
const paginatedTags = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTags.value.slice(start, end)
})

// 加载标签数据
const loadTags = async () => {
  loading.value = true
  try {
    const response = await api.get('/tags')
    tags.value = response.data || []
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签数据失败')
    tags.value = []
  } finally {
    loading.value = false
  }
}

// 过滤标签
const filterTags = () => {
  currentPage.value = 1
}

// 标签页切换
const handleTabClick = () => {
  currentPage.value = 1
}

// 分页切换
const handlePageChange = (page: number) => {
  currentPage.value = page
}

// 获取标签类型
const getTagType = (category: string) => {
  switch (category) {
    case 'language': return 'primary'
    case 'framework': return 'success'
    case 'database': return 'warning'
    case 'tools': return 'info'
    default: return 'default'
  }
}

// 跳转到标签相关问题
const goToTagQuestions = (tagId: number) => {
  router.push(`/questions?tag=${tagId}`)
}

// 格式化时间
const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

// 组件挂载时加载数据
onMounted(() => {
  loadTags()
})
</script>

<style lang="scss" scoped>
.tags-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 20px;
  padding-bottom: 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    font-size: 32px;
    color: #333;
    margin-bottom: 10px;
  }
  
  .page-desc {
    color: #666;
    font-size: 16px;
  }
}

.search-section {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
  
  .search-input {
    max-width: 500px;
  }
}

.stats-section {
  margin-bottom: 30px;
  
  .stat-card {
    text-align: center;
    
    .stat-content {
      .stat-number {
        font-size: 28px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 8px;
      }
      
      .stat-label {
        color: #666;
        font-size: 14px;
      }
    }
  }
}

.categories-section {
  margin-bottom: 30px;
}

.tags-section {
  .loading-container {
    margin: 40px 0;
  }
  
  .empty-state {
    text-align: center;
    margin: 40px 0;
  }
  
  .tags-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
  }
  
  .tag-card {
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
    
    .tag-content {
      .tag-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        
        .question-count {
          color: #666;
          font-size: 14px;
        }
      }
      
      .tag-description {
        color: #666;
        line-height: 1.5;
        margin-bottom: 15px;
        min-height: 40px;
      }
      
      .tag-meta {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #999;
        
        .used-today {
          color: #67c23a;
        }
      }
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 30px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .stats-section {
    .el-col {
      margin-bottom: 15px;
    }
  }
  
  .tags-grid {
    grid-template-columns: 1fr;
  }
}
</style> 