<template>
  <div class="container">
    <el-card v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2 style="margin: 0;">字典管理</h2>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="currentType" placeholder="选择字典类型" style="width: 280px;" @change="loadItems">
          <el-option v-for="t in types" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button v-if="currentType" size="small" type="primary" @click="showCreate">新增</el-button>
      </div>

      <el-table :data="items" style="width: 100%" v-if="currentType">
        <el-table-column prop="key" label="键" min-width="160" />
        <el-table-column prop="value" label="值" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!currentType" class="empty-hint">请先选择上方的字典类型</div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑字典项' : '新增字典项'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型">
          <el-input v-model="form.type" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="键">
          <el-input v-model="form.key" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="form.value" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dictApi } from '../api/dict'

const loading = ref(false)
const saveLoading = ref(false)
const types = ref([])
const currentType = ref('')
const items = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const form = ref({ type: '', key: '', value: '', sort: 0, enabled: true })

async function loadTypes() {
  const { data } = await dictApi.getTypes()
  types.value = data.data || []
}

async function loadItems() {
  if (!currentType.value) return
  loading.value = true
  try {
    const { data } = await dictApi.getByType(currentType.value)
    items.value = data.data || []
  } finally {
    loading.value = false
  }
}

function showCreate() {
  isEdit.value = false
  editingId.value = null
  form.value = { type: currentType.value, key: '', value: '', sort: 0, enabled: true }
  dialogVisible.value = true
}

function showEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  form.value = { type: row.type, key: row.key, value: row.value, sort: row.sort, enabled: row.enabled }
  dialogVisible.value = true
}

async function handleSave() {
  saveLoading.value = true
  try {
    if (isEdit.value) {
      await dictApi.update(editingId.value, form.value)
    } else {
      await dictApi.create(form.value)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await loadItems()
  } catch (e) {
    ElMessage.error('操作失败：' + (e.response?.data?.message || e.message))
  } finally {
    saveLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.key}" 吗？`, '确认删除', { type: 'warning' })
    await dictApi.delete(row.id)
    ElMessage.success('删除成功')
    await loadItems()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadTypes)
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.empty-hint {
  text-align: center;
  padding: 60px 0;
  color: rgba(255, 255, 255, 0.3);
  font-size: 14px;
}
</style>
