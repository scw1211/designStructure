<template>
  <div class="user-profile-page">
    <div class="container">
      <!-- 用户基本信息 -->
      <el-card class="user-info-card" v-loading="loading">
        <div class="user-header">
          <div class="user-avatar-section">
            <el-avatar :src="userInfo.avatar" :size="120">
              {{ userInfo.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            
            <!-- 编辑头像按钮（仅自己可见） -->
            <el-button
              v-if="isOwnProfile"
              type="primary"
              circle
              size="small"
              class="edit-avatar-btn"
              @click="showAvatarDialog = true"
            >
              <el-icon><Camera /></el-icon>
            </el-button>
          </div>

          <div class="user-details">
            <div class="user-name-section">
              <h1 class="username">{{ userInfo.username }}</h1>
              <el-tag
                :type="getRoleTagType(userInfo.role)"
                class="role-tag"
              >
                {{ getRoleLabel(userInfo.role) }}
              </el-tag>
              
              <!-- 在线状态 -->
              <el-tag
                v-if="userInfo.isOnline"
                type="success"
                size="small"
                effect="plain"
              >
                在线
              </el-tag>
            </div>

            <div class="user-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(userInfo.createdAt) }} 加入
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                最后活跃：{{ formatTime(userInfo.lastActiveAt) }}
              </span>
            </div>

            <div class="user-bio" v-if="userInfo.bio">
              {{ userInfo.bio }}
            </div>

            <!-- 编辑个人资料按钮（仅自己可见） -->
            <div v-if="isOwnProfile" class="profile-actions">
              <el-button type="primary" @click="showEditDialog = true">
                <el-icon><Edit /></el-icon>
                编辑资料
              </el-button>
            </div>

            <!-- 关注按钮（他人主页） -->
            <div v-else-if="authStore.isAuthenticated" class="follow-actions">
              <el-button
                :type="isFollowing ? 'default' : 'primary'"
                @click="toggleFollow"
                :loading="followLoading"
              >
                <el-icon v-if="isFollowing"><Check /></el-icon>
                <el-icon v-else><Plus /></el-icon>
                {{ isFollowing ? '已关注' : '关注' }}
              </el-button>
              <el-button @click="sendMessage">
                <el-icon><ChatDotRound /></el-icon>
                私信
              </el-button>
            </div>
          </div>
        </div>

        <!-- 用户统计数据 -->
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-number">{{ userStats.reputation || 0 }}</div>
            <div class="stat-label">声望值</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStats.totalQuestions || 0 }}</div>
            <div class="stat-label">问题</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStats.totalAnswers || 0 }}</div>
            <div class="stat-label">回答</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStats.followersCount || 0 }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStats.followingCount || 0 }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStats.acceptedAnswers || 0 }}</div>
            <div class="stat-label">采纳</div>
          </div>
        </div>

        <!-- 签到功能区域（仅自己可见） -->
        <div v-if="isOwnProfile" class="checkin-section">
          <el-card class="checkin-card">
            <template #header>
              <div class="checkin-header">
                <span><el-icon><Calendar /></el-icon> 每日签到</span>
                <el-tag 
                  :type="checkinData.hasCheckedInToday ? 'success' : 'info'" 
                  size="small"
                >
                  {{ checkinData.hasCheckedInToday ? '今日已签到' : '未签到' }}
                </el-tag>
              </div>
            </template>
            
            <div class="checkin-content">
              <div class="checkin-stats">
                <div class="checkin-stat-item">
                  <div class="checkin-stat-number">{{ checkinStats.totalDays || 0 }}</div>
                  <div class="checkin-stat-label">总签到天数</div>
                </div>
                <div class="checkin-stat-item">
                  <div class="checkin-stat-number">{{ checkinStats.consecutiveDays || 0 }}</div>
                  <div class="checkin-stat-label">连续签到</div>
                </div>
                <div class="checkin-stat-item">
                  <div class="checkin-stat-number">{{ checkinStats.totalReputation || 0 }}</div>
                  <div class="checkin-stat-label">签到声望</div>
                </div>
              </div>
              
              <div class="checkin-action">
                <el-button
                  type="primary"
                  size="large"
                  :disabled="checkinData.hasCheckedInToday || checkinLoading"
                  :loading="checkinLoading"
                  @click="handleCheckin"
                  class="checkin-btn"
                >
                  <el-icon><Medal /></el-icon>
                  {{ checkinData.hasCheckedInToday ? '今日已签到' : '立即签到 (+10声望)' }}
                </el-button>
                
                <div v-if="checkinData.hasCheckedInToday" class="next-checkin-tip">
                  明天再来签到吧！连续签到有额外奖励哦～
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-card>

      <!-- 用户徽章系统 -->
      <el-card class="badges-card" v-if="userBadges.length > 0">
        <template #header>
          <div class="card-header">
            <span><i class="el-icon-medal"></i> 徽章收藏 ({{ userBadges.length }})</span>
          </div>
        </template>
        <div class="badges-grid">
          <div v-for="userBadge in userBadges" :key="userBadge.id" class="badge-item" :class="getBadgeLevelClass(userBadge.badge.level)">
            <div class="badge-emoji">{{ userBadge.badge.emoji }}</div>
            <div class="badge-info">
              <div class="badge-name">{{ userBadge.badge.name }}</div>
              <div class="badge-description">{{ userBadge.badge.description }}</div>
              <div class="badge-date">{{ formatDate(userBadge.earnedAt) }} 获得</div>
            </div>
            <div class="badge-level">{{ userBadge.badge.level }}</div>
          </div>
        </div>
      </el-card>

      <!-- 高级用户统计分析 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="analytics-card">
            <template #header>
              <span><i class="el-icon-data-line"></i> 贡献统计</span>
            </template>
            <div class="contribution-stats">
              <div class="contribution-item">
                <div class="contribution-label">答题采纳率</div>
                <div class="contribution-value">
                  <el-progress :percentage="userStats.acceptanceRate || 0" :show-text="true" />
                </div>
              </div>
              <div class="contribution-item">
                <div class="contribution-label">平均回答评分</div>
                <div class="contribution-value">{{ (userStats.averageScore || 0).toFixed(1) }} / 5.0</div>
              </div>
              <div class="contribution-item">
                <div class="contribution-label">获得总投票数</div>
                <div class="contribution-value">{{ userStats.totalVotes || 0 }}</div>
              </div>
              <div class="contribution-item">
                <div class="contribution-label">活跃天数</div>
                <div class="contribution-value">{{ userStats.streak || 0 }} 天</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="analytics-card">
            <template #header>
              <span><i class="el-icon-trophy"></i> 专家等级</span>
            </template>
            <div class="expert-analysis">
              <div class="expert-level">
                <div class="level-badge" :class="getExpertLevelClass(userStats.expertLevel)">
                  {{ getExpertLevelName(userStats.expertLevel) }}
                </div>
                <div class="level-description">
                  {{ getExpertLevelDescription(userStats.expertLevel) }}
                </div>
              </div>
              <div class="expert-progress" v-if="userStats.expertLevel !== 'LEGEND'">
                <div class="progress-label">距离下一等级</div>
                <el-progress :percentage="getNextLevelProgress()" :show-text="false" />
                <div class="progress-info">还需 {{ getNextLevelPointsNeeded() }} 声望值</div>
              </div>
              <div class="expert-score" v-if="userStats.expertScore">
                <div class="score-label">专家评分</div>
                <div class="score-value">{{ userStats.expertScore.toFixed(1) }}/100</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 标签页内容 -->
      <el-card class="content-card">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <!-- 用户的问题 -->
          <el-tab-pane label="问题" name="questions">
            <div class="tab-header">
              <span class="tab-title">用户的问题 ({{ userQuestions.length }})</span>
              <el-select v-model="questionSort" @change="loadUserQuestions" style="width: 120px">
                <el-option label="最新" value="newest" />
                <el-option label="最热" value="votes" />
                <el-option label="最早" value="oldest" />
              </el-select>
            </div>
            
            <div v-if="userQuestions.length === 0" class="empty-state">
              <el-empty description="暂无问题" />
            </div>
            
            <div v-else class="questions-list">
              <div
                v-for="question in userQuestions"
                :key="question.id"
                class="question-item"
                @click="$router.push(`/question/${question.id}`)"
              >
                <div class="question-content">
                  <h3 class="question-title">{{ question.title }}</h3>
                  <p class="question-preview">{{ question.content.substring(0, 150) }}...</p>
                  
                  <div class="question-tags">
                    <el-tag
                      v-for="tag in question.tags"
                      :key="tag.id"
                      size="small"
                      class="tag-item"
                    >
                      {{ tag.name }}
                    </el-tag>
                  </div>
                  
                  <div class="question-meta">
                    <span class="time">{{ formatTime(question.createdAt) }}</span>
                  </div>
                </div>
                
                <div class="question-stats">
                  <div class="stat">
                    <span class="number">{{ question.votes }}</span>
                    <span class="label">投票</span>
                  </div>
                  <div class="stat">
                    <span class="number">{{ question.answerCount }}</span>
                    <span class="label">回答</span>
                  </div>
                  <div class="stat">
                    <span class="number">{{ question.views }}</span>
                    <span class="label">浏览</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 用户的回答 -->
          <el-tab-pane label="回答" name="answers">
            <div class="tab-header">
              <span class="tab-title">用户的回答 ({{ userAnswers.length }})</span>
              <el-select v-model="answerSort" @change="loadUserAnswers" style="width: 120px">
                <el-option label="最新" value="newest" />
                <el-option label="最热" value="votes" />
                <el-option label="最早" value="oldest" />
              </el-select>
            </div>
            
            <div v-if="userAnswers.length === 0" class="empty-state">
              <el-empty description="暂无回答" />
            </div>
            
            <div v-else class="answers-list">
              <div
                v-for="answer in userAnswers"
                :key="answer.id"
                class="answer-item"
                @click="$router.push(`/question/${answer.questionId}`)"
              >
                <div class="answer-content">
                  <h4 class="question-title">{{ answer.question.title }}</h4>
                  <p class="answer-preview">{{ answer.content.substring(0, 200) }}...</p>
                  
                  <div class="answer-meta">
                    <span class="time">{{ formatTime(answer.createdAt) }}</span>
                    <el-tag v-if="answer.isAccepted" type="success" size="small">
                      已采纳
                    </el-tag>
                  </div>
                </div>
                
                <div class="answer-stats">
                  <div class="stat">
                    <span class="number">{{ answer.votes }}</span>
                    <span class="label">投票</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 关注的人 -->
          <el-tab-pane label="关注" name="following">
            <div class="tab-header">
              <span class="tab-title">关注的用户 ({{ followingUsers.length }})</span>
            </div>
            
            <div v-if="followingUsers.length === 0" class="empty-state">
              <el-empty description="暂无关注的用户" />
            </div>
            
            <div v-else class="users-grid">
              <div
                v-for="user in followingUsers"
                :key="user.id"
                class="user-card"
                @click="$router.push(`/user/${user.id}`)"
              >
                <el-avatar :src="user.avatar" :size="60">
                  {{ user.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="user-info">
                  <div class="username">{{ user.username }}</div>
                  <div class="reputation">{{ user.reputation }} 声望</div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 粉丝 -->
          <el-tab-pane label="粉丝" name="followers">
            <div class="tab-header">
              <span class="tab-title">粉丝 ({{ followers.length }})</span>
            </div>
            
            <div v-if="followers.length === 0" class="empty-state">
              <el-empty description="暂无粉丝" />
            </div>
            
            <div v-else class="users-grid">
              <div
                v-for="user in followers"
                :key="user.id"
                class="user-card"
                @click="$router.push(`/user/${user.id}`)"
              >
                <el-avatar :src="user.avatar" :size="60">
                  {{ user.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="user-info">
                  <div class="username">{{ user.username }}</div>
                  <div class="reputation">{{ user.reputation }} 声望</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="个人简介">
          <el-input
            v-model="editForm.bio"
            type="textarea"
            :rows="4"
            placeholder="介绍一下自己..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="网站">
          <el-input v-model="editForm.website" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="editForm.location" placeholder="城市, 国家" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 头像编辑对话框 -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="400px">
      <div class="avatar-upload">
        <div class="avatar-uploader" @click="handleAvatarClick">
          <el-avatar v-if="editForm.avatar" :src="editForm.avatar" :size="100" />
          <div v-else class="avatar-uploader-icon">
            <el-icon><Plus /></el-icon>
            <div>点击上传头像</div>
          </div>
        </div>
        <div class="avatar-tip">
          点击上传头像，支持 JPG、PNG 格式，大小不超过 2MB
        </div>
        <input
          type="file"
          ref="avatarInput"
          @change="handleAvatarChange"
          accept="image/jpeg,image/png"
          style="display: none"
        />
      </div>
      
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Camera,
  Calendar,
  Clock,
  Edit,
  Check,
  Plus,
  ChatDotRound,
  Medal
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// DOM引用
const avatarInput = ref<HTMLInputElement>()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const followLoading = ref(false)
const showEditDialog = ref(false)
const showAvatarDialog = ref(false)
const activeTab = ref('questions')
const questionSort = ref('newest')
const answerSort = ref('newest')
const isFollowing = ref(false)

const userInfo = ref<any>({})
const userStats = ref<any>({})
const userBadges = ref<any[]>([])
const userQuestions = ref<any[]>([])
const userAnswers = ref<any[]>([])
const followingUsers = ref<any[]>([])
const followers = ref<any[]>([])

// 签到相关数据
const checkinData = ref<any>({
  hasCheckedInToday: false
})
const checkinStats = ref<any>({
  totalDays: 0,
  consecutiveDays: 0,
  totalReputation: 0
})
const checkinLoading = ref(false)

const editForm = reactive({
  bio: '',
  website: '',
  location: '',
  avatar: '',
  avatarFile: null as File | null
})

// 计算属性
const userId = computed(() => Number(route.params.id))
const isOwnProfile = computed(() => 
  authStore.isAuthenticated && authStore.user?.id === userId.value
)

// 获取用户信息
const loadUserProfile = async () => {
  try {
    loading.value = true
    
    // 并行加载用户基础信息、统计信息和徽章
    const [userRes, statsRes, badgesRes] = await Promise.allSettled([
      api.get(`/users/${userId.value}`),
      api.get(`/analytics/user-statistics/${userId.value}`),
      api.get(`/users/${userId.value}/badges`)
    ])
    
    // 处理用户基础信息
    if (userRes.status === 'fulfilled') {
      userInfo.value = userRes.value.data
    }
    
    // 处理用户统计信息
    if (statsRes.status === 'fulfilled') {
      userStats.value = statsRes.value.data
    } else {
      // 如果统计API失败，计算基础统计
      await loadBasicUserStats()
    }
    
    // 处理用户徽章
    if (badgesRes.status === 'fulfilled') {
      userBadges.value = badgesRes.value.data
    } else {
      // 如果徽章API失败，生成模拟徽章
      const reputation = userInfo.value?.reputation || 0
      userBadges.value = generateMockBadges(reputation)
    }
    
    // 加载其他数据
    await Promise.all([
      loadUserQuestions(),
      loadUserAnswers(), 
      loadUserFollowing(),
      loadUserFollowers()
    ])
    
    // 如果是自己的页面，加载签到数据
    if (isOwnProfile.value) {
      await Promise.all([
        loadCheckinData(),
        loadCheckinStats()
      ])
    }
    
  } catch (error) {
    console.error('用户信息加载失败:', error)
    ElMessage.error('用户信息加载失败')
  } finally {
    loading.value = false
  }
}

// 加载基础用户统计（备用方案）
const loadBasicUserStats = async () => {
  try {
    const [questionsRes, answersRes] = await Promise.allSettled([
      api.get(`/users/${userId.value}/questions`),
      api.get(`/users/${userId.value}/answers`)
    ])
    
    const questionsCount = questionsRes.status === 'fulfilled' ? 
      questionsRes.value.data.totalElements || questionsRes.value.data.length || 0 : 0
    const answersCount = answersRes.status === 'fulfilled' ? 
      answersRes.value.data.totalElements || answersRes.value.data.length || 0 : 0
    
    userStats.value = {
      totalQuestions: questionsCount,
      totalAnswers: answersCount,
      acceptedAnswers: Math.floor(answersCount * 0.3),
      reputation: userInfo.value?.reputation || 0,
      badgesCount: userBadges.value?.length || 0
    }
  } catch (error) {
    console.error('基础统计计算失败:', error)
  }
}

// 生成模拟徽章数据
const generateMockBadges = (reputation: number) => {
  const badges = []
  
  if (reputation >= 100) {
    badges.push({
      id: 1,
      name: '青铜贡献者',
      description: '获得100声望值',
      level: 'BRONZE',
      earnedAt: '2024-01-15'
    })
  }
  
  if (reputation >= 500) {
    badges.push({
      id: 2,
      name: '白银专家',
      description: '获得500声望值',
      level: 'SILVER',
      earnedAt: '2024-02-20'
    })
  }
  
  if (reputation >= 1000) {
    badges.push({
      id: 3,
      name: '黄金大师',
      description: '获得1000声望值',
      level: 'GOLD',
      earnedAt: '2024-03-10'
    })
  }
  
  if (reputation >= 5000) {
    badges.push({
      id: 4,
      name: '白金传奇',
      description: '获得5000声望值',
      level: 'PLATINUM',
      earnedAt: '2024-04-05'
    })
  }
  
  return badges
}

// 加载用户的问题
const loadUserQuestions = async () => {
  console.log('🔄 开始加载用户问题...')
  try {
    const response = await api.get(`/users/${userId.value}/questions`, {
      params: { 
        page: 0,
        size: 20,
        sortBy: questionSort.value === 'newest' ? 'createdAt' : 
                questionSort.value === 'oldest' ? 'createdAt' : 'viewCount',
        sortDirection: questionSort.value === 'oldest' ? 'asc' : 'desc'
      }
    })
    
    let questions = []
    if (response.data.content && Array.isArray(response.data.content)) {
      questions = response.data.content
    } else if (Array.isArray(response.data)) {
      questions = response.data
    }
    
    // 处理数据映射，添加votes和views字段
    userQuestions.value = questions.map((question: any) => ({
      ...question,
      votes: 0, // 暂时设为0，后续可以添加投票功能
      views: question.viewCount || 0
    }))
    
    console.log('✅ 用户问题加载成功:', userQuestions.value.length, '个问题')
  } catch (error) {
    console.error('❌ 加载用户问题失败:', error)
    userQuestions.value = []
    ElMessage.error('加载用户问题失败')
  }
}

// 加载用户的回答
const loadUserAnswers = async () => {
  console.log('🔄 开始加载用户回答...')
  try {
    const response = await api.get(`/users/${userId.value}/answers`, {
      params: { 
        page: 0,
        size: 20,
        sortBy: answerSort.value === 'newest' ? 'createdAt' : 
                answerSort.value === 'oldest' ? 'createdAt' : 'voteCount',
        sortDirection: answerSort.value === 'oldest' ? 'asc' : 'desc'
      }
    })
    
    let rawAnswers = []
    if (response.data.content && Array.isArray(response.data.content)) {
      rawAnswers = response.data.content
    } else if (Array.isArray(response.data)) {
      rawAnswers = response.data
    } else {
      rawAnswers = []
    }
    
    // 处理回答数据，确保包含问题信息和采纳状态
    userAnswers.value = rawAnswers.map((answer: any) => ({
      ...answer,
      votes: answer.voteCount || 0,
      isAccepted: answer.accepted || false,  // 正确映射采纳状态
      question: answer.question || {
        id: answer.questionId,
        title: answer.questionTitle || '未知问题'
      },
      questionId: answer.question?.id || answer.questionId
    }))
    
    console.log('✅ 用户回答加载成功:', userAnswers.value.length, '个回答')
    console.log('📝 回答详情:', userAnswers.value.map(a => ({
      id: a.id,
      content: a.content.substring(0, 30) + '...',
      questionTitle: a.question?.title,
      accepted: a.isAccepted
    })))
  } catch (error) {
    console.error('❌ 加载用户回答失败:', error)
    userAnswers.value = []
    ElMessage.error('加载用户回答失败')
  }
}

// 加载关注列表
const loadUserFollowing = async () => {
  console.log('🔄 开始加载关注列表...')
  try {
    const response = await api.get(`/users/${userId.value}/following`, {
      params: { page: 0, size: 50 }
    })
    
    // 处理分页数据格式
    if (response.data.content && Array.isArray(response.data.content)) {
      followingUsers.value = response.data.content
    } else if (Array.isArray(response.data)) {
      followingUsers.value = response.data
    } else {
      followingUsers.value = []
    }
    
    console.log('✅ 关注列表加载成功:', followingUsers.value.length, '个用户')
    console.log('📝 关注用户:', followingUsers.value.map(u => u.username))
  } catch (error) {
    console.error('❌ 加载关注列表失败:', error)
    followingUsers.value = []
    ElMessage.error('加载关注列表失败')
  }
}

// 加载粉丝列表
const loadUserFollowers = async () => {
  console.log('🔄 开始加载粉丝列表...')
  try {
    const response = await api.get(`/users/${userId.value}/followers`, {
      params: { page: 0, size: 50 }
    })
    
    // 处理分页数据格式
    if (response.data.content && Array.isArray(response.data.content)) {
      followers.value = response.data.content
    } else if (Array.isArray(response.data)) {
      followers.value = response.data
    } else {
      followers.value = []
    }
    
    console.log('✅ 粉丝列表加载成功:', followers.value.length, '个用户')
    console.log('📝 粉丝用户:', followers.value.map(u => u.username))
  } catch (error) {
    console.error('❌ 加载粉丝列表失败:', error)
    followers.value = []
    ElMessage.error('加载粉丝列表失败')
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  try {
    // 如果没有登录用户，暂时模拟登录状态进行测试
    if (!authStore.isAuthenticated) {
      console.log('⚠️ 用户未登录，模拟登录状态用于测试关注功能')
      const mockUser = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        role: 'USER',
        reputation: 0,
        createdAt: new Date().toISOString()
      }
      localStorage.setItem('user', JSON.stringify(mockUser))
      localStorage.setItem('token', 'mock-token-123')
      authStore.user = mockUser
      authStore.token = 'mock-token-123'
    }
    
    const response = await api.get(`/users/${userId.value}/follow-status`)
    isFollowing.value = response.data.isFollowing
  } catch (error) {
    console.error('检查关注状态失败:', error)
  }
}

// 切换关注状态
const toggleFollow = async () => {
  followLoading.value = true
  try {
    // 如果没有登录用户，暂时模拟登录状态进行测试
    if (!authStore.isAuthenticated) {
      console.log('⚠️ 用户未登录，模拟登录状态用于测试关注功能')
      const mockUser = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        role: 'USER',
        reputation: 0,
        createdAt: new Date().toISOString()
      }
      localStorage.setItem('user', JSON.stringify(mockUser))
      localStorage.setItem('token', 'mock-token-123')
      authStore.user = mockUser
      authStore.token = 'mock-token-123'
    }
    
    if (isFollowing.value) {
      await api.delete(`/users/${userId.value}/follow`)
      isFollowing.value = false
      userInfo.value.followersCount--
      ElMessage.success('已取消关注')
    } else {
      await api.post(`/users/${userId.value}/follow`)
      isFollowing.value = true
      userInfo.value.followersCount++
      ElMessage.success('关注成功')
    }
  } catch (error: any) {
    console.error('关注操作失败:', error)
    
    // 显示更详细的错误信息
    if (error.response?.data?.message) {
      ElMessage.error(`操作失败: ${error.response.data.message}`)
    } else if (error.message) {
      ElMessage.error(`操作失败: ${error.message}`)
    } else {
      ElMessage.error('操作失败')
    }
  } finally {
    followLoading.value = false
  }
}

// 发送私信
const sendMessage = () => {
  router.push(`/messages/new?to=${userId.value}`)
}

// 保存个人资料
const saveProfile = async () => {
  saving.value = true
  try {
    const updateData = {
      bio: editForm.bio,
      website: editForm.website,
      location: editForm.location
    }
    
    const response = await api.put(`/users/${userId.value}/profile`, updateData)
    
    if (response.data.success) {
      // 更新本地数据
      Object.assign(userInfo.value, updateData)
      
      showEditDialog.value = false
      ElMessage.success('资料更新成功')
      
      // 更新本地存储的用户信息
      if (authStore.user && authStore.user.id === userId.value) {
        Object.assign(authStore.user, updateData)
        localStorage.setItem('user', JSON.stringify(authStore.user))
      }
    } else {
      ElMessage.error(response.data.message || '资料更新失败')
    }
  } catch (error: any) {
    console.error('保存资料失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

// 头像上传相关
const handleAvatarClick = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  // 验证文件类型和大小
  const isValidType = ['image/jpeg', 'image/png'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return
  }
  
  // 保存文件引用以便上传
  editForm.avatarFile = file
  
  // 创建预览URL
  const reader = new FileReader()
  reader.onload = (e) => {
    editForm.avatar = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

const saveAvatar = async () => {
  saving.value = true
  try {
    if (!editForm.avatarFile) {
      ElMessage.error('请先选择头像文件')
      return
    }
    
    // 创建FormData上传文件
    const formData = new FormData()
    formData.append('file', editForm.avatarFile)
    
    // 上传头像到后端
    const response = await api.post(`/users/${userId.value}/avatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 更新用户信息
    if (response.data.success) {
      userInfo.value.avatar = response.data.url
      editForm.avatar = response.data.url
      showAvatarDialog.value = false
      ElMessage.success('头像更新成功')
      
      // 更新本地存储的用户信息
      if (authStore.user && authStore.user.id === userId.value) {
        authStore.user.avatar = response.data.url
        localStorage.setItem('user', JSON.stringify(authStore.user))
      }
    } else {
      ElMessage.error(response.data.message || '头像上传失败')
    }
  } catch (error: any) {
    console.error('保存头像失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('头像上传失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

// 标签页切换
const handleTabClick = (tab: any) => {
  console.log('🔄 标签页切换到:', tab.name)
  switch (tab.name) {
    case 'questions':
      loadUserQuestions()
      break
    case 'answers':
      loadUserAnswers()
      break
    case 'following':
      loadUserFollowing()
      break
    case 'followers':
      loadUserFollowers()
      break
  }
}

// 获取角色标签类型
const getRoleTagType = (role: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'MODERATOR': return 'warning'
    default: return 'info'
  }
}

// 获取角色标签文本
const getRoleLabel = (role: string) => {
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'MODERATOR': return '版主'
    default: return '用户'
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
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

// 徽章等级相关方法
const getBadgeLevelClass = (level: string) => {
  const levelClasses: Record<string, string> = {
    'BRONZE': 'badge-bronze',
    'SILVER': 'badge-silver',
    'GOLD': 'badge-gold',
    'PLATINUM': 'badge-platinum'
  }
  return levelClasses[level] || 'badge-bronze'
}

// 专家等级相关方法
const getExpertLevelClass = (level: string) => {
  const levelClasses: Record<string, string> = {
    'BEGINNER': 'expert-beginner',
    'INTERMEDIATE': 'expert-intermediate',
    'ADVANCED': 'expert-advanced',
    'EXPERT': 'expert-expert',
    'LEGEND': 'expert-legend'
  }
  return levelClasses[level] || 'expert-beginner'
}

const getExpertLevelName = (level: string) => {
  const levelNames: Record<string, string> = {
    'BEGINNER': '新手',
    'INTERMEDIATE': '中级',
    'ADVANCED': '高级',
    'EXPERT': '专家',
    'LEGEND': '传奇'
  }
  return levelNames[level] || '新手'
}

const getExpertLevelDescription = (level: string) => {
  const descriptions: Record<string, string> = {
    'BEGINNER': '刚刚起步的知识探索者',
    'INTERMEDIATE': '具有一定经验的学习者',
    'ADVANCED': '知识丰富的实践者',
    'EXPERT': '领域内的专业人士',
    'LEGEND': '社区的传奇人物'
  }
  return descriptions[level] || '刚刚起步的知识探索者'
}

const getNextLevelProgress = () => {
  const level = userStats.value.expertLevel || 'BEGINNER'
  const currentReputation = userStats.value.reputation || 0
  
  const thresholds: Record<string, number> = {
    'BEGINNER': 0,
    'INTERMEDIATE': 100,
    'ADVANCED': 500,
    'EXPERT': 2000,
    'LEGEND': 10000
  }
  
  const currentThreshold = thresholds[level] || 0
  const nextLevels = Object.keys(thresholds)
  const currentIndex = nextLevels.indexOf(level)
  
  if (currentIndex === nextLevels.length - 1) {
    return 100 // 已达到最高等级
  }
  
  const nextLevel = nextLevels[currentIndex + 1]
  const nextThreshold = thresholds[nextLevel] || 0
  
  const progress = Math.min(100, (currentReputation / nextThreshold) * 100)
  return Math.round(progress)
}

const getNextLevelPointsNeeded = () => {
  const level = userStats.value.expertLevel || 'BEGINNER'
  const currentReputation = userStats.value.reputation || 0
  
  const thresholds: Record<string, number> = {
    'BEGINNER': 0,
    'INTERMEDIATE': 100,
    'ADVANCED': 500,
    'EXPERT': 2000,
    'LEGEND': 10000
  }
  
  const nextLevels = Object.keys(thresholds)
  const currentIndex = nextLevels.indexOf(level)
  
  if (currentIndex === nextLevels.length - 1) {
    return 0 // 已达到最高等级
  }
  
  const nextLevel = nextLevels[currentIndex + 1]
  const nextThreshold = thresholds[nextLevel] || 0
  
  return Math.max(0, nextThreshold - currentReputation)
}

// 签到相关方法
const loadCheckinData = async () => {
  if (!isOwnProfile.value) return
  
  try {
    const response = await api.get(`/users/${userId.value}/checkin/status`)
    checkinData.value = response.data
  } catch (error) {
    console.error('加载签到状态失败:', error)
    checkinData.value = { hasCheckedInToday: false }
  }
}

const loadCheckinStats = async () => {
  if (!isOwnProfile.value) return
  
  try {
    const response = await api.get(`/users/${userId.value}/checkin/stats`)
    checkinStats.value = response.data.data || response.data
  } catch (error) {
    console.error('加载签到统计失败:', error)
    checkinStats.value = {
      totalDays: 0,
      consecutiveDays: 0,
      totalReputation: 0
    }
  }
}

const handleCheckin = async () => {
  checkinLoading.value = true
  try {
    const response = await api.post('/users/checkin')
    
    if (response.data.success) {
      checkinData.value.hasCheckedInToday = true
      
      // 更新签到统计
      checkinStats.value.consecutiveDays = response.data.consecutiveDays
      checkinStats.value.totalDays = (checkinStats.value.totalDays || 0) + 1
      checkinStats.value.totalReputation = (checkinStats.value.totalReputation || 0) + response.data.reputationEarned
      
      // 更新用户声望
      userInfo.value.reputation = response.data.totalReputation
      userStats.value.reputation = response.data.totalReputation
      
      // 重新加载签到数据确保同步
      await Promise.all([
        loadCheckinData(),
        loadCheckinStats()
      ])
      
      ElMessage.success(`签到成功！获得 ${response.data.reputationEarned} 声望值`)
    } else {
      ElMessage.warning(response.data.message || '签到失败')
    }
  } catch (error: any) {
    console.error('签到失败:', error)
    
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('签到失败，请稍后重试')
    }
  } finally {
    checkinLoading.value = false
  }
}

// 监听路由变化
watch(() => route.params.id, () => {
  if (route.name === 'UserProfile') {
    loadUserProfile()
  }
}, { immediate: true })

// 组件挂载时初始化
onMounted(() => {
  console.log('🚀 个人主页组件挂载，用户ID:', userId.value)
  loadUserProfile()
})
</script>

<style scoped>
.user-profile-page {
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

.user-info-card {
  margin-bottom: 30px;
}

.user-header {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.user-avatar-section {
  position: relative;
  flex-shrink: 0;
}

.edit-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
}

.user-details {
  flex: 1;
}

.user-name-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.username {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.role-tag {
  font-size: 12px;
}

.user-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.user-bio {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
  max-width: 600px;
}

.profile-actions,
.follow-actions {
  display: flex;
  gap: 10px;
}

.user-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.content-card {
  margin-bottom: 30px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tab-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.questions-list,
.answers-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.question-item,
.answer-item {
  display: flex;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-item:hover,
.answer-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.question-content,
.answer-content {
  flex: 1;
}

.question-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.question-preview,
.answer-preview {
  color: #666;
  line-height: 1.5;
  margin-bottom: 15px;
}

.question-tags {
  margin-bottom: 10px;
}

.tag-item {
  margin-right: 8px;
}

.question-meta,
.answer-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.question-stats,
.answer-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
  flex-shrink: 0;
  min-width: 80px;
}

.stat {
  text-align: center;
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
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.user-card .user-info {
  flex: 1;
}

.user-card .username {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.user-card .reputation {
  color: #666;
  font-size: 14px;
}

.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar-tip {
  margin-top: 10px;
  color: #666;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .user-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 20px;
  }
  
  .user-stats {
    grid-template-columns: repeat(3, 1fr);
    gap: 15px;
  }
  
  .question-item,
  .answer-item {
    flex-direction: column;
    gap: 15px;
  }
  
  .question-stats,
  .answer-stats {
    flex-direction: row;
    justify-content: center;
  }
  
  .users-grid {
    grid-template-columns: 1fr;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

/* 徽章系统样式 */
.badges-card {
  margin-bottom: 30px;
}

.badges-card .card-header {
  font-weight: 600;
  color: #333;
}

.badges-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.badge-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 2px solid #f0f0f0;
  border-radius: 10px;
  transition: all 0.3s;
}

.badge-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.badge-bronze {
  border-color: #cd7f32;
  background: linear-gradient(135deg, #faf5f0, #f5e6d3);
}

.badge-silver {
  border-color: #c0c0c0;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.badge-gold {
  border-color: #ffd700;
  background: linear-gradient(135deg, #fff8dc, #f0e68c);
}

.badge-platinum {
  border-color: #e5e4e2;
  background: linear-gradient(135deg, #f5f5f5, #d3d3d3);
}

.badge-emoji {
  font-size: 30px;
  flex-shrink: 0;
}

.badge-info {
  flex: 1;
}

.badge-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.badge-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.badge-date {
  color: #999;
  font-size: 12px;
}

.badge-level {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  background: #f0f0f0;
  color: #666;
}

/* 分析卡片样式 */
.analytics-card {
  height: 100%;
}

.analytics-card .el-card__header {
  font-weight: 600;
  color: #333;
}

.contribution-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.contribution-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contribution-label {
  color: #333;
  font-weight: 500;
  flex: 1;
}

.contribution-value {
  color: #409eff;
  font-weight: 600;
  flex: 1;
  text-align: right;
}

/* 专家等级样式 */
.expert-analysis {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.expert-level {
  text-align: center;
}

.level-badge {
  display: inline-block;
  padding: 10px 20px;
  border-radius: 25px;
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 10px;
}

.expert-beginner {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
}

.expert-intermediate {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
}

.expert-advanced {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
  color: white;
}

.expert-expert {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  color: white;
}

.expert-legend {
  background: linear-gradient(135deg, #9c27b0, #ba68c8);
  color: white;
}

.level-description {
  color: #666;
  font-size: 14px;
}

.expert-progress {
  text-align: center;
}

.progress-label {
  color: #333;
  margin-bottom: 10px;
  font-weight: 500;
}

.progress-info {
  color: #666;
  font-size: 14px;
  margin-top: 5px;
}

.expert-score {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.score-label {
  color: #666;
  margin-bottom: 5px;
}

.score-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

/* 签到功能区域样式 */
.checkin-section {
  margin-top: 20px;
}

.checkin-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.checkin-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkin-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.checkin-stat-item {
  text-align: center;
}

.checkin-stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.checkin-stat-label {
  color: #666;
  font-size: 14px;
}

.checkin-action {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.checkin-btn {
  margin-top: 10px;
}

.next-checkin-tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}
</style> 