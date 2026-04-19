<template>
  <div class="growth-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <span class="header-icon">🌱</span>
        <h2>成长跟踪</h2>
      </div>
      <div class="header-right">
        <el-select v-model="selectedChildId" placeholder="选择儿童" @change="loadGrowthData" class="child-select">
          <el-option v-for="child in childList" :key="child.id" :label="child.name" :value="child.id" />
        </el-select>
        <button class="report-btn weekly" @click="generateReportFn('weekly')">
          <span>📄</span> 生成周报
        </button>
        <button class="report-btn monthly" @click="generateReportFn('monthly')">
          <span>📊</span> 生成月报
        </button>
        <button class="export-btn" @click="exportReport">
          <span>📥</span> 导出报告
        </button>
        <button class="print-btn" @click="printReport">
          <span>🖨️</span> 打印
        </button>
      </div>
    </div>

    <!-- 新用户提示 -->
    <div v-if="!latestReport && selectedChildId" class="empty-data-tip">
      <div class="tip-icon">📚</div>
      <h3>暂无成长数据</h3>
      <p>该儿童还没有阅读记录，请先进行阅读活动以生成成长报告。</p>
      <p class="tip-hint">阅读记录积累后，系统将自动分析并生成成长跟踪报告。</p>
    </div>

    <!-- 核心指标概览 -->
    <div class="metrics-overview" v-if="latestReport">
      <div class="metric-card total-score">
        <div class="metric-circle">
          <svg viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="45" fill="none" stroke="#f0e6d3" stroke-width="8"/>
            <circle cx="50" cy="50" r="45" fill="none" stroke="#e17055" stroke-width="8"
                    :stroke-dasharray="circumference" :stroke-dashoffset="dashOffset"
                    transform="rotate(-90 50 50)" stroke-linecap="round"/>
          </svg>
          <div class="metric-value">
            <span class="number">{{ ((latestReport && latestReport.totalScore) || 0).toFixed(1) }}</span>
            <span class="unit">分</span>
          </div>
        </div>
        <div class="metric-info">
          <span class="metric-label">综合评分</span>
          <span class="metric-badge" :class="getScoreClass(latestReport && latestReport.totalScore)">
            {{ getScoreLabel(latestReport && latestReport.totalScore) }}
          </span>
        </div>
      </div>

      <div class="metric-card books-count">
        <div class="metric-icon">📚</div>
        <div class="metric-data">
          <span class="metric-number">{{ (latestReport && latestReport.bookCount) || 0 }}</span>
          <span class="metric-text">本</span>
        </div>
        <span class="metric-label">阅读绘本</span>
      </div>

      <div class="metric-card duration">
        <div class="metric-icon">⏱️</div>
        <div class="metric-data">
          <span class="metric-number">{{ Math.floor(((latestReport && latestReport.totalDuration) || 0) / 60) }}</span>
          <span class="metric-text">分钟</span>
        </div>
        <span class="metric-label">阅读时长</span>
      </div>

      <div class="metric-card ranking">
        <div class="metric-icon">🏆</div>
        <div class="metric-data">
          <span class="metric-number">{{ peerComparison.percentile || 50 }}</span>
          <span class="metric-text">%</span>
        </div>
        <span class="metric-label">同龄排名</span>
      </div>

      <div class="metric-card trend">
        <div class="metric-icon">{{ trendIcon }}</div>
        <div class="metric-data">
          <span class="metric-number" :class="trendClass">{{ growthTrendLabel }}</span>
        </div>
        <span class="metric-label">成长趋势</span>
      </div>
    </div>

    <!-- 八维度评分 -->
    <div class="dimension-scores">
      <h3 class="section-title"><span class="title-icon">📊</span> 八维度能力评估</h3>
      <div class="dimensions-grid">
        <div v-for="dim in dimensions" :key="dim.key" class="dimension-item">
          <div class="dim-header">
            <span class="dim-icon">{{ dim.icon }}</span>
            <span class="dim-name">{{ dim.name }}</span>
            <span class="dim-value">{{ ((latestReport && latestReport[dim.key]) || 0).toFixed(0) }}分</span>
          </div>
          <div class="dim-bar">
            <div class="bar-fill" :style="{ width: ((latestReport && latestReport[dim.key]) || 0) + '%' }" :class="dim.colorClass"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 成长曲线 -->
      <div class="chart-card full-width">
        <div class="chart-header">
          <div class="chart-title-area">
            <span class="chart-title">📈 成长曲线</span>
            <span class="chart-subtitle">追踪各维度能力的历史变化趋势</span>
          </div>
          <div class="dimension-tabs">
            <span v-for="tab in dimensionTabs" :key="tab.key"
                  :class="['tab-item', { active: selectedDimension === tab.key }]"
                  @click="selectedDimension = tab.key; loadTrendChart()">
              {{ tab.label }}
            </span>
          </div>
        </div>
        <div ref="trendChart" class="chart-container large"></div>
      </div>

      <!-- 能力雷达图 -->
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">🎯 能力雷达图</span>
          <span class="chart-subtitle">八维度能力综合展示</span>
        </div>
        <div ref="radarChart" class="chart-container"></div>
      </div>

      <!-- 同龄对比 -->
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">👥 同龄对比</span>
          <span class="chart-subtitle">与同龄儿童平均能力对比</span>
        </div>
        <div ref="comparisonChart" class="chart-container"></div>
      </div>
    </div>

    <!-- 成长里程碑 -->
    <div class="milestones-section">
      <h3 class="section-title">
        <span class="title-icon">🏅</span> 成长里程碑
        <span v-if="milestones.length" class="badge">{{ milestones.length }}个成就</span>
      </h3>
      <div class="timeline-container">
        <div class="timeline">
          <div v-for="(milestone, index) in milestones" :key="index" class="timeline-item">
            <div class="timeline-marker">
              <span class="marker-icon">{{ getMilestoneIcon(milestone.icon) }}</span>
            </div>
            <div class="timeline-content">
              <div class="milestone-card">
                <div class="milestone-header">
                  <span class="milestone-title">{{ milestone.title }}</span>
                  <span class="milestone-date">{{ milestone.date }}</span>
                </div>
                <p class="milestone-desc">{{ milestone.description }}</p>
              </div>
            </div>
          </div>
        </div>
        <div v-if="!milestones.length" class="empty-milestones">
          <span class="empty-icon">🎯</span>
          <p>暂无里程碑成就</p>
          <p class="empty-hint">继续阅读，解锁更多成就！</p>
        </div>
      </div>
    </div>

    <!-- 阅读计划与家长指导 -->
    <div class="plans-section">
      <div class="plan-card">
        <h3 class="card-title"><span>📋</span> 个性化阅读计划</h3>
        <div class="goals-list">
          <div v-for="(goal, index) in readingPlan.goals" :key="index" class="goal-item">
            <span class="goal-icon">{{ getGoalIcon(goal.type) }}</span>
            <div class="goal-content">
              <span class="goal-title">{{ goal.title }}</span>
              <span class="goal-desc">{{ goal.description }}</span>
            </div>
          </div>
          <div v-if="!readingPlan.goals?.length" class="empty-tip">暂无阅读计划</div>
        </div>
        <div class="recommended-times">
          <h4>推荐阅读时间</h4>
          <div class="times-list">
            <span v-for="time in readingPlan.recommendedTimes" :key="time" class="time-tag">{{ time }}</span>
          </div>
        </div>
      </div>

      <div class="guide-card">
        <h3 class="card-title"><span>👨‍👩‍👧</span> 家长指导建议</h3>
        <div class="suggestions-list">
          <div v-for="(suggestion, index) in parentGuide.suggestions?.slice(0, 5)" :key="index" class="suggestion-item">
            <span class="suggestion-bullet">💡</span>
            <span>{{ suggestion }}</span>
          </div>
        </div>
        <div class="activities-section">
          <h4>推荐亲子活动</h4>
          <div class="activities-grid">
            <div v-for="activity in parentGuide.activities" :key="activity.name" class="activity-item">
              <span class="activity-icon">{{ getActivityIcon(activity.type) }}</span>
              <span class="activity-name">{{ activity.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史报告 -->
    <div class="history-section">
      <h3 class="section-title"><span class="title-icon">📜</span> 历史报告</h3>
      <div class="report-table">
        <div class="table-header">
          <span>报告类型</span>
          <span>词汇量</span>
          <span>逻辑理解</span>
          <span>阅读量</span>
          <span>专注度</span>
          <span>认知发展</span>
          <span>综合得分</span>
          <span>日期</span>
          <span>操作</span>
        </div>
        <div v-for="report in reportHistory" :key="report.id" class="table-row">
          <span>
            <span :class="['type-tag', report.reportType]">{{ report.reportType === 'weekly' ? '周报' : '月报' }}</span>
          </span>
          <span>{{ (report.vocabularyScore || 0).toFixed(0) }}</span>
          <span>{{ (report.logicScore || 0).toFixed(0) }}</span>
          <span>{{ (report.readingScore || 0).toFixed(0) }}</span>
          <span>{{ (report.focusScore || 0).toFixed(0) }}</span>
          <span>{{ (report.cognitiveScore || 0).toFixed(0) }}</span>
          <span>
            <span :class="['score-tag', getScoreClass(report.totalScore)]">{{ (report.totalScore || 0).toFixed(1) }}</span>
          </span>
          <span>{{ report.reportDate }}</span>
          <span><button class="view-btn" @click="viewReport(report)">查看</button></span>
        </div>
        <div v-if="!reportHistory.length" class="empty-table">暂无历史报告</div>
      </div>
    </div>

    <!-- 报告详情弹窗 -->
    <div v-if="reportDialogVisible" class="report-modal" @click.self="reportDialogVisible = false">
      <div class="modal-content">
        <button class="close-btn" @click="reportDialogVisible = false">✕</button>
        <h3 class="modal-title">📄 报告详情</h3>
        <div v-if="currentReport" class="report-detail">
          <div class="detail-scores">
            <div v-for="dim in dimensions" :key="dim.key" class="detail-score-item">
              <span class="detail-icon">{{ dim.icon }}</span>
              <span class="detail-name">{{ dim.name }}</span>
              <span class="detail-value">{{ (currentReport[dim.key] || 0).toFixed(1) }}</span>
            </div>
          </div>
          <div class="detail-suggestion">
            <h4>💡 阅读建议</h4>
            <p>{{ currentReport.suggestion }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getMyChildren } from '@/api/child'
import {
  getLatestReport, getReportHistory, generateReport as generateReportApi,
  getGrowthCurve, getPeerComparison, getMilestones, getReadingPlan, getAbilityRadar, getParentGuide
} from '@/api/growth'

const route = useRoute()

const childList = ref([])
const selectedChildId = ref('')
const latestReport = ref(null)
const reportHistory = ref([])
const milestones = ref([])
const readingPlan = ref({ goals: [], recommendedTimes: [] })
const parentGuide = ref({ suggestions: [], activities: [] })
const peerComparison = ref({})
const growthTrend = ref('stable')
const selectedDimension = ref('total')
const reportDialogVisible = ref(false)
const currentReport = ref(null)

const radarChart = ref()
const comparisonChart = ref()
const trendChart = ref()

let chartInstances = []

const circumference = 2 * Math.PI * 45
const dashOffset = computed(() => {
  const score = (latestReport.value && latestReport.value.totalScore) || 0
  return circumference * (1 - score / 100)
})

const dimensions = [
  { key: 'vocabularyScore', name: '词汇量', icon: '📚', colorClass: 'blue' },
  { key: 'logicScore', name: '逻辑理解', icon: '🧩', colorClass: 'purple' },
  { key: 'readingScore', name: '阅读量', icon: '📖', colorClass: 'green' },
  { key: 'focusScore', name: '专注度', icon: '🎯', colorClass: 'orange' },
  { key: 'cognitiveScore', name: '认知发展', icon: '🧠', colorClass: 'pink' },
  { key: 'socialScore', name: '社交情感', icon: '❤️', colorClass: 'red' },
  { key: 'creativityScore', name: '创造力', icon: '🎨', colorClass: 'cyan' },
  { key: 'habitScore', name: '阅读习惯', icon: '⏰', colorClass: 'yellow' }
]

const dimensionTabs = [
  { key: 'total', label: '综合' },
  { key: 'vocabulary', label: '词汇量' },
  { key: 'logic', label: '逻辑理解' },
  { key: 'reading', label: '阅读量' },
  { key: 'focus', label: '专注度' },
  { key: 'cognitive', label: '认知发展' }
]

const growthTrendLabel = computed(() => {
  const labels = { improving: '进步中', declining: '需关注', stable: '稳定发展' }
  return labels[growthTrend.value] || '稳定'
})

const trendClass = computed(() => {
  return { improving: 'trend-up', declining: 'trend-down', stable: 'trend-stable' }[growthTrend.value] || ''
})

const trendIcon = computed(() => {
  return { improving: '📈', declining: '📉', stable: '➡️' }[growthTrend.value] || '➡️'
})

const getScoreClass = (score) => {
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  return 'needs-improve'
}

const getScoreLabel = (score) => {
  if (score >= 80) return '优秀'
  if (score >= 60) return '良好'
  return '需提升'
}

const getMilestoneIcon = (icon) => {
  const icons = { trophy: '🏆', star: '⭐', medal: '🎖️', crown: '👑', clock: '⏰' }
  return icons[icon] || '🏅'
}

const getGoalIcon = (type) => {
  const icons = { vocabulary: '📖', focus: '🎯', cognitive: '🧠' }
  return icons[type] || '📌'
}

const getActivityIcon = (type) => {
  const icons = { 创意: '🎨', 创作: '✏️', 习惯: '📅' }
  return icons[type] || '🎮'
}

const initCharts = () => {
  [radarChart, comparisonChart, trendChart].forEach(ref => {
    if (ref.value) {
      chartInstances.push(echarts.init(ref.value))
    }
  })
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  chartInstances.forEach(instance => instance?.resize())
}

const loadRadarChart = async () => {
  const instance = chartInstances[0]
  if (!instance) return

  try {
    const res = await getAbilityRadar(selectedChildId.value)
    const data = res.data || {}

    // 如果没有数据，使用默认值
    const defaultDimensions = ['词汇量', '逻辑思维', '阅读能力', '专注度', '认知发展', '社交情感', '创造力', '阅读习惯']
    const dimensions = data.dimensions && data.dimensions.length > 0 ? data.dimensions : defaultDimensions
    const values = data.values && data.values.length > 0 ? data.values : defaultDimensions.map(() => 0)

    const option = {
      tooltip: { trigger: 'item' },
      radar: {
        indicator: dimensions.map(name => ({ name, max: 100 })),
        shape: 'polygon',
        axisName: { color: '#666', fontSize: 11, fontWeight: 'bold' },
        splitLine: { lineStyle: { color: 'rgba(225,112,85,0.2)' } },
        splitArea: { areaStyle: { color: ['rgba(255,236,210,0.3)', 'rgba(255,236,210,0.1)'] } }
      },
      series: [{
        type: 'radar',
        data: [{
          value: values,
          name: '能力评估',
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#e17055', width: 2 },
          areaStyle: { color: 'rgba(225,112,85,0.3)' },
          itemStyle: { color: '#e17055' }
        }]
      }]
    }
    instance.setOption(option)
  } catch (error) {
    console.error('Load radar chart error:', error)
    // 设置空图表
    const defaultDimensions = ['词汇量', '逻辑思维', '阅读能力', '专注度', '认知发展', '社交情感', '创造力', '阅读习惯']
    instance.setOption({
      radar: {
        indicator: defaultDimensions.map(name => ({ name, max: 100 })),
        shape: 'polygon'
      },
      series: [{ type: 'radar', data: [{ value: defaultDimensions.map(() => 0) }] }]
    })
  }
}

const loadComparisonChart = async () => {
  const instance = chartInstances[1]
  if (!instance) return

  try {
    const res = await getPeerComparison(selectedChildId.value)
    peerComparison.value = res.data || {}

    const data = res.data || {}
    const childScores = data.childScores || {}
    const peerAvgScores = data.peerAvgScores || {}

    // 如果没有数据，使用默认值
    const hasData = data.childScores && Object.keys(data.childScores).length > 0
    if (!hasData) {
      childScores.vocabulary = 0
      childScores.logic = 0
      childScores.reading = 0
      childScores.focus = 0
      childScores.total = 0
      peerAvgScores.vocabulary = 50
      peerAvgScores.logic = 50
      peerAvgScores.reading = 50
      peerAvgScores.focus = 50
      peerAvgScores.total = 50
    }

    const option = {
      tooltip: { trigger: 'axis' },
      legend: { bottom: 0, textStyle: { fontSize: 11 } },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['词汇量', '逻辑理解', '阅读量', '专注度', '综合'],
        axisLabel: { fontSize: 10 }
      },
      yAxis: { type: 'value', max: 100, axisLabel: { fontSize: 10 } },
      series: [
        {
          name: '我的得分',
          type: 'bar',
          data: [childScores.vocabulary || 0, childScores.logic || 0, childScores.reading || 0, childScores.focus || 0, childScores.total || 0],
          itemStyle: { color: '#e17055', borderRadius: [6, 6, 0, 0] },
          barWidth: '30%'
        },
        {
          name: '同龄平均',
          type: 'bar',
          data: [peerAvgScores.vocabulary || 0, peerAvgScores.logic || 0, peerAvgScores.reading || 0, peerAvgScores.focus || 0, peerAvgScores.total || 0],
          itemStyle: { color: '#74b9ff', borderRadius: [6, 6, 0, 0] },
          barWidth: '30%'
        }
      ]
    }
    instance.setOption(option)
  } catch (error) {
    console.error('Load comparison chart error:', error)
    // 设置空图表
    instance.setOption({
      xAxis: { type: 'category', data: ['词汇量', '逻辑理解', '阅读量', '专注度', '综合'] },
      yAxis: { type: 'value', max: 100 },
      series: [
        { name: '我的得分', type: 'bar', data: [0, 0, 0, 0, 0] },
        { name: '同龄平均', type: 'bar', data: [50, 50, 50, 50, 50] }
      ]
    })
  }
}

const loadTrendChart = async () => {
  const instance = chartInstances[2]
  if (!instance) return

  try {
    const res = await getGrowthCurve(selectedChildId.value, selectedDimension.value)
    const data = res.data || {}

    // 如果没有数据，使用默认值
    const labels = data.labels && data.labels.length > 0 ? data.labels : ['第1周', '第2周', '第3周', '第4周']
    const chartData = data.data && data.data.length > 0 ? data.data : [0, 0, 0, 0]

    const option = {
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)' },
      grid: { left: '3%', right: '4%', bottom: '10%', top: '8%', containLabel: true },
      xAxis: {
        type: 'category',
        data: labels,
        axisLabel: { fontSize: 10 }
      },
      yAxis: { type: 'value', max: 100, axisLabel: { fontSize: 10 } },
      series: [{
        type: 'line',
        data: chartData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#e17055', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(225,112,85,0.3)' },
            { offset: 1, color: 'rgba(225,112,85,0.05)' }
          ])
        },
        itemStyle: { color: '#e17055' }
      }]
    }
    instance.setOption(option)
  } catch (error) {
    console.error('Load trend chart error:', error)
    // 设置空图表
    instance.setOption({
      xAxis: { type: 'category', data: ['第1周', '第2周', '第3周', '第4周'] },
      yAxis: { type: 'value', max: 100 },
      series: [{ type: 'line', data: [0, 0, 0, 0], smooth: true }]
    })
  }
}

