import request from '@/utils/request'

/** FILE-01 图片上传（multipart，字段名 file） */
export function uploadImage(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/admin/files', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
