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
            </div>

            <div class="user-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(userInfo.createdAt) }} 加入
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
      </el-card>

      <!-- 标签页内容 -->
      <el-card class="content-card">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <!-- 问题 -->
          <el-tab-pane label="问题" name="questions">
            <div class="tab-header">
              <span class="tab-title">发布的问题 ({{ questionPagination.total }})</span>
            </div>
            
            <div v-if="userQuestions.length === 0" class="empty-state">
              <el-empty description="暂无发布的问题" />
            </div>
            
            <div v-else class="questions-list">
              <div
                v-for="question in userQuestions"
                :key="question.id"
                class="question-item"
                @click="goToQuestion(question.id)"
              >
                <div class="question-content">
                  <h4 class="question-title">{{ question.title }}</h4>
                  <p class="question-preview">{{ question.content.substring(0, 150) }}...</p>
                  <div class="question-tags">
                    <el-tag
                      v-for="tag in question.tags"
                      :key="tag.id"
                      size="small"
                      class="tag"
                    >
                      {{ tag.name }}
                    </el-tag>
                  </div>
                  <div class="question-time">
                    {{ formatTime(question.createdAt) }}
                  </div>
                </div>
                
                <div class="question-stats">
                  <div class="stat">
                    <span class="number">{{ question.viewCount || 0 }}</span>
                    <span class="label">浏览</span>
                  </div>
                  <div class="stat">
                    <span class="number">{{ question.answerCount || 0 }}</span>
                    <span class="label">回答</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="pagination-container" v-if="userQuestions.length > 0">
              <el-pagination
                v-model:current-page="questionPagination.page"
                :page-size="questionPagination.size"
                :total="questionPagination.total"
                layout="prev, pager, next, total"
                @current-change="handleQuestionPageChange"
              />
            </div>
          </el-tab-pane>

          <!-- 回答 -->
          <el-tab-pane label="回答" name="answers">
            <div class="tab-header">
              <span class="tab-title">发表的回答 ({{ answerPagination.total }})</span>
            </div>
            
            <div v-if="userAnswers.length === 0" class="empty-state">
              <el-empty description="暂无发表的回答" />
            </div>
            
            <div v-else class="answers-list">
              <div
                v-for="answer in userAnswers"
                :key="answer.id"
                class="answer-item"
                @click="goToQuestion(answer.questionId)"
              >
                <div class="answer-content">
                  <div class="question-title-ref">
                    回答了: {{ answer.questionTitle }}
                  </div>
                  <p class="answer-preview">{{ answer.content.substring(0, 200) }}...</p>
                  <div class="answer-time">
                    {{ formatTime(answer.createdAt) }}
                  </div>
                </div>
                
                <div class="answer-stats">
                  <div class="stat">
                    <span class="number">{{ answer.voteCount || 0 }}</span>
                    <span class="label">点赞</span>
                  </div>
                  <div class="stat" v-if="answer.accepted">
                    <el-tag type="success" size="small">已采纳</el-tag>
                  </div>
                </div>
              </div>
            </div>

            <div class="pagination-container" v-if="userAnswers.length > 0">
              <el-pagination
                v-model:current-page="answerPagination.page"
                :page-size="answerPagination.size"
                :total="answerPagination.total"
                layout="prev, pager, next, total"
                @current-change="handleAnswerPageChange"
              />
            </div>
          </el-tab-pane>

          <!-- 关注 -->
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
                @click="goToUserProfile(user.id)"
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
              <span class="tab-title">粉丝 ({{ followersList.length }})</span>
            </div>
            
            <div v-if="followersList.length === 0" class="empty-state">
              <el-empty description="暂无粉丝" />
            </div>
            
            <div v-else class="users-grid">
              <div
                v-for="user in followersList"
                :key="user.id"
                class="user-card"
                @click="goToUserProfile(user.id)"
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

          <!-- 草稿箱 -->
          <el-tab-pane v-if="isOwnProfile" label="草稿箱" name="drafts">
            <div class="tab-header">
              <span class="tab-title">草稿箱 ({{ userDrafts.length }})</span>
            </div>
            
            <div v-if="userDrafts.length === 0" class="empty-state">
              <el-empty description="暂无草稿" />
            </div>
            
            <div v-else class="drafts-list">
              <div
                v-for="draft in userDrafts"
                :key="draft.id"
                class="draft-item"
                @click="goToEditDraft(draft)"
              >
                <div class="draft-content">
                  <h4 class="draft-title">{{ draft.title }}</h4>
                  <p class="draft-preview">{{ draft.content.substring(0, 150) }}...</p>
                  <div class="draft-tags" v-if="draft.tags && draft.tags.length > 0">
                    <el-tag
                      v-for="tag in draft.tags"
                      :key="tag"
                      size="small"
                      class="tag"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                  <div class="draft-time">
                    {{ formatTime(draft.createdAt) }}
                  </div>
                </div>
                
                <div class="draft-stats">
                  <div class="stat" v-if="draft.bounty && draft.bounty > 0">
                    <span class="number">{{ draft.bounty }}</span>
                    <span class="label">悬赏</span>
                  </div>
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
  Edit,
  Check,
  Plus
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
const isFollowing = ref(false)

