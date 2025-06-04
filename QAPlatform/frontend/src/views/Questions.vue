<template>
  <div class="questions-page">
    <div class="container">
      <div class="page-header">
        <h1>问题</h1>
        <el-button type="primary" @click="$router.push('/ask')" size="large">
          提问
        </el-button>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-input
              v-model="searchQuery"
              placeholder="搜索问题..."
              size="large"
              @keyup.enter="searchQuestions"
              @input="handleSearchInput"
              class="search-input"
            >
              <template #prepend>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button @click="searchQuestions">搜索</el-button>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              size="large"
              @change="sortQuestions"
              style="width: 100%"
            >
              <el-option label="最新发布" value="newest" />
              <el-option label="最多投票" value="votes" />
              <el-option label="最多回答" value="answers" />
              <el-option label="最多浏览" value="views" />
              <el-option label="最近活跃" value="active" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="statusFilter"
              placeholder="问题状态"
              size="large"
              @change="filterQuestions"
              style="width: 100%"
            >
              <el-option label="全部问题" value="" />
              <el-option label="未解决" value="unsolved" />
              <el-option label="已解决" value="solved" />
              <el-option label="无回答" value="unanswered" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 标签过滤 -->
      <div class="tags-filter" v-if="selectedTags.length > 0 || showTagFilter">
        <div class="tags-header">
          <span>筛选标签：</span>
          <el-button text @click="showTagFilter = !showTagFilter">
            {{ showTagFilter ? '隐藏' : '显示' }}标签选择
          </el-button>
        </div>
        
        <div v-if="showTagFilter" class="tags-selector">
          <el-tag
            v-for="tag in popularTags"
            :key="tag.id"
            :type="selectedTags.includes(tag.id) ? 'primary' : 'info'"
            @click="toggleTag(tag.id)"
            class="tag-item"
            effect="plain"
          >
            {{ tag.name }}
          </el-tag>
        </div>
        
        <div v-if="selectedTags.length > 0" class="selected-tags">
          <el-tag
            v-for="tagId in selectedTags"
            :key="tagId"
            type="primary"
            closable
            @close="removeTag(tagId)"
            class="selected-tag"
          >
            {{ getTagName(tagId) }}
          </el-tag>
          <el-button text @click="clearAllTags">清除所有</el-button>
        </div>
      </div>

      <!-- 问题统计 -->
      <div class="stats-section">
        <span class="total-count">共找到 {{ filteredQuestions.length }} 个问题</span>
        <div class="view-options">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="list">列表</el-radio-button>
            <el-radio-button label="card">卡片</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 问题列表 -->
      <div class="questions-container">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="6" animated />
        </div>
        
        <div v-else-if="paginatedQuestions.length === 0" class="empty-state">
          <el-empty description="暂无问题">
            <el-button type="primary" @click="$router.push('/ask')">
              发布第一个问题
            </el-button>
          </el-empty>
        </div>
        
        <div v-else>
          <!-- 列表视图 -->
          <div v-if="viewMode === 'list'" class="questions-list">
            <div 
              v-for="question in paginatedQuestions" 
              :key="question.id" 
              class="question-item"
              @click="goToQuestion(question.id)"
            >
              <div class="question-stats">
                <div class="stat">
                  <span class="number">{{ question.votes }}</span>
                  <span class="label">投票</span>
                </div>
                <div class="stat" :class="{ 'has-answers': question.answerCount > 0 }">
                  <span class="number">{{ question.answerCount }}</span>
                  <span class="label">回答</span>
                </div>
                <div class="stat">
                  <span class="number">{{ question.views }}</span>
                  <span class="label">浏览</span>
                </div>
              </div>
              
              <div class="question-content">
                <h3 class="question-title">
                  {{ question.title }}
                  <el-tag v-if="question.solved" type="success" size="small">已解决</el-tag>
                  <el-tag v-if="question.closed" type="info" size="small">已关闭</el-tag>
                </h3>
                
                <p class="question-preview">{{ question.content.substring(0, 200) }}...</p>
                
                <div class="question-tags">
                  <el-tag
                    v-for="tag in question.tags"
                    :key="tag.id"
                    size="small"
                    @click.stop="filterByTag(tag.id)"
                    class="question-tag"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
                
                <div class="question-meta">
                  <span class="author">
                    由 <strong>{{ question.author.username }}</strong> 提问
                  </span>
                  <span class="time">{{ formatTime(question.createdAt) }}</span>
                  <span v-if="question.lastActivity" class="last-activity">
                    最后活跃于 {{ formatTime(question.lastActivity) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 卡片视图 -->
          <div v-else class="questions-grid">
            <el-card 
              v-for="question in paginatedQuestions" 
              :key="question.id" 
              class="question-card"
              @click="goToQuestion(question.id)"
            >
              <div class="card-header">
                <h3 class="question-title">{{ question.title }}</h3>
                <div class="question-badges">
                  <el-tag v-if="question.solved" type="success" size="small">已解决</el-tag>
                  <el-tag v-if="question.closed" type="info" size="small">已关闭</el-tag>
                </div>
              </div>
              
              <p class="question-preview">{{ question.content.substring(0, 150) }}...</p>
              
              <div class="question-tags">
                <el-tag
                  v-for="tag in question.tags.slice(0, 3)"
                  :key="tag.id"
                  size="small"
                  @click.stop="filterByTag(tag.id)"
                  class="question-tag"
                >
                  {{ tag.name }}
                </el-tag>
                <span v-if="question.tags.length > 3" class="more-tags">
                  +{{ question.tags.length - 3 }}
                </span>
              </div>
              
              <div class="card-stats">
                <div class="stats-row">
                  <span class="stat">{{ question.votes }} 投票</span>
                  <span class="stat">{{ question.answerCount }} 回答</span>
                  <span class="stat">{{ question.views }} 浏览</span>
                </div>
                <div class="meta-row">
                  <span class="author">{{ question.author.username }}</span>
                  <span class="time">{{ formatTime(question.createdAt) }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="filteredQuestions.length > pageSize" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredQuestions.length"
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const sortBy = ref('newest')
const statusFilter = ref('')
const selectedTags = ref<number[]>([])
const showTagFilter = ref(false)
const viewMode = ref<'list' | 'card'>('list')
const currentPage = ref(1)
const pageSize = ref(20)

const questions = ref<any[]>([])
const popularTags = ref<any[]>([])

// 监听路由参数 - 优化防止无限循环
watch(() => route.query, (newQuery, oldQuery) => {
  console.log('🔄 路由查询参数变化:', { from: oldQuery, to: newQuery })
  
  let shouldReload = false
  
  // 检查搜索参数变化
  if (newQuery.search !== oldQuery?.search) {
    searchQuery.value = (newQuery.search as string) || ''
    shouldReload = true
    console.log('🔍 搜索参数更新:', searchQuery.value)
  }
  
  // 检查标签参数变化
  if (newQuery.tag !== oldQuery?.tag) {
    if (newQuery.tag) {
      const tagId = parseInt(newQuery.tag as string)
      if (!isNaN(tagId) && !selectedTags.value.includes(tagId)) {
        selectedTags.value = [tagId]
        shouldReload = true
        console.log('🏷️ 标签参数更新:', selectedTags.value)
      }
    } else {
      if (selectedTags.value.length > 0) {
        selectedTags.value = []
        shouldReload = true
        console.log('🗑️ 清除标签参数')
      }
    }
  }
  
  // 只有参数真正变化时才重新加载
  if (shouldReload) {
    console.log('🔄 参数变化，重新加载问题数据...')
    loadQuestions()
  }
}, { immediate: false }) // 改为false，避免初始化时重复触发

// 过滤后的问题
const filteredQuestions = computed(() => {
  let result = [...questions.value]

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(q => 
      q.title.toLowerCase().includes(query) ||
      q.content.toLowerCase().includes(query) ||
      q.tags.some((tag: any) => tag.name.toLowerCase().includes(query))
    )
  }

  // 状态过滤
  if (statusFilter.value) {
    switch (statusFilter.value) {
      case 'solved':
        result = result.filter(q => q.solved)
        break
      case 'unsolved':
        result = result.filter(q => !q.solved)
        break
      case 'unanswered':
        result = result.filter(q => q.answerCount === 0)
        break
    }
  }

  // 标签过滤
  if (selectedTags.value.length > 0) {
    result = result.filter(q => 
      selectedTags.value.some(tagId => 
        q.tags.some((tag: any) => tag.id === tagId)
      )
    )
  }

  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'newest':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'votes':
        return b.votes - a.votes
      case 'answers':
        return b.answerCount - a.answerCount
      case 'views':
        return b.views - a.views
      case 'active':
        return new Date(b.lastActivity || b.createdAt).getTime() - new Date(a.lastActivity || a.createdAt).getTime()
      default:
        return 0
    }
  })

  return result
})

