<template>
  <div class="container" style="max-width: 820px">
    <div class="page-head">
      <h1>归档</h1>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && !groups.length" description="还没有已发布的文章" />

      <div v-for="g in groups" :key="`${g.year}-${g.month}`" class="card" style="padding: 18px 22px; margin-bottom: 14px">
        <h3 style="margin: 0 0 10px">
          {{ g.year }} 年 {{ g.month }} 月
          <span class="muted" style="font-weight: 400">· {{ g.count }} 篇</span>
        </h3>
        <ul class="archive-list">
          <li v-for="a in g.articles" :key="a.id" @click="$router.push(`/article/${a.slug}`)">
            <span class="day">{{ formatDay(a.publishTime) }}</span>
            <span class="title">{{ a.title }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchArchive } from '@/api/article'
import { formatDay } from '@/utils/format'

const groups = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    groups.value = (await fetchArchive()) || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.archive-list { list-style: none; margin: 0; padding: 0; }
.archive-list li { display: flex; gap: 14px; padding: 7px 0; cursor: pointer; border-bottom: 1px dashed var(--border); }
.archive-list li:last-child { border-bottom: none; }
.archive-list li:hover .title { color: var(--brand); }
.archive-list .day { color: var(--text-3); font-size: 13px; flex: 0 0 84px; font-variant-numeric: tabular-nums; }
.archive-list .title { font-size: 14.5px; }
</style>