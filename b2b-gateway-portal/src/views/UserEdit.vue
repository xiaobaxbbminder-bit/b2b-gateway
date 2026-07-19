<template>
  <div class="container">
    <el-card v-loading="pageLoading">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2 style="margin: 0;">{{ isEdit ? '编辑用户' : '创建用户' }}</h2>
        </div>
      </template>

      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="120px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码（可选）" show-password />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="userForm.userType" :disabled="isEdit" placeholder="请选择用户类型" style="width: 100%;">
            <el-option label="服务端账号" value="server" />
            <el-option label="客户端账号" value="client" />
          </el-select>
        </el-form-item>
        <el-form-item label="密钥类型" v-if="userForm.userType === 'client'">
          <el-select v-model="keyType" placeholder="请选择密钥类型" style="width: 100%;">
            <el-option v-for="opt in dictOptions.key_type" :key="opt.key" :label="opt.value" :value="opt.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="登录公钥">
          <el-input v-model="userForm.publicKey" type="textarea" :rows="4"
            :placeholder="userForm.userType === 'server' ? '粘贴 SSH 公钥（用于密钥登录）' : '点击下方生成公钥'"
            :disabled="userForm.userType === 'client'" />
          <div v-if="userForm.userType === 'client'" style="margin-top: 8px;">
            <el-button type="primary" link :loading="keypairGenerating" @click="handleGenerateKeypair">
              生成公钥
            </el-button>
          </div>
        </el-form-item>

        <!-- 服务端账号配置 -->
        <template v-if="userForm.userType === 'server'">
          <el-form-item label="密码登录">
            <el-select v-model="userForm.passwordLogin" style="width: 100%;">
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item label="服务权限">
            <el-select v-model="selectedPermissions" multiple placeholder="请选择权限" style="width: 100%;">
              <el-option label="读取文件" value="read" />
              <el-option label="写入文件" value="write" />
              <el-option label="删除文件" value="deleteFile" />
              <el-option label="创建文件夹" value="createFolder" />
              <el-option label="删除文件夹" value="deleteFolder" />
              <el-option label="重命名" value="rename" />
            </el-select>
          </el-form-item>
          <el-form-item label="文件系统">
            <el-select v-model="userForm.filesystemType" placeholder="请选择文件系统" style="width: 100%;">
              <el-option v-for="opt in dictOptions.filesystem_type" :key="opt.key" :label="opt.value" :value="opt.key" />
            </el-select>
          </el-form-item>
        </template>

        <!-- 客户端账号配置 -->
        <template v-if="userForm.userType === 'client'">
          <el-form-item label="远程IP">
            <el-input v-model="userForm.remoteHost" placeholder="请输入远程服务器IP" />
          </el-form-item>
          <el-form-item label="远程端口">
            <el-input-number v-model="userForm.remotePort" :min="1" :max="65535" placeholder="22" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="主机密钥算法">
            <el-select v-model="userForm.hostKeyAlgorithm" placeholder="非必要不要填写，使用自动协商机制" style="width: 100%;" clearable>
              <el-option v-for="opt in dictOptions.host_key_algorithm" :key="opt.key" :label="opt.value" :value="opt.key" />
            </el-select>
          </el-form-item>
          <el-form-item label="公钥认证算法">
            <el-select v-model="userForm.publicKeyAlgorithm" placeholder="非必要不要填写，使用自动协商机制" style="width: 100%;" clearable>
              <el-option v-for="opt in dictOptions.public_key_algorithm" :key="opt.key" :label="opt.value" :value="opt.key" />
            </el-select>
          </el-form-item>
          <el-form-item label="密钥交换算法">
            <el-select v-model="userForm.kexAlgorithm" placeholder="非必要不要填写，使用自动协商机制" style="width: 100%;" clearable>
              <el-option v-for="opt in dictOptions.kex_algorithm" :key="opt.key" :label="opt.value" :value="opt.key" />
            </el-select>
          </el-form-item>
          <el-form-item label="加密算法">
            <el-select v-model="userForm.encryptAlgorithm" placeholder="非必要不要填写，使用自动协商机制" style="width: 100%;" clearable>
              <el-option v-for="opt in dictOptions.encrypt_algorithm" :key="opt.key" :label="opt.value" :value="opt.key" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>

      <div style="display: flex; justify-content: center; gap: 12px; margin-top: 32px; padding-top: 24px; border-top: 1px solid rgba(255, 255, 255, 0.1);">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { userApi } from '../api/user'
import { dictApi } from '../api/dict'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const editingId = ref(null)

const pageLoading = ref(false)
const submitLoading = ref(false)
const userFormRef = ref(null)
const keypairGenerating = ref(false)

const dictOptions = ref({})
const DICT_TYPES = ['key_type', 'host_key_algorithm', 'public_key_algorithm', 'kex_algorithm', 'encrypt_algorithm', 'filesystem_type']

async function loadDictOptions() {
  const tasks = DICT_TYPES.map(async type => {
    try {
      const { data } = await dictApi.getByType(type)
      dictOptions.value[type] = data.data || []
    } catch {
      dictOptions.value[type] = []
    }
  })
  await Promise.all(tasks)
}

const userForm = reactive({
  username: '',
  password: '',
  userType: 'server',
  publicKey: '',
  keypairId: '',
  filesystemType: '',
  permissions: null,
  passwordLogin: true,
  remoteHost: '',
  remotePort: 22,
  hostKeyAlgorithm: '',
  publicKeyAlgorithm: '',
  kexAlgorithm: '',
  encryptAlgorithm: ''
})

