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
            <h2 style="margin: 0;">{{ service.name || '服务配置' }}</h2>
          </div>
          <div class="header-right">
            <el-tag size="small" :type="service.userType === 'server' ? 'primary' : 'warning'">
              {{ service.userType === 'server' ? '服务端账号' : '客户端账号' }}
            </el-tag>
          </div>
        </div>
      </template>

      <el-steps :active="activeStep" align-center finish-status="success" style="margin-bottom: 32px;">
        <el-step :title="step1Title" description="配置触发条件或调用方式" />
        <el-step title="插件配置" description="配置相关插件参数" />
        <el-step title="异常告警" description="配置异常告警规则" />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: 触发事件 / 调用方式 -->
        <div v-show="activeStep === 0">
          <el-form label-width="120px" label-position="right" class="config-form">
            <template v-if="service.userType === 'server'">
              <h3 class="section-title">触发事件</h3>
              <el-form-item label="触发方式">
                <el-select v-model="config.triggerType" placeholder="请选择触发方式" style="width: 100%;">
                  <el-option label="文件上传时" value="file_upload" />
                  <el-option label="文件下载时" value="file_download" />
                </el-select>
              </el-form-item>
              <el-form-item label="监听路径">
                <el-input v-model="config.watchPath" placeholder="例：/upload 或留空监听根目录" />
              </el-form-item>
              <el-form-item label="文件过滤">
                <el-input v-model="config.fileFilter" placeholder="例：*.txt,*.csv（逗号分隔，留空不过滤）" />
              </el-form-item>
              <el-form-item v-if="config.triggerType === 'file_upload'" label="兼容模式">
                <el-switch v-model="config.compatibilityMode" />
                <div class="form-tip">&nbsp;&nbsp;开启后，若检测到.filepart临时文件，将等待其重命名完成后再触发流程</div>
              </el-form-item>
            </template>
            <template v-else>
              <h3 class="section-title">调用方式</h3>
              <el-form-item label="调用方式">
                <el-select v-model="config.invokeMode" placeholder="请选择调用方式" style="width: 100%;">
                  <el-option label="SFTP下载" value="sftp_download" />
                  <el-option label="SFTP上传" value="sftp_upload" />
                  <el-option label="批量文件上传" value="batch_upload" />
                  <el-option label="批量文件下载" value="batch_download" />
                </el-select>
              </el-form-item>

              <!-- 批量上传 / 批量下载: 数据源配置 -->
              <template v-if="config.invokeMode === 'batch_upload' || config.invokeMode === 'batch_download'">
                <el-form-item label="数据源类型">
                  <el-select v-model="config.dataSource.type" placeholder="请选择数据源类型" style="width: 100%;">
                    <el-option label="S3 对象存储" value="S3" />
                  </el-select>
                </el-form-item>

                <template v-if="config.dataSource.type === 'S3'">
                  <h3 class="section-title" style="margin-top: 8px;">S3 配置</h3>
                  <el-form-item label="Endpoint">
                    <el-input v-model="config.dataSource.args.endpoint" placeholder="例：https://s3.amazonaws.com" />
                  </el-form-item>
                  <el-form-item label="存储桶名称">
                    <el-input v-model="config.dataSource.args.bucket" placeholder="请输入Bucket名称" />
                  </el-form-item>
                  <el-form-item label="区域">
                    <el-input v-model="config.dataSource.args.region" placeholder="例：us-east-1" />
                  </el-form-item>
                  <el-form-item label="Access Key">
                    <el-input v-model="config.dataSource.args.accessKey" placeholder="请输入Access Key" />
                  </el-form-item>
                  <el-form-item label="Secret Key">
                    <el-input v-model="config.dataSource.args.secretKey" type="password" placeholder="请输入Secret Key" show-password />
                  </el-form-item>
                  <el-form-item label="路径前缀">
                    <el-input v-model="config.dataSource.args.prefix" :placeholder="config.invokeMode === 'batch_upload' ? '源路径前缀（留空则从根目录下载）' : '目标路径前缀（留空则上传到根目录）'" />
                  </el-form-item>
                  <el-form-item label="HTTPS">
                    <el-switch v-model="config.dataSource.args.useHttps" />
                    <div class="form-tip">&nbsp;&nbsp;开启后使用 HTTPS 协议连接 S3</div>
                  </el-form-item>
                  <el-form-item label="路径风格">
                    <el-switch v-model="config.dataSource.args.pathStyle" />
                    <div class="form-tip">&nbsp;&nbsp;开启后使用路径风格（path-style），关闭则使用虚拟主机风格（virtual-hosted-style）</div>
                  </el-form-item>
                </template>
              </template>
            </template>
          </el-form>
        </div>

        <!-- Step 2: 插件配置 -->
        <div v-show="activeStep === 1">
          <div class="plugin-header">
            <h3 class="section-title" style="margin-bottom: 0; border-bottom: none; padding-bottom: 0;">插件配置</h3>
            <el-button type="primary" size="small" @click="addPlugin">
              <el-icon><Plus /></el-icon> 添加插件
            </el-button>
          </div>

          <div v-if="config.plugins.length === 0" class="empty-plugins">
            暂无插件，点击上方按钮添加
          </div>

          <div v-for="(plugin, index) in config.plugins" :key="index" class="plugin-card">
            <div class="plugin-card-header">
              <span class="plugin-index">插件 {{ index + 1 }}</span>
              <div class="plugin-card-actions">
                <el-switch v-model="plugin.enabled" size="small" />
                <el-button text type="danger" size="small" @click="removePlugin(index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-form label-width="140px" label-position="right" class="config-form" v-if="plugin.enabled">
              <el-form-item label="插件类型">
                <el-select v-model="plugin.name" placeholder="请选择插件类型" style="width: 100%;" @change="onPluginTypeChange(plugin)">
                  <el-option v-for="(pd, key) in availablePlugins" :key="key" :label="pd.label" :value="pd.name" />
                </el-select>
              </el-form-item>

              <el-form-item label="执行节点" v-if="plugin.name">
                <el-select v-model="plugin.execNode" placeholder="请选择执行节点" style="width: 100%;">
                  <el-option v-for="opt in getPluginExecNodes(plugin.name)" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>

              <!-- Checksum 配置 -->
              <template v-if="plugin.name === 'checksum'">
                <el-form-item label="校验算法">
                  <el-select v-model="plugin.args.algorithm" placeholder="请选择校验算法" style="width: 100%;">
                    <el-option label="MD5" value="MD5" />
                    <el-option label="SHA1" value="SHA1" />
                    <el-option label="SHA256" value="SHA256" />
                    <el-option label="SHA512" value="SHA512" />
                  </el-select>
                </el-form-item>
              </template>

              <!-- 病毒扫描配置 -->
              <template v-if="plugin.name === 'virus_scan'">
                <el-form-item label="扫描引擎">
                  <el-select v-model="plugin.args.engine" placeholder="请选择扫描引擎" style="width: 100%;">
                    <el-option label="ClamAV" value="clamav" />
                    <el-option label="自定义" value="custom" />
                  </el-select>
                </el-form-item>
                <el-form-item label="自定义引擎路径" v-if="plugin.args.engine === 'custom'">
                  <el-input v-model="plugin.args.customEnginePath" placeholder="请输入自定义扫描引擎路径" />
                </el-form-item>
                <el-form-item label="隔离感染文件">
                  <el-switch v-model="plugin.args.quarantineInfected" />
                </el-form-item>
                <el-form-item label="扫描超时(秒)">
                  <el-input-number v-model="plugin.args.scanTimeout" :min="10" :max="300" :step="10" style="width: 100%;" />
                </el-form-item>
              </template>

              <!-- S3文件上传配置 -->
              <template v-if="plugin.name === 's3_upload'">
                <el-form-item label="S3 Endpoint">
                  <el-input v-model="plugin.args.endpoint" placeholder="例：https://s3.amazonaws.com" />
                </el-form-item>
                <el-form-item label="存储桶名称">
                  <el-input v-model="plugin.args.bucket" placeholder="请输入Bucket名称" />
                </el-form-item>
                <el-form-item label="区域">
                  <el-input v-model="plugin.args.region" placeholder="例：us-east-1" />
                </el-form-item>
                <el-form-item label="Access Key">
                  <el-input v-model="plugin.args.accessKey" placeholder="请输入Access Key" />
                </el-form-item>
                <el-form-item label="Secret Key">
                  <el-input v-model="plugin.args.secretKey" type="password" placeholder="请输入Secret Key" show-password />
                </el-form-item>
                <el-form-item label="目标路径前缀">
                  <el-input v-model="plugin.args.targetPrefix" placeholder="例：uploads/（留空则上传到根目录）" />
                </el-form-item>
                <el-form-item label="HTTPS">
                  <el-switch v-model="plugin.args.useHttps" />
                  <div class="form-tip">&nbsp;&nbsp;开启后使用 HTTPS 协议连接 S3</div>
                </el-form-item>
                <el-form-item label="路径风格">
                  <el-switch v-model="plugin.args.pathStyle" />
                  <div class="form-tip">&nbsp;&nbsp;开启后使用路径风格（path-style），关闭则使用虚拟主机风格（virtual-hosted-style）</div>
                </el-form-item>
              </template>

              <!-- API调用配置 -->
              <template v-if="plugin.name === 'api_call'">
                <el-form-item label="请求方式">
                  <el-select v-model="plugin.args.method" placeholder="请选择请求方式" style="width: 100%;">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                    <el-option label="PATCH" value="PATCH" />
                  </el-select>
                </el-form-item>
                <el-form-item label="请求URL">
                  <el-input v-model="plugin.args.url" placeholder="请输入请求URL" />
                </el-form-item>
                <el-form-item label="请求头">
                  <div v-for="(hdr, idx) in plugin.args._headerRows" :key="idx" style="display:flex;gap:8px;margin-bottom:8px;width:100%;">
                    <el-input v-model="hdr.name" placeholder="Header名称" size="small" style="width:200px;" />
                    <el-input v-model="hdr.value" placeholder="Header值" size="small" style="flex:1;" />
                    <el-button text type="danger" size="small" @click="removeHeader(plugin, idx)"><el-icon><Delete /></el-icon></el-button>
                  </div>
                  <el-button size="small" @click="addHeader(plugin)">添加请求头</el-button>
                </el-form-item>
                <el-form-item label="请求体">
                  <el-input v-model="plugin.args.body" type="textarea" :rows="4" />
                </el-form-item>
                <el-form-item label="连接超时(毫秒)">
                  <el-input-number v-model="plugin.args.connectTimeout" :min="1000" :max="60000" :step="1000" style="width: 100%;" />
                </el-form-item>
                <el-form-item label="读取超时(毫秒)">
                  <el-input-number v-model="plugin.args.readTimeout" :min="1000" :max="120000" :step="1000" style="width: 100%;" />
                </el-form-item>
              </template>

              <!-- Kafka消息通知配置 -->
              <template v-if="plugin.name === 'kafka'">
                <el-form-item label="Bootstrap Servers">
                  <el-input v-model="plugin.args.bootstrapServers" placeholder="例：localhost:9092" />
                </el-form-item>
                <el-form-item label="Topic">
                  <el-input v-model="plugin.args.topic" placeholder="请输入Topic名称" />
                </el-form-item>
                <el-form-item label="消息Key">
                  <el-input v-model="plugin.args.key" placeholder="可选，消息Key" />
                </el-form-item>
                <el-form-item label="消息模板">
                  <el-input v-model="plugin.args.messageTemplate" type="textarea" :rows="4" placeholder='JSON格式，可用变量：{filename}, {filepath}, {filesize}, {timestamp}' />
                </el-form-item>
              </template>

              <!-- RocketMQ消息通知配置 -->
              <template v-if="plugin.name === 'rocketmq'">
                <el-form-item label="NameServer地址">
                  <el-input v-model="plugin.args.nameServer" placeholder="例：localhost:9876" />
                </el-form-item>
                <el-form-item label="Topic">
                  <el-input v-model="plugin.args.topic" placeholder="请输入Topic名称" />
                </el-form-item>
                <el-form-item label="生产者组">
                  <el-input v-model="plugin.args.producerGroup" placeholder="请输入Producer Group" />
                </el-form-item>
                <el-form-item label="Tag">
                  <el-input v-model="plugin.args.tag" placeholder="可选，消息Tag" />
                </el-form-item>
                <el-form-item label="消息Key">
                  <el-input v-model="plugin.args.key" placeholder="可选，消息Key" />
                </el-form-item>
                <el-form-item label="消息模板">
                  <el-input v-model="plugin.args.messageTemplate" type="textarea" :rows="4" placeholder='JSON格式，可用变量：{filename}, {filepath}, {filesize}, {timestamp}' />
                </el-form-item>
              </template>
            </el-form>
          </div>
        </div>

        <!-- Step 3: 异常告警 -->
        <div v-show="activeStep === 2">
          <el-form label-width="120px" label-position="right" class="config-form">
            <h3 class="section-title">异常告警</h3>
            <el-form-item label="启用告警">
              <el-switch v-model="config.alertEnabled" />
            </el-form-item>
            <template v-if="config.alertEnabled">
              <el-form-item label="告警级别">
                <el-select v-model="config.alertLevel" placeholder="请选择告警级别" style="width: 100%;">
                  <el-option label="低" value="low" />
                  <el-option label="中" value="medium" />
                  <el-option label="高" value="high" />
                  <el-option label="紧急" value="critical" />
                </el-select>
              </el-form-item>
              <el-form-item label="失败次数阈值">
                <el-input-number v-model="config.failureThreshold" :min="1" :max="100" :step="1" style="width: 100%;" />
              </el-form-item>
              <el-form-item label="告警消息模板">
                <el-input v-model="config.alertMessageTemplate" type="textarea" :rows="3" placeholder="可用变量：{service}, {time}, {error}" />
              </el-form-item>
            </template>
          </el-form>
        </div>
      </div>

      <div class="step-actions">
        <el-button v-if="activeStep > 0" @click="activeStep--">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 2" type="primary" :loading="saveLoading" @click="handleSave">保存配置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import { serviceApi } from '../api/service'
