<template>
  <div class="users-page">
    <div class="container">
      <div class="page-header">
        <h1>用户</h1>
        <p class="page-desc">发现社区中的开发者</p>
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
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ onlineUsers }}</div>
                <div class="stat-label">在线用户</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ newUsersToday }}</div>
                <div class="stat-label">今日新用户</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-number">{{ activeUsersWeek }}</div>
                <div class="stat-label">周活跃用户</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 用户列表 -->
      <div class="users-list">
        <div class="list-header">
          <h2>用户列表</h2>
          <div class="sort-options">
            <span>共 {{ filteredUsers.length }} 个用户</span>
          </div>
        </div>

        <div class="users-grid">
          <el-card 
            v-for="user in paginatedUsers" 
            :key="user.id" 
            class="user-card"
            @click="goToUserProfile(user.id)"
          >
            <div class="user-content">
              <div class="user-avatar">
                <el-avatar :src="user.avatar" :size="60">
                  {{ user.username.charAt(0).toUpperCase() }}
                </el-avatar>
              </div>
              
              <div class="user-info">
                <h3 class="user-name">{{ user.username }}</h3>
                <div class="user-role" :class="user.role.toLowerCase()">
                  {{ getRoleText(user.role) }}
                </div>
                <div class="user-bio" v-if="user.bio">{{ user.bio }}</div>
              </div>
              
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ user.reputation }}</span>
                  <span class="stat-label">声望</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ user.questionCount || 0 }}</span>
                  <span class="stat-label">问题</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ user.answerCount || 0 }}</span>
                  <span class="stat-label">回答</span>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredUsers.length"
            layout="prev, pager, next, total"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const sortBy = ref('reputation')
const roleFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(12)

const users = ref<any[]>([])
const totalUsers = ref(0)
const onlineUsers = ref(0)
const newUsersToday = ref(0)
const activeUsersWeek = ref(0)

// 计算属性
const filteredUsers = computed(() => {
  let filtered = users.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(user => 
      user.username.toLowerCase().includes(query) ||
      (user.bio && user.bio.toLowerCase().includes(query))
    )
  }

  // 角色过滤
  if (roleFilter.value) {
    filtered = filtered.filter(user => user.role === roleFilter.value)
  }

  return filtered
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// 方法
const loadUserStats = async () => {
  try {
    // 调用统计API获取今日新用户数据
    const response = await api.get('/stats')
    if (response.data && response.data.todayUsers !== undefined) {
      newUsersToday.value = response.data.todayUsers
    } else {
      newUsersToday.value = 0
    }
  } catch (error) {
    console.warn('获取用户统计数据失败:', error)
    newUsersToday.value = 0
  }
}

const loadUsers = async () => {
  try {
    const response = await api.get('/users')
    const usersData = response.data.content || response.data || []
    
    // 获取所有用户的ID
    const userIds = usersData.map((user: any) => user.id)
    
    // 批量获取用户统计信息
    const statsResponse = await api.post('/users/batch-stats', userIds)
    const statsMap = statsResponse.data
    
    // 合并用户数据和统计信息
    const usersWithStats = usersData.map((user: any) => {
      const stats = statsMap[user.id] || {}
      return {
        ...user,
        questionCount: stats.questionCount || 0,
        answerCount: stats.answerCount || 0,
        followersCount: stats.followerCount || 0,
        followingCount: stats.followingCount || 0,
        acceptedAnswers: stats.acceptedAnswers || 0
      }
    })
    
    users.value = usersWithStats
    totalUsers.value = users.value.length
    
    // 获取真实统计数据
    await loadUserStats()
    
    // 模拟部分数据（在线用户和周活跃用户）
    onlineUsers.value = Math.floor(totalUsers.value * 0.1)
    activeUsersWeek.value = Math.floor(totalUsers.value * 0.3)
  } catch (error) {
    console.error('Failed to load users:', error)
    ElMessage.error('加载用户列表失败')
  }
}

const filterUsers = () => {
  currentPage.value = 1
}

const sortUsers = () => {
  switch (sortBy.value) {
    case 'reputation':
      users.value.sort((a, b) => (b.reputation || 0) - (a.reputation || 0))
      break
    case 'newest':
      users.value.sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
      break
    case 'questions':
      users.value.sort((a, b) => (b.questionCount || 0) - (a.questionCount || 0))
      break
    case 'answers':
      users.value.sort((a, b) => (b.answerCount || 0) - (a.answerCount || 0))
      break
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

const goToUserProfile = (userId: number) => {
  router.push(`/user/${userId}`)
}

const getRoleText = (role: string) => {
  const roleMap = {
    'USER': '用户',
    'MODERATOR': '版主',
    'ADMIN': '管理员'
  }
  return roleMap[role as keyof typeof roleMap] || '用户'
}

// 生命周期
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
  color: #333;
}

.page-desc {
  color: #666;
  font-size: 1.1em;
}

.search-section {
  margin-bottom: 30px;
}

.stats-section {
  margin-bottom: 40px;
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

.stat-content {
  padding: 10px;
}

.stat-number {
  font-size: 2em;
  font-weight: bold;
  color: #409eff;
  display: block;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.users-list {
  margin-top: 30px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h2 {
  margin: 0;
  color: #333;
}

.sort-options {
  color: #666;
  font-size: 14px;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.user-card {
  cursor: pointer;
  transition: all 0.3s;
}

.user-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.user-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px;
}

.user-avatar {
  margin-bottom: 15px;
}

.user-info {
  margin-bottom: 20px;
  min-height: 80px;
}

.user-name {
  margin: 0 0 8px 0;
  font-size: 1.2em;
  font-weight: 600;
  color: #333;
}

.user-role {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 8px;
  display: inline-block;
}

.user-role.user {
  background: #e1f5fe;
  color: #01579b;
}

.user-role.moderator {
  background: #fff3e0;
  color: #e65100;
}

.user-role.admin {
  background: #fce4ec;
  color: #880e4f;
}

.user-bio {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  width: 100%;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.stat-item {
  text-align: center;
}

.stat-item .stat-number {
  display: block;
  font-size: 1.2em;
  font-weight: bold;
  color: #333;
  margin-bottom: 2px;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #666;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .users-grid {
    grid-template-columns: 1fr;
  }
  
  .search-section .el-row {
    flex-direction: column;
  }
  
  .search-section .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style> 