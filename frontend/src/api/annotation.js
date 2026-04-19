import request from '@/utils/request'

// 获取儿童批注列表
export function getAnnotations(childId) {
  return request({
    url: `/annotation/child/${childId}`,
    method: 'get'
  })
}

// 获取儿童对某绘本的批注
export function getBookAnnotations(childId, bookId) {
  return request({
    url: `/annotation/child/${childId}/book/${bookId}`,
    method: 'get'
  })
}

// 添加批注
export function addAnnotation(data) {
  return request({
    url: '/annotation',
    method: 'post',
    data
  })
}

// 更新批注
export function updateAnnotation(data) {
  return request({
    url: '/annotation',
    method: 'put',
    data
  })
}

// 删除批注
export function deleteAnnotation(id) {
  return request({
    url: `/annotation/${id}`,
    method: 'delete'
  })
}
