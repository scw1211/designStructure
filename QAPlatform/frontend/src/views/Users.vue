<template>
  <div class="users-page">
    <div class="container">
      <div class="page-header">
        <h1>用户</h1>
        <p class="page-desc">发现社区中的优秀开发者和专家</p>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-input
              v-model="searchQuery"
              placeholder="搜索用户..."
              size="large"
              @input="filterUsers"
              class="search-input"
            >
              <template #prepend>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              size="large"
              @change="sortUsers"
              style="width: 100%"
            >
              <el-option label="声望值" value="reputation" />
              <el-option label="最新注册" value="newest" />
              <el-option label="最活跃" value="active" />
              <el-option label="问题最多" value="questions" />
              <el-option label="回答最多" value="answers" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="roleFilter"
              placeholder="用户角色"
              size="large"
              @change="filterUsers"
              style="width: 100%"
            >
              <el-option label="全部用户" value="" />
              <el-option label="普通用户" value="USER" />
              <el-option label="版主" value="MODERATOR" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 用户统计 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ expertUsers.length }}</div>
                <div class="stat-label">专家用户</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ onlineUsers }}</div>
                <div class="stat-label">在线用户</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ newUsersToday }}</div>
                <div class="stat-label">今日新用户</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ activeUsersWeek }}</div>
                <div class="stat-label">周活跃用户</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ expertUsers.length }}</div>
                <div class="stat-label">专家用户</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 专家用户排行榜 -->
      <div class="expert-section" v-if="expertUsers.length > 0">
        <div class="expert-header">
          <div class="expert-title">
            <div class="title-icon">👑</div>
            <h2>专家用户排行榜</h2>
          </div>
          <p class="expert-subtitle">基于回答质量、采纳率和专业水平评选的顶级专家</p>
        </div>
        
        <!-- 前三名特殊展示 -->
        <div class="podium-section" v-if="expertUsers.length >= 3">
          <div class="podium-container">
            <!-- 第二名 -->
            <div class="podium-item rank-2" @click="goToUserProfile(expertUsers[1].id)">
              <div class="rank-crown">🥈</div>
              <el-avatar :src="expertUsers[1].avatar" :size="80" class="expert-avatar">
                {{ expertUsers[1].username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="expert-name">{{ expertUsers[1].username }}</div>
              <div class="expert-level expert-expert">{{ getExpertLevelName(expertUsers[1].level) }}</div>
              <div class="expert-score">{{ expertUsers[1].expertScore.toFixed(1) }}</div>
              <div class="achievement-cards">
                <div class="achievement-card">
                  <div class="achievement-number">{{ expertUsers[1].totalAnswers }}</div>
                  <div class="achievement-label">回答</div>
                </div>
                <div class="achievement-card">
                  <div class="achievement-number">{{ expertUsers[1].acceptedAnswers }}</div>
                  <div class="achievement-label">采纳</div>
                </div>
                <div class="achievement-card">
                  <div class="achievement-number">{{ (expertUsers[1].acceptanceRate || 0).toFixed(1) }}%</div>
                  <div class="achievement-label">采纳率</div>
                </div>
              </div>
            </div>

            <!-- 第一名 -->
            <div class="podium-item rank-1" @click="goToUserProfile(expertUsers[0].id)">
              <div class="rank-crown champion">👑</div>
              <el-avatar :src="expertUsers[0].avatar" :size="100" class="expert-avatar champion">
                {{ expertUsers[0].username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="expert-name champion">{{ expertUsers[0].username }}</div>
              <div class="expert-level expert-legend">{{ getExpertLevelName(expertUsers[0].level) }}</div>
              <div class="expert-score champion">{{ expertUsers[0].expertScore.toFixed(1) }}</div>
              <div class="achievement-cards">
                <div class="achievement-card">
                  <div class="achievement-number">{{ expertUsers[0].totalAnswers }}</div>
                  <div class="achievement-label">回答</div>
                </div>
                <div class="achievement-card">
                  <div class="achievement-number">{{ expertUsers[0].acceptedAnswers }}</div>
                  <div class="achievement-label">采纳</div>
                </div>
                <div class="achievement-card">
                  <div class="achievement-number">{{ (expertUsers[0].acceptanceRate || 0).toFixed(1) }}%</div>
                  <div class="achievement-label">采纳率</div>
                </div>
              </div>
            </div>

            <!-- 第三名 -->
            <div class="podium-item rank-3" @click="goToUserProfile(expertUsers[2].id)">
              <div class="rank-crown">🥉</div>
              <el-avatar :src="expertUsers[2].avatar" :size="80" class="expert-avatar">
                {{ expertUsers[2].username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="expert-name">{{ expertUsers[2].username }}</div>
              <div class="expert-level expert-advanced">{{ getExpertLevelName(expertUsers[2].level) }}</div>
              <div class="expert-score">{{ expertUsers[2].expertScore.toFixed(1) }}</div>
              <div class="achievement-cards">
                <div class="achievement-card">
                  <div class="achievement-number">{{ expertUsers[2].totalAnswers }}</div>
                  <div class="achievement-label">回答</div>
                </div>
                <div class="achievement-card">
                  <div class="achievement-number">{{ expertUsers[2].acceptedAnswers }}</div>
                  <div class="achievement-label">采纳</div>
                </div>
                <div class="achievement-card">
                  <div class="achievement-number">{{ (expertUsers[2].acceptanceRate || 0).toFixed(1) }}%</div>
                  <div class="achievement-label">采纳率</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 其他排名列表 -->
        <div class="ranking-list" v-if="expertUsers.length > 3">
          <div 
            v-for="(expert, index) in expertUsers.slice(3, 10)" 
            :key="expert.id"
            class="ranking-item"
            @click="goToUserProfile(expert.id)"
          >
            <div class="ranking-position">
              <span class="rank-number">{{ index + 4 }}</span>
            </div>
            <el-avatar :src="expert.avatar" :size="60" class="ranking-avatar">
              {{ expert.username.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="ranking-info">
              <div class="ranking-name">{{ expert.username }}</div>
              <div class="ranking-level" :class="getExpertLevelClass(expert.level)">
                {{ getExpertLevelName(expert.level) }}
              </div>
            </div>
            <div class="ranking-stats">
              <div class="stat-group">
                <span class="stat-value">{{ expert.expertScore.toFixed(1) }}</span>
                <span class="stat-label">专家评分</span>
              </div>
              <div class="stat-group">
                <span class="stat-value">{{ expert.totalAnswers }}</span>
                <span class="stat-label">回答</span>
              </div>
              <div class="stat-group">
                <span class="stat-value">{{ (expert.acceptanceRate || 0).toFixed(1) }}%</span>
                <span class="stat-label">采纳率</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="users-section">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="6" animated />
        </div>
        
        <div v-else-if="filteredUsers.length === 0" class="empty-state">
          <el-empty description="未找到相关用户" />
        </div>
        
        <div v-else class="users-grid">
          <el-card 
            v-for="user in paginatedUsers" 
            :key="user.id" 
            class="user-card"
            @click="goToUserProfile(user.id)"
          >
            <div class="user-content">
              <div class="user-header">
                <el-avatar :src="user.avatar" :size="60">
                  {{ user.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="user-info">
                  <div class="username-section">
                    <span class="username">{{ user.username }}</span>
                    <el-tag :type="getRoleTagType(user.role)" size="small">
                      {{ getRoleLabel(user.role) }}
                    </el-tag>
                    <el-tag v-if="user.isOnline" type="success" size="small" effect="plain">
                      在线
                    </el-tag>
                  </div>
                  <div class="user-meta">
                    <span class="reputation">声望: {{ user.reputation }}</span>
                    <span class="join-date">{{ formatDate(user.createdAt) }}加入</span>
                  </div>
                </div>
              </div>
              
              <div class="user-bio" v-if="user.bio">
                {{ user.bio }}
              </div>
              
              <div class="user-stats">
                <div class="stat-item">
                  <span class="number">{{ user.questionCount }}</span>
                  <span class="label">问题</span>
                </div>
                <div class="stat-item">
                  <span class="number">{{ user.answerCount }}</span>
                  <span class="label">回答</span>
                </div>
                <div class="stat-item">
                  <span class="number">{{ user.acceptedAnswers }}</span>
                  <span class="label">采纳</span>
                </div>
                <div class="stat-item">
                  <span class="number">{{ user.followersCount }}</span>
                  <span class="label">粉丝</span>
                </div>
              </div>
              
              <div class="user-tags" v-if="user.topTags && user.topTags.length > 0">
                <el-tag 
                  v-for="tag in user.topTags.slice(0, 3)" 
                  :key="tag.id" 
                  size="small" 
                  class="user-tag"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div v-if="filteredUsers.length > pageSize" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredUsers.length"
            layout="total, prev, pager, next, jumper"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const sortBy = ref('reputation')
const roleFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const users = ref<any[]>([])
const expertUsers = ref<any[]>([])
const userOverview = ref<any>({})
const onlineUsers = ref(0)

// 统计数据
const totalUsers = computed(() => users.value.length)
const newUsersToday = computed(() => {
  const today = new Date()
  return users.value.filter(u => {
    const createdDate = new Date(u.createdAt)
    return createdDate.toDateString() === today.toDateString()
  }).length
})
const activeUsersWeek = computed(() => {
  const weekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
  return users.value.filter(u => {
    const lastActiveDate = new Date(u.lastActiveAt || u.updatedAt)
    return lastActiveDate > weekAgo
  }).length
})

// 过滤和排序用户
const filteredUsers = computed(() => {
  let result = [...users.value]

  // 角色过滤
  if (roleFilter.value) {
    result = result.filter(user => user.role === roleFilter.value)
  }

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(user => 
      user.username.toLowerCase().includes(query) ||
      (user.bio && user.bio.toLowerCase().includes(query))
    )
  }

  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'reputation':
        return b.reputation - a.reputation
      case 'newest':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'active':
        return new Date(b.lastActiveAt).getTime() - new Date(a.lastActiveAt).getTime()
      case 'questions':
        return b.questionCount - a.questionCount
      case 'answers':
        return b.answerCount - a.answerCount
      default:
        return 0
    }
  })

  return result
})

// 分页用户
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// 加载用户数据
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await api.get('/users')
    // 处理后端返回的分页数据
    if (response.data.content) {
      users.value = response.data.content
    } else if (Array.isArray(response.data)) {
      users.value = response.data
    } else {
      users.value = []
    }
    console.log('✅ 用户数据加载成功:', users.value)
  } catch (error) {
    console.error('❌ 加载用户失败，使用默认值:', error)
    users.value = []
  } finally {
    loading.value = false
  }
}

// 过滤用户
const filterUsers = () => {
  currentPage.value = 1
}

// 排序用户
const sortUsers = () => {
  currentPage.value = 1
}

// 分页切换
const handlePageChange = (page: number) => {
  currentPage.value = page
}

// 跳转到用户主页
const goToUserProfile = (userId: number) => {
  router.push(`/user/${userId}`)
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

// 加载专家用户
const loadExpertUsers = async () => {
  try {
    const response = await api.get('/analytics/expert-users', {
      params: { limit: 3 }
    })
    expertUsers.value = response.data.map((user: any) => ({
      ...user,
      level: user.reputation >= 5000 ? 'LEGEND' : user.reputation >= 1000 ? 'EXPERT' : 'ADVANCED',
      expertScore: user.reputation || 0
    }))
    console.log('✅ 专家用户加载成功:', expertUsers.value)
  } catch (error) {
    console.error('❌ 专家用户API失败，使用基础API:', error)
    // 降级到基础API
    await loadExpertUsersFromBasicAPI()
  }
}

// 从基础API加载专家用户（降级方案）
const loadExpertUsersFromBasicAPI = async () => {
  try {
    const response = await api.get('/users')
    const users = response.data.content || response.data || []
    expertUsers.value = users
      .filter((user: any) => (user.reputation || 0) >= 200) // 声望200以上算专家
      .sort((a: any, b: any) => (b.reputation || 0) - (a.reputation || 0))
      .slice(0, 3) // 只取前3名
      .map((user: any) => ({
        ...user,
        level: user.reputation >= 5000 ? 'LEGEND' : user.reputation >= 1000 ? 'EXPERT' : 'ADVANCED',
        expertScore: user.reputation || 0
      }))
    console.log('✅ 专家用户基础API加载成功:', expertUsers.value)
  } catch (error) {
    console.error('❌ 专家用户基础API失败:', error)
    expertUsers.value = []
  }
}

// 专家等级相关方法
const getExpertLevelClass = (level: string) => {
  const levelClasses = {
    'BEGINNER': 'expert-beginner',
    'INTERMEDIATE': 'expert-intermediate',
    'ADVANCED': 'expert-advanced',
    'EXPERT': 'expert-expert',
    'LEGEND': 'expert-legend'
  }
  return levelClasses[level as keyof typeof levelClasses] || 'expert-beginner'
}

const getExpertLevelName = (level: string) => {
  const levelNames = {
    'BEGINNER': '新手',
    'INTERMEDIATE': '进阶',
    'ADVANCED': '高级',
    'EXPERT': '专家',
    'LEGEND': '传奇'
  }
  return levelNames[level as keyof typeof levelNames] || '新手'
}

// 计算用户统计概览
const calculateUserOverview = async () => {
  try {
    const response = await api.get('/users')
    const users = response.data.content || response.data || []
    
    userOverview.value = {
      totalUsers: users.length,
      activeUsers: Math.floor(users.length * 0.3), // 估算30%活跃用户
      newUsersThisWeek: Math.floor(users.length * 0.05), // 估算5%本周新用户
      expertUsers: users.filter((u: any) => (u.reputation || 0) >= 200).length,
      averageReputation: users.length > 0 ? 
        Math.floor(users.reduce((sum: number, u: any) => sum + (u.reputation || 0), 0) / users.length) : 0
    }
    console.log('✅ 用户统计概览计算成功:', userOverview.value)
  } catch (error) {
    console.error('❌ 用户统计概览计算失败:', error)
    userOverview.value = {
      totalUsers: 0,
      activeUsers: 0,
      newUsersThisWeek: 0,
      expertUsers: 0,
      averageReputation: 0
    }
  }
}

// 加载在线用户统计
const loadOnlineStats = async () => {
  try {
    const response = await api.get('/users/online-stats')
    onlineUsers.value = response.data.onlineCount || 0
    console.log('✅ 在线用户统计加载成功:', response.data)
  } catch (error) {
    console.error('❌ 在线用户统计API失败:', error)
    // 降级到基于isOnline字段的统计
    onlineUsers.value = users.value.filter(u => u.isOnline).length
  }
}

// 初始化数据加载
onMounted(async () => {
  await Promise.all([
    loadUsers(),
    loadExpertUsers(),
    calculateUserOverview(),
    loadOnlineStats()
  ])
})
</script>

<style lang="scss" scoped>
.users-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  color: white;
  
  h1 {
    font-size: 32px;
    margin-bottom: 10px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  }
  
  .page-desc {
    font-size: 16px;
    opacity: 0.9;
  }
}

.search-section {
  margin-bottom: 30px;
}

.stats-section {
  margin-bottom: 30px;
  
  .stat-card {
    text-align: center;
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    }
    
    .stat-content {
      .stat-number {
        font-size: 28px;
        font-weight: bold;
        color: #FFD700;
        margin-bottom: 8px;
        text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
      }
      
      .stat-label {
        color: rgba(255, 255, 255, 0.9);
        font-size: 14px;
      }
    }
  }
}

/* 专家用户排行榜样式 */
.expert-section {
  margin-bottom: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  
  .expert-header {
    text-align: center;
    margin-bottom: 40px;
    
    .expert-title {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 15px;
      margin-bottom: 10px;
      
      .title-icon {
        font-size: 40px;
        animation: bounce 2s infinite;
      }
      
      h2 {
        font-size: 32px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin: 0;
        font-weight: 700;
      }
    }
    
    .expert-subtitle {
      color: #666;
      font-size: 16px;
      margin: 0;
    }
  }
  
  /* 前三名奖台 */
  .podium-section {
    margin-bottom: 40px;
    
    .podium-container {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      gap: 30px;
      max-width: 800px;
      margin: 0 auto;
      
      .podium-item {
        background: linear-gradient(145deg, #ffffff, #f0f0f0);
        border-radius: 20px;
        padding: 30px 20px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;
        position: relative;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        
        &:hover {
          transform: translateY(-10px) scale(1.02);
          box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
        }
        
        &.rank-1 {
          order: 2;
          background: linear-gradient(145deg, #FFD700, #FFA500);
          color: white;
          transform: translateY(-20px) scale(1.05);
          
          &:hover {
            transform: translateY(-30px) scale(1.08);
          }
        }
        
        &.rank-2 {
          order: 1;
          background: linear-gradient(145deg, #C0C0C0, #A8A8A8);
          color: white;
        }
        
        &.rank-3 {
          order: 3;
          background: linear-gradient(145deg, #CD7F32, #B8860B);
          color: white;
        }
        
        .rank-crown {
          font-size: 50px;
          margin-bottom: 15px;
          display: block;
          
          &.champion {
            font-size: 60px;
            animation: glow 2s ease-in-out infinite alternate;
          }
        }
        
        .expert-avatar {
          margin-bottom: 15px;
          border: 4px solid rgba(255, 255, 255, 0.3);
          
          &.champion {
            border: 6px solid #FFD700;
            box-shadow: 0 0 30px rgba(255, 215, 0, 0.5);
          }
        }
        
        .expert-name {
          font-size: 18px;
          font-weight: 700;
          margin-bottom: 8px;
          
          &.champion {
            font-size: 22px;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
          }
        }
        
        .expert-level {
          font-size: 12px;
          padding: 4px 12px;
          border-radius: 20px;
          margin-bottom: 10px;
          font-weight: 600;
          display: inline-block;
        }
        
        .expert-score {
          font-size: 24px;
          font-weight: 800;
          margin-bottom: 20px;
          
          &.champion {
            font-size: 28px;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
          }
        }
        
        .achievement-cards {
          display: flex;
          justify-content: space-around;
          gap: 8px;
          
          .achievement-card {
            background: rgba(255, 255, 255, 0.2);
            border-radius: 10px;
            padding: 8px 6px;
            flex: 1;
            min-width: 0;
            
            .achievement-number {
              font-size: 16px;
              font-weight: 700;
              margin-bottom: 2px;
            }
            
            .achievement-label {
              font-size: 10px;
              opacity: 0.8;
            }
          }
        }
      }
    }
  }
  
  /* 排名列表 */
  .ranking-list {
    .ranking-item {
      display: flex;
      align-items: center;
      padding: 20px;
      margin-bottom: 15px;
      background: linear-gradient(135deg, #f8f9fa, #e9ecef);
      border-radius: 15px;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 2px solid transparent;
      
      &:hover {
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
        transform: translateX(10px);
        box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
      }
      
      .ranking-position {
        width: 60px;
        text-align: center;
        margin-right: 20px;
        
        .rank-number {
          font-size: 24px;
          font-weight: 800;
          color: #667eea;
          background: rgba(102, 126, 234, 0.1);
          width: 45px;
          height: 45px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0 auto;
        }
      }
      
      .ranking-avatar {
        margin-right: 20px;
        border: 3px solid rgba(102, 126, 234, 0.2);
      }
      
      .ranking-info {
        flex: 1;
        
        .ranking-name {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 5px;
        }
        
        .ranking-level {
          font-size: 12px;
          padding: 3px 10px;
          border-radius: 15px;
          font-weight: 600;
          display: inline-block;
        }
      }
      
      .ranking-stats {
        display: flex;
        gap: 20px;
        
        .stat-group {
          text-align: center;
          
          .stat-value {
            display: block;
            font-size: 16px;
            font-weight: 700;
            color: #667eea;
            margin-bottom: 2px;
          }
          
          .stat-label {
            font-size: 12px;
            color: #666;
          }
        }
      }
      
      &:hover {
        .ranking-position .rank-number {
          background: rgba(255, 255, 255, 0.2);
          color: white;
        }
        
        .ranking-stats .stat-value {
          color: #FFD700;
        }
        
        .ranking-stats .stat-label {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }
}

/* 专家等级颜色 */
.expert-beginner {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
}

.expert-intermediate {
  background: linear-gradient(135deg, #17a2b8, #007bff);
  color: white;
}

.expert-advanced {
  background: linear-gradient(135deg, #ffc107, #fd7e14);
  color: white;
}

.expert-expert {
  background: linear-gradient(135deg, #dc3545, #e83e8c);
  color: white;
}

.expert-legend {
  background: linear-gradient(135deg, #6f42c1, #e83e8c);
  color: white;
}

/* 动画效果 */
@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes glow {
  from {
    text-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
  }
  to {
    text-shadow: 0 0 30px rgba(255, 215, 0, 0.8);
  }
}

.users-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  backdrop-filter: blur(10px);
  
  .loading-container {
    margin: 40px 0;
  }
  
  .empty-state {
    text-align: center;
    margin: 40px 0;
  }
  
  .users-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
  }
  
  .user-card {
    cursor: pointer;
    transition: all 0.3s ease;
    border-radius: 15px;
    
    &:hover {
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
      transform: translateY(-5px);
    }
    
    .user-content {
      .user-header {
        display: flex;
        gap: 15px;
        margin-bottom: 15px;
        
        .user-info {
          flex: 1;
          
          .username-section {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 5px;
            
            .username {
              font-size: 18px;
              font-weight: 600;
              color: #333;
            }
          }
          
          .user-meta {
            display: flex;
            flex-direction: column;
            gap: 2px;
            font-size: 12px;
            color: #666;
            
            .reputation {
              color: #409eff;
              font-weight: 500;
            }
          }
        }
      }
      
      .user-bio {
        color: #666;
        line-height: 1.5;
        margin-bottom: 15px;
        font-size: 14px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .user-stats {
        display: flex;
        justify-content: space-between;
        margin-bottom: 15px;
        
        .stat-item {
          text-align: center;
          
          .number {
            display: block;
            font-size: 16px;
            font-weight: bold;
            color: #333;
          }
          
          .label {
            display: block;
            font-size: 12px;
            color: #666;
          }
        }
      }
      
      .user-tags {
        display: flex;
        gap: 5px;
        flex-wrap: wrap;
        
        .user-tag {
          font-size: 12px;
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
  
  .search-section .el-col {
    margin-bottom: 15px;
  }
  
  .stats-section .el-col {
    margin-bottom: 15px;
  }
  
  .users-grid {
    grid-template-columns: 1fr;
  }
  
  .podium-container {
    grid-template-columns: 1fr !important;
    gap: 20px !important;
    
    .podium-item {
      &.rank-1 {
        order: 1 !important;
        transform: none !important;
      }
      
      &.rank-2 {
        order: 2 !important;
      }
      
      &.rank-3 {
        order: 3 !important;
      }
    }
  }
  
  .ranking-item {
    flex-direction: column;
    text-align: center;
    
    .ranking-position {
      margin-right: 0;
      margin-bottom: 10px;
    }
    
    .ranking-avatar {
      margin-right: 0;
      margin-bottom: 15px;
    }
    
    .ranking-stats {
      justify-content: center;
      margin-top: 15px;
    }
  }
}
</style> 