const userInfo = ref<any>({})
const userStats = ref<any>({})
const userQuestions = ref<any[]>([])
const userAnswers = ref<any[]>([])
const followingUsers = ref<any[]>([])
const followersList = ref<any[]>([])
const userDrafts = ref<any[]>([])

// 分页相关
const questionPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const answerPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const editForm = reactive({
  bio: '',
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
    
    // 加载用户基础信息
    const userRes = await api.get(`/users/${userId.value}`)
    console.log('用户信息:', userRes.data)
    userInfo.value = userRes.data
    
    // 加载用户统计信息
    const statsRes = await api.get(`/users/${userId.value}/stats`)
    console.log('用户统计:', statsRes.data)
    userStats.value = {
      reputation: userInfo.value.reputation || 0,
      totalQuestions: statsRes.data.questionCount || 0,
      totalAnswers: statsRes.data.answerCount || 0,
      followersCount: statsRes.data.followerCount || 0,
      followingCount: statsRes.data.followingCount || 0,
      acceptedAnswers: statsRes.data.acceptedAnswers || 0
    }
    
    // 检查关注状态（如果不是自己的主页且已登录）
    if (!isOwnProfile.value && authStore.isAuthenticated) {
      await checkFollowStatus()
    }
    
    // 加载其他数据
    await Promise.all([
      loadUserQuestions(),
      loadUserAnswers(), 
      loadUserFollowing(),
      loadUserFollowers(),
      // 只有在访问自己的主页时才加载草稿
      ...(isOwnProfile.value ? [loadUserDrafts()] : [])
    ])
    
  } catch (error: any) {
    console.error('用户信息加载失败:', error)
    ElMessage.error(error.response?.data?.message || '用户信息加载失败')
  } finally {
    loading.value = false
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  if (!authStore.isAuthenticated) {
    console.log('用户未登录，设置关注状态为false')
    isFollowing.value = false
    return
  }

  try {
    console.log('检查关注状态 - 当前用户:', authStore.user?.id, '目标用户:', userId.value)
    const response = await api.get(`/users/${userId.value}/follow-status`)
    console.log('关注状态API响应:', response.data)
    isFollowing.value = response.data.isFollowing
    console.log('设置关注状态为:', isFollowing.value)
  } catch (error: any) {
    console.error('关注状态检查失败:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error(error.response?.data?.message || '关注状态检查失败')
  }
}

// 加载用户回答
const loadUserAnswers = async () => {
  try {
    const response = await api.get(`/users/${userId.value}/answers`, {
      params: {
        page: answerPagination.page - 1,
        size: answerPagination.size,
        sortBy: 'createdAt',
        sortDirection: 'desc'
      }
    })
    
    // 处理分页数据
    if (response.data.content) {
      userAnswers.value = response.data.content.map((answer: any) => ({
        ...answer,
        questionId: answer.question?.id || answer.questionId,
        questionTitle: answer.question?.title || answer.questionTitle || '未知问题'
      }))
      answerPagination.total = response.data.totalElements
      // 更新统计数据为实际总数
      userStats.value.totalAnswers = response.data.totalElements
    } else if (Array.isArray(response.data)) {
      userAnswers.value = response.data.map((answer: any) => ({
        ...answer,
        questionId: answer.question?.id || answer.questionId,
        questionTitle: answer.question?.title || answer.questionTitle || '未知问题'
      }))
      answerPagination.total = response.data.length
      // 更新统计数据为实际总数
      userStats.value.totalAnswers = response.data.length
    } else {
      userAnswers.value = []
      answerPagination.total = 0
      // 更新统计数据为实际总数
      userStats.value.totalAnswers = 0
    }
    
    console.log('加载用户回答成功:', userAnswers.value)
    console.log('回答总数:', userStats.value.totalAnswers)
  } catch (error: any) {
    console.error('加载用户回答失败:', error)
    ElMessage.error(error.response?.data?.message || '加载回答失败')
    userAnswers.value = []
    answerPagination.total = 0
    // 更新统计数据为实际总数
    userStats.value.totalAnswers = 0
  }
}

// 加载用户问题
const loadUserQuestions = async () => {
  try {
    const response = await api.get(`/users/${userId.value}/questions`, {
      params: {
        page: questionPagination.page - 1,
        size: questionPagination.size
      }
    })
    
    // 处理分页数据
    if (response.data.content) {
      userQuestions.value = response.data.content
      questionPagination.total = response.data.totalElements
      // 更新统计数据为实际总数
      userStats.value.totalQuestions = response.data.totalElements
    } else if (Array.isArray(response.data)) {
      userQuestions.value = response.data
      questionPagination.total = response.data.length
      // 更新统计数据为实际总数
      userStats.value.totalQuestions = response.data.length
    } else {
      userQuestions.value = []
      questionPagination.total = 0
      // 更新统计数据为实际总数
      userStats.value.totalQuestions = 0
    }
    
    console.log('加载用户问题成功:', userQuestions.value)
    console.log('问题总数:', userStats.value.totalQuestions)
  } catch (error: any) {
    console.error('加载用户问题失败:', error)
    ElMessage.error(error.response?.data?.message || '加载问题失败')
    userQuestions.value = []
    questionPagination.total = 0
    // 更新统计数据为实际总数
    userStats.value.totalQuestions = 0
  }
}

// 加载关注的用户
const loadUserFollowing = async () => {
  try {
    const response = await api.get(`/users/${userId.value}/following`)
    let following = []
    if (response.data.content) {
      following = response.data.content
    } else if (Array.isArray(response.data)) {
      following = response.data
    }
    
    followingUsers.value = following
    
    console.log('加载关注用户成功:', followingUsers.value)
  } catch (error: any) {
    console.error('加载关注用户失败:', error)
    ElMessage.error(error.response?.data?.message || '加载关注列表失败')
    followingUsers.value = []
  }
}

// 加载粉丝
const loadUserFollowers = async () => {
  try {
    const response = await api.get(`/users/${userId.value}/followers`);
    followersList.value = response.data.content;
  } catch (error) {
    console.error('加载粉丝列表失败:', error);
  }
}

// 关注/取消关注
const toggleFollow = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  followLoading.value = true
  try {
    if (isFollowing.value) {
      const response = await api.delete(`/users/${authStore.user?.id}/unfollow/${userId.value}`)
      if (response.data.success) {
        isFollowing.value = false
        // 更新粉丝数统计
        userStats.value.followersCount = Math.max(0, userStats.value.followersCount - 1)
        ElMessage.success(response.data.message || '已取消关注')
      } else {
        ElMessage.warning(response.data.message || '操作失败')
      }
    } else {
      const response = await api.post(`/users/${authStore.user?.id}/follow/${userId.value}`)
      if (response.data.success) {
        isFollowing.value = true
        // 更新粉丝数统计
        userStats.value.followersCount = userStats.value.followersCount + 1
        ElMessage.success(response.data.message || '关注成功')
      } else {
        ElMessage.warning(response.data.message || '操作失败')
      }
    }
    
    // 重新加载粉丝列表以确保数据一致性
    await loadUserFollowers()
    
  } catch (error: any) {
    console.error('关注操作失败:', error)
    
    if (error.response?.data?.message) {
      ElMessage.error(`操作失败: ${error.response.data.message}`)
    } else if (error.message) {
      ElMessage.error(`操作失败: ${error.message}`)
    } else {
      ElMessage.error('操作失败')
    }
    
    // 操作失败后重新检查关注状态
    await checkFollowStatus()
  } finally {
    followLoading.value = false
  }
}

// 保存个人资料
const saveProfile = async () => {
  saving.value = true
  try {
    const updateData = {
      bio: editForm.bio
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
    case 'drafts':
      loadUserDrafts()
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

// 跳转到问题详情页面
const goToQuestion = (questionId: number) => {
  router.push(`/question/${questionId}`)
}

// 跳转到用户个人页面
const goToUserProfile = (userId: number) => {
  router.push(`/user/${userId}`)
}

// 跳转到编辑草稿
const goToEditDraft = (draft: any) => {
  // 将草稿数据存储到sessionStorage，然后跳转到提问页面
  sessionStorage.setItem('editDraft', JSON.stringify(draft))
  router.push('/ask')
}

// 处理问题分页变化
const handleQuestionPageChange = (page: number) => {
  questionPagination.page = page
  loadUserQuestions()
}

// 处理回答分页变化
const handleAnswerPageChange = (page: number) => {
  answerPagination.page = page
  loadUserAnswers()
}

// 加载草稿箱
const loadUserDrafts = async () => {
  try {
    const response = await api.get(`/users/${userId.value}/drafts`)
    console.log('草稿箱响应:', response.data)
    
    if (response.data.content) {
      userDrafts.value = response.data.content
    } else if (Array.isArray(response.data)) {
      userDrafts.value = response.data
    } else {
      userDrafts.value = []
    }
    
    console.log('加载草稿箱成功:', userDrafts.value)
  } catch (error: any) {
    console.error('加载草稿箱失败:', error)
    ElMessage.error(error.response?.data?.message || '加载草稿箱失败')
    userDrafts.value = []
  }
}

// 监听路由变化
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId && route.name === 'UserProfile') {
    // 重置组件状态
    activeTab.value = 'questions'
    isFollowing.value = false
    userInfo.value = {}
    userStats.value = {}
    userQuestions.value = []
    userAnswers.value = []
    followingUsers.value = []
    followersList.value = []
    userDrafts.value = []
    
    // 重新加载数据
    loadUserProfile()
  }
}, { immediate: true })

// 组件挂载时初始化
onMounted(() => {
  console.log('🚀 个人主页组件挂载，用户ID:', userId.value)
  if (userId.value && !isNaN(userId.value)) {
    loadUserProfile()
  }
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
.answer-item,
.draft-item {
  display: flex;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.question-item:hover,
.answer-item:hover,
.draft-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.question-content,
.answer-content,
.draft-content {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
}

.question-title,
.draft-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
  line-height: 1.4;
}

.question-title:hover,
.draft-title:hover {
  color: #409eff;
}

.question-preview,
.answer-preview,
.draft-preview {
  color: #666;
  line-height: 1.5;
  margin-bottom: 15px;
  word-wrap: break-word;
}

.question-tags {
  margin-bottom: 10px;
}

.question-tag,
.tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.question-time,
.answer-time,
.draft-time {
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
  align-items: center;
}

.stat {
  text-align: center;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
  min-width: 60px;
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

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
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
  background: white;
}

.user-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.user-card .user-info {
  flex: 1;
  min-width: 0;
}

.user-card .username {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  font-size: 14px;
}

.user-card .reputation {
  color: #666;
  font-size: 12px;
}

.question-title-ref {
  color: #409eff;
  font-size: 14px;
  margin-bottom: 8px;
  font-weight: 500;
}

/* 确保标签页内容可以正常交互 */
.el-tab-pane {
  outline: none;
}

/* 调试用的边框 */
.debug-border {
  border: 2px solid red !important;
}

.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  display: inline-block;
  cursor: pointer;
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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
</style> 