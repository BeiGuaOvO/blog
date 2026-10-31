<template>
  <div>
    <div class="toolbar">
      <h3 style="margin: 0">站点设置</h3>
      <div class="spacer" />
      <el-button @click="reload">放弃修改</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </div>

    <div class="settings-grid">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="card form-card">
        <el-divider content-position="left">基础信息</el-divider>

        <el-form-item label="站点名称" prop="siteName">
          <el-input v-model="form.siteName" maxlength="40" show-word-limit placeholder="显示在顶栏、标签页、页脚" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.siteSubtitle" maxlength="120" show-word-limit placeholder="一句话介绍，显示在侧边栏" />
        </el-form-item>
        <el-form-item label="站点描述">
          <el-input v-model="form.siteDescription" type="textarea" :rows="2" maxlength="300" show-word-limit />
        </el-form-item>

        <el-form-item label="Logo 文字">
          <el-input v-model="form.logoText" maxlength="4" style="width: 160px" placeholder="如 G 或 北" />
        </el-form-item>
        <el-form-item label="Logo 图片">
          <div class="cover-row">
            <img v-if="form.logoImage" :src="form.logoImage" class="logo-preview" alt="logo" />
            <el-upload :show-file-list="false" accept="image/*" :http-request="(o) => uploadTo(o, 'logoImage')">
              <el-button :loading="uploading === 'logoImage'">
                {{ form.logoImage ? '更换' : '上传' }}
              </el-button>
            </el-upload>
            <el-button v-if="form.logoImage" text type="danger" @click="form.logoImage = ''">移除</el-button>
          </div>
        </el-form-item>

        <el-divider content-position="left">作者信息</el-divider>

        <el-form-item label="作者名">
          <el-input v-model="form.authorName" maxlength="40" />
        </el-form-item>
        <el-form-item label="作者简介">
          <el-input v-model="form.authorBio" type="textarea" :rows="2" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="作者头像">
          <div class="cover-row">
            <img v-if="form.authorAvatar" :src="form.authorAvatar" class="avatar-preview" alt="avatar" />
            <div v-else class="avatar">{{ site.logoInitial }}</div>
            <el-upload :show-file-list="false" accept="image/*" :http-request="(o) => uploadTo(o, 'authorAvatar')">
              <el-button :loading="uploading === 'authorAvatar'">
                {{ form.authorAvatar ? '更换' : '上传' }}
              </el-button>
            </el-upload>
            <el-button v-if="form.authorAvatar" text type="danger" @click="form.authorAvatar = ''">移除</el-button>
          </div>
        </el-form-item>

        <el-divider content-position="left">页脚与 SEO</el-divider>

        <el-form-item label="页脚文字">
          <el-input v-model="form.footerText" maxlength="120" placeholder="留空则自动生成 © 起始年-今年 站点名" />
        </el-form-item>
        <el-form-item label="建站年份">
          <el-input-number v-model="form.startYear" :min="2000" :max="2100" />
        </el-form-item>
        <el-form-item label="备案号">
          <el-input v-model="form.icp" maxlength="60" placeholder="如 京ICP备xxxxxxxx号-1，留空不显示" />
        </el-form-item>
        <el-form-item label="SEO 关键词">
          <el-input v-model="form.seoKeywords" maxlength="200" placeholder="英文逗号分隔" />
        </el-form-item>
        <el-form-item label="SEO 描述">
          <el-input v-model="form.seoDescription" type="textarea" :rows="2" maxlength="300" show-word-limit />
        </el-form-item>

        <el-divider content-position="left">社交链接</el-divider>

        <el-form-item label="GitHub">
          <el-input v-model="form.github" placeholder="https://github.com/yourname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="you@example.com" />
        </el-form-item>
      </el-form>

      <aside class="card preview-card">
        <div class="preview-title">实时预览</div>

        <div class="preview-block">
          <div class="preview-label">顶栏</div>
          <div class="preview-nav">
            <img v-if="form.logoImage" :src="form.logoImage" class="logo-preview" alt="logo" />
            <span v-else class="preview-dot" />
            <strong>{{ form.siteName || '（未填写）' }}</strong>
          </div>
        </div>

        <div class="preview-block">
          <div class="preview-label">作者卡片</div>
          <div class="preview-side">
            <img v-if="form.authorAvatar" :src="form.authorAvatar" class="avatar-preview" alt="avatar" />
            <div v-else class="avatar">{{ previewInitial }}</div>
            <div class="preview-name">{{ form.authorName || '（未填写）' }}</div>
            <div class="preview-bio">{{ form.authorBio || '（未填写）' }}</div>
          </div>
        </div>

        <div class="preview-block">
          <div class="preview-label">标签页标题</div>
          <div class="preview-tab">{{ form.siteName || '（未填写）' }}</div>
        </div>

        <div class="preview-block">
          <div class="preview-label">页脚</div>
          <div class="preview-footer">
            <div>{{ previewFooter }}</div>
            <div v-if="form.icp">{{ form.icp }}</div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminFetchSiteConfig, adminUpdateSiteConfig } from '@/api/site'
