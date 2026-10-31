import request from './request'

/* 前台 */
export const fetchArticles = (params) => request.get('/portal/articles', { params })
export const fetchArticle = (slug) => request.get(`/portal/articles/${encodeURIComponent(slug)}`)
export const fetchHotArticles = (limit = 5) => request.get('/portal/articles/hot', { params: { limit } })
export const fetchArchive = () => request.get('/portal/archive')

/* 后台 */
export const adminFetchArticles = (params) => request.get('/admin/articles', { params })
export const adminFetchArticle = (id) => request.get(`/admin/articles/${id}`)
export const adminCreateArticle = (data) => request.post('/admin/articles', data)
export const adminUpdateArticle = (id, data) => request.put(`/admin/articles/${id}`, data)
export const adminDeleteArticle = (id) => request.delete(`/admin/articles/${id}`)
export const adminChangeStatus = (id, status) =>
  request.patch(`/admin/articles/${id}/status`, null, { params: { status } })