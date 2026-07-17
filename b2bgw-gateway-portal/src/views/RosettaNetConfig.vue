<template>
  <div class="container">
    <el-card v-loading="loading">
      <template #header><h2 style="margin:0;">通信配置</h2></template>
      <el-button type="primary" size="small" style="margin-bottom:16px;" @click="showCreate">新增</el-button>
      <el-table :data="items" style="width:100%">
        <el-table-column label="通信主体" min-width="120"><template #default="{row}">{{ row.personalityName }} ({{ row.personalityDuns }})</template></el-table-column>
        <el-table-column label="合作伙伴" min-width="120"><template #default="{row}">{{ row.partnerName }} ({{ row.partnerDuns }})</template></el-table-column>
        <el-table-column label="PIP" min-width="120"><template #default="{row}">{{ row.pipId }} {{ row.pipVersion }}</template></el-table-column>
        <el-table-column label="方向" width="100"><template #default="{row}"><el-tag size="small">{{ row.direction === 'send' ? '发送' : '接收' }}</el-tag></template></el-table-column>
        <el-table-column label="签名" width="60"><template #default="{row}"><el-tag size="small" :type="row.signingEnabled?'success':'info'">{{ row.signingEnabled?'是':'否' }}</el-tag></template></el-table-column>
        <el-table-column label="加密" width="60"><template #default="{row}"><el-tag size="small" :type="row.encryptionEnabled?'success':'info'">{{ row.encryptionEnabled?'是':'否' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{row}">
            <el-button size="small" link @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑通信配置':'新增通信配置'" width="80%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="通信主体"><el-select v-model="form.personalityId" style="width:100%;"><el-option v-for="p in personalities" :key="p.id" :label="p.name+' ('+p.duns+')'" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="合作伙伴"><el-select v-model="form.partnerId" style="width:100%;"><el-option v-for="p in partners" :key="p.id" :label="p.name+' ('+p.duns+')'" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="PIP 定义"><el-select v-model="form.pipDefinitionId" style="width:100%;"><el-option v-for="p in pips" :key="p.id" :label="p.pipId+' '+p.pipVersion+' - '+p.documentType" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="方向"><el-select v-model="form.direction" style="width:100%;"><el-option label="发送（通信主体 → 伙伴）" value="send" /><el-option label="接收（伙伴 → 通信主体）" value="receive" /></el-select></el-form-item>
        <template v-if="form.direction === 'send'">
          <el-form-item label="签名"><el-switch v-model="form.signingEnabled" /></el-form-item>
          <el-form-item label="签名算法" v-if="form.signingEnabled"><el-select v-model="form.signAlgorithm" style="width:100%;"><el-option label="SHA256" value="SHA256" /><el-option label="SHA1" value="SHA1" /></el-select></el-form-item>
          <el-form-item label="加密"><el-switch v-model="form.encryptionEnabled" /></el-form-item>
          <el-form-item label="加密算法" v-if="form.encryptionEnabled"><el-select v-model="form.encryptAlgorithm" style="width:100%;"><el-option label="AES-128" value="AES-128" /><el-option label="AES-256" value="AES-256" /><el-option label="3DES" value="3DES" /></el-select></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rosettanetApi } from '../api/rosettanet'
const loading=ref(false),saveLoading=ref(false),dialogVisible=ref(false),isEdit=ref(false),editingId=ref(null),items=ref([])
const personalities=ref([]),partners=ref([]),pips=ref([])
const form=ref({personalityId:'',partnerId:'',pipDefinitionId:'',direction:'',signingEnabled:false,signAlgorithm:'',encryptionEnabled:false,encryptAlgorithm:''})
async function loadOptions(){try{const[a,b,c]=await Promise.all([rosettanetApi.personalities.list(),rosettanetApi.partners.list(),rosettanetApi.pips.list()]);personalities.value=a.data.data||[];partners.value=b.data.data||[];pips.value=c.data.data||[]}catch{}}
async function load(){loading.value=true;try{const{data}=await rosettanetApi.configs.list();items.value=data.data||[]}finally{loading.value=false}}
function showCreate(){isEdit.value=false;editingId.value=null;form.value={personalityId:'',partnerId:'',pipDefinitionId:'',direction:'',signingEnabled:false,signAlgorithm:'',encryptionEnabled:false,encryptAlgorithm:''};dialogVisible.value=true}
function showEdit(r){isEdit.value=true;editingId.value=r.id;form.value={personalityId:r.personalityId,partnerId:r.partnerId,pipDefinitionId:r.pipDefinitionId,direction:r.direction,signingEnabled:r.signingEnabled,signAlgorithm:r.signAlgorithm||'',encryptionEnabled:r.encryptionEnabled,encryptAlgorithm:r.encryptAlgorithm||''};dialogVisible.value=true}
async function handleSave(){saveLoading.value=true;try{if(isEdit.value){await rosettanetApi.configs.update(editingId.value,form.value)}else{await rosettanetApi.configs.create(form.value)}ElMessage.success('保存成功');dialogVisible.value=false;await load()}catch(e){ElMessage.error(e.response?.data?.message||e.message)}finally{saveLoading.value=false}}
async function handleDelete(r){try{await ElMessageBox.confirm(`确定删除此配置？`,'确认',{type:'warning'});await rosettanetApi.configs.delete(r.id);ElMessage.success('删除成功');await load()}catch(e){if(e!=='cancel')ElMessage.error('删除失败')}}
onMounted(async()=>{await loadOptions();await load()})
</script>
<style scoped>.container{padding:0;width:100%;box-sizing:border-box}</style>
