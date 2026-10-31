<template>
  <el-tree
    ref="treeRef"
    :data="tree"
    node-key="id"
    :props="{ label: 'title', children: 'children' }"
    highlight-current
    :expand-on-click-node="false"
    default-expand-all
    @node-click="onClick"
  >
    <template #default="{ data }">
      <span class="doc-node">
        <el-icon><Document /></el-icon>
        <span>{{ data.title }}</span>
      </span>
    </template>
  </el-tree>
  <el-empty v-if="!tree.length" description="暂无文档" :image-size="60" />
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  tree: { type: Array, default: () => [] },
  currentId: { type: [Number, String], default: null }
})

const router = useRouter()
const treeRef = ref(null)

watch(
  () => props.currentId,
  async (id) => {
    await nextTick()
    if (id != null) treeRef.value?.setCurrentKey(id)
    else treeRef.value?.setCurrentKey(null)
  },
  { immediate: true }
)

function onClick(node) {
  router.push(`/docs/${node.slug}`)
}
</script>