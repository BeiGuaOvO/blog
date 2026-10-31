import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSiteStore } from '@/stores/site'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/PortalLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/portal/Home.vue') },
      { path: 'article/:slug', name: 'article', component: () => import('@/views/portal/ArticleDetail.vue') },
      { path: 'archive', name: 'archive', component: () => import('@/views/portal/Archive.vue') },
      {
        path: 'docs',
        component: () => import('@/layouts/DocsLayout.vue'),
        children: [
          { path: '', name: 'docs', component: () => import('@/views/portal/DocsIndex.vue') },
          { path: ':slug', name: 'doc', component: () => import('@/views/portal/DocDetail.vue') }
        ]
      }
    ]
  },
  { path: '/login', name: 'login', component: () => import('@/views/admin/Login.vue') },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/articles' },
      { path: 'articles', name: 'admin-articles', component: () => import('@/views/admin/ArticleList.vue') },
      { path: 'articles/new', name: 'admin-article-new', component: () => import('@/views/admin/ArticleEdit.vue') },
      { path: 'articles/:id/edit', name: 'admin-article-edit', component: () => import('@/views/admin/ArticleEdit.vue') },
      { path: 'docs', name: 'admin-docs', component: () => import('@/views/admin/DocManage.vue') },
      { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoryManage.vue') },
      { path: 'tags', name: 'admin-tags', component: () => import('@/views/admin/TagManage.vue') },
      { path: 'settings', name: 'admin-settings', component: () => import('@/views/admin/SiteSettings.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    if (to.hash) return { el: to.hash, top: 80 }
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const store = useUserStore()
  if (to.meta.requiresAuth && !store.isLogin) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && store.isLogin) return '/admin/articles'
  return true
})

router.afterEach((to) => {
  const site = useSiteStore()
  const name = site.config.siteName || '博客'
  const title = to.meta?.title
  document.title = title && title !== name ? `${title} - ${name}` : name
})

export default router