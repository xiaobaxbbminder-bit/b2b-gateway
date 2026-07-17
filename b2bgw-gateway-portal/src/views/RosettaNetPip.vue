<template>
  <div class="container">
    <el-card v-loading="loading">
      <template #header><h2 style="margin:0;">PIP 定义</h2></template>
      <el-button type="primary" size="small" style="margin-bottom:16px;" @click="showCreate">新增</el-button>
      <el-table :data="items">
        <el-table-column prop="pipId" label="PIP ID" width="80" />
        <el-table-column prop="pipVersion" label="版本" width="100" />
        <el-table-column prop="documentType" label="文档类型" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{row}">
            <el-button size="small" link @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑 PIP':'新增 PIP'" width="80%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="PIP ID"><el-input v-model="form.pipId" /></el-form-item>
        <el-form-item label="版本"><el-input v-model="form.pipVersion" /></el-form-item>
        <el-form-item label="文档类型"><el-input v-model="form.documentType" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
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
const form=ref({pipId:'',pipVersion:'',documentType:'',description:''})
async function load(){loading.value=true;try{const{data}=await rosettanetApi.pips.list();items.value=data.data||[]}finally{loading.value=false}}
function showCreate(){isEdit.value=false;editingId.value=null;form.value={pipId:'',pipVersion:'',documentType:'',description:''};dialogVisible.value=true}
function showEdit(r){isEdit.value=true;editingId.value=r.id;form.value={pipId:r.pipId,pipVersion:r.pipVersion,documentType:r.documentType,description:r.description||''};dialogVisible.value=true}
async function handleSave(){saveLoading.value=true;try{if(isEdit.value){await rosettanetApi.pips.update(editingId.value,form.value)}else{await rosettanetApi.pips.create(form.value)}ElMessage.success('保存成功');dialogVisible.value=false;await load()}catch(e){ElMessage.error(e.response?.data?.message||e.message)}finally{saveLoading.value=false}}
async function handleDelete(r){try{await ElMessageBox.confirm(`确定删除 ${r.pipId}？`,'确认',{type:'warning'});await rosettanetApi.pips.delete(r.id);ElMessage.success('删除成功');await load()}catch(e){if(e!=='cancel')ElMessage.error('删除失败')}}
onMounted(load)
</script>
<style scoped>.container{padding:0;width:100%;box-sizing:border-box}</style>
