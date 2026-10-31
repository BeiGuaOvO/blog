<template>
  <div class="container home-grid">
    <section>
      <div class="page-head">
        <h1 v-if="keyword">搜索「{{ keyword }}」</h1>
        <h1 v-else-if="categoryName">{{ categoryName }}</h1>
        <h1 v-else-if="tagName"># {{ tagName }}</h1>
        <h1 v-else>最新文章</h1>
        <span class="muted" v-if="total">共 {{ total }} 篇</span>
      </div>

      <div v-if="loading" class="list">
        <div v-for="i in 3" :key="i" class="card" style="padding: 18px">
          <el-skeleton animated :rows="3" />
        </div>
      </div>

      <template v-else>
        <el-empty v-if="!articles.length" description="没有找到文章">
          <el-button type="primary" @click="reset">查看全部文章</el-button>
        </el-empty>

        <div v-else class="list">
          <article
            v-for="a in articles"
            :key="a.id"
            class="card article-card"
            @click="openArticle(a.slug)"
          >
            <div v-if="a.cover" class="card-cover">
              <img :src="a.cover" :alt="a.title" loading="lazy" />
            </div>
            <div class="card-body">
              <div class="card-meta">
                <el-tag v-if="a.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
                <span v-if="a.categoryName" class="chip">{{ a.categoryName }}</span>
                <span>{{ formatDate(a.publishTime || a.createdAt) }}</span>
                <span><el-icon><View /></el-icon>{{ a.views ?? 0 }}</span>
              </div>
              <h2 class="card-title">{{ a.title }}</h2>
              <p class="card-summary">{{ a.summary || '暂无摘要' }}</p>
              <div class="card-tags" v-if="a.tags?.length">
                <el-tag
                  v-for="t in a.tags"
                  :key="t.id"
                  size="small"
                  effect="plain"
                  round
                  @click.stop="goTag(t.id)"
                >
                  #{{ t.name }}
                </el-tag>
              </div>
            </div>
          </article>
        </div>

        <div class="pager" v-if="total > size">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="changePage"
          />
        </div>
      </template>
    </section>

    <aside class="sidebar">
      <div class="card side-card">
        <img v-if="site.config.authorAvatar" :src="site.config.authorAvatar" class="avatar-img" alt="avatar" />
        <div v-else class="avatar">{{ site.logoInitial }}</div>
        <h3>{{ site.config.authorName }}</h3>
        <p class="muted" style="margin: 0">{{ site.config.authorBio }}</p>
  <div v-if="site.config.github || site.config.email" class="author-links">
    <a v-if="site.config.github" :href="site.config.github" target="_blank" rel="noopener">
      <el-icon><Link /></el-icon> GitHub
    </a>
    <a v-if="site.config.email" :href="`mailto:${site.config.email}`">
      <el-icon><Message /></el-icon> 邮箱
    </a>
  </div>
</div>

      <div class="card side-card">
        <h3 class="side-title">分类</h3>
        <ul class="side-list">
          <li :class="{ active: !categoryId }" @click="goCategory(null)">
            <span>全部</span>
          </li>
          <li
            v-for="c in categories"
            :key="c.id"
            :class="{ active: String(c.id) === String(categoryId) }"
            @click="goCategory(c.id)"
          >
            <span>{{ c.name }}</span>
          </li>
        </ul>
      </div>

      <div class="card side-card" v-if="tags.length">
        <h3 class="side-title">标签</h3>
        <div class="tag-cloud">
          <el-tag
            v-for="t in tags"
            :key="t.id"
            round
            effect="plain"
            :type="String(t.id) === String(tagId) ? 'primary' : 'info'"
            @click="goTag(t.id)"
          >
            {{ t.name }}
          </el-tag>
        </div>
      </div>

      <div class="card side-card" v-if="hot.length">
        <h3 class="side-title">热门阅读</h3>
        <ol class="hot-list">
          <li v-for="(h, i) in hot" :key="h.id" @click="openArticle(h.slug)">
            <span class="num">{{ i + 1 }}</span>
            <span class="hot-title">{{ h.title }}</span>
          </li>
        </ol>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchArticles, fetchHotArticles } from '@/api/article'
import { fetchCategories, fetchTags } from '@/api/taxonomy'
import { formatDate } from '@/utils/format'
import { useSiteStore } from '@/stores/site'

const route = useRoute()
const router = useRouter()
const site = useSiteStore()

const size = 8
const articles = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const tags = ref([])
const hot = ref([])

const page = computed(() => Number(route.query.page || 1))
const keyword = computed(() => route.query.keyword || '')
const categoryId = computed(() => route.query.categoryId || '')
const tagId = computed(() => route.query.tagId || '')

const categoryName = computed(
  () => categories.value.find((c) => String(c.id) === String(categoryId.value))?.name || ''
)
const tagName = computed(
  () => tags.value.find((t) => String(t.id) === String(tagId.value))?.name || ''
)

async function load() {
  loading.value = true
  try {
    const data = await fetchArticles({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      tagId: tagId.value || undefined
    })
    articles.value = data?.records || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

watch(() => route.query, load, { immediate: true })

onMounted(async () => {
  try {
    const [c, t, h] = await Promise.all([fetchCategories(), fetchTags(), fetchHotArticles(5)])
    categories.value = c || []
    tags.value = t || []
    hot.value = h || []
  } catch (_) { /* 错误已由拦截器提示 */ }
})

const openArticle = (slug) => router.push(`/article/${slug}`)
const goCategory = (id) => router.push({ name: 'home', query: id ? { categoryId: String(id) } : {} })
const goTag = (id) => router.push({ name: 'home', query: id ? { tagId: String(id) } : {} })
const changePage = (p) => router.push({ query: { ...route.query, page: p } })
const reset = () => router.push({ name: 'home' })
</script>