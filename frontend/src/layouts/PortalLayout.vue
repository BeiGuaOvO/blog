<template>
  <div>
    <header class="site-header">
  <div class="container header-inner">
    <router-link to="/" class="logo">
      <img v-if="site.config.logoImage" :src="site.config.logoImage" class="logo-img" alt="logo" />
      <span v-else class="dot" />
      {{ site.config.siteName }}
    </router-link>
    <nav class="nav">
      <router-link to="/" exact-active-class="active">博客</router-link>
      <router-link to="/archive" active-class="active">归档</router-link>
      <router-link to="/docs" active-class="active">知识库</router-link>
    </nav>
    <div class="header-right">
      <el-input v-model="keyword" class="search" placeholder="搜索文章…" clearable @keyup.enter="doSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button text circle @click="theme.toggle()">
        <el-icon><component :is="theme.dark ? 'Sunny' : 'Moon'" /></el-icon>
      </el-button>
      <el-button v-if="!user.isLogin" text @click="$router.push('/login')">登录</el-button>
      <el-button v-else text @click="$router.push('/admin/articles')">后台</el-button>
    </div>
  </div>
</header>
<main class="site-main"><router-view /></main>
<footer class="site-footer">
  <div class="container footer-inner">
    <span>{{ site.copyright }}</span>
    <a v-if="site.config.icp" href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">
      {{ site.config.icp }}
    </a>
  </div>
</footer>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { useSiteStore } from '@/stores/site'

const route = useRoute()
const router = useRouter()
const theme = useThemeStore()
const user = useUserStore()
const site = useSiteStore()

const year = new Date().getFullYear()
const keyword = ref(route.query.keyword || '')

watch(
  () => route.query.keyword,
  (v) => (keyword.value = v || '')
)

function doSearch() {
  router.push({ name: 'home', query: keyword.value ? { keyword: keyword.value } : {} })
}
</script>