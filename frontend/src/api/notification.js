import request from '@/utils/request'

/**
 * 获取通知列表
 */
export function getNotifications(limit = 20) {
  return request({
    url: '/notification/list',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

/**
 * 标记通知为已读
 */
export function markAsRead(id) {
  return request({
    url: `/notification/${id}/read`,
    method: 'post'
  })
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead() {
  return request({
    url: '/notification/read-all',
    method: 'post'
  })
}

// ========== 管理员接口 ==========

/**
 * 管理员发送通知
 */
export function sendNotification(data) {
  return request({
    url: '/notification/admin/send',
    method: 'post',
    data
  })
}

/**
 * 获取所有用户列表（用于选择通知接收者）
 */
export function getUsersForNotification() {
  return request({
    url: '/notification/admin/users',
    method: 'get'
  })
}
