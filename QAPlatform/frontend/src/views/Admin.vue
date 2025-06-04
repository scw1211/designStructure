<template>
  <div class="admin-page">
    <div class="container">
      <div class="page-header">
        <h1>
          <el-icon><Setting /></el-icon>
          系统管理
        </h1>
        <p class="page-desc">管理用户、问题、回答和系统设置</p>
      </div>

      <!-- 权限检查 -->
      <div v-if="!authStore.isModerator" class="no-permission">
        <el-result
          icon="warning"
          title="权限不足"
          sub-title="您没有权限访问此页面"
        >
          <template #extra>
            <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
          </template>
        </el-result>
      </div>

      <!-- 管理界面 -->
      <div v-else>
        <!-- 系统统计 -->
        <el-card class="stats-card" v-loading="statsLoading">
          <template #header>
            <span>系统概览</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="loadStats">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </template>
          
          <el-row :gutter="20">
            <!-- 用户统计 -->
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.adminCount }}</div>
                <div class="stat-label">管理员</div>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.moderatorCount }}</div>
                <div class="stat-label">版主</div>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.regularUserCount }}</div>
                <div class="stat-label">普通用户</div>
              </div>
            </el-col>
            
            <!-- 问题统计 -->
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.totalQuestions }}</div>
                <div class="stat-label">总问题数</div>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.solvedQuestions }}</div>
                <div class="stat-label">已解决</div>
              </div>
            </el-col>
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.closedQuestions }}</div>
                <div class="stat-label">已关闭</div>
              </div>
            </el-col>
            
            <!-- 回答统计 -->
            <el-col :span="3">
              <div class="stat-item">
                <div class="stat-number">{{ stats.totalAnswers }}</div>
                <div class="stat-label">总回答数</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 管理标签页 -->
        <el-card class="management-card">
          <el-tabs v-model="activeTab" type="border-card">
            <!-- 用户管理标签页 -->
            <el-tab-pane label="用户管理" name="users">
              <div class="tab-header">
                <div class="header-actions">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="搜索用户..."
                    style="width: 200px; margin-right: 10px"
                    @input="searchUsers"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-select v-model="roleFilter" placeholder="角色筛选" style="width: 120px" @change="filterUsers">
                    <el-option label="全部" value="" />
                    <el-option label="管理员" value="ADMIN" />
                    <el-option label="版主" value="MODERATOR" />
                    <el-option label="普通用户" value="USER" />
                  </el-select>
                </div>
              </div>

              <!-- 用户列表 -->
              <el-table :data="users" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                
                <el-table-column label="用户信息" min-width="200">
                  <template #default="scope">
                    <div class="user-info">
                      <el-avatar :src="scope.row.avatar" :size="40">
                        {{ scope.row.username.charAt(0).toUpperCase() }}
                      </el-avatar>
                      <div class="user-details">
                        <div class="username">{{ scope.row.username }}</div>
                        <div class="email">{{ scope.row.email }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="角色" width="120">
                  <template #default="scope">
                    <el-tag :type="getRoleTagType(scope.row.role)">
                      {{ getRoleLabel(scope.row.role) }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column prop="reputation" label="声望" width="100" />
                
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
                      {{ scope.row.enabled ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="注册时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createdAt) }}
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="300">
                  <template #default="scope">
                    <div class="action-buttons">
                      <!-- 更改角色 -->
                      <el-dropdown @command="(command: string) => handleRoleAction(command, scope.row)">
                        <el-button type="primary" size="small">
                          更改角色<el-icon class="el-icon--right"><arrow-down /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item 
                              v-if="canChangeToRole('USER', scope.row)" 
                              command="USER"
                            >
                              设为普通用户
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canChangeToRole('MODERATOR', scope.row)" 
                              command="MODERATOR"
                            >
                              设为版主
                            </el-dropdown-item>
                            <el-dropdown-item 
                              v-if="canChangeToRole('ADMIN', scope.row)" 
                              command="ADMIN"
                            >
                              设为管理员
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                      
                      <!-- 启用/禁用 -->
                      <el-button
                        v-if="authStore.isAdmin && scope.row.id !== authStore.user?.id"
                        :type="scope.row.enabled ? 'warning' : 'success'"
                        size="small"
                        @click="toggleUserStatus(scope.row)"
                      >
                        {{ scope.row.enabled ? '禁用' : '启用' }}
                      </el-button>
                      
                      <!-- 删除用户 -->
                      <el-button
                        v-if="authStore.isAdmin && scope.row.id !== authStore.user?.id"
                        type="danger"
                        size="small"
                        @click="deleteUser(scope.row)"
                      >
                        删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 用户分页 -->
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="userPagination.page"
                  v-model:page-size="userPagination.size"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="userPagination.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleUserSizeChange"
                  @current-change="handleUserPageChange"
                />
              </div>
            </el-tab-pane>

            <!-- 问题管理标签页 -->
            <el-tab-pane label="问题与回答管理" name="questions">
              <div class="tab-header">
                <div class="header-actions">
                  <el-input
                    v-model="questionSearchKeyword"
                    placeholder="搜索问题..."
                    style="width: 200px; margin-right: 10px"
                    @input="searchQuestions"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-select v-model="questionStatusFilter" placeholder="状态筛选" style="width: 120px" @change="filterQuestions">
                    <el-option label="全部" value="" />
                    <el-option label="开放" value="open" />
                    <el-option label="已解决" value="solved" />
                    <el-option label="已关闭" value="closed" />
                  </el-select>
                </div>
              </div>

              <!-- 问题列表 -->
              <el-table 
                :data="questions" 
                v-loading="questionsLoading" 
                style="width: 100%"
                :expand-row-keys="expandedQuestions"
                row-key="id"
              >
                <el-table-column type="expand">
                  <template #default="props">
                    <div class="question-detail">
                      <div class="question-content">
                        <h4>问题内容</h4>
                        <p>{{ props.row.content }}</p>
                      </div>
                      
                      <!-- 问题的回答管理 -->
                      <div class="answers-management">
                        <div class="answers-header">
                          <h4>回答管理 ({{ questionAnswers[props.row.id]?.length || 0 }} 个回答)</h4>
                          <el-button 
                            type="primary" 
                            size="small" 
                            @click="loadQuestionAnswers(props.row.id)"
                            :loading="loadingAnswers[props.row.id]"
                          >
                            {{ questionAnswers[props.row.id] ? '刷新回答' : '加载回答' }}
                          </el-button>
                        </div>
                        
                        <div v-if="questionAnswers[props.row.id]" class="answers-list">
                          <el-table :data="questionAnswers[props.row.id]" size="small">
                            <el-table-column prop="id" label="ID" width="60" />
                            <el-table-column label="回答内容" min-width="300">
                              <template #default="scope">
                                <div class="answer-content">
                                  {{ scope.row.content.substring(0, 100) }}{{ scope.row.content.length > 100 ? '...' : '' }}
                                </div>
                              </template>
                            </el-table-column>
                            <el-table-column label="作者" width="120">
                              <template #default="scope">
                                {{ scope.row.author?.username }}
                              </template>
                            </el-table-column>
                            <el-table-column label="状态" width="100">
                              <template #default="scope">
                                <el-tag v-if="scope.row.accepted" type="success">已采纳</el-tag>
                                <el-tag v-else type="info">未采纳</el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column prop="voteCount" label="点赞" width="80" />
                            <el-table-column label="创建时间" width="140">
                              <template #default="scope">
                                {{ formatDate(scope.row.createdAt) }}
                              </template>
                            </el-table-column>
                            <el-table-column label="操作" width="120">
                              <template #default="scope">
                                <el-button
                                  type="danger"
                                  size="small"
                                  @click="deleteAnswer(scope.row, props.row.id)"
                                >
                                  删除
                                </el-button>
                              </template>
                            </el-table-column>
                          </el-table>
                        </div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column prop="id" label="ID" width="80" />
                
                <el-table-column label="标题" min-width="300">
                  <template #default="scope">
                    <div class="question-title">
                      <el-link :href="`/questions/${scope.row.id}`" target="_blank">
                        {{ scope.row.title }}
                      </el-link>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="作者" width="120">
                  <template #default="scope">
                    {{ scope.row.author?.username }}
                  </template>
                </el-table-column>
                
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag v-if="scope.row.solved" type="success">已解决</el-tag>
                    <el-tag v-else-if="scope.row.closed" type="danger">已关闭</el-tag>
                    <el-tag v-else type="primary">开放</el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column prop="answerCount" label="回答数" width="100" />
                <el-table-column prop="viewCount" label="浏览数" width="100" />
                
                <el-table-column label="创建时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createdAt) }}
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="200">
                  <template #default="scope">
                    <div class="action-buttons">
                      <!-- 关闭/开启问题 -->
                      <el-button
                        :type="scope.row.closed ? 'success' : 'warning'"
                        size="small"
                        @click="toggleQuestionStatus(scope.row)"
                      >
                        {{ scope.row.closed ? '开启' : '关闭' }}
                      </el-button>
                      
                      <!-- 删除问题 -->
                      <el-button
                        type="danger"
                        size="small"
                        @click="deleteQuestion(scope.row)"
                      >
                        删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 问题分页 -->
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="questionPagination.page"
                  v-model:page-size="questionPagination.size"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="questionPagination.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleQuestionSizeChange"
                  @current-change="handleQuestionPageChange"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </div>

    <!-- 确认对话框 -->
    <el-dialog v-model="confirmDialogVisible" title="确认操作" width="400px">
      <div class="confirm-content">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <div class="confirm-text">
          <div class="confirm-title">{{ confirmTitle }}</div>
          <div class="confirm-message">{{ confirmMessage }}</div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="executeConfirmedAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Refresh,
  Search,
  Warning,
  ArrowDown
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const statsLoading = ref(false)
const searchKeyword = ref('')
const roleFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalUsers = ref(0)
const users = ref<any[]>([])

// 标签页管理
const activeTab = ref('users')

// 用户管理分页
const userPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 问题管理
const questionsLoading = ref(false)
const questionSearchKeyword = ref('')
const questionStatusFilter = ref('')
const questions = ref<any[]>([])
const questionPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 展开的问题和回答管理
const expandedQuestions = ref<string[]>([])
const questionAnswers = ref<Record<string, any[]>>({})
const loadingAnswers = ref<Record<string, boolean>>({})

// 系统统计
const stats = reactive({
  totalUsers: 0,
  adminCount: 0,
  moderatorCount: 0,
  regularUserCount: 0,
  enabledUsers: 0,
  disabledUsers: 0,
  totalQuestions: 0,
  solvedQuestions: 0,
  closedQuestions: 0,
  totalAnswers: 0,
  acceptedAnswers: 0
})

// 确认对话框
const confirmDialogVisible = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<() => void>(() => {})

// 加载系统统计
const loadStats = async () => {
  if (!authStore.isModerator) return
  
  statsLoading.value = true
  try {
    const response = await api.get('/admin/stats')
    Object.assign(stats, response.data)
  } catch (error: any) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 加载用户列表
const loadUsers = async () => {
  if (!authStore.isModerator) return
  
  loading.value = true
  try {
    const params = {
      page: userPagination.page - 1,
      size: userPagination.size,
      search: searchKeyword.value,
      role: roleFilter.value
    }
    
    const response = await api.get('/admin/users', { params })
    
    if (response.data.content) {
      users.value = response.data.content
      userPagination.total = response.data.totalElements
      totalUsers.value = response.data.totalElements
    } else {
      users.value = response.data
      userPagination.total = response.data.length
      totalUsers.value = response.data.length
    }
  } catch (error: any) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
    users.value = []
  } finally {
    loading.value = false
  }
}

// 搜索用户
const searchUsers = () => {
  userPagination.page = 1
  loadUsers()
}

// 筛选用户
const filterUsers = () => {
  userPagination.page = 1
  loadUsers()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadUsers()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadUsers()
}

// 用户管理分页处理
const handleUserSizeChange = (size: number) => {
  userPagination.size = size
  userPagination.page = 1
  loadUsers()
}

const handleUserPageChange = (page: number) => {
  userPagination.page = page
  loadUsers()
}

// 问题管理方法
const loadQuestions = async () => {
  if (!authStore.isModerator) return
  
  questionsLoading.value = true
  try {
    const params = {
      page: questionPagination.page - 1,
      size: questionPagination.size,
      search: questionSearchKeyword.value,
      status: questionStatusFilter.value
    }
    
    const response = await api.get('/admin/questions', { params })
    
    if (response.data.content) {
      questions.value = response.data.content
      questionPagination.total = response.data.totalElements
    } else {
      questions.value = response.data
      questionPagination.total = response.data.length
    }
  } catch (error: any) {
    console.error('加载问题列表失败:', error)
    ElMessage.error('加载问题列表失败')
    questions.value = []
  } finally {
    questionsLoading.value = false
  }
}

const searchQuestions = () => {
  questionPagination.page = 1
  loadQuestions()
}

const filterQuestions = () => {
  questionPagination.page = 1
  loadQuestions()
}

const handleQuestionSizeChange = (size: number) => {
  questionPagination.size = size
  questionPagination.page = 1
  loadQuestions()
}

const handleQuestionPageChange = (page: number) => {
  questionPagination.page = page
  loadQuestions()
}

// 加载问题的回答
const loadQuestionAnswers = async (questionId: number) => {
  loadingAnswers.value[questionId] = true
  try {
    const response = await api.get(`/admin/answers?questionId=${questionId}&page=0&size=100`)
    questionAnswers.value[questionId] = response.data.content || response.data
  } catch (error: any) {
    console.error('加载回答失败:', error)
    ElMessage.error('加载回答失败')
  } finally {
    loadingAnswers.value[questionId] = false
  }
}

const toggleQuestionStatus = async (question: any) => {
  const action = question.closed ? '开启' : '关闭'
  
  confirmTitle.value = `${action}问题`
  confirmMessage.value = `确定要${action}问题 "${question.title}" 吗？`
  
  pendingAction.value = async () => {
    try {
      await api.put(`/admin/questions/${question.id}/status`, {
        closed: !question.closed,
        reason: `管理员操作：${action}问题`
      })
      
      ElMessage.success(`问题已${action}`)
      loadQuestions()
      loadStats()
    } catch (error: any) {
      console.error(`${action}问题失败:`, error)
      ElMessage.error(error.response?.data?.message || `${action}问题失败`)
    }
  }
  
  confirmDialogVisible.value = true
}

const deleteQuestion = async (question: any) => {
  confirmTitle.value = '删除问题'
  confirmMessage.value = `确定要删除问题 "${question.title}" 吗？此操作不可撤销。`
  
  pendingAction.value = async () => {
    try {
      await api.delete(`/admin/questions/${question.id}`)
      
      ElMessage.success('问题已删除')
      loadQuestions()
      loadStats()
    } catch (error: any) {
      console.error('删除问题失败:', error)
      ElMessage.error(error.response?.data?.message || '删除问题失败')
    }
  }
  
  confirmDialogVisible.value = true
}

// 删除回答
const deleteAnswer = async (answer: any, questionId: number) => {
  confirmTitle.value = '删除回答'
  confirmMessage.value = `确定要删除这个回答吗？此操作不可撤销。`
  
  pendingAction.value = async () => {
    try {
      await api.delete(`/admin/answers/${answer.id}`)
      
      ElMessage.success('回答已删除')
      loadQuestionAnswers(questionId) // 重新加载该问题的回答
      loadStats()
    } catch (error: any) {
      console.error('删除回答失败:', error)
      ElMessage.error(error.response?.data?.message || '删除回答失败')
    }
  }
  
  confirmDialogVisible.value = true
}

// 处理角色更改
const handleRoleAction = async (newRole: string, user: any) => {
  const roleNames: Record<string, string> = {
    'USER': '普通用户',
    'MODERATOR': '版主',
    'ADMIN': '管理员'
  }
  
  confirmTitle.value = '更改用户角色'
  confirmMessage.value = `确定要将用户 "${user.username}" 的角色更改为 "${roleNames[newRole] || newRole}" 吗？`
  
  pendingAction.value = async () => {
    try {
      await api.put(`/admin/users/${user.id}/role`, {
        newRole: newRole,
        reason: `管理员操作：角色更改为${roleNames[newRole] || newRole}`
      })
      
      ElMessage.success('角色更改成功')
      loadUsers()
      loadStats()
    } catch (error: any) {
      console.error('更改角色失败:', error)
      ElMessage.error(error.response?.data?.message || '更改角色失败')
    }
  }
  
  confirmDialogVisible.value = true
}

// 切换用户状态
const toggleUserStatus = async (user: any) => {
  const action = user.enabled ? '禁用' : '启用'
  
  confirmTitle.value = `${action}用户`
  confirmMessage.value = `确定要${action}用户 "${user.username}" 吗？`
  
  pendingAction.value = async () => {
    try {
      await api.put(`/admin/users/${user.id}/status`, {
        enabled: !user.enabled,
        reason: `管理员操作：${action}用户`
      })
      
      ElMessage.success(`用户已${action}`)
      loadUsers()
      loadStats()
    } catch (error: any) {
      console.error(`${action}用户失败:`, error)
      ElMessage.error(error.response?.data?.message || `${action}用户失败`)
    }
  }
  
  confirmDialogVisible.value = true
}

// 删除用户
const deleteUser = async (user: any) => {
  confirmTitle.value = '删除用户'
  confirmMessage.value = `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`
  
  pendingAction.value = async () => {
    try {
      await api.delete(`/admin/users/${user.id}`)
      
      ElMessage.success('用户已删除')
      loadUsers()
      loadStats()
    } catch (error: any) {
      console.error('删除用户失败:', error)
      ElMessage.error(error.response?.data?.message || '删除用户失败')
    }
  }
  
  confirmDialogVisible.value = true
}

// 执行确认的操作
const executeConfirmedAction = async () => {
  confirmDialogVisible.value = false
  await pendingAction.value()
}

// 检查是否可以更改为指定角色
const canChangeToRole = (targetRole: string, user: any): boolean => {
  if (user.id === authStore.user?.id) return false // 不能更改自己的角色
  if (user.role === targetRole) return false // 已经是目标角色
  
  const currentUserRole = authStore.user?.role
  
  // 管理员可以更改任何角色
  if (currentUserRole === 'ADMIN') {
    return true
  }
  
  // 版主的权限
  if (currentUserRole === 'MODERATOR') {
    // 版主可以将普通用户提升为管理员或版主
    if (user.role === 'USER' && (targetRole === 'ADMIN' || targetRole === 'MODERATOR')) {
      return true
    }
    // 版主可以将用户降级为普通用户
    if (targetRole === 'USER') {
      return true
    }
  }
  
  return false
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
    default: return '普通用户'
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (authStore.isModerator && newTab === 'questions') {
    loadQuestions()
  }
})

// 组件挂载时加载数据
onMounted(() => {
  if (authStore.isModerator) {
    loadStats()
    loadUsers()
    loadQuestions()
  }
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 20px;
  padding-bottom: 40px;
}

.container {
  max-width: 1400px;
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
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
  }
  
  .page-desc {
    color: #666;
    font-size: 16px;
  }
}

.no-permission {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.stats-card {
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;
  
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

.users-management-card {
  margin-bottom: 30px;
}

.management-card {
  margin-bottom: 30px;
}

.tab-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  
  .user-details {
    .username {
      font-weight: 600;
      color: #333;
      margin-bottom: 4px;
    }
    
    .email {
      color: #666;
      font-size: 12px;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

.confirm-content {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  
  .warning-icon {
    color: #e6a23c;
    font-size: 24px;
    margin-top: 2px;
  }
  
  .confirm-text {
    flex: 1;
    
    .confirm-title {
      font-weight: 600;
      font-size: 16px;
      margin-bottom: 8px;
    }
    
    .confirm-message {
      color: #666;
      line-height: 1.5;
    }
  }
}

.question-title {
  .el-link {
    font-weight: 500;
    color: #409eff;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.answer-content {
  line-height: 1.5;
  color: #333;
}

.question-detail {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin: 10px 0;
}

.question-content {
  margin-bottom: 20px;
}

.question-content h4 {
  color: #333;
  margin-bottom: 10px;
}

.question-content p {
  color: #666;
  line-height: 1.6;
  background-color: white;
  padding: 15px;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.answers-management {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.answers-header h4 {
  color: #333;
  margin: 0;
}

.answers-list {
  background-color: white;
  border-radius: 6px;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 10px;
  }
}
</style> 