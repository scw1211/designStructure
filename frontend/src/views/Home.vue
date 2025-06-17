<template>
  <div class="home">
    <!-- 平台统计 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalQuestions }}</div>
              <div class="stat-label">总问题数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalAnswers }}</div>
              <div class="stat-label">总回答数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.todayQuestions }}</div>
              <div class="stat-label">今日新问题</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索栏 -->
    <div class="search-container">
      <div class="search-wrapper">
        <el-input
          v-model="searchQuery"
          placeholder="搜索问题..."
          size="large"
          @keyup.enter="searchQuestions"
          class="search-input"
        >
          <template #append>
            <el-select
              v-model="searchStrategy"
              placeholder="策略"
              style="width: 120px"
              @change="searchQuestions"
            >
              <el-option label="标签搜索" value="TAG" />
              <el-option label="标题搜索" value="TITLE" />
            </el-select>
          </template>
        </el-input>
        <el-button @click="searchQuestions" :icon="Search" type="primary" size="large">搜索</el-button>
      </div>
    </div>

    <!-- 标签过滤 -->
    <div class="tags-container">
      <h3>热门标签</h3>
      <el-tag
        v-for="tag in popularTags"
        :key="tag.id"
        :type="selectedTag === tag.id ? 'primary' : 'info'"
        @click="filterByTag(tag.id)"
        class="tag-item"
        effect="plain"
      >
        {{ tag.name }} ({{ tag.questionCount }})
      </el-tag>
      <el-tag v-if="selectedTag" @click="clearTagFilter" type="danger" class="tag-item">
        清除筛选
      </el-tag>
    </div>

    <!-- 问题列表 -->
    <div class="questions-container">
      <div class="questions-header">
        <h2>{{ selectedTag ? '筛选的问题' : '最新问题' }}</h2>
        <el-button type="primary" @click="$router.push('/ask')">
          提问
        </el-button>
      </div>

      <el-card v-for="question in questions" :key="question.id" class="question-card" @click="goToQuestion(question.id)">
        <div class="question-content">
          <div class="question-main">
            <h3 class="question-title">{{ question.title }}</h3>
            <p class="question-preview">{{ question.content.substring(0, 200) }}...</p>
            
            <div class="question-tags">
              <el-tag
                v-for="tag in question.tags"
                :key="tag.id"
                size="small"
                class="question-tag"
              >
                {{ tag.name }}
              </el-tag>
            </div>

            <div class="question-meta">
              <span class="author">{{ question.author.username }}</span>
              <span class="time">{{ formatTime(question.createdAt) }}</span>
            </div>
          </div>

          <div class="question-stats">
            <div class="stat">
              <span class="number">{{ question.votes || 0 }}</span>
              <span class="label">投票</span>
            </div>
            <div class="stat">
              <span class="number">{{ question.answerCount }}</span>
              <span class="label">回答</span>
            </div>
            <div class="stat">
              <span class="number">{{ question.viewCount }}</span>
              <span class="label">浏览</span>
            </div>
          </div>
        </div>
      </el-card>

      <div v-if="loading" class="loading-container">
        <el-loading-directive />
      </div>

      <div v-if="!loading && questions.length === 0" class="empty-state">
        <el-empty description="暂无问题" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const selectedTag = ref<number | null>(null)
const loading = ref(false)
const searchStrategy = ref('TAG')

const stats = ref({
  totalQuestions: 0,
  totalAnswers: 0,
  totalUsers: 0,
  todayQuestions: 0,
  todayUsers: 0
})

const questions = ref<any[]>([])
const popularTags = ref<any[]>([])

// 加载统计数据
const loadStats = async () => {
  try {
    // 尝试从问题和用户接口获取基础统计
    const [questionsRes, usersRes] = await Promise.allSettled([
      api.get('/questions?page=0&size=1'),
      api.get('/users?page=0&size=1')
    ])
    
    let totalQuestions = 0
    let totalUsers = 0
    
    if (questionsRes.status === 'fulfilled' && questionsRes.value.data.totalElements) {
      totalQuestions = questionsRes.value.data.totalElements
    }
    
    if (usersRes.status === 'fulfilled' && usersRes.value.data.totalElements) {
      totalUsers = usersRes.value.data.totalElements
    }
    
    // 调用后端统计API获取真实数据
    const statsResponse = await api.get('/stats')
    if (statsResponse.data) {
      stats.value = {
        totalQuestions: statsResponse.data.totalQuestions || totalQuestions || 156,
        totalAnswers: statsResponse.data.totalAnswers || Math.floor(totalQuestions * 1.5) || 234,
        totalUsers: statsResponse.data.totalUsers || totalUsers || 89,
        todayQuestions: statsResponse.data.todayQuestions || 0,
        todayUsers: statsResponse.data.todayUsers || 0  // 新增今日新用户数据
      }
    } else {
    stats.value = {
      totalQuestions: totalQuestions || 156,
      totalAnswers: Math.floor(totalQuestions * 1.5) || 234,
      totalUsers: totalUsers || 89,
        todayQuestions: 0,
        todayUsers: 0  // 新增今日新用户数据
      }
    }
  } catch (error) {
    console.warn('加载统计数据失败，使用默认数据:', error)
    // 模拟数据
    stats.value = {
      totalQuestions: 156,
      totalUsers: 89,
      totalAnswers: 234,
      todayQuestions: 0,  // 使用固定值而不是随机数
      todayUsers: 0  // 新增今日新用户数据
    }
  }
}

