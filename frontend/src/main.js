import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as Icons from '@element-plus/icons-vue'

import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import 'highlight.js/styles/atom-one-dark.css'
import 'katex/dist/katex.min.css'
import '@/styles/main.css'

import App from './App.vue'
import router from './router'
import { useSiteStore } from '@/stores/site'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)                 // 必须先装 pinia，router 守卫里要用 store
app.use(router)
app.use(ElementPlus, { locale: zhCn })
Object.entries(Icons).forEach(([name, comp]) => app.component(name, comp))

// 先取站点配置再挂载；失败也会 resolve，用默认值兜底
const site = useSiteStore(pinia)
site.load().finally(() => app.mount('#app'))