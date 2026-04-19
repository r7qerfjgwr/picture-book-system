import request from '@/utils/request'

// 获取儿童收藏列表
export function getBookmarks(childId) {
  return request({
    url: `/bookmark/child/${childId}`,
    method: 'get'
  })
}

// 添加收藏
export function addBookmark(data) {
  return request({
    url: '/bookmark',
    method: 'post',
    data
  })
}

// 取消收藏
export function removeBookmark(childId, bookId) {
  return request({
    url: `/bookmark/${childId}/${bookId}`,
    method: 'delete'
  })
}

// 检查是否已收藏
export function checkBookmark(childId, bookId) {
  return request({
    url: `/bookmark/check/${childId}/${bookId}`,
    method: 'get'
  })
}

// 获取收藏统计
export function getBookmarkStats(childId) {
  return request({
    url: `/bookmark/stats/${childId}`,
    method: 'get'
  })
}
