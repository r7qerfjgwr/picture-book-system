import request from '@/utils/request'

export function getReadingLogList(params) {
  return request({
    url: '/reading/list',
    method: 'get',
    params
  })
}

export function getReadingLogById(id) {
  return request({
    url: `/reading/${id}`,
    method: 'get'
  })
}

export function createReadingLog(data) {
  return request({
    url: '/reading/log',
    method: 'post',
    data
  })
}

export function updateReadingLog(id, data) {
  return request({
    url: `/reading/log/${id}`,
    method: 'put',
    data
  })
}

export function getChildReadingLogs(childId, params) {
  return request({
    url: `/reading/logs/${childId}`,
    method: 'get',
    params
  })
}

export function getReadingStatistics(childId, params) {
  return request({
    url: `/reading/stats/${childId}`,
    method: 'get',
    params
  })
}

export function getReadingTrend(childId, days = 7) {
  return request({
    url: `/reading/trend/${childId}`,
    method: 'get',
    params: { days }
  })
}

export function getCategoryStats(childId) {
  return request({
    url: `/reading/category-stats/${childId}`,
    method: 'get'
  })
}
