<template>
  <div class="navbar">
    <div class="container navbar-content">
      <div class="navbar-left">
        <router-link to="/" class="logo">
          <el-icon size="24"><QuestionFilled /></el-icon>
          <span>问答平台</span>
        </router-link>
        
        <el-menu 
          mode="horizontal" 
          :default-active="activeMenu"
          :ellipsis="false"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/questions">问题</el-menu-item>
          <el-menu-item index="/tags">标签</el-menu-item>
          <el-menu-item index="/users">用户</el-menu-item>
        </el-menu>
      </div>

      <div class="navbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索问题..."
          prefix-icon="Search"
          style="width: 200px; margin-right: 20px;"
          @keyup.enter="handleSearch"
        />

        <template v-if="isLoggedIn">
          <el-button type="primary" @click="$router.push('/ask')">
            <el-icon><EditPen /></el-icon>
            提问
          </el-button>
          
          <el-dropdown trigger="click" style="margin-left: 20px;">
            <span class="user-avatar">
              <el-avatar :src="currentUser.avatar" :size="32">
                {{ currentUser.username?.[0] }}
              </el-avatar>
              <span class="username">{{ currentUser.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push(`/user/${currentUser.id}`)">
                  <el-icon><User /></el-icon>
                  个人主页
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/settings')">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <el-button @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  QuestionFilled, 
  Search, 
  EditPen, 
  User, 
  Setting, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 是否已登录
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 当前用户信息
const currentUser = ref({
  id: 1,
  username: 'testuser',
  avatar: ''
})

// 搜索处理
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/questions',
      query: { q: searchKeyword.value }
    })
  }
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出登录成功')
  router.push('/')
}
</script>

<style lang="scss" scoped>
.navbar {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  height: 60px;

  .navbar-content {
    height: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .navbar-left {
    display: flex;
    align-items: center;

    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: bold;
      color: #409eff;
      text-decoration: none;
      margin-right: 30px;

      &:hover {
        color: #66b1ff;
      }
    }

    .el-menu {
      border: none;
      height: 60px;
      
      .el-menu-item {
        height: 60px;
        line-height: 60px;
      }
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;

    .user-avatar {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        color: #606266;
      }

      &:hover {
        .username {
          color: #409eff;
        }
      }
    }
  }
}
</style> 