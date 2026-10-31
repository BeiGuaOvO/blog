import request from './request'

export const fetchDocTree = () => request.get('/portal/docs/tree')
export const fetchDoc = (slug) => request.get(`/portal/docs/${encodeURIComponent(slug)}`)

export const adminFetchDocTree = () => request.get('/admin/docs/tree')
export const adminFetchDoc = (id) => request.get(`/admin/docs/${id}`)
export const adminCreateDoc = (data) => request.post('/admin/docs', data)
export const adminUpdateDoc = (id, data) => request.put(`/admin/docs/${id}`, data)
export const adminDeleteDoc = (id) => request.delete(`/admin/docs/${id}`)