import { pluginDefApi } from '../api/plugin-definition'

const route = useRoute()
const router = useRouter()
const serviceId = route.params.id

const loading = ref(false)
const saveLoading = ref(false)
const activeStep = ref(0)
const service = ref({})

const availablePlugins = ref({})
const execNodeLabels = {
  before_upload: '上传前',
  after_upload: '上传后',
  before_download: '下载前',
  after_download: '下载后',
  process_start: '流程开始',
  process_end: '流程结束'
}

const config = reactive({
  triggerType: '',
  watchPath: '',
  fileFilter: '',
  compatibilityMode: true,
  invokeMode: '',
  dataSource: {
    type: '',
    args: {}
  },
  plugins: [],
  alertEnabled: false,
  alertLevel: 'medium',
  failureThreshold: 5,
  alertMessageTemplate: ''
})

const getPluginExecNodes = (pluginName) => {
  const pd = availablePlugins.value[pluginName]
  if (!pd || !pd.visibility || pd.visibility.length === 0) return []
  return (pd.visibility[0].execNodes || []).map(v => ({ label: execNodeLabels[v] || v, value: v }))
}

const getPluginDefaults = (pluginName) => {
  const pd = availablePlugins.value[pluginName]
  return pd?.defaults || {}
}

const getPluginRequiredFields = (pluginName) => {
  const pd = availablePlugins.value[pluginName]
  return pd?.requiredFields || []
}

