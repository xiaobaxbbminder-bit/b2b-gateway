<template>
  <div class="container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button text @click="goBack">
              <el-icon>
                <ArrowLeft />
              </el-icon>
              返回
            </el-button>
            <h2 style="margin: 0;">日志详情</h2>
          </div>
        </div>
      </template>

      <div class="log-info" v-if="log">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="用户名">{{ log.username }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ log.action }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="getStatusType(log.status)">
              {{ log.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="文件路径" :span="3">
            <span class="file-path">{{ log.filePath }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="客户端地址">{{ log.clientAddress }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ log.fileSize ? formatBytes(log.fileSize) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ getDuration(log.startTime, log.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(log.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatTime(log.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ log.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider content-position="left">操作明细</el-divider>

      <el-table :data="details" style="width: 100%" v-loading="detailsLoading">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.logTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="200">
          <template #default="{ row }">
            <el-tooltip placement="top" :disabled="!row.description" :content="row.description">
              <span class="ellipsis">{{ row.description || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="上下文" min-width="120">
          <template #default="{ row }">
            <el-popover v-if="row.context" placement="left" trigger="click" width="400" popper-class="log-detail-ctx-popover">
              <template #reference>
                <el-button text size="small">查看</el-button>
              </template>
              <pre class="context-json">{{ formatContext(row.context) }}</pre>
            </el-popover>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { logApi } from '../api/log'

const route = useRoute()
const router = useRouter()
const logId = route.params.id

const loading = ref(false)
const detailsLoading = ref(false)
const log = ref(null)
const details = ref([])

const getStatusType = (status) => {
  const types = {
    SUCCESS: 'info',
    COMPLETED: 'success',
    ERROR: 'danger',
    PENDING: 'info'
  }
  return types[status] || 'info'
}

const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getDuration = (start, end) => {
  if (!start || !end) return '-'
  const ms = new Date(end) - new Date(start)
  if (ms < 1000) return ms + 'ms'
  return (ms / 1000).toFixed(1) + 's'
}

const formatContext = (ctx) => {
  if (!ctx) return '-'
  try {
    return JSON.stringify(JSON.parse(ctx), null, 2)
  } catch {
    return ctx
  }
}

const goBack = () => {
  router.push({ name: 'LogList' })
}

const loadLog = async () => {
  loading.value = true
  try {
    const { data } = await logApi.get(logId)
    log.value = data.data
  } catch (error) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

const loadDetails = async () => {
  detailsLoading.value = true
  try {
    const { data } = await logApi.getDetails(logId)
    details.value = data.data
  } catch (error) {
    ElMessage.error('加载日志明细失败')
  } finally {
    detailsLoading.value = false
  }
}

onMounted(() => {
  loadLog()
  loadDetails()
})
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-info {
  margin-bottom: 8px;
}

.file-path {
  font-family: monospace;
  color: rgba(255, 255, 255, 0.9);
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.context-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow-y: auto;
}
</style>
