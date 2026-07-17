<template>
  <div class="container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2 style="margin: 0;">用户管理</h2>
        </div>
      </template>

      <el-button type="primary" @click="$router.push({ name: 'UserCreate' })" style="margin-bottom: 16px;">
        创建用户
      </el-button>

      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip />
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
        <el-table-column label="公钥" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.publicKey" placement="top" :show-after="300" :disabled="!row.publicKey">
              <div class="cell-truncate">
                <span v-if="row.publicKey">{{ row.publicKey }}</span>
                <span v-else style="color: rgba(255,255,255,0.3);">未配置</span>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="密钥" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.publicKey" size="small" type="success">已配置</el-tag>
            <el-tag v-else size="small" type="info">未配置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push({ name: 'UserEdit', params: { id: row.id } })">编辑</el-button>
            <el-button size="small" @click="showChangePasswordDialog(row)">修改密码</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../api/user'

const users = ref([])
const loading = ref(false)

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordEditingUser = ref(null)
const passwordFormRef = ref(null)

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (rule, value, cb) => value === passwordForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ]
}

const loadUsers = async () => {
  loading.value = true
  try {
    const { data } = await userApi.list()
    users.value = data.data
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const showChangePasswordDialog = (user) => {
  passwordEditingUser.value = user
  resetPasswordForm()
  passwordDialogVisible.value = true
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }
  passwordLoading.value = true
  try {
    const { data } = await userApi.changePassword(passwordEditingUser.value.id, passwordForm.newPassword)
    if (data.code !== 20000) {
      ElMessage.error(data.message || '修改密码失败')
      return
    }
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (error) {
    ElMessage.error('修改密码失败：' + (error.response?.data?.message || error.message))
  } finally {
    passwordLoading.value = false
  }
}

const handleToggleStatus = async (user) => {
  try {
    const { data } = await userApi.toggleStatus(user.id)
    if (data.code !== 20000) {
      ElMessage.error(data.message || '状态更新失败')
      return
    }
    ElMessage.success('状态更新成功')
    loadUsers()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？`, '确认删除', {
      type: 'warning'
    })
    const { data } = await userApi.delete(user.id)
    if (data.code !== 20000) {
      ElMessage.error(data.message || '删除失败')
      return
    }
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}
</style>
