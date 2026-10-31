<template>
  <div class="md-body" v-html="html"></div>
</template>

<script setup>
import { computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps({
  source: { type: String, default: '' }
})

const html = computed(() => renderMarkdown(props.source).html)
</script>