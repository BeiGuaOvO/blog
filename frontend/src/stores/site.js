import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { fetchSiteConfig } from '@/api/site'

export const DEFAULT_SITE = {
  siteName: '极客笔记',
  siteSubtitle: '记录 Java / 前端 / 运维的实践与思考。',
  siteDescription: '记录 Java / 前端 / 运维的实践与思考。',
  logoText: 'G',
  logoImage: '',
  authorName: '极客笔记',
  authorBio: '记录 Java / 前端 / 运维的实践与思考。',
  authorAvatar: '',
  footerText: '',
  icp: '',
  seoKeywords: 'Java,Spring Boot,Vue,知识库',
  seoDescription: '记录 Java / 前端 / 运维的实践与思考。',
  startYear: new Date().getFullYear(),
  github: '',
  email: ''
}

export const useSiteStore = defineStore('site', () => {
  const config = reactive({ ...DEFAULT_SITE })
  const loaded = ref(false)

  async function load() {
    try {
      const data = await fetchSiteConfig()
      Object.assign(config, { ...DEFAULT_SITE, ...(data || {}) })
    } catch (_) {
      // 后端没起时也要能渲染，用默认值兜底
    }
    loaded.value = true
    applyDocumentMeta()
    return config
  }

  /** 首字母，用于没有图片时的圆形头像 */
  const logoInitial = computed(() => {
    const text = (config.logoText || config.siteName || 'B').trim()
    return text.slice(0, 1).toUpperCase()
  })

  const copyright = computed(() => {
    if (config.footerText) return config.footerText
    const now = new Date().getFullYear()
    const start = config.startYear || now
    return start >= now
      ? `© ${now} ${config.siteName}`
      : `© ${start}-${now} ${config.siteName}`
  })

  function applyDocumentMeta() {
    document.title = config.siteName
    const setMeta = (name, content) => {
      if (!content) return
      let el = document.querySelector(`meta[name="${name}"]`)
      if (!el) {
        el = document.createElement('meta')
        el.setAttribute('name', name)
        document.head.appendChild(el)
      }
      el.setAttribute('content', content)
    }
    setMeta('description', config.seoDescription || config.siteDescription)
    setMeta('keywords', config.seoKeywords)
  }

  return { config, loaded, load, logoInitial, copyright, applyDocumentMeta }
})