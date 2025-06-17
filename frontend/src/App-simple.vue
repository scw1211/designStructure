<template>
  <div>
    <h1 style="text-align: center; color: #409eff; margin-top: 100px;">
      问答平台测试页面
    </h1>
    <p style="text-align: center;">
      如果您能看到这个页面，说明Vue应用正在运行！
    </p>
    <div style="text-align: center; margin-top: 20px;">
      <button @click="count++">点击次数: {{ count }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const count = ref(0)
</script> 