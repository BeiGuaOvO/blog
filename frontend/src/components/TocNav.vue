<template>
  <nav v-if="items.length" class="toc card">
    <div class="toc-title">目录</div>
    <ul>
      <li
        v-for="item in items"
        :key="item.id"
        :class="[`lv${item.level}`, { active: item.id === activeId }]"
      >
        <a :href="`#${item.id}`" @click.prevent="jump(item.id)">{{ item.text }}</a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] },
  activeId: { type: String, default: '' }
})

function jump(id) {
  const el = document.getElementById(id)
  if (!el) return
  const top = el.getBoundingClientRect().top + window.scrollY - 80
  window.scrollTo({ top, behavior: 'smooth' })
  history.replaceState(null, '', `#${id}`)
}
</script>