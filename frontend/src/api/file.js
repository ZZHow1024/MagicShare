import request from '@/utils/request.js'

// 查询分享的文件列表
export const getFileListService = () => {
  return request.get('/file/list')
}
