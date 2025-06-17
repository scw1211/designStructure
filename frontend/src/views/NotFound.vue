<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="$router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  
  .not-found-content {
    text-align: center;
    
    h1 {
      font-size: 120px;
      color: #409eff;
      margin: 0;
    }
    
    h2 {
      font-size: 32px;
      color: #303133;
      margin: 20px 0;
    }
    
    p {
      font-size: 16px;
      color: #606266;
      margin-bottom: 30px;
    }
  }
}
</style> 