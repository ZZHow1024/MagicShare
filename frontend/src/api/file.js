import request from '@/utils/request.js'

// 检查当前分享
export const checkCurrentShareService = (shareId) => {
  return request.get('/file/check', {
    params: {
      shareId: shareId,
    },
  })
}

// 查询分享的文件列表
export const getFileListService = (publicKey) => {
  return request.post('/file/list', {
    publicKey,
  })
}