// 分页后的问题
const paginatedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredQuestions.value.slice(start, end)
})

// 加载问题数据
const loadQuestions = async () => {
  console.log('🔄 开始加载Questions页面数据...')
  loading.value = true
  try {
    const params: any = { 
      page: 0, 
      size: 100,  // 获取更多数据用于前端过滤
      sortBy: 'createdAt',
      sortDirection: 'desc'
    }
    if (searchQuery.value) params.search = searchQuery.value
    if (selectedTags.value.length > 0) params.tags = selectedTags.value.join(',')
    
    console.log('📤 发送API请求:', params)
    const response = await api.get('/questions', { params })
    console.log('📨 API响应:', response.data)
    
    // 处理分页数据
    if (response.data.content) {
      questions.value = response.data.content.map((question: any) => ({
        ...question,
        votes: 0, // 暂时设为0，后续可以添加投票功能
        views: question.viewCount || 0,
        lastActivity: question.updatedAt || question.createdAt
      }))
    } else if (Array.isArray(response.data)) {
      questions.value = response.data.map((question: any) => ({
        ...question,
        votes: 0,
        views: question.viewCount || 0,
        lastActivity: question.updatedAt || question.createdAt
      }))
    } else {
      questions.value = []
    }
    console.log('✅ 问题数据处理完成:', questions.value.length, '个问题')
    console.log('📋 问题列表:', questions.value)
  } catch (error) {
    console.error('❌ 加载问题失败:', error)
    questions.value = []
    // 添加用户友好的错误提示
    ElMessage.error('加载问题列表失败，请检查网络连接或稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载热门标签
const loadPopularTags = async () => {
  try {
    const response = await api.get('/tags/popular')
    popularTags.value = response.data
  } catch (error) {
    console.error('加载热门标签失败:', error)
    // 模拟数据
    popularTags.value = [
      { id: 1, name: 'Spring Boot' },
      { id: 2, name: 'Java' },
      { id: 4, name: 'Vue.js' },
      { id: 5, name: '前端' },
      { id: 7, name: 'MySQL' },
      { id: 10, name: 'Python' }
    ]
  }
}

// 搜索处理
const handleSearchInput = () => {
  currentPage.value = 1
}

const searchQuestions = () => {
  currentPage.value = 1
  if (searchQuery.value) {
    router.push({ path: '/questions', query: { search: searchQuery.value } })
  } else {
    router.push('/questions')
  }
}

// 排序处理
const sortQuestions = () => {
  currentPage.value = 1
}

// 筛选处理
const filterQuestions = () => {
  currentPage.value = 1
}

// 标签处理
const toggleTag = (tagId: number) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagId)
  }
  currentPage.value = 1
}

