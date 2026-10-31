import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('blog_token') || '')
  const user = ref(JSON.parse(localStorage.getItem('blog_user') || 'null'))
  const isLogin = computed(() => !!token.value)

  async function login(form) {
    const data = await loginApi(form)
    token.value = data.token
    user.value = data.user
    localStorage.setItem('blog_token', data.token)
    localStorage.setItem('blog_user', JSON.stringify(data.user))
    return data
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('blog_token')
    localStorage.removeItem('blog_user')
  }

  return { token, user, isLogin, login, logout }
})