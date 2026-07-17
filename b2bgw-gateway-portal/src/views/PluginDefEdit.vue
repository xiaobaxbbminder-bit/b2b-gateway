<template>
  <div class="container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button text @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2 style="margin: 0;">{{ isNew ? '新增插件定义' : '编辑插件定义' }}</h2>
          </div>
        </div>
      </template>

      <el-form label-width="120px" label-position="right">
        <el-form-item label="标识">
          <el-input v-model="form.name" :disabled="!isNew" placeholder="插件唯一标识，如 s3_upload" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.label" placeholder="插件显示名称，如 S3 文件上传" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="插件功能描述" />
        </el-form-item>

        <el-divider>可见性规则</el-divider>
        <div v-for="(rule, idx) in form.visibility" :key="idx" class="rule-card">
          <div class="rule-header">
            <span class="rule-index">规则 {{ idx + 1 }}</span>
            <el-button text type="danger" size="small" @click="form.visibility.splice(idx, 1)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-form label-width="100px" label-position="right">
            <el-form-item label="用户类型">
              <el-select v-model="rule.userType" style="width: 100%;" @change="onRuleTypeChange(rule)">
                <el-option label="服务端" value="server" />
                <el-option label="客户端" value="client" />
              </el-select>
            </el-form-item>
            <el-form-item label="触发方式" v-if="rule.userType === 'server'">
              <el-select v-model="rule.triggerType" style="width: 100%;" @change="onModeChange(rule)">
                <el-option label="文件上传" value="file_upload" />
                <el-option label="文件下载" value="file_download" />
              </el-select>
            </el-form-item>
            <el-form-item label="调用方式" v-if="rule.userType === 'client'">
              <el-select v-model="rule.invokeMode" style="width: 100%;" @change="onModeChange(rule)">
                <el-option label="SFTP下载" value="sftp_download" />
                <el-option label="SFTP上传" value="sftp_upload" />
                <el-option label="批量上传" value="batch_upload" />
                <el-option label="批量下载" value="batch_download" />
              </el-select>
            </el-form-item>
            <el-form-item label="执行节点">
              <el-checkbox-group v-model="rule.execNodes">
                <el-checkbox v-for="opt in getExecNodeOptions(rule)" :key="opt.value" :label="opt.value">{{ opt.label }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </div>
        <el-button size="small" @click="addRule">添加规则</el-button>

        <el-divider>参数字段</el-divider>
        <el-table :data="paramRows" style="width: 100%;" border>
          <el-table-column label="字段名" min-width="140">
            <template #default="{ row, $index }">
              <el-input v-model="row.field" placeholder="字段名" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="默认值" min-width="140">
            <template #default="{ row, $index }">
              <el-input v-model="row.defaultValue" placeholder="默认值" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="显示名称" min-width="140">
            <template #default="{ row, $index }">
              <el-input v-model="row.label" placeholder="显示名称" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="类型" width="100">
            <template #default="{ row, $index }">
              <el-select v-model="row.type" size="small" style="width: 100%;">
                <el-option label="字符串" value="string" />
                <el-option label="数字" value="number" />
                <el-option label="布尔值" value="boolean" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="必填" width="64" align="center">
            <template #default="{ row, $index }">
              <el-checkbox v-model="row.required" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="64" align="center">
            <template #default="{ row, $index }">
              <el-button text type="danger" size="small" @click="removeParamRow($index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button size="small" style="margin-top: 8px;" @click="addParamRow">添加参数</el-button>
      </el-form>

      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete } from '@element-plus/icons-vue'
import { pluginDefApi } from '../api/plugin-definition'

const route = useRoute()
const router = useRouter()
const isNew = !route.params.id || route.params.id === 'new'

const loading = ref(false)
const saveLoading = ref(false)
const paramRows = ref([])

const form = reactive({
  name: '',
  label: '',
  description: '',
  visibility: [],
  defaults: {},
  requiredFields: [],
  fieldLabels: {}
})

const goBack = () => {
  router.push({ name: 'PluginDefList' })
}

const mergeToParamRows = (defaults, requiredFields, fieldLabels) => {
  const reqSet = new Set(requiredFields || [])
  const labels = fieldLabels || {}
  const rows = []
  for (const [key, value] of Object.entries(defaults || {})) {
    let type = 'string'
    if (typeof value === 'number') type = 'number'
    else if (typeof value === 'boolean') type = 'boolean'
    rows.push({
      field: key,
      defaultValue: String(value),
      label: labels[key] || '',
      type,
      required: reqSet.has(key)
    })
  }
  for (const key of Object.keys(labels)) {
    if (!(key in (defaults || {}))) {
      rows.push({
        field: key,
        defaultValue: '',
        label: labels[key],
        type: 'string',
        required: reqSet.has(key)
      })
    }
  }
  for (const key of (requiredFields || [])) {
    if (!rows.some(r => r.field === key)) {
      rows.push({
        field: key,
        defaultValue: '',
        label: labels[key] || '',
        type: 'string',
        required: true
      })
    }
  }
  return rows
}