// 加载最新问题
const loadQuestions = async () => {
  loading.value = true
  try {
    const response = await api.get('/questions', {
      params: { 
        page: 0, 
        size: 5,
        sortBy: 'createdAt',
        sortDirection: 'desc'
      }
    })
    
    // 处理分页数据
    if (response.data.content) {
      questions.value = response.data.content
    } else if (Array.isArray(response.data)) {
      questions.value = response.data
    } else {
      questions.value = []
    }
    console.log('成功加载问题数据:', questions.value)
  } catch (error) {
    console.warn('API请求失败，使用模拟数据:', error)
    // 模拟数据
    questions.value = [
      {
        id: 1,
        title: '如何学习Spring Boot？',
        content: '我是一个初学者，想要学习Spring Boot框架，请问有什么好的学习路径和资源推荐吗？',
        author: { username: 'java_learner' },
        createdAt: new Date().toISOString(),
        viewCount: 25,
        answerCount: 3,
        votes: 0,
        solved: false,
        tags: [{ name: 'Spring Boot' }, { name: 'Java' }]
      },
      {
        id: 2,
        title: 'Vue 3组合式API最佳实践',
        content: '在使用Vue 3的组合式API时，有哪些最佳实践和注意事项？',
        author: { username: 'vue_developer' },
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        viewCount: 18,
        answerCount: 2,
        votes: 1,
        solved: true,
        tags: [{ name: 'Vue.js' }, { name: '前端' }]
      },
      {
        id: 3,
        title: 'MySQL索引优化策略',
        content: '数据库查询性能较慢，想了解MySQL索引的优化策略和最佳实践。',
        author: { username: 'db_admin' },
        createdAt: new Date(Date.now() - 7200000).toISOString(),
        viewCount: 42,
        answerCount: 5,
        votes: 2,
        solved: false,
        tags: [{ name: 'MySQL' }, { name: '数据库' }]
      }
    ]
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
    console.warn('API请求失败，使用模拟数据:', error)
    // 模拟数据
    popularTags.value = [
      { id: 1, name: 'Spring Boot', questionCount: 45 },
      { id: 2, name: 'Java', questionCount: 67 },
      { id: 3, name: 'Vue.js', questionCount: 34 },
      { id: 4, name: '前端', questionCount: 56 },
      { id: 5, name: 'MySQL', questionCount: 23 },
      { id: 6, name: '数据库优化', questionCount: 12 },
      { id: 7, name: 'Python', questionCount: 38 },
      { id: 8, name: 'JavaScript', questionCount: 89 }
    ]
  }
}

// 搜索问题
const searchQuestions = async () => {
  // 如果没有搜索内容，加载默认问题列表
  if (!searchQuery.value || !searchQuery.value.trim()) {
    loadQuestions()
    return
  }

  loading.value = true
  try {
    const response = await api.get('/questions/search', {
      params: { keyword: searchQuery.value.trim(), strategy: searchStrategy.value }
    })
    questions.value = response.data.content || response.data
    console.log('搜索完成，策略:', searchStrategy.value, '结果数量:', questions.value.length)
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 按标签过滤
const filterByTag = (tagId: number) => {
  selectedTag.value = tagId
  loadQuestions()
}

// 清除标签过滤
const clearTagFilter = () => {
  selectedTag.value = null
  loadQuestions()
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
onMounted(() => {
  loadStats()
  loadQuestions()
  loadPopularTags()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.stats-container {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-item {
  padding: 10px;
}

.stat-number {
  font-size: 2em;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.search-container {
  margin-bottom: 30px;
}

.search-wrapper {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  align-items: center;
}

.search-input {
  flex: 1;
}

.tags-container {
  margin-bottom: 30px;
}

.tags-container h3 {
  margin-bottom: 15px;
  color: #333;
}

.tag-item {
  margin: 5px 8px 5px 0;
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: scale(1.05);
}

.questions-container {
  margin-bottom: 30px;
}

.questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.questions-header h2 {
  margin: 0;
  color: #333;
}

.question-card {
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.question-content {
  display: flex;
  justify-content: space-between;
}

.question-main {
  flex: 1;
  margin-right: 20px;
}

.question-title {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.question-title:hover {
  color: #409eff;
}

.question-preview {
  margin: 10px 0;
  color: #666;
  line-height: 1.5;
}

.question-tags {
  margin: 15px 0;
}

.question-tag {
  margin-right: 8px;
}

.question-meta {
  color: #999;
  font-size: 12px;
}

.question-meta .author {
  font-weight: 500;
  color: #409eff;
  margin-right: 15px;
}

.question-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 80px;
}

.stat {
  text-align: center;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat .number {
  display: block;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.stat .label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.loading-container {
  text-align: center;
  padding: 40px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

@media (max-width: 768px) {
  .question-content {
    flex-direction: column;
  }
  
  .question-main {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .question-stats {
    flex-direction: row;
    justify-content: space-around;
  }
}
</style> 