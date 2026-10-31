<template>
  <div>
    <div class="toolbar">
      <h3 style="margin: 0">分类管理</h3>
      <div class="spacer" />
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>&nbsp;新建分类
      </el-button>
    </div>

    <el-table :data="rows" v-loading="loading" class="admin-table" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="slug" label="slug" min-width="140" />
      <el-table-column prop="description" label="描述" min-width="220" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="删除该分类？该分类下不能有文章" width="240" @confirm="remove(row)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑分类' : '新建分类'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="slug" prop="slug"><el-input v-model="form.slug" placeholder="英文小写，如 backend" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
  adminCreateCategory,
  adminDeleteCategory,
  adminFetchCategories,
  adminUpdateCategory
} from '@/api/taxonomy'

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const formRef = ref(null)

const form = reactive({ id: null, name: '', slug: '', description: '', sort: 0 })
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
    rows.value = (await adminFetchCategories()) || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row ? { ...row } : { id: null, name: '', slug: '', description: '', sort: 0 })
  visible.value = true
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      name: form.name,
      slug: form.slug,
      description: form.description,
      sort: form.sort
    }
    if (form.id) await adminUpdateCategory(form.id, payload)
    else await adminCreateCategory(payload)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await adminDeleteCategory(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>