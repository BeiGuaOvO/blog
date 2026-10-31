<template>
  <div class="doc-admin">
    <div class="card doc-tree-card">
      <div class="card-head">
        <span>文档目录</span>
        <el-button size="small" type="primary" @click="startCreate(null)">新建根文档</el-button>
      </div>

      <el-tree
        v-loading="treeLoading"
        :data="tree"
        node-key="id"
        :props="{ label: 'title', children: 'children' }"
        default-expand-all
        highlight-current
        :expand-on-click-node="false"
        @node-click="startEdit"
      >
        <template #default="{ data }">
          <div class="tree-row">
            <span class="tree-label">
              <el-icon><Document /></el-icon>
              <span>{{ data.title }}</span>
              <el-tag v-if="data.isPublic === 0" size="small" type="info" effect="plain">私有</el-tag>
            </span>
            <span class="tree-actions" @click.stop>
              <el-button link size="small" @click="startCreate(data)">子文档</el-button>
              <el-popconfirm title="删除该文档？" width="200" @confirm="remove(data)">
                <template #reference>
                  <el-button link size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </span>
          </div>
        </template>
      </el-tree>

      <el-empty v-if="!treeLoading && !tree.length" description="还没有文档" :image-size="60" />
    </div>

    <div class="card doc-form-card">
      <div class="card-head">
        <span>{{ mode === 'edit' ? '编辑文档' : '新建文档' }}</span>
        <div>
          <el-button @click="reset">清空</el-button>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </div>

      <el-form :model="form" label-width="76px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="文档标题" />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="slug">
              <el-input v-model="form.slug" placeholder="留空用标题" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="公开">
              <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="父文档">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            node-key="id"
            :props="{ label: 'title', children: 'children' }"
            check-strictly
            clearable
            :render-after-expand="false"
            placeholder="不选则为根文档"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input v-model="form.summary" placeholder="可选" />
        </el-form-item>

        <el-form-item label="正文">
          <MdEditor
            v-model="form.contentMd"
            :theme="theme.dark ? 'dark' : 'light'"
            :preview-theme="theme.dark ? 'github' : 'default'"
            :style="{ height: '500px', width: '100%' }"
            @on-upload-img="onUploadImg"
          />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

import {
  adminCreateDoc,
  adminDeleteDoc,
  adminFetchDoc,
  adminFetchDocTree,
  adminUpdateDoc
} from '@/api/doc'
import { uploadFile } from '@/api/upload'
import { excludeSubtree } from '@/utils/tree'
import { useThemeStore } from '@/stores/theme'

const theme = useThemeStore()

const tree = ref([])
const treeLoading = ref(false)
const saving = ref(false)
const mode = ref('create')

const emptyForm = () => ({
  id: null,
  title: '',
  slug: '',
  parentId: null,
  summary: '',
  contentMd: '',
  sort: 0,
  isPublic: 1
})

const form = reactive(emptyForm())

// 编辑时不能把父节点选成自己或自己的子孙
const parentOptions = computed(() =>
  mode.value === 'edit' && form.id ? excludeSubtree(tree.value, form.id) : tree.value
)

async function loadTree() {
  treeLoading.value = true
  try {
    tree.value = (await adminFetchDocTree()) || []
  } finally {
    treeLoading.value = false
  }
}

function reset() {
  mode.value = 'create'
  Object.assign(form, emptyForm())
}

function startCreate(parent) {
  const parentId = parent ? parent.id : null
  Object.assign(form, emptyForm(), { parentId })
  mode.value = 'create'
}

async function startEdit(node) {
  mode.value = 'edit'
  Object.assign(form, emptyForm(), { id: node.id, title: node.title, parentId: node.parentId ?? null })
  const data = await adminFetchDoc(node.id)
  Object.assign(form, {
    slug: data.slug || '',
    summary: data.summary || '',
    contentMd: data.contentMd || '',
    sort: data.sort ?? 0,
    isPublic: data.isPublic ?? 1,
    parentId: data.parentId ?? null
  })
}

async function save() {
  if (!form.title.trim()) return ElMessage.warning('请填写标题')
  if (!form.contentMd.trim()) return ElMessage.warning('正文不能为空')

  saving.value = true
  try {
    const payload = {
      title: form.title,
      slug: form.slug || undefined,
      parentId: form.parentId ?? null,
      summary: form.summary,
      contentMd: form.contentMd,
      sort: form.sort,
      isPublic: form.isPublic
    }
    if (mode.value === 'edit') {
      await adminUpdateDoc(form.id, payload)
      ElMessage.success('已更新')
    } else {
      await adminCreateDoc(payload)
      ElMessage.success('已创建')
      Object.assign(form, emptyForm(), { parentId: form.parentId })
    }
    await loadTree()
  } finally {
    saving.value = false
  }
}

async function remove(node) {
  await adminDeleteDoc(node.id)
  ElMessage.success('已删除')
  if (form.id === node.id) reset()
  await loadTree()
}

async function onUploadImg(files, callback) {
  const urls = []
  for (const file of files) {
    try {
      const res = await uploadFile(file)
      urls.push(res.url)
    } catch (_) { /* ignore */ }
  }
  callback(urls)
}

onMounted(loadTree)
</script>