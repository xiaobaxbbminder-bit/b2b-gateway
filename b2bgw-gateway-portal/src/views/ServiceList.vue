<template>
  <div class="container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2 style="margin: 0;">服务管理</h2>
        </div>
      </template>

      <el-button type="primary" @click="showCreateDialog" style="margin-bottom: 16px;">
        创建服务
      </el-button>

      <el-table :data="services" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="服务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="username" label="关联用户" min-width="120" show-overflow-tooltip />
        <el-table-column label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.userType === 'server' ? 'primary' : 'warning'">
              {{ row.userType === 'server' ? '服务端' : '客户端' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button size="small" plain @click="goToConfig(row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑服务对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑服务' : '创建服务'" width="50%">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="关联用户" prop="userId">
          <el-select v-model="form.userId" placeholder="请选择用户" style="width: 100%;"
            :disabled="isEdit">
            <el-option v-for="item in userOptions" :key="item.id"
              :label="item.username + ' (' + (item.userType === 'server' ? '服务端' : '客户端') + ')'"
              :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serviceApi } from '../api/service'

const router = useRouter()
const services = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const userOptions = ref([])

const formRef = ref(null)

const form = reactive({
  name: '',
  userId: ''
})

const formRules = computed(() => ({
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }]
}))

const loadServices = async () => {
  loading.value = true
  try {
    const { data } = await serviceApi.list()
    services.value = data.data
  } catch (error) {
    ElMessage.error('加载服务列表失败')
  } finally {
    loading.value = false
  }
}

const loadUserOptions = async () => {
  try {
    const { data } = await serviceApi.getUserOptions()
    userOptions.value = data.data
  } catch (error) {
    ElMessage.error('加载用户选项失败')
  }
}

const resetForm = () => {
  form.name = ''
  form.userId = ''
}

const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const showEditDialog = (service) => {
  isEdit.value = true
  editingId.value = service.id
  form.name = service.name
  form.userId = service.userId
  dialogVisible.value = true
}

const goToConfig = (service) => {
  router.push({ name: 'ServiceConfig', params: { id: service.id } })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const submitData = {
      name: form.name,
      userId: form.userId
    }

    if (isEdit.value) {
      const { data } = await serviceApi.update(editingId.value, submitData)
      if (data.code !== 20000) {
        ElMessage.error(data.message || '更新失败')
        return
      }
      ElMessage.success('更新成功')
    } else {
      const { data } = await serviceApi.create(submitData)
      if (data.code !== 20000) {
        ElMessage.error(data.message || '创建失败')
        return
      }
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadServices()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (service) => {
  try {
    await ElMessageBox.confirm(`确定要删除服务 ${service.name} 吗？`, '确认删除', {
      type: 'warning'
    })
    const { data } = await serviceApi.delete(service.id)
    if (data.code !== 20000) {
      ElMessage.error(data.message || '删除失败')
      return
    }
    ElMessage.success('删除成功')
    loadServices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadServices()
  loadUserOptions()
})
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}
</style>
