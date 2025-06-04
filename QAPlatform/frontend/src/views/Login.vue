<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>{{ isLogin ? '登录' : '注册' }}问答平台</h1>
        <p>{{ isLogin ? '欢迎回来，请输入您的账号信息' : '创建您的账号，加入我们的社区' }}</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item v-if="!isLogin" prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱地址"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item v-if="!isLogin" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item v-if="isLogin" class="forgot-password">
          <el-link type="primary" @click="handleForgotPassword">
            忘记密码？
          </el-link>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleSubmit"
            size="large"
          >
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </el-form-item>

        <el-form-item class="switch-mode">
          <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
          <el-link type="primary" @click="toggleMode">
            {{ isLogin ? '立即注册' : '立即登录' }}
          </el-link>
        </el-form-item>
      </el-form>

      <!-- 第三方登录 -->
      <div class="social-login">
        <el-divider>或者</el-divider>
        <div class="social-buttons">
          <el-button class="social-btn github" @click="socialLogin('github')">
            <i class="fab fa-github"></i>
            GitHub
          </el-button>
          <el-button class="social-btn google" @click="socialLogin('google')">
            <i class="fab fa-google"></i>
            Google
          </el-button>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage, ElForm } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const isLogin = ref(true)
const loading = ref(false)
const formRef = ref<InstanceType<typeof ElForm>>()

// 表单数据
const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  email: isLogin.value ? [] : [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: isLogin.value ? [] : [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}))

// 确认密码验证器
function validateConfirmPassword(rule: any, value: string, callback: Function) {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 切换登录/注册模式
const toggleMode = () => {
  isLogin.value = !isLogin.value
  resetForm()
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  })
}

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true

    if (isLogin.value) {
      // 登录
      await authStore.login({
        username: form.username,
        password: form.password
      })
      
      ElMessage.success('登录成功！')
      router.push('/')
    } else {
      // 注册
      await authStore.register({
        username: form.username,
        email: form.email,
        password: form.password
      })
      
      ElMessage.success('注册成功！请登录')
      isLogin.value = true
      resetForm()
    }
  } catch (error: any) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败，请重试')
  } finally {
    loading.value = false
  }
}

// 忘记密码
const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能开发中...')
}

// 第三方登录
const socialLogin = (provider: string) => {
  ElMessage.info(`${provider}登录功能开发中...`)
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
  font-weight: 600;
}

.login-header p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.login-form {
  margin-bottom: 20px;
}

.forgot-password {
  text-align: right;
  margin-bottom: 0;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.switch-mode {
  text-align: center;
  margin-bottom: 0;
}

.switch-mode span {
  color: #666;
  margin-right: 8px;
}

.social-login {
  margin-top: 20px;
}

.social-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.social-btn {
  flex: 1;
  height: 45px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

.social-btn.github {
  background-color: #333;
  border-color: #333;
  color: white;
}

.social-btn.github:hover {
  background-color: #555;
  border-color: #555;
}

.social-btn.google {
  background-color: #db4437;
  border-color: #db4437;
  color: white;
}

.social-btn.google:hover {
  background-color: #c23321;
  border-color: #c23321;
}

.social-btn i {
  margin-right: 8px;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 100px;
  height: 100px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.circle-3 {
  width: 80px;
  height: 80px;
  bottom: 20%;
  left: 30%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    margin: 20px;
    padding: 30px 20px;
  }
  
  .login-header h1 {
    font-size: 24px;
  }
  
  .social-buttons {
    flex-direction: column;
  }
}

/* 输入框样式优化 */
:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 0 15px;
  height: 50px;
}

:deep(.el-input__inner) {
  font-size: 15px;
}

:deep(.el-form-item__error) {
  margin-top: 5px;
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .login-box {
    background: rgba(30, 30, 30, 0.95);
  }
  
  .login-header h1 {
    color: #fff;
  }
  
  .login-header p {
    color: #ccc;
  }
  
  .switch-mode span {
    color: #ccc;
  }
}
</style> 