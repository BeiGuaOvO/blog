import request from './request'

export const fetchSiteConfig = () => request.get('/portal/site-config')
export const adminFetchSiteConfig = () => request.get('/admin/site-config')
export const adminUpdateSiteConfig = (data) => request.put('/admin/site-config', data)