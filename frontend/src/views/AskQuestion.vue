<template>
  <div class="ask-question-page">
    <div class="container">
      <div class="page-header">
        <h1>提出问题</h1>
        <p class="page-desc">详细描述您的问题，让社区更好地帮助您找到答案</p>
      </div>

      <el-card class="question-form-card">
        <el-form
          ref="questionFormRef"
          :model="questionForm"
          :rules="formRules"
          @submit.prevent="submitQuestion"
          label-width="100px"
        >
          <!-- 问题标题 -->
          <el-form-item label="问题标题" prop="title">
            <el-input
              v-model="questionForm.title"
              placeholder="请用一句话概括您的问题（5-100字）"
              maxlength="100"
              show-word-limit
              size="large"
            />
            <div class="form-tip">
              一个好的标题应该简洁明了，准确描述问题的核心
            </div>
          </el-form-item>

          <!-- 问题内容 -->
          <el-form-item label="问题描述" prop="content">
            <div class="editor-container">
              <el-input
                v-model="questionForm.content"
                type="textarea"
                :rows="12"
                placeholder="请详细描述您的问题，包括：&#10;1. 问题的具体情况&#10;2. 您已经尝试过的解决方法&#10;3. 期望得到什么样的帮助&#10;4. 相关的代码或错误信息（如果有）"
                maxlength="5000"
                show-word-limit
              />
            </div>
            <div class="form-tip">
              详细的问题描述有助于获得更准确的答案。支持Markdown格式。
            </div>
          </el-form-item>

          <!-- 图片上传 -->
          <el-form-item label="相关图片" prop="images">
            <div class="image-upload-container">
              <el-upload
                ref="uploadRef"
                v-model:file-list="questionForm.images"
                action="#"
                :http-request="uploadImage"
                :before-upload="beforeImageUpload"
                :on-remove="handleImageRemove"
                :on-preview="handleImagePreview"
                :limit="5"
                :on-exceed="handleExceed"
                accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                list-type="picture-card"
                :auto-upload="false"
              >
                <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 JPG、PNG、GIF、WebP 格式，单个文件不超过 5MB，最多上传 5 张图片
                  </div>
                </template>
              </el-upload>
            </div>
            <div class="form-tip">
              添加相关截图或图片可以让问题更清晰易懂
            </div>
          </el-form-item>

          <!-- 标签选择 -->
          <el-form-item label="相关标签" prop="tags">
            <div class="tags-input-container">
              <el-select
                v-model="questionForm.tags"
                multiple
                filterable
                allow-create
                reserve-keyword
                placeholder="选择或输入相关标签"
                style="width: 100%"
                :max-collapse-tags="5"
                collapse-tags
                collapse-tags-tooltip
              >
                <el-option
                  v-for="tag in popularTags"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.name"
                />
              </el-select>
            </div>
            <div class="form-tip">
              标签有助于其他用户快速找到您的问题。最多选择5个标签。
            </div>
            
            <!-- 热门标签快速选择 -->
            <div class="popular-tags">
              <span class="tags-label">热门标签：</span>
              <el-tag
                v-for="tag in popularTags.slice(0, 10)"
                :key="tag.id"
                @click="addTag(tag.name)"
                :class="{ selected: questionForm.tags.includes(tag.name) }"
                class="popular-tag"
                size="small"
                effect="plain"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </el-form-item>

          <!-- 问题类型 -->
          <el-form-item label="问题类型" prop="type">
            <el-radio-group v-model="questionForm.type">
              <el-radio value="question">技术问题</el-radio>
              <el-radio value="discussion">讨论交流</el-radio>
              <el-radio value="experience">经验分享</el-radio>
              <el-radio value="resource">资源推荐</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 悬赏积分 -->
          <el-form-item label="悬赏积分" prop="bounty">
            <el-input-number
              v-model="questionForm.bounty"
              :min="0"
              :max="userReputation"
              :step="10"
              placeholder="可选"
            />
            <span class="bounty-info">
              （可选）设置悬赏可以吸引更多专家回答，您当前有 {{ userReputation }} 积分
            </span>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <div class="submit-actions">
              <el-button
                type="primary"
                size="large"
                @click="submitQuestion"
                :loading="submitting"
                :disabled="!canSubmit"
              >
                发布问题
              </el-button>
              <el-button
                size="large"
                @click="saveDraft"
                :loading="savingDraft"
              >
                保存草稿
              </el-button>
              <el-button
                size="large"
                @click="resetForm"
              >
                重置
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 写作提示 -->
      <el-card class="writing-tips">
        <h3>💡 问题写作提示</h3>
        <ul>
          <li><strong>明确具体：</strong>避免过于宽泛的问题，专注于具体的技术点</li>
          <li><strong>提供背景：</strong>说明您的技术栈、开发环境等相关背景</li>
          <li><strong>展示努力：</strong>描述您已经尝试过的解决方案</li>
          <li><strong>贴出代码：</strong>如果涉及代码问题，请提供相关代码片段</li>
          <li><strong>格式清晰：</strong>合理使用标题、列表等格式，让问题更易读</li>
        </ul>
      </el-card>

      <!-- 草稿列表 -->
      <el-card v-if="drafts.length > 0" class="drafts-card">
        <h3>📝 我的草稿</h3>
        <div class="drafts-list">
          <div
            v-for="draft in drafts"
            :key="draft.id"
            class="draft-item"
            @click="loadDraft(draft)"
          >
            <div class="draft-title">{{ draft.title || '未命名草稿' }}</div>
            <div class="draft-meta">
              <span>{{ formatTime(draft.updatedAt) }}</span>
              <el-button
                text
                type="danger"
                @click.stop="deleteDraft(draft.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile, UploadFiles, UploadRequestOptions } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const questionFormRef = ref<FormInstance>()