const loadGrowthData = async () => {
  if (!selectedChildId.value) return

  // 重置所有数据
  latestReport.value = null
  reportHistory.value = []
  milestones.value = []
  readingPlan.value = { goals: [], recommendedTimes: [] }
  parentGuide.value = { suggestions: [], activities: [] }
  peerComparison.value = {}

  try {
    // 获取最新报告
    try {
      const latestRes = await getLatestReport(selectedChildId.value)
      latestReport.value = latestRes.data || null
    } catch (e) {
      console.log('获取最新报告失败:', e)
      latestReport.value = null
    }

    // 获取历史报告
    try {
      const historyRes = await getReportHistory(selectedChildId.value)
      reportHistory.value = historyRes.data || []
    } catch (e) {
      console.log('获取历史报告失败:', e)
      reportHistory.value = []
    }

    // 其他API调用
    try { milestones.value = (await getMilestones(selectedChildId.value)).data || [] } catch (e) { console.log('获取里程碑失败:', e); milestones.value = [] }
    try { readingPlan.value = (await getReadingPlan(selectedChildId.value)).data || { goals: [], recommendedTimes: [] } } catch (e) { console.log('获取阅读计划失败:', e); readingPlan.value = { goals: [], recommendedTimes: [] } }
    try { parentGuide.value = (await getParentGuide(selectedChildId.value)).data || { suggestions: [], activities: [] } } catch (e) { console.log('获取家长指导失败:', e); parentGuide.value = { suggestions: [], activities: [] } }

    // 等待 DOM 更新后初始化图表
    await nextTick()
    initCharts()

    // 即使没有报告数据也初始化图表（使用默认值）
    loadRadarChart()
    loadComparisonChart()
    loadTrendChart()
  } catch (error) {
    console.error('Load growth data error:', error)

    // 仍然尝试初始化图表
    await nextTick()
    initCharts()
    loadRadarChart()
    loadComparisonChart()
    loadTrendChart()
  }
}

