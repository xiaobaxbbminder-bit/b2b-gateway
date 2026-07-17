<template>
  <div class="container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2 style="margin: 0;">插件定义管理</h2>
        </div>
      </template>

      <el-button type="primary" @click="goCreate" style="margin-bottom: 16px;">
        新增
      </el-button>

      <el-table :data="definitions" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="标识" width="140" show-overflow-tooltip />
        <el-table-column prop="label" label="名称" width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="可见规则数" width="100">
          <template #default="{ row }">
            {{ row.visibility?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="goEdit(row.id)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { pluginDefApi } from '../api/plugin-definition'

const router = useRouter()
const loading = ref(false)
const definitions = ref([])

const loadDefinitions = async () => {
  loading.value = true
  try {
    const { data } = await pluginDefApi.list()
    definitions.value = data.data
  } catch {
    ElMessage.error('加载插件定义失败')
  } finally {
    loading.value = false
  }
}

const goCreate = () => {
  router.push({ name: 'PluginDefEdit', params: { id: 'new' } })
}

const goEdit = (id) => {
  router.push({ name: 'PluginDefEdit', params: { id } })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除插件 "${row.label}" 吗？`, '确认删除', { type: 'warning' })
    await pluginDefApi.delete(row.id)
    ElMessage.success('删除成功')
    loadDefinitions()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadDefinitions()
})
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}
</style>
