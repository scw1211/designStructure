<template>
  <nav class="navbar">
    <!-- 左侧：Logo -->
    <div class="navbar-logo">
      <router-link to="/" class="logo-link">
        <el-icon class="logo-icon"><ChatDotRound /></el-icon>
        <span class="logo-text">问答平台</span>
      </router-link>
    </div>

    <!-- 中间：真正的菜单 -->
    <el-menu
      mode="horizontal"
      :default-active="activeIndex"
      class="navbar-menu"
      @select="handleSelect"
    >
      <el-menu-item index="/" @click="$router.push('/')">首页</el-menu-item>
      <el-menu-item index="/questions" @click="$router.push('/questions')">问题</el-menu-item>
      <el-menu-item index="/tags" @click="$router.push('/tags')">标签</el-menu-item>
      <el-menu-item index="/users" @click="$router.push('/users')">用户</el-menu-item>
    </el-menu>

    <!-- 搜索框 -->
    <div class="navbar-search">
      <el-input
        v-model="searchQuery"
        placeholder="搜索问题..."
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch" :icon="Search" />
        </template>
      </el-input>
    </div>

    <!-- 右侧：用户区（已脱离 el-menu 的 mouseleave 控制） -->
    <div class="navbar-user">
      <template v-if="authStore.isAuthenticated">
        <!-- 提问按钮 -->
        <el-button
          type="primary"
          @click="$router.push('/ask')"
          class="ask-button"
        >
          提问
        </el-button>

        <!-- 通知图标 -->
        <el-badge :value="notificationCount" :hidden="notificationCount === 0">
          <el-button text @click="toggleNotifications">
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>

        <!-- 用户头像下拉菜单 -->
        <el-dropdown 
          trigger="hover" 
          @command="handleUserCommand"
          :show-timeout="0"
          :hide-timeout="300"
        >
          <div class="user-avatar">
            <el-avatar :src="authStore.user?.avatar" :size="32">
              {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="username">{{ authStore.user?.username }}</span>
            <el-icon class="arrow-down">
              <ArrowDown />
            </el-icon>
          </div>
          
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                <span>个人资料</span>
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                <span>设置</span>
              </el-dropdown-item>
              <el-dropdown-item command="my-questions">
                <el-icon><Document /></el-icon>
                <span>我的问题</span>
              </el-dropdown-item>
              <el-dropdown-item command="my-answers">
                <el-icon><ChatDotRound /></el-icon>
                <span>我的回答</span>
              </el-dropdown-item>
              <el-dropdown-item v-if="authStore.isModerator" command="admin" divided>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>

      <!-- 未登录状态 -->
      <template v-else>
        <el-button @click="$router.push('/login')">登录</el-button>
        <el-button type="primary" @click="$router.push('/register')">
          注册
        </el-button>
      </template>
    </div>

    <!-- 通知面板 -->
    <el-drawer
      v-model="notificationDrawer"
      title="通知"
      direction="rtl"
      size="400px"
    >
      <div class="notifications-panel">
        <div v-if="notifications.length === 0" class="no-notifications">
          <el-empty description="暂无通知" />
        </div>
        <div v-else>
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.read }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-content">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-message">{{ notification.message }}</div>
              <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
            </div>
            <el-button
              v-if="!notification.read"
              text
              size="small"
              @click.stop="markAsRead(notification.id)"
            >
              标记已读
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Search,
  Bell,
  User,
  Setting,
  Document,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const searchQuery = ref('')
const notificationDrawer = ref(false)
const notifications = ref<any[]>([])

// 计算当前活动菜单项
const activeIndex = computed(() => route.path)

// 未读通知数量
const notificationCount = computed(() => 
  notifications.value.filter(n => !n.read).length
)

// 处理菜单选择
const handleSelect = (key: string) => {
  console.log('Selected menu:', key)
}

// 处理搜索
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/questions',
      query: { search: searchQuery.value }
    })
  }
}

// 切换通知面板
const toggleNotifications = () => {
  notificationDrawer.value = !notificationDrawer.value
  if (notificationDrawer.value) {
    loadNotifications()
  }
}

// 处理用户下拉菜单命令
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push(`/user/${authStore.user?.id}`)
      break
    case 'settings':
      router.push('/settings')
      break
    case 'my-questions':
      router.push('/my/questions')
      break
    case 'my-answers':
      router.push('/my/answers')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = () => {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

// 加载通知
const loadNotifications = async () => {
  try {
    // 模拟通知数据
    notifications.value = [
      {
        id: 1,
        title: '新回答',
        message: '您的问题"如何学习Spring Boot？"有新的回答',
        read: false,
        createdAt: new Date().toISOString()
      },
      {
        id: 2,
        title: '获得赞同',
        message: '您的回答获得了一个赞同',
        read: true,
        createdAt: new Date(Date.now() - 3600000).toISOString()
      }
    ]
  } catch (error) {
    console.error('加载通知失败:', error)
  }
}

// 处理通知点击
const handleNotificationClick = (notification: any) => {
  // 标记为已读
  markAsRead(notification.id)
  
  // 跳转到相关页面
  if (notification.questionId) {
    router.push(`/question/${notification.questionId}`)
  }
  
  notificationDrawer.value = false
}

// 标记通知为已读
const markAsRead = (notificationId: number) => {
  const notification = notifications.value.find(n => n.id === notificationId)
  if (notification) {
    notification.read = true
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

// 组件挂载时初始化
onMounted(() => {
  if (authStore.isAuthenticated) {
    loadNotifications()
  }
})

// 组件卸载时清理
onUnmounted(() => {
  // 清理工作（如果需要）
})
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  background: white;
  height: 60px;
  overflow: visible;
}

.navbar-logo {
  flex-shrink: 0;
  margin-right: 30px;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #409eff;
  font-size: 18px;
  font-weight: 600;
}

.logo-icon {
  font-size: 24px;
  margin-right: 8px;
}

.logo-text {
  white-space: nowrap;
}

.navbar-menu {
  flex: 1;
  min-width: 0;
  border-bottom: none !important;
}

.navbar-search {
  margin: 0 20px;
  flex-shrink: 0;
}

.search-input {
  width: 300px;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-shrink: 0;
}

.ask-button {
  border-radius: 20px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-avatar:hover {
  background-color: #f5f7fa;
}

.username {
  font-weight: 500;
  color: #333;
}

.arrow-down {
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s;
}

.notifications-panel {
  padding: 0;
}

.no-notifications {
  padding: 40px 0;
  text-align: center;
}

.notification-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.notification-item:hover {
  background-color: #f8f9fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.notification-message {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.notification-time {
  color: #999;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar {
    padding: 0 10px;
  }
  
  .navbar-search {
    display: none;
  }
  
  .search-input {
    width: 200px;
  }
  
  .username {
    display: none;
  }
}

@media (max-width: 480px) {
  .navbar-menu {
    display: none;
  }
  
  .logo-text {
    display: none;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-menu--horizontal .el-menu-item) {
  border-bottom: none;
}

:deep(.el-menu--horizontal .el-menu-item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-menu--horizontal .el-menu-item.is-active) {
  border-bottom: 2px solid #409eff;
  color: #409eff;
}

:deep(.navbar-menu.el-menu--horizontal) {
  border-bottom: none;
}
</style> 