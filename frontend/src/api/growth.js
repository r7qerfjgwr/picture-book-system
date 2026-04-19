import request from '@/utils/request'

export function getGrowthReportList(childId, params) {
  return request({
    url: `/growth/history/${childId}`,
    method: 'get',
    params
  })
}

export function getLatestReport(childId) {
  return request({
    url: `/growth/latest/${childId}`,
    method: 'get'
  })
}

export function generateReport(childId, reportType = 'weekly') {
  return request({
    url: `/growth/generate/${childId}`,
    method: 'post',
    params: { reportType }
  })
}

export function getGrowthTrend(childId) {
  return request({
    url: `/growth/history/${childId}`,
    method: 'get'
  })
}

export function getReportHistoryByType(childId, reportType) {
  return request({
    url: `/growth/history/${childId}/${reportType}`,
    method: 'get'
  })
}

// 新增接口
export function getGrowthCurve(childId, dimension = 'total') {
  return request({
    url: `/growth/curve/${childId}`,
    method: 'get',
    params: { dimension }
  })
}

export function getPeerComparison(childId) {
  return request({
    url: `/growth/peer-comparison/${childId}`,
    method: 'get'
  })
}

export function getMilestones(childId) {
  return request({
    url: `/growth/milestones/${childId}`,
    method: 'get'
  })
}

export function getReadingPlan(childId) {
  return request({
    url: `/growth/reading-plan/${childId}`,
    method: 'get'
  })
}

export function getAbilityRadar(childId) {
  return request({
    url: `/growth/ability-radar/${childId}`,
    method: 'get'
  })
}

export function getParentGuide(childId) {
  return request({
    url: `/growth/parent-guide/${childId}`,
    method: 'get'
  })
}

// 导出成长报告PDF
export function exportReportPdf(reportId) {
  return request({
    url: `/growth/export/pdf/${reportId}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 导出阅读记录Excel
export function exportReadingLogsExcel(childId) {
  return request({
    url: `/reading-log/export/${childId}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 别名，兼容旧代码
export const getReportHistory = getGrowthReportList
