<template>
  <div class="question-detail-page">
    <div class="container">
      <!-- 问题详情 -->
      <el-card v-if="question" class="question-card" v-loading="loading">
        <div class="question-header">
          <h1 class="question-title">{{ question.title }}</h1>
          <div class="question-meta">
            <el-tag
              v-for="tag in question.tags"
              :key="tag.id"
              size="small"
              class="tag-item"
            >
              {{ tag.name }}
            </el-tag>
            <span class="author-info">
              <el-avatar :src="question.author.avatar" :size="24">
                {{ question.author.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="author-name">{{ question.author.username }}</span>
              <span class="created-time">{{ formatTime(question.createdAt) }}提问</span>
            </span>
          </div>
        </div>

        <div class="question-body">
          <div class="vote-section">
            <el-button 
              :type="userVote === 1 ? 'primary' : 'default'"
              @click="vote(1)"
              :disabled="!authStore.isAuthenticated"
              circle
              :icon="CaretTop"
              size="large"
            />
            <span class="vote-count">{{ question.votes }}</span>
            <el-button 
              :type="userVote === -1 ? 'danger' : 'default'"
              @click="vote(-1)"
              :disabled="!authStore.isAuthenticated"
              circle
              :icon="CaretBottom"
              size="large"
            />
          </div>

          <div class="question-content">
            <div class="content-text" v-html="question.content"></div>
            
            <!-- 问题图片显示 -->
            <div v-if="questionImages.length > 0" class="question-images">
              <h4>相关图片：</h4>
              <div class="images-grid">
                <div
                  v-for="image in questionImages"
                  :key="image.id"
                  class="image-item"
                >
                  <el-image
                    :src="`/api/questions/images/${image.id}/file`"
                    :alt="image.fileName"
                    fit="cover"
                    loading="lazy"
                    :preview-src-list="imagePreviewList"
                    :initial-index="getImageIndex(image.id)"
                    preview-teleported
                  />
                  <div class="image-overlay">
                    <span class="image-name">{{ image.fileName }}</span>
                    <span class="image-size">{{ formatFileSize(image.fileSize) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="question-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ question.viewCount || 0 }} 浏览
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ question.answerCount }} 回答
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                <el-button
                  v-if="authStore.isAuthenticated"
                  :type="isFollowing ? 'primary' : 'default'"
                  size="small"
                  @click="toggleFollow"
                  :loading="followLoading"
                >
                  {{ isFollowing ? '已关注' : '关注' }}
                </el-button>
                <span v-else>关注功能</span>
              </span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 回答列表 -->
      <div class="answers-section">
        <div class="answers-header">
          <h2>{{ question?.answerCount || 0 }} 个回答</h2>
          <el-select v-model="sortBy" @change="loadAnswers" style="width: 120px">
            <el-option label="最新" value="newest" />
            <el-option label="最热" value="votes" />
            <el-option label="最早" value="oldest" />
          </el-select>
        </div>

        <div v-if="answers.length === 0 && !loadingAnswers" class="no-answers">
          <el-empty description="暂无回答，来抢沙发吧！" />
        </div>

        <el-card 
          v-for="answer in answers" 
          :key="answer.id" 
          class="answer-card"
          v-loading="loadingAnswers"
        >
          <div class="answer-body">
            <div class="vote-section">
              <el-button 
                :type="answer.userVote === 1 ? 'primary' : 'default'"
                @click="voteAnswer(answer.id, 1)"
                :disabled="!authStore.isAuthenticated"
                circle
                :icon="CaretTop"
              />
              <span class="vote-count">{{ answer.votes }}</span>
              <el-button 
                :type="answer.userVote === -1 ? 'danger' : 'default'"
                @click="voteAnswer(answer.id, -1)"
                :disabled="!authStore.isAuthenticated"
                circle
                :icon="CaretBottom"
              />
              <el-button 
                v-if="answer.accepted"
                type="success"
                circle
                :icon="Check"
                title="已采纳"
              />
            </div>

            <div class="answer-content">
              <div class="content-text" v-html="answer.content"></div>
              
              <div class="answer-meta">
                <span class="author-info">
                  <el-avatar :src="answer.author.avatar" :size="20">
                    {{ answer.author.username.charAt(0).toUpperCase() }}
                  </el-avatar>
                  <span class="author-name">{{ answer.author.username }}</span>
                  <span class="created-time">{{ formatTime(answer.createdAt) }}回答</span>
                </span>
                
                <div class="answer-actions">
                  <el-button 
                    v-if="authStore.user?.id === question?.author.id && !answer.accepted"
                    type="success"
                    size="small"
                    @click="acceptAnswer(answer.id)"
                  >
                    采纳
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 发表回答 -->
      <el-card v-if="authStore.isAuthenticated" class="answer-form-card">
        <h3>发表回答</h3>
        <el-form @submit.prevent="submitAnswer">
          <el-form-item>
            <div class="editor-container">
              <el-input
                v-model="newAnswer.content"
                type="textarea"
                :rows="8"
                placeholder="请输入您的回答内容..."
                maxlength="5000"
                show-word-limit
              />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="submitAnswer"
              :loading="submittingAnswer"
              :disabled="!newAnswer.content.trim()"
            >
              发表回答
            </el-button>
            <el-button @click="newAnswer.content = ''">清空</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <div v-else class="login-prompt">
        <el-alert
          title="请先登录后再回答问题"
          type="info"
          center
          show-icon
        >
          <template #default>
            <el-button type="primary" @click="$router.push('/login')">
              立即登录
            </el-button>
          </template>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CaretTop,
  CaretBottom,
  View,
  ChatDotRound,
  Check,
  Star
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const question = ref<any>(null)
const answers = ref<any[]>([])
const questionImages = ref<any[]>([])
const loading = ref(false)
const loadingAnswers = ref(false)
const submittingAnswer = ref(false)
const userVote = ref(0)
const sortBy = ref('newest')
const isFollowing = ref(false)
const followLoading = ref(false)

const newAnswer = reactive({
  content: ''
})

// 计算属性：图片预览列表
const imagePreviewList = computed(() => {
  return questionImages.value.map(image => `/api/questions/images/${image.id}/file`)
})

// 获取图片在预览列表中的索引
const getImageIndex = (imageId: number) => {
  return questionImages.value.findIndex(image => image.id === imageId)
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let fileSize = size
  let unitIndex = 0
  
  while (fileSize >= 1024 && unitIndex < units.length - 1) {
    fileSize /= 1024
    unitIndex++
  }
  
  return `${fileSize.toFixed(1)} ${units[unitIndex]}`
}

// 获取问题图片
const loadQuestionImages = async () => {
  try {
    const questionId = route.params.id
    const response = await api.get(`/questions/${questionId}/images`)
    if (response.data.success && response.data.images) {
      questionImages.value = response.data.images
      console.log('✅ 问题图片加载成功:', questionImages.value.length, '张图片')
    }
  } catch (error) {
    console.error('❌ 加载问题图片失败:', error)
    questionImages.value = []
  }
}

// 预览图片
const previewImage = (image: any) => {
  // 使用Element Plus的图片预览功能
  const imageUrl = `/api/questions/images/${image.id}/file`
  window.open(imageUrl, '_blank')
}

// 获取问题详情
const loadQuestion = async () => {
  loading.value = true
  try {
    const questionId = route.params.id
    const response = await api.get(`/questions/${questionId}`)
    question.value = response.data
    
    // 同时加载问题图片
    await loadQuestionImages()
    
    // 获取用户投票状态
    if (authStore.isAuthenticated) {
      try {
        const voteResponse = await api.get(`/questions/${questionId}/vote`)
        userVote.value = voteResponse.data.vote || 0
      } catch (error) {
        console.error('获取投票状态失败:', error)
      }
    }
  } catch (error) {
    console.error('加载问题失败:', error)
    ElMessage.error('加载问题失败，请稍后重试')
    // 如果API失败，不显示模拟数据，让用户知道出现了问题
    question.value = null
  } finally {
    loading.value = false
  }
}

// 获取回答列表
const loadAnswers = async () => {
  loadingAnswers.value = true
  try {
    const questionId = route.params.id
    const response = await api.get(`/questions/${questionId}/answers`, {
      params: { sort: sortBy.value }
    })
    
    // 处理分页数据格式
    let rawAnswers = []
    if (response.data.content && Array.isArray(response.data.content)) {
      rawAnswers = response.data.content
    } else if (Array.isArray(response.data)) {
      rawAnswers = response.data
    } else {
      rawAnswers = []
    }
    
    // 确保每个回答都有正确的属性，包括采纳状态
    answers.value = rawAnswers.map((answer: any) => ({
      ...answer,
      accepted: answer.accepted || false,  // 将后端的accepted映射为前端的isAccepted
      voteCount: answer.voteCount || 0,
      userVote: 0  // 默认用户未投票，后续可以通过单独API获取
    }))
    
    // 获取用户对每个回答的投票状态
    if (authStore.isAuthenticated) {
      for (const answer of answers.value) {
        try {
          const voteResponse = await api.get(`/questions/answers/${answer.id}/vote-status`)
          if (voteResponse.data) {
            answer.userVote = voteResponse.data.userVote || 0
            answer.voteCount = voteResponse.data.voteCount || answer.voteCount
          }
        } catch (error) {
          console.warn(`获取回答${answer.id}投票状态失败:`, error)
        }
      }
    }
    
    console.log('✅ 回答列表加载成功:', answers.value.length, '个回答')
  } catch (error) {
    console.error('❌ 加载回答失败:', error)
    ElMessage.error('加载回答失败')
    // 不使用模拟数据，保持空数组
    answers.value = []
  } finally {
    loadingAnswers.value = false
  }
}

// 投票
const vote = async (voteType: number) => {
  try {
    const questionId = route.params.id
    const response = await api.post(`/questions/${questionId}/vote`, { vote: voteType })
    
    if (response.data.success) {
      // 更新UI
      userVote.value = response.data.userVote
      if (question.value) {
        question.value.votes = response.data.voteCount
      }
      
      ElMessage.success(response.data.message)
    } else {
      ElMessage.error(response.data.message || '投票失败')
    }
  } catch (error: any) {
    console.error('投票失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('投票失败')
    }
  }
}

// 回答投票
const voteAnswer = async (answerId: number, voteType: number) => {
  try {
    const response = await api.post(`/questions/answers/${answerId}/vote`, { vote: voteType })
    
    if (response.data.success) {
      // 更新UI
      const answer = answers.value.find(a => a.id === answerId)
      if (answer) {
        answer.userVote = response.data.userVote
        answer.voteCount = response.data.voteCount
      }
      
      ElMessage.success(response.data.message)
    } else {
      ElMessage.error(response.data.message || '投票失败')
    }
  } catch (error: any) {
    console.error('投票失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('投票失败')
    }
  }
}

// 采纳回答
const acceptAnswer = async (answerId: number) => {
  try {
    const questionId = route.params.id
    const response = await api.post(`/questions/${questionId}/accept`, { answerId })
    
    if (response.data.success) {
      // 更新UI - 使用正确的属性名
      answers.value.forEach(answer => {
        answer.accepted = answer.id === answerId  // 同时更新后端属性名
      })
      
      ElMessage.success(response.data.message)
    } else {
      ElMessage.error(response.data.message || '采纳失败')
    }
  } catch (error: any) {
    console.error('采纳失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('采纳失败')
    }
  }
}

// 提交回答
const submitAnswer = async () => {
  if (!newAnswer.content.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }
  
  // 检查用户是否已登录
  if (!authStore.user || !authStore.user.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  submittingAnswer.value = true
  try {
    const questionId = route.params.id
    const response = await api.post(`/questions/${questionId}/answers`, {
      content: newAnswer.content,
      authorId: authStore.user.id  // 添加authorId参数
    })
    
    // 检查响应格式
    let newAnswerData
    if (response.data.success && response.data.answer) {
      // 使用后端返回的真实数据
      newAnswerData = response.data.answer
    } else {
      // 兼容旧格式或创建默认数据
      newAnswerData = {
        id: Date.now(),
        content: newAnswer.content,
        author: authStore.user,
        createdAt: new Date().toISOString(),
        voteCount: 0,
        userVote: 0,
        accepted: false
      }
    }
    
    // 添加到回答列表
    answers.value.unshift(newAnswerData)
    newAnswer.content = ''
    
    // 更新问题的回答数
    if (question.value) {
      question.value.answerCount++
    }
    
    ElMessage.success('回答发表成功')
  } catch (error: any) {
    console.error('发表回答失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(`发表回答失败: ${error.response.data.message}`)
    } else {
      ElMessage.error('发表回答失败')
    }
  } finally {
    submittingAnswer.value = false
  }
}

// 回复回答（暂时跳转到回答表单）
const replyToAnswer = (answerId: number) => {
  const answerElement = document.querySelector('.answer-form-card')
  if (answerElement) {
    answerElement.scrollIntoView({ behavior: 'smooth' })
    newAnswer.content = `@${answers.value.find(a => a.id === answerId)?.author.username} `
  }
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

// 关注/取消关注问题
const toggleFollow = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  followLoading.value = true
  try {
    const questionId = route.params.id
    const response = await api.post(`/questions/${questionId}/follow`, null, {
      params: { follow: !isFollowing.value }
    })
    
    if (response.data.success) {
      isFollowing.value = response.data.followed
      ElMessage.success(response.data.message)
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error: any) {
    console.error('关注操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  if (!authStore.isAuthenticated) return
  
  try {
    const questionId = route.params.id
    const response = await api.get(`/questions/${questionId}/follow-status`)
    isFollowing.value = response.data.isFollowing || false
  } catch (error) {
    console.error('检查关注状态失败:', error)
  }
}

// 增加浏览次数
const incrementViewCount = async () => {
  try {
    const questionId = route.params.id
    await api.post(`/questions/${questionId}/view`)
  } catch (error) {
    console.error('增加浏览次数失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadQuestion()
  loadAnswers()
  checkFollowStatus()
  incrementViewCount()
})
</script>

<style scoped>
.question-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 20px;
  padding-bottom: 40px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.question-card {
  margin-bottom: 30px;
}

.question-header {
  margin-bottom: 20px;
}

.question-title {
  font-size: 24px;
  color: #333;
  margin: 0 0 15px 0;
  line-height: 1.4;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.tag-item {
  margin-right: 8px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.author-name {
  color: #409eff;
  font-weight: 500;
}

.question-body {
  display: flex;
  gap: 20px;
}

.vote-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.vote-count {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.question-content {
  flex: 1;
}

.content-text {
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.question-stats {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 问题图片样式 */
.question-images {
  margin: 20px 0;
}

.question-images h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.image-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-item :deep(.el-image) {
  width: 100%;
  height: 150px;
  display: block;
}

.image-item :deep(.el-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: white;
  padding: 8px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.image-name {
  display: block;
  font-weight: 500;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.image-size {
  font-size: 11px;
  opacity: 0.8;
}

.answers-section {
  margin-bottom: 30px;
}

.answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.answers-header h2 {
  margin: 0;
  color: #333;
}

.answer-card {
  margin-bottom: 20px;
}

.answer-body {
  display: flex;
  gap: 20px;
}

.answer-content {
  flex: 1;
}

.answer-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.answer-actions {
  display: flex;
  gap: 10px;
}

.answer-form-card {
  margin-bottom: 30px;
}

.answer-form-card h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.no-answers {
  text-align: center;
  padding: 40px 0;
}

.login-prompt {
  text-align: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .question-body,
  .answer-body {
    flex-direction: column;
    gap: 15px;
  }
  
  .vote-section {
    flex-direction: row;
    justify-content: center;
  }
  
  .question-title {
    font-size: 20px;
  }
  
  .container {
    padding: 0 15px;
  }
}
</style> 