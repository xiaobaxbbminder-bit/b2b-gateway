<template>
  <div class="login-container">
    <!-- 粒子/几何背景动效 -->
    <div class="particles">
      <div class="particle p1"></div>
      <div class="particle p2"></div>
      <div class="particle p3"></div>
      <div class="particle p4"></div>
      <div class="particle p5"></div>
      <div class="particle p6"></div>
      <div class="particle p7"></div>
      <div class="particle p8"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="white" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
          </svg>
        </div>
        <h1 class="title">SFTP RPA</h1>
        <p class="subtitle">文件传输管理系统</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleLogin"
            :loading="loading"
            size="large"
            class="login-btn"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '../api/user'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const { data } = await authApi.login(loginForm)
    if (data.code === 20000) {
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('username', data.data.username)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(data.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error('登录失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 40%, rgba(102, 126, 234, 0.15) 0%, transparent 50%),
              radial-gradient(circle at 70% 60%, rgba(118, 75, 162, 0.1) 0%, transparent 50%);
  animation: aurora 20s ease-in-out infinite;
}

@keyframes aurora {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(2%, -2%) rotate(1deg); }
  50% { transform: translate(-1%, 1%) rotate(-1deg); }
  75% { transform: translate(1%, 2%) rotate(0.5deg); }
}

/* 粒子/几何背景动效 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.particle {
  position: absolute;
  border-radius: 50%;
  opacity: 0;
  animation: float linear infinite;
}

.p1 { width: 80px; height: 80px; background: rgba(255, 255, 255, 0.06); top: 10%; left: 15%; animation-duration: 20s; }
.p2 { width: 40px; height: 40px; background: rgba(255, 255, 255, 0.08); top: 70%; left: 80%; border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%; animation-duration: 18s; animation-delay: -3s; }
.p3 { width: 60px; height: 60px; background: rgba(255, 255, 255, 0.05); top: 40%; left: 70%; animation-duration: 22s; animation-delay: -5s; }
.p4 { width: 30px; height: 30px; background: rgba(255, 255, 255, 0.1); top: 80%; left: 20%; border-radius: 40%; animation-duration: 15s; animation-delay: -2s; }
.p5 { width: 50px; height: 50px; background: rgba(255, 255, 255, 0.04); top: 20%; left: 60%; animation-duration: 25s; animation-delay: -7s; }
.p6 { width: 20px; height: 20px; background: rgba(255, 255, 255, 0.12); top: 55%; left: 10%; border-radius: 50% 50% 20% 80%; animation-duration: 16s; animation-delay: -4s; }
.p7 { width: 70px; height: 70px; background: rgba(255, 255, 255, 0.03); top: 85%; left: 50%; border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%; animation-duration: 23s; animation-delay: -6s; }
.p8 { width: 35px; height: 35px; background: rgba(255, 255, 255, 0.07); top: 5%; left: 85%; animation-duration: 19s; animation-delay: -1s; }

@keyframes float {
  0% { opacity: 0; transform: translateY(0) rotate(0deg) scale(0.8); }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; transform: translateY(-100vh) rotate(720deg) scale(1.2); }
}

/* 登录卡片 */
.login-card {
  width: 480px;
  padding: 48px 44px 40px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4),
              0 0 80px rgba(102, 126, 234, 0.1),
              inset 0 1px 0 rgba(255, 255, 255, 0.1);
  z-index: 1;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 头部区域 */
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  margin-bottom: 20px;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.05); opacity: 1; }
}

.title {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 3px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.subtitle {
  margin: 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 1.5px;
}

/* 表单样式 */
.login-form {
  margin-top: 8px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input) {
  --el-input-bg-color: transparent;
  --el-input-border-color: rgba(255, 255, 255, 0.08);
  --el-input-hover-border-color: rgba(255, 255, 255, 0.2);
  --el-input-focus-border-color: rgba(102, 126, 234, 0.5);
  --el-input-text-color: #ffffff;
  --el-input-placeholder-color: rgba(255, 255, 255, 0.4);
  --el-fill-color-blank: transparent;
  width: 100%;
}

.login-form :deep(.el-input__wrapper) {
  background-color: transparent !important;
  background-image: none !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
  border: none;
  border-radius: 10px;
  padding-left: 12px;
  padding-right: 0;
  height: 42px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.5) inset, 0 0 0 3px rgba(102, 126, 234, 0.12) !important;
}

.login-form :deep(.el-input__inner) {
  background: transparent !important;
  color: #ffffff;
  font-size: 14px;
  width: 100%;
  height: 42px;
  line-height: 42px;
  border-radius: 0;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.login-form :deep(.el-input__prefix) {
  display: flex;
  align-items: center;
  left: 4px;
}

.login-form :deep(.el-input__prefix .el-icon) {
  color: rgba(255, 255, 255, 0.5);
  font-size: 16px;
}

.login-form :deep(.el-input__prefix + .el-input__inner) {
  padding-left: 28px;
}

.login-form :deep(.el-input__suffix) {
  display: none;
}

.login-form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
  padding-bottom: 8px;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  margin-top: 8px;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.45);
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3);
}
</style>

<style>
.login-form .el-input__wrapper {
  background-color: transparent !important;
  background-image: none !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
}

.login-form .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset !important;
}

.login-form .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.5) inset, 0 0 0 3px rgba(102, 126, 234, 0.12) !important;
}

.login-form .el-input__inner {
  background: transparent !important;
  color: #ffffff;
}
</style>
