<template>
  <div class="card docs-welcome">
    <h1>知识库</h1>
    <p class="muted">按主题整理的长期文档，和博客流水账分开维护。</p>

    <div v-if="store.tree.length" class="entry-grid">
      <div
        v-for="node in store.tree"
        :key="node.id"
        class="entry card"
        @click="$router.push(`/docs/${node.slug}`)"
      >
        <div class="entry-title">
          <el-icon><Notebook /></el-icon>{{ node.title }}
        </div>
        <div class="muted">{{ node.children?.length || 0 }} 篇子文档</div>
      </div>
    </div>
    <el-empty v-else description="还没有公开文档" />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useDocStore } from '@/stores/doc'

const store = useDocStore()
onMounted(() => store.load())
</script>

<style scoped>
.entry-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; margin-top: 22px; }
.entry { padding: 16px; cursor: pointer; transition: transform .15s; }
.entry:hover { transform: translateY(-2px); }
.entry-title { display: flex; align-items: center; gap: 6px; font-weight: 600; margin-bottom: 6px; }
</style>