const removeTag = (tagId: number) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  }
  currentPage.value = 1
}

const clearAllTags = () => {
  selectedTags.value = []
  currentPage.value = 1
}

const filterByTag = (tagId: number) => {
  if (!selectedTags.value.includes(tagId)) {
    selectedTags.value.push(tagId)
    currentPage.value = 1
  }
}

const getTagName = (tagId: number) => {
  const tag = popularTags.value.find(t => t.id === tagId)
  return tag ? tag.name : `标签${tagId}`
}

// 分页处理
const handlePageChange = (page: number) => {
  currentPage.value = page
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

// 跳转到问题详情
const goToQuestion = (questionId: number) => {
  router.push(`/question/${questionId}`)
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
onMounted(async () => {
  console.log('🚀 Questions页面挂载，开始初始化...')
  
  // 先加载热门标签
  await loadPopularTags()
  
  // 处理路由参数
  if (route.query.search) {
    searchQuery.value = route.query.search as string
    console.log('📝 设置搜索查询:', searchQuery.value)
  }
  
  if (route.query.tag) {
    const tagId = parseInt(route.query.tag as string)
    if (!isNaN(tagId)) {
      selectedTags.value = [tagId]
      console.log('🏷️ 设置选中标签:', selectedTags.value)
    }
  }
  
  // 强制加载问题数据
  console.log('📊 强制加载Questions页面数据...')
  await loadQuestions()
  
  console.log('✅ Questions页面初始化完成')
})
</script>

<style lang="scss" scoped>
.questions-page {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  
  h1 {
    font-size: 32px;
    color: #333;
    margin: 0;
  }
}

.search-section {
  margin-bottom: 20px;
}

.tags-filter {
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  
  .tags-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-weight: 500;
  }
  
  .tags-selector {
    margin-bottom: 15px;
    
    .tag-item {
      margin: 5px 8px 5px 0;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: scale(1.05);
      }
    }
  }
  
  .selected-tags {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .selected-tag {
      margin: 0;
    }
  }
}

