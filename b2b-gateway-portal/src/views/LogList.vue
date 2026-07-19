<template>
  <div class="container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2 style="margin: 0;">日志管理</h2>
        </div>
      </template>

      <el-table :data="logs" style="width: 100%" v-loading="loading">
        <el-table-column label="会话ID" width="140">
          <template #default="{ row }">
            <el-tooltip :content="row.sessionId" placement="top" :disabled="!row.sessionId">
              <span class="text-ellipsis">{{ row.sessionId }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="用户名" width="120">
          <template #default="{ row }">
            <el-tooltip :content="row.username" placement="top" :disabled="!row.username">
              <span class="text-ellipsis">{{ row.username }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="文件路径" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.filePath" placement="top" :disabled="!row.filePath">
              <span class="text-ellipsis">{{ row.filePath }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="120">
          <template #default="{ row }">
            <el-tooltip :content="row.action" placement="top" :disabled="!row.action">
              <span class="text-ellipsis">{{ row.action }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="100">
          <template #default="{ row }">
            {{ row.fileSize ? formatBytes(row.fileSize) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="客户端地址" width="140">
          <template #default="{ row }">
            <el-tooltip :content="row.clientAddress" placement="top" :disabled="!row.clientAddress">
              <span class="text-ellipsis">{{ row.clientAddress }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="80">
          <template #default="{ row }">
            {{ getDuration(row.startTime, row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="goToDetail(row)">详情</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[15, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadLogs"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logApi } from '../api/log'

const router = useRouter()
const logs = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(15)
const total = ref(0)

const statusLabels = {
  SUCCESS: '成功',
  ERROR: '失败',
  PENDING: '处理中'
}

const getStatusLabel = (status) => statusLabels[status] || status

const getStatusType = (status) => {
  const types = {
    SUCCESS: 'success',
    ERROR: 'danger',
    PENDING: 'warning'
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

const loadLogs = async () => {
  loading.value = true
  try {
    const { data } = await logApi.list(pageNum.value - 1, pageSize.value)
    logs.value = data.data.content
    total.value = data.data.totalElements
  } catch (error) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  loadLogs()
}

const goToDetail = (log) => {
  router.push({ name: 'LogDetail', params: { id: log.id } })
}

const handleDelete = async (log) => {
  try {
    await ElMessageBox.confirm('确定要删除该日志吗？', '确认删除', {
      type: 'warning'
    })
    const { data } = await logApi.delete(log.id)
    if (data.code !== 20000) {
      ElMessage.error(data.message || '删除失败')
      return
    }
    ElMessage.success('删除成功')
    loadLogs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}

.text-ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
