<template>
  <div v-loading="loading">
    <div class="editor-head">
      <h2>{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
      <div>
        <el-button @click="$router.push('/admin/articles')">返回</el-button>
        <el-button :loading="saving" @click="save(0)">存草稿</el-button>
        <el-button :loading="saving" type="success" @click="save(1)">发布</el-button>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="72px" class="card form-card">
      <el-row :gutter="18">
        <el-col :xs="24" :md="16">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="文章标题" />
          </el-form-item>
          <el-form-item label="slug">
            <el-input v-model="form.slug" placeholder="留空自动生成；发布后不要修改，否则外链失效" />
          </el-form-item>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="form.tagIds" multiple filterable placeholder="可多选" style="width: 100%">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="置顶">
            <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="列表页展示，留空则由前端从正文截取"
        />
      </el-form-item>

      <el-form-item label="封面">
        <div class="cover-row">
          <img v-if="form.cover" :src="form.cover" class="cover-preview" alt="封面" />
          <el-upload :show-file-list="false" accept="image/*" :http-request="uploadCover">
            <el-button :loading="uploading">{{ form.cover ? '更换封面' : '上传封面' }}</el-button>
          </el-upload>
          <el-button v-if="form.cover" text type="danger" @click="form.cover = ''">移除</el-button>
        </div>
      </el-form-item>

      <el-form-item label="正文" prop="contentMd">
        <MdEditor
          v-model="form.contentMd"
          :theme="theme.dark ? 'dark' : 'light'"
          :preview-theme="theme.dark ? 'github' : 'default'"
          :style="{ height: '560px', width: '100%' }"
          @on-upload-img="onUploadImg"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

import {
  adminCreateArticle,
  adminFetchArticle,
  adminUpdateArticle
} from '@/api/article'
import { adminFetchCategories, adminFetchTags } from '@/api/taxonomy'
import { uploadFile } from '@/api/upload'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const theme = useThemeStore()

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const categories = ref([])
const tags = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  slug: '',
  summary: '',
  cover: '',
  contentMd: '',
  categoryId: null,
  tagIds: [],
  status: 0,
  isTop: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  contentMd: [{ required: true, message: '正文不能为空', trigger: 'blur' }]
}

async function loadDetail() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await adminFetchArticle(route.params.id)
    Object.assign(form, {
      title: data.title || '',
      slug: data.slug || '',
      summary: data.summary || '',
      cover: data.cover || '',
      contentMd: data.contentMd || '',
      categoryId: data.categoryId ?? null,
      isTop: data.isTop ?? 0,
      status: data.status ?? 0,
      tagIds: (data.tags || []).map((t) => t.id)
    })
  } finally {
    loading.value = false
  }
}

async function save(status) {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form, status }
    if (isEdit.value) {
      await adminUpdateArticle(route.params.id, payload)
    } else {
      await adminCreateArticle(payload)
    }
    ElMessage.success(status === 1 ? '已发布' : '已保存为草稿')
    router.push('/admin/articles')
  } finally {
    saving.value = false
  }
}

async function uploadCover({ file }) {
  uploading.value = true
  try {
    const res = await uploadFile(file)
    form.cover = res.url
    ElMessage.success('封面已上传')
  } finally {
    uploading.value = false
  }
}

/** md-editor-v3 粘贴/拖拽图片 */
async function onUploadImg(files, callback) {
  const urls = []
  for (const file of files) {
    try {
      const res = await uploadFile(file)
      urls.push(res.url)
    } catch (_) { /* 拦截器已提示 */ }
  }
  callback(urls)
}

onMounted(async () => {
  loadDetail()
  const [c, t] = await Promise.all([adminFetchCategories(), adminFetchTags()])
  categories.value = c || []
  tags.value = t || []
})
</script>