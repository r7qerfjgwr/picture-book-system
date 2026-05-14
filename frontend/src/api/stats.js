import request from '@/utils/request'

/**
 * 获取系统总体统计
 */
export function getOverviewStats() {
  return request({
    url: '/stats/overview',
    method: 'get'
  })
}

/**
 * 获取阅读趋势数据
 */
export function getReadingTrend(days = 7) {
  return request({
    url: '/stats/reading-trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取绘本分类统计
 */
export function getBookCategoryStats() {
  return request({
    url: '/stats/book-categories',
    method: 'get'
  })
}

/**
 * 获取年龄段分布
 */
export function getAgeDistribution() {
  return request({
    url: '/stats/age-distribution',
    method: 'get'
  })
}

/**
 * 获取阅读时段分布
 */
export function getReadingHoursDistribution() {
  return request({
    url: '/stats/reading-hours',
    method: 'get'
  })
}

/**
 * 获取管理员仪表盘数据
 */
export function getAdminDashboard() {
  return request({
    url: '/stats/admin-dashboard',
    method: 'get'
  })
}

/**
 * 获取教师仪表盘数据
 */
export function getTeacherDashboard(classId) {
  const params = classId ? { classId } : {}
  return request({
    url: '/stats/teacher-dashboard',
    method: 'get',
    params
  })
}

/**
 * 获取家长仪表盘数据
 */
export function getParentDashboard(childId) {
  return request({
    url: '/stats/parent-dashboard',
    method: 'get',
    params: { childId }
  })
}

/**
 * 获取今日阅读统计
 */
export function getTodayStats() {
  return request({
    url: '/stats/today',
    method: 'get'
  })
}

/**
 * 获取本周阅读统计
 */
export function getWeekStats() {
  return request({
    url: '/stats/week',
    method: 'get'
  })
}

/**
 * 获取今日活跃用户数
 */
export function getActiveUsersToday() {
  return request({
    url: '/stats/active-users-today',
    method: 'get'
  })
}

/**
 * 获取操作日志列表
 */
export function getOperationLogs(current = 1, size = 10) {
  return request({
    url: '/stats/operation-logs',
    method: 'get',
    params: { current, size }
  })
}

/**
 * 获取儿童分析概览数据
 */
export function getChildrenAnalysis() {
  return request({
    url: '/stats/children-analysis',
    method: 'get'
  })
}

/**
 * 获取儿童排行榜
 */
export function getChildrenRanking(type = 'readingTime', limit = 10) {
  return request({
    url: '/stats/children-ranking',
    method: 'get',
    params: { type, limit }
  })
}

export function getBigscreenData() {
  return request({
    url: '/stats/bigscreen',
    method: 'get'
  })
}
