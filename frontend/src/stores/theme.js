import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const dark = ref(localStorage.getItem('blog_theme') === 'dark')

  function apply() {
    document.documentElement.classList.toggle('dark', dark.value)
  }

  function toggle() {
    dark.value = !dark.value
    localStorage.setItem('blog_theme', dark.value ? 'dark' : 'light')
    apply()
  }

  return { dark, toggle, apply }
})