const keyType = ref('RSA')

watch(keyType, () => {
  userForm.publicKey = ''
  userForm.keypairId = ''
})

const selectedPermissions = ref([])

const permissionLabels = {
  read: '读取文件',
  write: '写入文件',
  deleteFile: '删除文件',
  createFolder: '创建文件夹',
  deleteFolder: '删除文件夹',
  rename: '重命名'
}

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

const selectedToPermissions = (selected) => {
  const permissions = { read: false, write: false, deleteFile: false, createFolder: false, deleteFolder: false, rename: false }
  if (selected && Array.isArray(selected)) {
    selected.forEach(key => { permissions[key] = true })
  }
  return permissions
}

const permissionsToSelected = (permissions) => {
  if (!permissions) return []
  return Object.entries(permissions).filter(([_, v]) => v).map(([k]) => k)
}

const resetForm = () => {
  userForm.username = ''
  userForm.password = ''
  userForm.userType = 'server'
  userForm.publicKey = ''
  userForm.keypairId = ''
  userForm.filesystemType = ''
  userForm.permissions = null
  userForm.passwordLogin = true
  userForm.remoteHost = ''
  userForm.remotePort = 22
  userForm.hostKeyAlgorithm = ''
  userForm.publicKeyAlgorithm = ''
  userForm.kexAlgorithm = ''
  userForm.encryptAlgorithm = ''
  keyType.value = 'RSA'
  selectedPermissions.value = []
}

const goBack = () => {
  router.push({ name: 'UserList' })
}

const handleGenerateKeypair = async () => {
  if (!keyType.value) {
    ElMessage.warning('请先选择密钥类型')
    return
  }
  keypairGenerating.value = true
  try {
    const { data } = await userApi.generateKeypair(keyType.value)
    if (data.code !== 20000) {
      ElMessage.error(data.message || '生成密钥对失败')
      return
    }
    userForm.publicKey = data.data.publicKey
    userForm.keypairId = data.data.id
    ElMessage.success('密钥对生成成功')
  } catch (error) {
    ElMessage.error('生成密钥对失败：' + (error.response?.data?.message || error.message))
  } finally {
    keypairGenerating.value = false
  }
}

const handleSubmit = async () => {
  try {
    await userFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const submitData = {
      username: userForm.username,
      publicKey: userForm.publicKey,
      keypairId: userForm.keypairId,
      filesystemType: userForm.userType === 'server' ? userForm.filesystemType : null,
      permissions: userForm.userType === 'server' ? selectedToPermissions(selectedPermissions.value) : null,
      passwordLogin: userForm.userType === 'server' ? userForm.passwordLogin : null,
      remoteHost: userForm.userType === 'client' ? userForm.remoteHost : null,
      remotePort: userForm.userType === 'client' ? userForm.remotePort : null,
      hostKeyAlgorithm: userForm.userType === 'client' ? userForm.hostKeyAlgorithm : null,
      publicKeyAlgorithm: userForm.userType === 'client' ? userForm.publicKeyAlgorithm : null,
      kexAlgorithm: userForm.userType === 'client' ? userForm.kexAlgorithm : null,
      encryptAlgorithm: userForm.userType === 'client' ? userForm.encryptAlgorithm : null
    }

    if (isEdit.value) {
      const { data } = await userApi.update(editingId.value, submitData)
      if (data.code !== 20000) {
        ElMessage.error(data.message || '更新失败')
        return
      }
      ElMessage.success('更新成功')
    } else {
      if (userForm.password) {
        submitData.password = userForm.password
      }
      submitData.userType = userForm.userType
      const { data } = await userApi.create(submitData)
      if (data.code !== 20000) {
        ElMessage.error(data.message || '创建失败')
        return
      }
      ElMessage.success('创建成功')
    }
    router.push({ name: 'UserList' })
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await loadDictOptions()
  if (route.params.id) {
    isEdit.value = true
    editingId.value = route.params.id
    pageLoading.value = true
    try {
      const { data } = await userApi.get(route.params.id)
      if (data.code !== 20000 || !data.data) {
        ElMessage.error('用户不存在')
        router.push({ name: 'UserList' })
        return
      }
      const user = data.data
      userForm.username = user.username
      userForm.userType = user.userType || 'server'
      userForm.publicKey = user.publicKey || ''
      userForm.filesystemType = user.filesystemType || ''
      userForm.permissions = user.permissions
      userForm.passwordLogin = user.passwordLogin != null ? user.passwordLogin : true
      userForm.remoteHost = user.remoteHost || ''
      userForm.remotePort = user.remotePort || 22
      userForm.hostKeyAlgorithm = user.hostKeyAlgorithm || ''
      userForm.publicKeyAlgorithm = user.publicKeyAlgorithm || ''
      userForm.kexAlgorithm = user.kexAlgorithm || ''
      userForm.encryptAlgorithm = user.encryptAlgorithm || ''
      keyType.value = user.keyType || 'RSA'
      selectedPermissions.value = permissionsToSelected(user.permissions)
    } catch (error) {
      ElMessage.error('加载用户信息失败')
      router.push({ name: 'UserList' })
    } finally {
      pageLoading.value = false
    }
  }
})
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}
</style>
