<template>
  <div class="container docs-layout">
    <aside class="card docs-aside">
      <div class="toc-title" style="margin-bottom: 10px">知识库目录</div>
      <DocTree v-loading="loading" :tree="store.tree" :current-id="currentId" />
    </aside>
    <section class="docs-main">
      <router-view />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useDocStore } from '@/stores/doc'
import DocTree from '@/components/DocTree.vue'

const store = useDocStore()
const route = useRoute()
const loading = ref(false)

// 由 DocDetail 回填当前文档 id（slug 无法直接定位树节点）
const currentId = computed(() => route.meta.docId ?? null)

onMounted(async () => {
  loading.value = true
  try {
    await store.load()
  } finally {
    loading.value = false
  }
})
</script>