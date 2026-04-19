import request from '@/utils/request'

export function getAnalysisResult(childId) {
  return request({
    url: `/analysis/${childId}`,
    method: 'get'
  })
}

export function analyzeChild(childId) {
  return request({
    url: `/analysis/analyze/${childId}`,
    method: 'post'
  })
}

export function analyzeAllChildren() {
  return request({
    url: '/analysis/analyze-all',
    method: 'post'
  })
}

export function getClassAnalysis(classId) {
  return request({
    url: `/analysis/class/${classId}`,
    method: 'get'
  })
}

export function getClassAnalysisStats(classId) {
  return request({
    url: `/analysis/class-stats/${classId}`,
    method: 'get'
  })
}

// 新增接口
export function getMultiDimensionAnalysis(childId) {
  return request({
    url: `/analysis/multi-dimension/${childId}`,
    method: 'get'
  })
}

export function getReadingDurationDistribution(childId) {
  return request({
    url: `/analysis/duration-distribution/${childId}`,
    method: 'get'
  })
}

export function getTurnSpeedAnalysis(childId) {
  return request({
    url: `/analysis/turn-speed-analysis/${childId}`,
    method: 'get'
  })
}

export function getGrowthTrajectory(childId) {
  return request({
    url: `/analysis/growth-trajectory/${childId}`,
    method: 'get'
  })
}

export function getReplayPreferenceScore(childId) {
  return request({
    url: `/analysis/replay-score/${childId}`,
    method: 'get'
  })
}

export function getInteractionScore(childId) {
  return request({
    url: `/analysis/interaction-score/${childId}`,
    method: 'get'
  })
}

export function getEmotionStabilityScore(childId) {
  return request({
    url: `/analysis/emotion-score/${childId}`,
    method: 'get'
  })
}

export function getCognitiveStage(childId) {
  return request({
    url: `/analysis/cognitive-stage/${childId}`,
    method: 'get'
  })
}

export function getReadingAbilityScore(childId) {
  return request({
    url: `/analysis/reading-ability/${childId}`,
    method: 'get'
  })
}

export function getHabitFormationScore(childId) {
  return request({
    url: `/analysis/habit-score/${childId}`,
    method: 'get'
  })
}

// 兼容旧方法名
export const runAnalysis = analyzeChild
