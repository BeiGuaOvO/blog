<template>
  <div>
    <div class="toolbar">
      <h3 style="margin: 0">标签管理</h3>
      <div class="spacer" />
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>&nbsp;新建标签
      </el-button>
    </div>

    <el-table :data="rows" v-loading="loading" class="admin-table" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="slug" label="slug" min-width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="删除该标签？会同时解除文章关联" width="260" @confirm="remove(row)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑标签' : '新建标签'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="62px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="slug" prop="slug"><el-input v-model="form.slug" placeholder="英文小写，如 spring-boot" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminCreateTag,
  adminDeleteTag,
  adminFetchTags,
  adminUpdateTag
} from '@/api/taxonomy'

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const formRef = ref(null)

const form = reactive({ id: null, name: '', slug: '' })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  slug: [
    { required: true, message: '请输入 slug', trigger: 'blur' },
    { pattern: /^[a-z0-9-]+$/, message: '只能是小写字母、数字和连字符', trigger: 'blur' }
  ]
}

async function load() {
  loading.value = true
  try {
    rows.value = (await adminFetchTags()) || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row ? { ...row } : { id: null, name: '', slug: '' })
  visible.value = true
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { name: form.name, slug: form.slug }
    if (form.id) await adminUpdateTag(form.id, payload)
    else await adminCreateTag(payload)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await adminDeleteTag(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>