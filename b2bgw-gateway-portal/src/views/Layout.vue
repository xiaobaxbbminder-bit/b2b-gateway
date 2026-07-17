<template>
  <div class="layout">
    <div class="layout-left">
      <div class="logo">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="white" stroke-width="1.5">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
        </svg>
        <span class="logo-text">B2B 网关</span>
      </div>

      <el-menu :default-active="activeFirst" router class="left-menu">
        <el-menu-item index="/file-transfer/user">
          <el-icon><Connection /></el-icon>
          <span>SFTP</span>
        </el-menu-item>
        <el-menu-item index="/rosettanet">
          <el-icon><Link /></el-icon>
          <span>RosettaNet</span>
        </el-menu-item>
        <el-menu-item index="/as2">
          <el-icon><Cpu /></el-icon>
          <span>AS2</span>
        </el-menu-item>
        <el-menu-item index="/open-api">
          <el-icon><Open /></el-icon>
          <span>Open API</span>
        </el-menu-item>
        <el-menu-item index="/dict">
          <el-icon><Collection /></el-icon>
          <span>字典管理</span>
        </el-menu-item>
        <el-menu-item index="/ai">
          <el-icon><ChatDotSquare /></el-icon>
          <span>问答助手</span>
        </el-menu-item>
      </el-menu>

      <div class="left-footer">
        <div class="user-info">
          <el-icon><UserFilled /></el-icon>
          <span>{{ username }}</span>
        </div>
        <el-button text class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-button>
      </div>
    </div>

    <div class="layout-middle" v-if="showSubMenu">
      <div class="middle-title">{{ subMenuTitle }}</div>
      <el-menu :default-active="activeSecond" router class="middle-menu">
        <template v-if="route.path.startsWith('/file-transfer')">
          <el-menu-item index="/file-transfer/user">用户管理</el-menu-item>
          <el-menu-item index="/file-transfer/service">服务管理</el-menu-item>
          <el-menu-item index="/file-transfer/log">日志管理</el-menu-item>
          <el-menu-item index="/file-transfer/traffic">流量控制</el-menu-item>
          <el-menu-item index="/file-transfer/plugin-definitions">插件定义</el-menu-item>
        </template>
        <template v-else-if="route.path.startsWith('/rosettanet')">
          <el-menu-item index="/rosettanet/partners">合作伙伴</el-menu-item>
          <el-menu-item index="/rosettanet/personality">通信主体</el-menu-item>
          <el-menu-item index="/rosettanet/pips">PIP 管理</el-menu-item>
          <el-menu-item index="/rosettanet/configs">通信配置</el-menu-item>
        </template>
      </el-menu>
    </div>

    <div class="layout-main">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Connection, Collection, Link, Cpu, Open, ChatDotSquare, UserFilled, SwitchButton } from '@element-plus/icons-vue'
import { authApi } from '../api/user'

const route = useRoute()
const router = useRouter()
const username = localStorage.getItem('username') || 'Admin'

const activeFirst = computed(() => {
  const path = route.path
  if (path.startsWith('/file-transfer')) return '/file-transfer/user'
  if (path.startsWith('/rosettanet')) return '/rosettanet/partners'
  return path
})

const activeSecond = computed(() => route.path)

const showSubMenu = computed(() =>
  route.path.startsWith('/file-transfer') || route.path.startsWith('/rosettanet')
)

const subMenuTitle = computed(() => {
  if (route.path.startsWith('/file-transfer')) return 'SFTP'
  if (route.path.startsWith('/rosettanet')) return 'RosettaNet'
  return ''
})

const handleLogout = async () => {
  try { await authApi.logout() } catch {}
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background: rgba(0, 0, 0, 0.3);
}

.layout-left {
  width: 260px;
  min-width: 260px;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
}

.left-menu {
  flex: 1;
  border-right: none !important;
  background: transparent !important;
  padding: 8px;
  overflow-y: auto;
}

.left-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  margin-bottom: 2px;
  height: 42px;
  line-height: 42px;
}

.left-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
}

.left-menu .el-menu-item.is-active {
  background: rgba(102, 126, 234, 0.2);
  color: #a0b4f0;
}

.left-menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 8px;
}

.left-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 4px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

.logout-btn {
  width: 100%;
  justify-content: flex-start;
  color: rgba(255, 255, 255, 0.5) !important;
  margin-top: 4px;
  height: 34px;
  border-radius: 6px;
}

.logout-btn:hover {
  color: #f89898 !important;
  background: rgba(245, 108, 108, 0.1) !important;
}

.layout-middle {
  width: 260px;
  min-width: 260px;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
}

.middle-title {
  padding: 20px 16px 12px;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.middle-menu {
  flex: 1;
  border-right: none !important;
  background: transparent !important;
  padding: 0 8px 8px;
  overflow-y: auto;
}

.middle-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.55);
  border-radius: 6px;
  margin-bottom: 2px;
  height: 36px;
  line-height: 36px;
}

.middle-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
}

.middle-menu .el-menu-item.is-active {
  background: rgba(102, 126, 234, 0.2);
  color: #a0b4f0;
}

.layout-main {
  flex: 1;
  overflow-y: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  padding: 24px 12px;
}
</style>