import { uploadFile } from '@/api/upload'
import { useSiteStore } from '@/stores/site'

const site = useSiteStore()
const formRef = ref(null)
const saving = ref(false)
const uploading = ref('')
const form = reactive({ ...site.config })

const rules = {
  siteName: [{ required: true, message: '请输入站点名称', trigger: 'blur' }]
}

const previewInitial = computed(() => {
  const t = (form.logoText || form.siteName || 'B').trim()
  return t.slice(0, 1).toUpperCase()
})

const previewFooter = computed(() => {
  if (form.footerText) return form.footerText
  const now = new Date().getFullYear()
  const start = form.startYear || now
  return start >= now ? `© ${now} ${form.siteName || ''}` : `© ${start}-${now} ${form.siteName || ''}`
})

async function reload() {
  const data = await adminFetchSiteConfig()
  Object.assign(form, { ...site.config, ...(data || {}) })
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    await adminUpdateSiteConfig({ ...form })
    await site.load()          // 立刻刷新 store，顶栏/页脚/标题同步变
    ElMessage.success('站点设置已保存')
  } finally {
    saving.value = false
  }
}

async function uploadTo({ file }, field) {
  uploading.value = field
  try {
    const res = await uploadFile(file)
    form[field] = res.url
  } finally {
    uploading.value = ''
  }
}

onMounted(reload)
</script>

<style scoped>
.settings-grid { display: grid; grid-template-columns: minmax(0, 1fr) 320px; gap: 16px; align-items: start; }
.preview-card { padding: 16px; position: sticky; top: 16px; }
.preview-title { font-weight: 600; margin-bottom: 14px; }
.preview-block { margin-bottom: 18px; }
.preview-label { font-size: 12px; color: var(--text-3); margin-bottom: 6px; }
.preview-nav { display: flex; align-items: center; gap: 8px; padding: 10px 12px;
  border: 1px solid var(--border); border-radius: 10px; background: var(--bg-soft); }
.preview-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--brand); }
.preview-side { padding: 14px; border: 1px solid var(--border); border-radius: 10px;
  background: var(--bg-soft); display: flex; flex-direction: column; align-items: flex-start; gap: 4px; }
.preview-name { font-weight: 600; margin-top: 6px; }
.preview-bio { font-size: 12.5px; color: var(--text-2); line-height: 1.5; }
.preview-tab { display: inline-block; padding: 6px 14px; background: #dee1e6; color: #202124;
  border-radius: 8px 8px 0 0; font-size: 13px; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.preview-footer { font-size: 12.5px; color: var(--text-2); text-align: center;
  padding: 10px; border-top: 1px solid var(--border); }
.logo-preview { width: 24px; height: 24px; border-radius: 6px; object-fit: cover; }
.avatar-preview { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; }
@media (max-width: 1100px) { .settings-grid { grid-template-columns: minmax(0, 1fr); } .preview-card { position: static; } }
</style>