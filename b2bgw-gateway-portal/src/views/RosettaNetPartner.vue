<template>
  <div class="container">
    <el-card v-loading="loading">
      <template #header><h2 style="margin:0;">合作伙伴</h2></template>
      <el-button type="primary" size="small" style="margin-bottom:16px;" @click="showCreate">新增</el-button>
      <el-table :data="items" style="width:100%">
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="duns" label="DUNS" width="130" />
        <el-table-column prop="url" label="URL" min-width="200" show-overflow-tooltip />
        <el-table-column label="签名证书" width="80"><template #default="{row}">{{ row.signCert ? '已配置' : '-' }}</template></el-table-column>
        <el-table-column label="加密证书" width="80"><template #default="{row}">{{ row.encryptCert ? '已配置' : '-' }}</template></el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{row}">
            <el-button size="small" link @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑合作伙伴':'新增合作伙伴'" width="80%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="DUNS"><el-input v-model="form.duns" :disabled="isEdit" /></el-form-item>
        <el-form-item label="URL"><el-input v-model="form.url" placeholder="https://" /></el-form-item>
        <el-form-item label="签名证书"><el-input v-model="form.signCert" type="textarea" :rows="3" placeholder="Base64" /></el-form-item>
        <el-form-item label="加密证书"><el-input v-model="form.encryptCert" type="textarea" :rows="3" placeholder="Base64" /></el-form-item>
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
const form=ref({name:'',duns:'',url:'',signCert:'',encryptCert:''})
async function load(){loading.value=true;try{const{data}=await rosettanetApi.partners.list();items.value=data.data||[]}finally{loading.value=false}}
function showCreate(){isEdit.value=false;editingId.value=null;form.value={name:'',duns:'',url:'',signCert:'',encryptCert:''};dialogVisible.value=true}
function showEdit(r){isEdit.value=true;editingId.value=r.id;form.value={name:r.name,duns:r.duns,url:r.url,signCert:r.signCert||'',encryptCert:r.encryptCert||''};dialogVisible.value=true}
async function handleSave(){saveLoading.value=true;try{if(isEdit.value){await rosettanetApi.partners.update(editingId.value,form.value)}else{await rosettanetApi.partners.create(form.value)}ElMessage.success('保存成功');dialogVisible.value=false;await load()}catch(e){ElMessage.error(e.response?.data?.message||e.message)}finally{saveLoading.value=false}}
async function handleDelete(r){try{await ElMessageBox.confirm(`确定删除 ${r.name}？`,'确认',{type:'warning'});await rosettanetApi.partners.delete(r.id);ElMessage.success('删除成功');await load()}catch(e){if(e!=='cancel')ElMessage.error('删除失败')}}
onMounted(load)
</script>
<style scoped>.container{padding:0;width:100%;box-sizing:border-box}</style>