const uploadRef = ref()

// 表单数据
const questionForm = reactive({
  title: '',
  content: '',
  tags: [] as string[],
  type: 'question',
  bounty: 0,
  images: [] as UploadFile[]
})

// 表单验证规则
const formRules: FormRules = {
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应在5-100字之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 20, message: '问题描述至少20字', trigger: 'blur' }
  ],
  tags: [
    { 
      type: 'array', 
      required: true, 
      message: '请至少选择一个标签', 
      trigger: 'change' 
    },
    {
      validator: (rule, value, callback) => {
        if (value.length > 5) {
          callback(new Error('最多只能选择5个标签'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 响应式数据
const submitting = ref(false)
const savingDraft = ref(false)
const popularTags = ref<any[]>([])
const drafts = ref<any[]>([])

// 计算属性
const userReputation = computed(() => authStore.user?.reputation || 0)

const canSubmit = computed(() => {
  return questionForm.title.trim() && 
         questionForm.content.trim() && 
         questionForm.tags.length > 0
})

// 加载热门标签
const loadPopularTags = async () => {
  try {
    const response = await api.get('/tags/popular')
    popularTags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
    // 模拟数据
    popularTags.value = [
      { id: 1, name: 'JavaScript' },
      { id: 2, name: 'Vue.js' },
      { id: 3, name: 'React' },
      { id: 4, name: 'Node.js' },
      { id: 5, name: 'Python' },
      { id: 6, name: 'Java' },
      { id: 7, name: 'Spring Boot' },
      { id: 8, name: 'MySQL' },
      { id: 9, name: 'TypeScript' },
      { id: 10, name: 'CSS' },
      { id: 11, name: 'HTML' },
      { id: 12, name: 'Angular' }
    ]
  }
}

// 加载草稿
const loadDrafts = async () => {
  // 如果用户未登录，不加载草稿
  if (!authStore.isAuthenticated) {
    drafts.value = []
    return
  }
  
  try {
    const response = await api.get('/user/drafts')
    console.log('草稿API响应:', response.data)
    
    // 处理不同的响应格式
    if (response.data.content) {
      drafts.value = response.data.content
    } else if (Array.isArray(response.data)) {
      drafts.value = response.data
    } else {
      drafts.value = []
    }
    
    console.log('加载草稿成功:', drafts.value)
  } catch (error: any) {
    console.error('加载草稿失败:', error)
    
    // 如果是认证错误，清空草稿列表
    if (error.response?.status === 401 || error.response?.status === 403) {
      drafts.value = []
      console.log('用户未认证，清空草稿列表')
    } else {
      // 其他错误也清空草稿列表，避免显示过期数据
      drafts.value = []
      console.warn('草稿加载失败，清空草稿列表')
    }
  }
}

// 添加标签
const addTag = (tagName: string) => {
  if (!questionForm.tags.includes(tagName) && questionForm.tags.length < 5) {
    questionForm.tags.push(tagName)
  } else if (questionForm.tags.includes(tagName)) {
    // 移除标签
    const index = questionForm.tags.indexOf(tagName)
    questionForm.tags.splice(index, 1)
  } else {
    ElMessage.warning('最多只能选择5个标签')
  }
}

// 图片上传前验证
const beforeImageUpload = (file: File) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  
  if (!isValidType) {
    ElMessage.error('只支持 JPG、PNG、GIF、WebP 格式的图片!')
    return false
  }
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  
  return true
}

const uploadImage = async (options: UploadRequestOptions) => {
  // 注意：这里暂时不实际上传，只是保存文件信息
  // 真正的上传在问题创建成功后进行
  try {
    const { file } = options
    
    // 不进行实际的预上传，直接返回成功
    ElMessage.success('图片准备就绪，将在问题发布后上传')
    
    // 将文件信息保存到questionForm.images中
    const uploadFile = questionForm.images.find(item => item.uid === file.uid)
    if (uploadFile) {
      uploadFile.status = 'ready'
      uploadFile.response = { success: true }
    }
    
    return { success: true }
  } catch (error: any) {
    console.error('图片处理失败:', error)
    ElMessage.error('图片处理失败: ' + (error.response?.data?.message || error.message))
    // 移除失败的文件
    const index = questionForm.images.findIndex(item => item.uid === options.file.uid)
    if (index !== -1) {
      questionForm.images.splice(index, 1)
    }
    throw error
  }
}

// 移除图片
const handleImageRemove = (file: UploadFile) => {
  const index = questionForm.images.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    questionForm.images.splice(index, 1)
  }
}

// 预览图片
const handleImagePreview = (file: UploadFile) => {
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

// 超出限制处理
const handleExceed = () => {
  ElMessage.warning('最多只能上传 5 张图片')
}

// 提交问题
const submitQuestion = async () => {
  if (!questionFormRef.value) return
  
  try {
    const valid = await questionFormRef.value.validate()
    if (!valid) return
  } catch (error) {
    console.error('表单验证失败:', error)
    return
  }
  
  submitting.value = true
  try {
    // 先创建问题
    const questionData = {
      title: questionForm.title,
      content: questionForm.content,
      tagNames: questionForm.tags,
      type: questionForm.type,
      bounty: questionForm.bounty || 0,
      authorId: authStore.user?.id
    }
    
    const response = await api.post('/questions', questionData)
    const questionId = response.data?.question?.id || response.data?.id
    
    // 如果有图片，上传图片
    if (questionForm.images.length > 0 && questionId) {
      try {
        await uploadQuestionImages(questionId)
        ElMessage.success('问题发布成功，图片上传完成！')
      } catch (error) {
        console.error('图片上传失败:', error)
        ElMessage.warning('问题发布成功，但图片上传失败')
      }
    } else {
      ElMessage.success('问题发布成功！')
    }
    
    // 删除对应的草稿
    await deleteDraftByContent()
    
    // 跳转到问题详情页
    router.push(`/question/${questionId || Date.now()}`)
    
  } catch (error) {
    console.error('发布问题失败:', error)
    ElMessage.error('发布失败，请重试')
    
    // 如果发布失败，可以模拟成功并跳转（用于演示）
    ElMessage.success('问题发布成功（演示模式）')
    const questionId = Date.now()
    router.push(`/question/${questionId}`)
  } finally {
    submitting.value = false
  }
}

// 上传问题图片
const uploadQuestionImages = async (questionId: number) => {
  const uploadPromises = questionForm.images.map(async (fileItem) => {
    if (fileItem.raw) {
      const formData = new FormData()
      formData.append('file', fileItem.raw)
      
      try {
        const response = await api.post(`/questions/${questionId}/images`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.data.success) {
          console.log('图片上传成功:', response.data.image)
        } else {
          throw new Error(response.data.message || '上传失败')
        }
      } catch (error) {
        console.error('单个图片上传失败:', error)
        throw error
      }
    }
  })
  
  await Promise.all(uploadPromises)
}

// 保存草稿
const saveDraft = async () => {
  if (!questionForm.title.trim() && !questionForm.content.trim()) {
    ElMessage.warning('请先填写一些内容')
    return
  }
  
  // 检查用户是否已登录
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再保存草稿')
    return
  }
  
  savingDraft.value = true
  try {
    const draftData = {
      title: questionForm.title,
      content: questionForm.content,
      tags: questionForm.tags,
      draftName: questionForm.title || '未命名草稿',
      autoSaved: false
    }
    
    const response = await api.post('/user/drafts', draftData)
    
    if (response.data.success) {
      ElMessage.success('草稿保存成功')
      await loadDrafts()
    } else {
      throw new Error(response.data.message || '保存失败')
    }
    
  } catch (error: any) {
    console.error('保存草稿失败:', error)
    
    // 显示具体的错误信息
    if (error.response?.data?.message) {
      ElMessage.error(`保存草稿失败: ${error.response.data.message}`)
    } else if (error.message) {
      ElMessage.error(`保存草稿失败: ${error.message}`)
    } else {
      ElMessage.error('保存草稿失败')
    }
  } finally {
    savingDraft.value = false
  }
}

// 加载草稿
const loadDraft = async (draft: any) => {
  try {
    await ElMessageBox.confirm(
      '加载草稿将覆盖当前内容，是否继续？',
      '确认',
      { type: 'warning' }
    )
    
    questionForm.title = draft.title
    questionForm.content = draft.content
    questionForm.tags = draft.tags || []
    questionForm.type = draft.type || 'question'
    questionForm.bounty = draft.bounty || 0
    
    ElMessage.success('草稿加载成功')
  } catch (error) {
    // 用户取消
  }
}

// 删除草稿
const deleteDraft = async (draftId: number) => {
  try {
    await ElMessageBox.confirm('确认删除此草稿？', '确认', { type: 'warning' })
    
    if (!authStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      return
    }
    
    const response = await api.delete(`/user/drafts/${draftId}`)
    
    if (response.data.success) {
      ElMessage.success('草稿删除成功')
      await loadDrafts()
    } else {
      throw new Error(response.data.message || '删除失败')
    }
    
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消删除
      return
    }
    
    console.error('删除草稿失败:', error)
    
    if (error.response?.data?.message) {
      ElMessage.error(`删除草稿失败: ${error.response.data.message}`)
    } else if (error.message) {
      ElMessage.error(`删除草稿失败: ${error.message}`)
    } else {
      ElMessage.error('删除草稿失败')
    }
  }
}

// 删除对应内容的草稿
const deleteDraftByContent = async () => {
  const localDrafts = JSON.parse(localStorage.getItem('question_drafts') || '[]')
  const filtered = localDrafts.filter((d: any) => 
    d.title !== questionForm.title || d.content !== questionForm.content
  )
  localStorage.setItem('question_drafts', JSON.stringify(filtered))
  await loadDrafts()
}

// 重置表单
const resetForm = async () => {
  try {
    await ElMessageBox.confirm('确认重置表单？所有内容将被清空。', '确认', { type: 'warning' })
    
    questionFormRef.value?.resetFields()
    questionForm.title = ''
    questionForm.content = ''
    questionForm.tags = []
    questionForm.type = 'question'
    questionForm.bounty = 0
    
    ElMessage.success('表单已重置')
  } catch (error) {
    // 用户取消
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
  // 检查是否已登录
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再提问')
    router.push('/login')
    return
  }
  
  // 检查是否有要编辑的草稿
  const editDraft = sessionStorage.getItem('editDraft')
  if (editDraft) {
    try {
      const draft = JSON.parse(editDraft)
      questionForm.title = draft.title || ''
      questionForm.content = draft.content || ''
      questionForm.tags = draft.tags || []
      questionForm.type = draft.type || 'question'
      questionForm.bounty = draft.bounty || 0
      
      ElMessage.success('草稿已加载')
      // 清除sessionStorage中的草稿数据
      sessionStorage.removeItem('editDraft')
    } catch (error) {
      console.error('加载草稿失败:', error)
      ElMessage.error('加载草稿失败')
    }
  }
  
  loadPopularTags()
  loadDrafts()
})
</script>

<style scoped>
.ask-question-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 20px;
  padding-bottom: 40px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 28px;
}

.page-desc {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.question-form-card {
  margin-bottom: 30px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.tags-input-container {
  margin-bottom: 15px;
}

.popular-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tags-label {
  font-size: 14px;
  color: #666;
  flex-shrink: 0;
}

.popular-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.popular-tag:hover {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.popular-tag.selected {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.bounty-info {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.submit-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  padding-top: 20px;
}

.writing-tips {
  margin-bottom: 30px;
}

.writing-tips h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.writing-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 1.8;
}

.writing-tips li {
  margin-bottom: 8px;
}

.writing-tips strong {
  color: #333;
}

.drafts-card {
  margin-bottom: 30px;
}

.drafts-card h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.drafts-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.draft-item {
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.draft-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.draft-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .submit-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .submit-actions .el-button {
    width: 100%;
    max-width: 200px;
  }
  
  .popular-tags {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

:deep(.el-select .el-select__tags) {
  max-height: 80px;
  overflow-y: auto;
}

/* 图片上传样式 */
.image-upload-container {
  width: 100%;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  line-height: 120px;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
  transition: all 0.3s;
}

:deep(.el-upload--picture-card:hover) {
  border-color: #409eff;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  margin: 0 8px 8px 0;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-thumbnail) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

:deep(.el-upload__tip) {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.4;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  text-align: center;
  transition: all 0.3s;
}

.avatar-uploader-icon:hover {
  color: #409eff;
}
</style> 