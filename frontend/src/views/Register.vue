<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>加入问答平台</h1>
        <p>创建您的账号，开始您的知识分享之旅</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
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

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 密码强度指示器 -->
        <div class="password-strength">
          <div class="strength-label">密码强度：</div>
          <div class="strength-bar">
            <div 
              class="strength-fill" 
              :class="passwordStrengthClass"
              :style="{ width: passwordStrengthPercent }"
            ></div>
          </div>
          <div class="strength-text">{{ passwordStrengthText }}</div>
        </div>

        <!-- 服务协议 -->
        <el-form-item prop="agree">
          <el-checkbox v-model="form.agree">
            我已阅读并同意
            <el-link type="primary" @click="showTerms">《用户服务协议》</el-link>
            和
            <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
            size="large"
          >
            注册账号
          </el-button>
        </el-form-item>

        <el-form-item class="login-link">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">
            立即登录
          </el-link>
        </el-form-item>
      </el-form>

      <!-- 优势展示 -->
      <div class="features">
        <div class="feature-item">
          <el-icon class="feature-icon"><Star /></el-icon>
          <span>获得声誉积分</span>
        </div>
        <div class="feature-item">
          <el-icon class="feature-icon"><ChatDotRound /></el-icon>
          <span>参与社区讨论</span>
        </div>
        <div class="feature-item">
          <el-icon class="feature-icon"><Trophy /></el-icon>
          <span>展示专业技能</span>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, Star, ChatDotRound, Trophy } from '@element-plus/icons-vue'
import { ElMessage, ElForm, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const formRef = ref<InstanceType<typeof ElForm>>()

// 表单数据
const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agree: false
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agree: [
    { required: true, message: '请同意用户服务协议', trigger: 'change' },
    { validator: validateAgree, trigger: 'change' }
  ]
}

// 密码验证器
function validatePassword(rule: any, value: string, callback: Function) {
  if (value && value.length >= 6) {
    // 检查密码复杂度
    const hasLetter = /[a-zA-Z]/.test(value)
    const hasNumber = /\d/.test(value)
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value)
    
    if (!hasLetter && !hasNumber) {
      callback(new Error('密码应包含字母或数字'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

// 确认密码验证器
function validateConfirmPassword(rule: any, value: string, callback: Function) {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 同意协议验证器
function validateAgree(rule: any, value: boolean, callback: Function) {
  if (!value) {
    callback(new Error('请同意用户服务协议'))
  } else {
    callback()
  }
}

// 密码强度计算
const passwordStrength = computed(() => {
  const password = form.password
  if (!password) return { score: 0, text: '', class: '', percent: '0%' }
  
  let score = 0
  const checks = [
    password.length >= 8,                    // 长度
    /[a-z]/.test(password),                 // 小写字母
    /[A-Z]/.test(password),                 // 大写字母
    /\d/.test(password),                    // 数字
    /[!@#$%^&*(),.?":{}|<>]/.test(password) // 特殊字符
  ]
  
  score = checks.filter(Boolean).length
  
  if (score <= 1) return { score, text: '弱', class: 'weak', percent: '25%' }
  if (score <= 2) return { score, text: '一般', class: 'fair', percent: '50%' }
  if (score <= 3) return { score, text: '良好', class: 'good', percent: '75%' }
  return { score, text: '强', class: 'strong', percent: '100%' }
})

const passwordStrengthText = computed(() => passwordStrength.value.text)
const passwordStrengthClass = computed(() => passwordStrength.value.class)
const passwordStrengthPercent = computed(() => passwordStrength.value.percent)

// 监听密码变化，触发确认密码验证
watch(() => form.password, () => {
  if (form.confirmPassword) {
    formRef.value?.validateField('confirmPassword')
  }
})

// 处理注册
const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true

    await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password
    })
    
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (error: any) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}

// 显示服务条款
const showTerms = () => {
  ElMessageBox.alert(
    '这里是用户服务协议的内容...',
    '用户服务协议',
    {
      confirmButtonText: '确定'
    }
  )
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessageBox.alert(
    '这里是隐私政策的内容...',
    '隐私政策',
    {
      confirmButtonText: '确定'
    }
  )
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.register-box {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
  font-weight: 600;
}

.register-header p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.register-form {
  margin-bottom: 20px;
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 12px;
}

.strength-label {
  color: #666;
  white-space: nowrap;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s;
  border-radius: 2px;
}

.strength-fill.weak { background-color: #f56c6c; }
.strength-fill.fair { background-color: #e6a23c; }
.strength-fill.good { background-color: #909399; }
.strength-fill.strong { background-color: #67c23a; }

.strength-text {
  color: #666;
  white-space: nowrap;
  font-weight: 500;
}

.register-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.login-link {
  text-align: center;
  margin-bottom: 0;
}

.login-link span {
  color: #666;
  margin-right: 8px;
}

.features {
  display: flex;
  justify-content: space-around;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 12px;
}

.feature-icon {
  font-size: 24px;
  color: #409eff;
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

.shape {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.shape-2 {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  top: 70%;
  right: 10%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  border-radius: 30%;
  bottom: 10%;
  left: 15%;
  animation-delay: 4s;
}

.shape-4 {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  top: 30%;
  right: 20%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  33% {
    transform: translateY(-15px) rotate(120deg);
  }
  66% {
    transform: translateY(10px) rotate(240deg);
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-box {
    margin: 10px;
    padding: 30px 20px;
  }
  
  .register-header h1 {
    font-size: 24px;
  }
  
  .features {
    flex-direction: column;
    gap: 15px;
  }
  
  .feature-item {
    flex-direction: row;
    justify-content: center;
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

:deep(.el-checkbox__label) {
  font-size: 14px;
  color: #666;
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .register-box {
    background: rgba(30, 30, 30, 0.95);
  }
  
  .register-header h1 {
    color: #fff;
  }
  
  .register-header p {
    color: #ccc;
  }
  
  .login-link span {
    color: #ccc;
  }
  
  .strength-label,
  .strength-text {
    color: #ccc;
  }
  
  .features {
    border-top-color: #444;
  }
  
  .feature-item {
    color: #ccc;
  }
}
</style> 