.stats-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .total-count {
    color: #666;
    font-size: 14px;
  }
}

.loading-container {
  margin: 40px 0;
}

.empty-state {
  text-align: center;
  margin: 40px 0;
}

// 列表视图样式
.questions-list {
  .question-item {
    display: flex;
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 15px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
    
    .question-stats {
      display: flex;
      flex-direction: column;
      gap: 10px;
      margin-right: 20px;
      min-width: 80px;
      
      .stat {
        text-align: center;
        padding: 8px;
        background: #f5f7fa;
        border-radius: 4px;
        
        &.has-answers {
          background: #e8f5e8;
          color: #67c23a;
        }
        
        .number {
          display: block;
          font-size: 16px;
          font-weight: bold;
        }
        
        .label {
          display: block;
          font-size: 12px;
          margin-top: 2px;
        }
      }
    }
    
    .question-content {
      flex: 1;
      
      .question-title {
        margin: 0 0 10px 0;
        color: #333;
        font-size: 18px;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &:hover {
          color: #409eff;
        }
      }
      
      .question-preview {
        margin: 10px 0;
        color: #666;
        line-height: 1.5;
      }
      
      .question-tags {
        margin: 15px 0;
        
        .question-tag {
          margin-right: 8px;
          cursor: pointer;
          
          &:hover {
            opacity: 0.8;
          }
        }
      }
      
      .question-meta {
        color: #999;
        font-size: 12px;
        display: flex;
        gap: 15px;
        
        .author strong {
          color: #409eff;
        }
      }
    }
  }
}

// 卡片视图样式
.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  
  .question-card {
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 10px;
      
      .question-title {
        margin: 0;
        color: #333;
        font-size: 16px;
        font-weight: 600;
        flex: 1;
        margin-right: 10px;
      }
      
      .question-badges {
        display: flex;
        gap: 5px;
      }
    }
    
    .question-preview {
      color: #666;
      line-height: 1.5;
      margin-bottom: 15px;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .question-tags {
      margin-bottom: 15px;
      
      .question-tag {
        margin-right: 5px;
        margin-bottom: 5px;
        cursor: pointer;
        
        &:hover {
          opacity: 0.8;
        }
      }
      
      .more-tags {
        color: #999;
        font-size: 12px;
      }
    }
    
    .card-stats {
      .stats-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        
        .stat {
          color: #666;
          font-size: 12px;
        }
      }
      
      .meta-row {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #999;
        
        .author {
          color: #409eff;
          font-weight: 500;
        }
      }
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

// 响应式设计
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    
    h1 {
      font-size: 24px;
    }
  }
  
  .search-section .el-col {
    margin-bottom: 15px;
  }
  
  .questions-list .question-item {
    flex-direction: column;
    
    .question-stats {
      flex-direction: row;
      justify-content: space-around;
      margin-right: 0;
      margin-bottom: 15px;
    }
  }
  
  .questions-grid {
    grid-template-columns: 1fr;
  }
}
</style> 