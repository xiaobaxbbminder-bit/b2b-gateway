<template>
  <div class="placeholder">
    <el-icon :size="48"><WarningFilled /></el-icon>
    <h3>功能开发中，敬请期待</h3>
  </div>
</template>

<script setup>
import { WarningFilled } from '@element-plus/icons-vue'
</script>

<style scoped>
.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
  color: rgba(255, 255, 255, 0.3);
  gap: 16px;
}
</style>
