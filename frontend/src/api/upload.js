import request from './request'

export const uploadFile = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/upload', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}