const getPluginFieldLabels = (pluginName) => {
  const pd = availablePlugins.value[pluginName]
  return pd?.fieldLabels || {}
}

const addPlugin = () => {
  config.plugins.push({
    name: '',
    enabled: true,
    execNode: '',
    args: {}
  })
}

const onPluginTypeChange = (plugin) => {
  plugin.args = { ...getPluginDefaults(plugin.name) }
  plugin.execNode = ''
  if (plugin.name === 'api_call') {
    plugin.args._headerRows = parseHeadersToRows(plugin.args.headers)
  }
}

const removePlugin = (index) => {
  config.plugins.splice(index, 1)
}

const parseHeadersToRows = (headersStr) => {
  if (!headersStr) return []
  try {
    const obj = JSON.parse(headersStr)
    return Object.entries(obj).map(([k, v]) => ({ name: k, value: String(v) }))
  } catch { return [] }
}

const addHeader = (plugin) => {
  if (!plugin.args._headerRows) plugin.args._headerRows = []
  plugin.args._headerRows.push({ name: '', value: '' })
}

const removeHeader = (plugin, idx) => {
  plugin.args._headerRows.splice(idx, 1)
}

const validatePlugins = () => {
  const enabledPlugins = config.plugins.filter(p => p.enabled)
  for (const plugin of enabledPlugins) {
    if (!plugin.name) {
      ElMessage.warning('存在未选择类型的插件，请先选择插件类型')
      return false
    }
    const execNodeOptions = getPluginExecNodes(plugin.name)
    if (execNodeOptions.length > 0 && (!plugin.execNode || !execNodeOptions.some(o => o.value === plugin.execNode))) {
      ElMessage.warning(`插件 "${plugin.name}" 的"执行节点"不能为空`)
      return false
    }
    const required = getPluginRequiredFields(plugin.name)
    const labels = getPluginFieldLabels(plugin.name)
    for (const field of required) {
      const value = plugin.args[field]
      if (value === undefined || value === null || value === '') {
        const label = labels[field] || field
        ElMessage.warning(`插件 "${plugin.name}" 的 "${label}" 不能为空`)
        return false
      }
    }
    if (plugin.name === 'virus_scan' && plugin.args.engine === 'custom') {
      if (!plugin.args.customEnginePath) {
        ElMessage.warning('插件 "virus_scan" 的 "自定义引擎路径" 不能为空')
        return false
      }
    }
  }
  return true
}

