<template>
  <div v-loading="loading">
    <div v-if="doc" class="doc-body-grid">
      <article class="card doc-article">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ name: 'docs' }">知识库</el-breadcrumb-item>
          <el-breadcrumb-item
            v-for="c in crumbs"
            :key="c.id"
            :to="{ name: 'doc', params: { slug: c.slug } }"
          >
            {{ c.title }}
          </el-breadcrumb-item>
          <el-breadcrumb-item>{{ doc.title }}</el-breadcrumb-item>
        </el-breadcrumb>

        <h1 class="doc-title">{{ doc.title }}</h1>
        <p class="muted" v-if="doc.summary">{{ doc.summary }}</p>

        <div class="md-body" v-html="html"></div>

        <div class="doc-footer">
          <span class="muted">最后更新：{{ formatDate(doc.updatedAt) }}</span>
        </div>
      </article>

      <aside>
        <div class="card" style="position: sticky; top: 86px">
          <TocNav :items="toc" :active-id="activeId" />
        </div>
      </aside>
    </div>

    <el-empty v-else-if="!loading" description="文档不存在或未公开">
      <el-button type="primary" @click="$router.push('/docs')">返回知识库</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchDoc } from '@/api/doc'
import { useDocStore } from '@/stores/doc'
import { renderMarkdown } from '@/utils/markdown'
import { formatDate } from '@/utils/format'
import { findPath } from '@/utils/tree'
import { useActiveHeading } from '@/utils/useActiveHeading'
import TocNav from '@/components/TocNav.vue'
import { useSiteStore } from '@/stores/site'

const route = useRoute()
const site = useSiteStore()
const store = useDocStore()
const doc = ref(null)
const loading = ref(false)
const { activeId, refresh } = useActiveHeading()

const rendered = computed(() => renderMarkdown(doc.value?.contentMd || ''))
const html = computed(() => rendered.value.html)
const toc = computed(() => rendered.value.toc)
const crumbs = computed(() => {
  const path = findPath(store.tree, doc.value?.id)
  return path ? path.slice(0, -1) : []
})

async function load() {
  loading.value = true
  doc.value = null
  try {
    const [d] = await Promise.all([fetchDoc(route.params.slug), store.load()])
    doc.value = d
    // 让左侧树高亮当前节点
    route.meta.docId = d.id
    document.title = `${d.title} - ${site.config.siteName}`
    await nextTick()
    refresh()
  } catch (_) {
    doc.value = null
  } finally {
    loading.value = false
  }
}

watch(() => route.params.slug, load, { immediate: true })
</script>

<style scoped>
.doc-title { margin: 18px 0 4px; font-size: 28px; }
.doc-footer { margin-top: 26px; padding-top: 16px; border-top: 1px solid var(--border); }
</style>