const generateReportFn = async (type) => {
  if (!selectedChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }
  try {
    const res = await generateReportApi(selectedChildId.value, type)
    loadGrowthData()
    // 判断是否是新生成的报告（通过检查历史报告数量是否增加）
    ElMessage.success(type === 'weekly' ? '周报已就绪' : '月报已就绪')
  } catch (error) {
    ElMessage.error('报告生成失败')
  }
}

const viewReport = (report) => {
  currentReport.value = report
  reportDialogVisible.value = true
}

// 导出报告为PDF
const exportReport = () => {
  if (!selectedChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }

  if (!latestReport.value) {
    ElMessage.warning('暂无报告数据可导出')
    return
  }

  const childName = childList.value.find(c => c.id === selectedChildId.value)?.name || '儿童'
  const reportDate = new Date().toLocaleDateString('zh-CN')

  // 生成报告HTML内容
  const reportContent = generateReportHTML(childName, reportDate)

  // 创建Blob并下载
  const blob = new Blob([reportContent], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `成长报告_${childName}_${reportDate.replace(/\//g, '-')}.html`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('报告导出成功')
}

// 生成报告HTML内容
const generateReportHTML = (childName, reportDate) => {
  const report = latestReport.value
  const dims = dimensions.map(d => ({
    name: d.name,
    icon: d.icon,
    value: ((report && report[d.key]) || 0).toFixed(1)
  }))

  return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>成长报告 - ${childName}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: 'Microsoft YaHei', sans-serif; background: #f5f5f5; padding: 40px; }
    .report-container { max-width: 800px; margin: 0 auto; background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
    .report-header { text-align: center; margin-bottom: 40px; padding-bottom: 20px; border-bottom: 2px solid #ffeaa7; }
    .report-title { font-size: 28px; color: #2d3436; margin-bottom: 10px; }
    .report-subtitle { font-size: 14px; color: #636e72; }
    .score-section { display: flex; justify-content: center; margin-bottom: 40px; }
    .total-score { text-align: center; }
    .score-circle { width: 150px; height: 150px; border-radius: 50%; border: 8px solid #e17055; display: flex; flex-direction: column; align-items: center; justify-content: center; margin: 0 auto; }
    .score-number { font-size: 48px; font-weight: bold; color: #e17055; }
    .score-label { font-size: 14px; color: #636e72; margin-top: 10px; }
    .dimensions-section { margin-bottom: 40px; }
    .section-title { font-size: 18px; color: #2d3436; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; }
    .dimension-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
    .dimension-item { background: #f8f9fa; border-radius: 12px; padding: 16px; display: flex; align-items: center; gap: 12px; }
    .dim-icon { font-size: 24px; }
    .dim-info { flex: 1; }
    .dim-name { font-size: 14px; color: #636e72; }
    .dim-value { font-size: 20px; font-weight: bold; color: #2d3436; }
    .milestones-section, .suggestions-section { margin-bottom: 30px; }
    .milestone-list, .suggestion-list { list-style: none; }
    .milestone-item, .suggestion-item { padding: 12px; background: #fff9f0; border-radius: 8px; margin-bottom: 8px; border-left: 4px solid #ffeaa7; }
    .footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #dfe6e9; color: #b2bec3; font-size: 12px; }
    @media print { body { padding: 0; } .report-container { box-shadow: none; } }
  </style>
</head>
<body>
  <div class="report-container">
    <div class="report-header">
      <h1 class="report-title">🌱 ${childName}的成长报告</h1>
      <p class="report-subtitle">报告日期：${reportDate}</p>
    </div>

    <div class="score-section">
      <div class="total-score">
        <div class="score-circle">
          <span class="score-number">${((report && report.totalScore) || 0).toFixed(1)}</span>
          <span style="font-size: 14px; color: #636e72;">综合评分</span>
        </div>
        <p class="score-label">${getScoreLabel(report && report.totalScore)} · ${getScoreClass(report && report.totalScore) === 'excellent' ? '表现优秀' : getScoreClass(report && report.totalScore) === 'good' ? '表现良好' : '继续努力'}</p>
      </div>
    </div>

    <div class="dimensions-section">
      <h3 class="section-title">📊 八维度能力评估</h3>
      <div class="dimension-grid">
        ${dims.map(d => `
          <div class="dimension-item">
            <span class="dim-icon">${d.icon}</span>
            <div class="dim-info">
              <div class="dim-name">${d.name}</div>
              <div class="dim-value">${d.value}分</div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>

    ${milestones.value.length > 0 ? `
    <div class="milestones-section">
      <h3 class="section-title">🏅 成长里程碑</h3>
      <ul class="milestone-list">
        ${milestones.value.slice(0, 5).map(m => `
          <li class="milestone-item">
            <strong>${m.title}</strong> - ${m.description}
          </li>
        `).join('')}
      </ul>
    </div>
    ` : ''}

    ${parentGuide.value.suggestions?.length > 0 ? `
    <div class="suggestions-section">
      <h3 class="section-title">💡 家长指导建议</h3>
      <ul class="suggestion-list">
        ${parentGuide.value.suggestions.slice(0, 5).map(s => `
          <li class="suggestion-item">${s}</li>
        `).join('')}
      </ul>
    </div>
    ` : ''}

    <div class="footer">
      <p>儿童绘本阅读行为分析与成长跟踪系统</p>
      <p>本报告由系统自动生成，仅供参考</p>
    </div>
  </div>
</body>
</html>`
}

// 打印报告
const printReport = () => {
  if (!selectedChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }

  if (!latestReport.value) {
    ElMessage.warning('暂无报告数据可打印')
    return
  }

  const childName = childList.value.find(c => c.id === selectedChildId.value)?.name || '儿童'
  const reportDate = new Date().toLocaleDateString('zh-CN')
  const reportContent = generateReportHTML(childName, reportDate)

  const printWindow = window.open('', '_blank')
  printWindow.document.write(reportContent)
  printWindow.document.close()
  printWindow.print()
}

onMounted(async () => {
  try {
    const res = await getMyChildren()
    childList.value = res.data || []

    // 优先使用 query 参数中的 childId
    const queryChildId = route.query.childId
    if (queryChildId) {
      selectedChildId.value = Number(queryChildId)
    } else if (childList.value.length > 0) {
      selectedChildId.value = childList.value[0].id
    }

    // 加载成长数据
    if (selectedChildId.value) {
      await loadGrowthData()
    }
  } catch (error) {
    console.error('Load children error:', error)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(instance => instance?.dispose())
})
</script>

<style scoped>
.growth-page {
  font-family: 'Microsoft YaHei', sans-serif;
  padding: 20px;
  background: linear-gradient(180deg, #fef9f3 0%, #fff5eb 100%);
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon { font-size: 28px; }
.header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #2d3436; }

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.child-select { width: 140px; }

.report-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.report-btn.weekly {
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  color: #fff;
}

.report-btn.monthly {
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  color: #fff;
}

.report-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.export-btn, .print-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  background: #fff;
  color: #2d3436;
  border: 2px solid #dfe6e9;
}

.export-btn:hover, .print-btn:hover {
  transform: translateY(-2px);
  border-color: #74b9ff;
  color: #0984e3;
}

/* 核心指标 */
.metrics-overview {
  display: grid;
  grid-template-columns: 1.5fr repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.metric-card.total-score {
  flex-direction: row;
  gap: 20px;
}

.metric-circle {
  position: relative;
  width: 100px;
  height: 100px;
}

.metric-circle svg { width: 100%; height: 100%; }

.metric-value {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.metric-value .number { display: block; font-size: 26px; font-weight: 800; color: #e17055; }
.metric-value .unit { font-size: 12px; color: #a0937d; }

.metric-info { text-align: center; }
.metric-label { display: block; font-size: 13px; color: #6d4c41; margin-top: 8px; }
.metric-badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; margin-top: 8px; }
.metric-badge.excellent { background: #d4fc79; color: #2d3436; }
.metric-badge.good { background: #ffeaa7; color: #2d3436; }
.metric-badge.needs-improve { background: #fab1a0; color: #2d3436; }

.metric-icon { font-size: 32px; margin-bottom: 8px; }
.metric-data { display: flex; align-items: baseline; gap: 4px; }
.metric-number { font-size: 28px; font-weight: 800; color: #2d3436; }
.metric-text { font-size: 14px; color: #a0937d; }

.trend-up { color: #55efc4 !important; }
.trend-down { color: #fab1a0 !important; }
.trend-stable { color: #74b9ff !important; }

/* 八维度评分 */
.dimension-scores {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.title-icon { font-size: 20px; }
.badge { padding: 4px 10px; background: #ffeaa7; border-radius: 10px; font-size: 12px; margin-left: 8px; }

.dimensions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.dimension-item {
  padding: 12px;
  background: #fef9f0;
  border-radius: 12px;
}

.dim-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.dim-icon { font-size: 18px; }
.dim-name { font-size: 13px; color: #6d4c41; flex: 1; }
.dim-value { font-size: 14px; font-weight: 700; color: #2d3436; }

.dim-bar { height: 8px; background: #f0e6d3; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; }
.bar-fill.blue { background: linear-gradient(90deg, #74b9ff, #0984e3); }
.bar-fill.purple { background: linear-gradient(90deg, #a29bfe, #6c5ce7); }
.bar-fill.green { background: linear-gradient(90deg, #55efc4, #00b894); }
.bar-fill.orange { background: linear-gradient(90deg, #ffeaa7, #fdcb6e); }
.bar-fill.pink { background: linear-gradient(90deg, #fd79a8, #e84393); }
.bar-fill.red { background: linear-gradient(90deg, #fab1a0, #e17055); }
.bar-fill.cyan { background: linear-gradient(90deg, #81ecec, #00cec9); }
.bar-fill.yellow { background: linear-gradient(90deg, #ffeaa7, #fdcb6e); }

/* 图表区域 */
.charts-section { margin-bottom: 24px; }

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.chart-card.full-width {
  margin-bottom: 20px;
}

.charts-section:not(.chart-card) {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px dashed #f0e6d3;
}

.chart-title { display: block; font-size: 16px; font-weight: 700; color: #2d3436; }
.chart-subtitle { display: block; font-size: 12px; color: #a0937d; margin-top: 4px; }

.dimension-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tab-item {
  padding: 6px 12px;
  background: #fef9f0;
  border-radius: 8px;
  font-size: 12px;
  color: #6d4c41;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-item:hover { background: #ffeaa7; }
.tab-item.active { background: #e17055; color: #fff; }

.chart-container { width: 100%; height: 280px; }
.chart-container.large { height: 300px; }

/* 里程碑 */
.milestones-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.timeline { position: relative; padding-left: 40px; }
.timeline::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #ffeaa7, #fdcb6e, #e17055);
  border-radius: 2px;
}

.timeline-item {
  position: relative;
  margin-bottom: 20px;
}

.timeline-marker {
  position: absolute;
  left: -40px;
  width: 32px;
  height: 32px;
  background: #fff;
  border: 3px solid #ffeaa7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.marker-icon { font-size: 16px; }

.milestone-card {
  background: #fef9f0;
  border-radius: 12px;
  padding: 16px;
}

.milestone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.milestone-title { font-weight: 700; color: #2d3436; }
.milestone-date { font-size: 12px; color: #a0937d; }
.milestone-desc { font-size: 13px; color: #6d4c41; margin: 0; }

.empty-milestones {
  text-align: center;
  padding: 40px;
  color: #a0937d;
}

.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-hint { font-size: 13px; }

/* 阅读计划 */
.plans-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.plan-card, .guide-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.goals-list { margin-bottom: 16px; }
.goal-item { display: flex; align-items: flex-start; gap: 12px; padding: 12px; background: #fef9f0; border-radius: 10px; margin-bottom: 8px; }
.goal-icon { font-size: 24px; }
.goal-content { display: flex; flex-direction: column; gap: 4px; }
.goal-title { font-weight: 600; color: #2d3436; }
.goal-desc { font-size: 12px; color: #6d4c41; }

.recommended-times h4 { margin: 0 0 10px 0; font-size: 14px; color: #2d3436; }
.times-list { display: flex; flex-wrap: wrap; gap: 8px; }
.time-tag { padding: 6px 12px; background: #ffeaa7; border-radius: 8px; font-size: 12px; font-weight: 600; color: #2d3436; }

.suggestions-list { margin-bottom: 16px; }
.suggestion-item { display: flex; align-items: flex-start; gap: 8px; padding: 8px 0; border-bottom: 1px dashed #f0e6d3; font-size: 13px; color: #6d4c41; }
.suggestion-bullet { font-size: 14px; }

.activities-section h4 { margin: 0 0 10px 0; font-size: 14px; color: #2d3436; }
.activities-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.activity-item { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 12px; background: #fef9f0; border-radius: 10px; }
.activity-icon { font-size: 24px; }
.activity-name { font-size: 12px; font-weight: 600; color: #2d3436; }

.empty-tip { color: #a0937d; font-size: 13px; text-align: center; padding: 20px; }

/* 历史报告 */
.history-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.report-table { overflow-x: auto; }

.table-header, .table-row {
  display: grid;
  grid-template-columns: 80px repeat(6, 70px) 100px 80px 60px;
  gap: 8px;
  padding: 12px 0;
  align-items: center;
}

.table-header {
  background: #fef9f0;
  border-radius: 8px;
  font-weight: 600;
  color: #6d4c41;
  font-size: 12px;
  text-align: center;
}

.table-row {
  border-bottom: 1px solid #f0e6d3;
  font-size: 13px;
  text-align: center;
}

.type-tag { padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.type-tag.weekly { background: #e17055; color: #fff; }
.type-tag.monthly { background: #55efc4; color: #2d3436; }

.score-tag { padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.score-tag.excellent { background: #d4fc79; color: #2d3436; }
.score-tag.good { background: #ffeaa7; color: #2d3436; }
.score-tag.needs-improve { background: #fab1a0; color: #2d3436; }

.view-btn {
  padding: 6px 12px;
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  border: none;
  border-radius: 6px;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
}

.empty-table { text-align: center; padding: 40px; color: #a0937d; }

/* 弹窗 */
.report-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: #ffeaa7;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
}

.modal-title { margin: 0 0 20px 0; font-size: 18px; font-weight: 700; color: #2d3436; }

.detail-scores {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.detail-score-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #fef9f0;
  border-radius: 10px;
}

.detail-icon { font-size: 20px; }
.detail-name { flex: 1; font-size: 13px; color: #6d4c41; }
.detail-value { font-weight: 700; color: #2d3436; }

.detail-suggestion h4 { margin: 0 0 10px 0; color: #2d3436; }
.detail-suggestion p { margin: 0; font-size: 13px; color: #6d4c41; line-height: 1.8; white-space: pre-wrap; background: #fef9f0; padding: 16px; border-radius: 10px; }

/* 空数据提示 */
.empty-data-tip {
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  margin-bottom: 24px;
  border: 3px solid #f0e6d3;
}

.empty-data-tip .tip-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-data-tip h3 {
  font-size: 20px;
  color: #2d3436;
  margin-bottom: 12px;
}

.empty-data-tip p {
  font-size: 14px;
  color: #6d4c41;
  margin-bottom: 8px;
}

.empty-data-tip .tip-hint {
  font-size: 12px;
  color: #a0937d;
}

/* 响应式 */
@media (max-width: 1200px) {
  .metrics-overview { grid-template-columns: repeat(3, 1fr); }
  .metrics-overview .metric-card:first-child { grid-column: span 3; flex-direction: column; }
  .dimensions-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .metrics-overview { grid-template-columns: repeat(2, 1fr); }
  .metrics-overview .metric-card:first-child { grid-column: span 2; }
  .charts-section:not(.chart-card) { grid-template-columns: 1fr; }
  .plans-section { grid-template-columns: 1fr; }
  .activities-grid { grid-template-columns: 1fr; }
}
</style>