const nextStep = () => {
  if (activeStep.value === 0 && !validateStep0()) return
  if (activeStep.value === 1 && !validatePlugins()) return
  activeStep.value++
}

const step1Title = computed(() => {
  return service.value.userType === 'server' ? '触发事件' : '调用方式'
})

const goBack = () => {
  router.push({ name: 'ServiceList' })
}

const validateStep0 = () => {
  if (config.dataSource.type === 'S3') {
    const args = config.dataSource.args
    if (!args.endpoint) {
      ElMessage.warning('请输入 S3 Endpoint')
      return false
    }
    if (!args.bucket) {
      ElMessage.warning('请输入存储桶名称')
      return false
    }
    if (!args.accessKey) {
      ElMessage.warning('请输入 Access Key')
      return false
    }
    if (!args.secretKey) {
      ElMessage.warning('请输入 Secret Key')
      return false
    }
  }
  return true
}

const loadAvailablePlugins = async () => {
  try {
    const { data } = await pluginDefApi.getAvailable(
      service.value.userType,
      config.triggerType || undefined,
      config.invokeMode || undefined
    )
    const map = {}
    ;(data.data || []).forEach(pd => { map[pd.name] = pd })
    availablePlugins.value = map
  } catch {
    availablePlugins.value = {}
  }
}

