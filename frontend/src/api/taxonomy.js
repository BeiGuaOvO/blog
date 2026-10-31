import request from './request'

export const fetchCategories = () => request.get('/portal/categories')
export const fetchTags = () => request.get('/portal/tags')

export const adminFetchCategories = () => request.get('/admin/categories')
export const adminCreateCategory = (data) => request.post('/admin/categories', data)
export const adminUpdateCategory = (id, data) => request.put(`/admin/categories/${id}`, data)
export const adminDeleteCategory = (id) => request.delete(`/admin/categories/${id}`)

export const adminFetchTags = () => request.get('/admin/tags')
export const adminCreateTag = (data) => request.post('/admin/tags', data)
export const adminUpdateTag = (id, data) => request.put(`/admin/tags/${id}`, data)
export const adminDeleteTag = (id) => request.delete(`/admin/tags/${id}`)