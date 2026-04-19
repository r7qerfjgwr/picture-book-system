import request from '@/utils/request'

export function getRecommendations(childId, params) {
  return request({
    url: `/recommend/${childId}`,
    method: 'get',
    params
  })
}

export function getRecommendationsByType(childId, recommendType) {
  return request({
    url: `/recommend/${childId}/${recommendType}`,
    method: 'get'
  })
}

export function getHotBooks(params) {
  return request({
    url: '/recommend/hot',
    method: 'get',
    params
  })
}

export function updateRecommendations() {
  return request({
    url: '/recommend/update',
    method: 'post'
  })
}

// 新增接口
export function getMultiStrategyRecommendations(childId, limit = 10) {
  return request({
    url: `/recommend/multi-strategy/${childId}`,
    method: 'get',
    params: { limit }
  })
}

export function getDifficultyMatchedRecommendations(childId, limit = 10) {
  return request({
    url: `/recommend/difficulty/${childId}`,
    method: 'get',
    params: { limit }
  })
}

export function getAgeRangeRecommendations(childId, limit = 10) {
  return request({
    url: `/recommend/age-range/${childId}`,
    method: 'get',
    params: { limit }
  })
}

export function getRecommendationExplanation(childId, bookId) {
  return request({
    url: `/recommend/explanation/${childId}/${bookId}`,
    method: 'get'
  })
}

export function calculateMatchScore(childId, bookId) {
  return request({
    url: `/recommend/match-score/${childId}/${bookId}`,
    method: 'get'
  })
}

// 别名，兼容旧代码
export const refreshRecommendations = updateRecommendations
