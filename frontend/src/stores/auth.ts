import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

// 用户接口定义
export interface User {
  id: number
  username: string
  email: string
  avatar?: string
  role: string
  reputation: number
  createdAt: string
}

// 登录请求接口
export interface LoginRequest {
  username: string
  password: string
}

// 注册请求接口
export interface RegisterRequest {
  username: string
  email: string
  password: string
}

// 认证响应接口
export interface AuthResponse {
  token: string
  user: User
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)
  const loading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isModerator = computed(() => user.value?.role === 'MODERATOR' || isAdmin.value)

  // 初始化用户信息
  const initUser = () => {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        logout()
      }
    }
  }

  // 登录
  const login = async (credentials: LoginRequest): Promise<void> => {
    loading.value = true
    try {
      const response = await api.post<AuthResponse>('/users/login', credentials)
      const { token: newToken, user: newUser } = response.data
      
      // 保存到状态
      token.value = newToken
      user.value = newUser
      
      // 保存到localStorage
      localStorage.setItem('token', newToken)
      localStorage.setItem('user', JSON.stringify(newUser))
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 注册
  const register = async (userData: RegisterRequest): Promise<void> => {
    loading.value = true
    try {
      await api.post('/users/register', userData)
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 更新用户信息
  const updateUser = (newUser: Partial<User>) => {
    if (user.value) {
      user.value = { ...user.value, ...newUser }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async (): Promise<void> => {
    if (!token.value) return
    
    try {
      const response = await api.get<User>('/users/me')
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(user.value))
    } catch (error) {
      console.error('获取用户信息失败:', error)
      logout()
    }
  }

  // 检查权限
  const hasPermission = (permission: string): boolean => {
    if (!user.value) return false
    
    // 简单的权限检查逻辑
    switch (permission) {
      case 'admin':
        return user.value.role === 'ADMIN'
      case 'moderate':
        return user.value.role === 'ADMIN' || user.value.role === 'MODERATOR'
      case 'user':
        return true
      default:
        return false
    }
  }

  // 初始化
  initUser()

  return {
    // 状态
    token,
    user,
    loading,
    
    // 计算属性
    isAuthenticated,
    isAdmin,
    isModerator,
    
    // 方法
    login,
    register,
    logout,
    updateUser,
    fetchCurrentUser,
    hasPermission
  }
}) 