import { onBeforeUnmount, onMounted, ref } from 'vue'

/** 滚动时高亮当前阅读到的标题 */
export function useActiveHeading(selector = '.md-body h1[id], .md-body h2[id], .md-body h3[id], .md-body h4[id]') {
  const activeId = ref('')
  let ticking = false

  function update() {
    const headings = Array.from(document.querySelectorAll(selector))
    if (!headings.length) {
      activeId.value = ''
      return
    }
    let current = headings[0].id
    for (const el of headings) {
      if (el.getBoundingClientRect().top <= 120) current = el.id
      else break
    }
    activeId.value = current
  }

  function onScroll() {
    if (ticking) return
    ticking = true
    requestAnimationFrame(() => {
      update()
      ticking = false
    })
  }

  onMounted(() => {
    update()
    window.addEventListener('scroll', onScroll, { passive: true })
  })
  onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))

  return { activeId, refresh: update }
}