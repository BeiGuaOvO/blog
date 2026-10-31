import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchDocTree } from '@/api/doc'

export const useDocStore = defineStore('doc', () => {
  const tree = ref([])
  const loaded = ref(false)

  async function load(force = false) {
    if (loaded.value && !force) return tree.value
    tree.value = await fetchDocTree()
    loaded.value = true
    return tree.value
  }

  return { tree, loaded, load }
})