const loadService = async () => {
  loading.value = true
  try {
    const { data } = await serviceApi.get(serviceId)
    if (data.data) {
      service.value = data.data
    } else {
      ElMessage.error('服务不存在')
      router.push({ name: 'ServiceList' })
      return
    }
    const { data: configData } = await serviceApi.getConfig(serviceId)
    if (configData.data) {
      const c = configData.data
      config.triggerType = c.triggerType || ''
      config.watchPath = c.watchPath || ''
      config.fileFilter = c.fileFilter || ''
      config.compatibilityMode = c.compatibilityMode ?? true
      config.invokeMode = c.invokeMode || ''
      config.dataSource = c.dataSource || { type: '', args: {} }
      config.plugins = (c.plugins || []).map(p => {
        if (p.name === 'api_call' && p.args) {
          p.args._headerRows = parseHeadersToRows(p.args.headers)
        }
        return p
      })
      config.alertEnabled = c.alertEnabled ?? false
      config.alertLevel = c.alertLevel || 'medium'
      config.failureThreshold = c.failureThreshold ?? 5
      config.alertMessageTemplate = c.alertMessageTemplate || ''
    }
    await loadAvailablePlugins()
  } catch (error) {
    ElMessage.error('加载服务信息失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saveLoading.value = true
  try {
    for (const plugin of config.plugins) {
      if (plugin.name === 'api_call' && plugin.args._headerRows) {
        const obj = {}
        for (const row of plugin.args._headerRows) {
          if (row.name) obj[row.name] = row.value
        }
        plugin.args.headers = JSON.stringify(obj)
        delete plugin.args._headerRows
      }
    }
    await serviceApi.updateConfig(serviceId, config)
    ElMessage.success('配置保存成功')
    router.push({ name: 'ServiceList' })
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

watch(() => config.triggerType, () => {
  if (service.value.userType === 'server') loadAvailablePlugins()
})

watch(() => config.invokeMode, () => {
  if (service.value.userType === 'client') loadAvailablePlugins()
})

onMounted(() => {
  loadService()
})
</script>

<style scoped>
.container {
  padding: 0;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.container > :deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.container > :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
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

.section-title {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.form-tip {
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.5;
}

.plugin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.empty-plugins {
  text-align: center;
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
}

.plugin-card {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}

.plugin-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.plugin-index {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  font-size: 14px;
}

.plugin-card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-content {
  min-height: 300px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}
</style>