const loadDefinition = async () => {
  if (isNew) return
  loading.value = true
  try {
    const { data } = await pluginDefApi.get(route.params.id)
    const d = data.data
    form.name = d.name || ''
    form.label = d.label || ''
    form.description = d.description || ''
    form.visibility = d.visibility || []
    paramRows.value = mergeToParamRows(d.defaults, d.requiredFields, d.fieldLabels)
  } catch {
    ElMessage.error('加载插件定义失败')
    goBack()
  } finally {
    loading.value = false
  }
}

const EXEC_NODE_OPTIONS = {
  server_file_upload: [
    { label: '上传前', value: 'before_upload' },
    { label: '上传后', value: 'after_upload' }
  ],
  server_file_download: [
    { label: '下载前', value: 'before_download' },
    { label: '下载后', value: 'after_download' }
  ],
  client_sftp_download: [
    { label: '下载前', value: 'before_download' },
    { label: '下载后', value: 'after_download' }
  ],
  client_sftp_upload: [
    { label: '上传前', value: 'before_upload' },
    { label: '上传后', value: 'after_upload' }
  ],
  client_batch_upload: [
    { label: '上传前', value: 'before_upload' },
    { label: '上传后', value: 'after_upload' },
    { label: '流程开始', value: 'process_start' },
    { label: '流程结束', value: 'process_end' }
  ],
  client_batch_download: [
    { label: '下载前', value: 'before_download' },
    { label: '下载后', value: 'after_download' },
    { label: '流程开始', value: 'process_start' },
    { label: '流程结束', value: 'process_end' }
  ]
}

const getRuleKey = (rule) => {
  if (rule.userType === 'server') {
    if (rule.triggerType === 'file_upload') return 'server_file_upload'
    if (rule.triggerType === 'file_download') return 'server_file_download'
    return null
  }
  if (rule.invokeMode === 'sftp_download') return 'client_sftp_download'
  if (rule.invokeMode === 'sftp_upload') return 'client_sftp_upload'
  if (rule.invokeMode === 'batch_upload') return 'client_batch_upload'
  if (rule.invokeMode === 'batch_download') return 'client_batch_download'
  return null
}

const getExecNodeOptions = (rule) => {
  const key = getRuleKey(rule)
  return key ? EXEC_NODE_OPTIONS[key] : []
}

const addRule = () => {
  form.visibility.push({
    userType: 'server',
    triggerType: 'file_upload',
    invokeMode: null,
    execNodes: []
  })
}

const onRuleTypeChange = (rule) => {
  rule.triggerType = null
  rule.invokeMode = null
  rule.execNodes = []
}

const onModeChange = (rule) => {
  rule.execNodes = []
}

const addParamRow = () => {
  paramRows.value.push({
    field: '',
    defaultValue: '',
    label: '',
    type: 'string',
    required: false
  })
}

const removeParamRow = (idx) => {
  paramRows.value.splice(idx, 1)
}

const handleSave = async () => {
  saveLoading.value = true
  try {
    const defaults = {}
    const requiredFields = []
    const fieldLabels = {}
    for (const row of paramRows.value) {
      if (!row.field) continue
      fieldLabels[row.field] = row.label
      if (row.required) requiredFields.push(row.field)
      let value
      if (row.type === 'number') {
        value = row.defaultValue === '' ? '' : Number(row.defaultValue)
      } else if (row.type === 'boolean') {
        value = row.defaultValue === 'true'
      } else {
        value = row.defaultValue
      }
      defaults[row.field] = value
    }
    const payload = { ...form, defaults, requiredFields, fieldLabels }
    if (isNew) {
      await pluginDefApi.create(payload)
    } else {
      await pluginDefApi.update(route.params.id, payload)
    }
    ElMessage.success('保存成功')
    router.push({ name: 'PluginDefList' })
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  loadDefinition()
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

.rule-card {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rule-index {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  font-size: 14px;
}

.container :deep(.el-divider__text) {
  color: rgba(255, 255, 255, 0.6);
  background: transparent;
}

.container :deep(.el-divider--horizontal) {
  border-top-color: rgba(255, 255, 255, 0.1);
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}
</style>
