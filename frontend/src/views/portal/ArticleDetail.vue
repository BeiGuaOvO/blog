<template>
  <div class="container" v-loading="loading">
    <div v-if="article" class="detail-grid">
      <article class="card article-main">
        <header class="article-head">
          <h1>{{ article.title }}</h1>
          <div class="meta">
            <span><el-icon><Calendar /></el-icon>{{ formatDate(article.publishTime || article.createdAt) }}</span>
            <span v-if="article.categoryName"><el-icon><Folder /></el-icon>{{ article.categoryName }}</span>
            <span><el-icon><View /></el-icon>{{ article.views }} 次阅读</span>
            <el-tag
              v-for="t in article.tags || []"
              :key="t.id"
              size="small"
              effect="plain"
              round
              @click="$router.push({ name: 'home', query: { tagId: String(t.id) } })"
            >
              #{{ t.name }}
            </el-tag>
          </div>
        </header>

        <div ref="contentRef" class="md-body" v-html="html"></div>
      </article>

      <aside class="detail-aside">
        <TocNav :items="toc" :active-id="activeId" />
      </aside>
    </div>

    <el-empty v-else-if="!loading" description="文章不存在或已下线">
      <el-button type="primary" @click="$router.push('/')">回到首页</el-button>
    </el-empty>

    <el-button
      v-show="showTop"
      class="back-top"
      type="primary"
      circle
      @click="scrollTop"
    >
      <el-icon><Top /></el-icon>
    </el-button>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchArticle } from '@/api/article'
import { renderMarkdown } from '@/utils/markdown'
import { formatDate } from '@/utils/format'
import { useActiveHeading } from '@/utils/useActiveHeading'
import TocNav from '@/components/TocNav.vue'
import { useSiteStore } from '@/stores/site'

const route = useRoute()
const article = ref(null)
const loading = ref(false)
const showTop = ref(false)
const { activeId, refresh } = useActiveHeading()
const site = useSiteStore()

const rendered = computed(() => renderMarkdown(article.value?.contentMd || ''))
const html = computed(() => rendered.value.html)
const toc = computed(() => rendered.value.toc)

async function load() {
  loading.value = true
  article.value = null
  try {
    article.value = await fetchArticle(route.params.slug)
    document.title = `${article.value.title} - ${site.config.siteName}`
    await nextTick()
    refresh()
  } catch (_) {
    article.value = null
  } finally {
    loading.value = false
  }
}

watch(() => route.params.slug, load, { immediate: true })

function onScroll() {
  showTop.value = window.scrollY > 600
}
const scrollTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>