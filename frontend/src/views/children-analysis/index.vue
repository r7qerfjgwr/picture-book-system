<template>
  <div class="children-analysis" v-loading="loading">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <span class="header-icon">👶</span>
        <h2>儿童分析概览</h2>
      </div>
      <p class="header-desc">全面了解系统中所有儿童的阅读行为特征和发展状况</p>
    </div>

    <!-- 统计卡片 - 儿童维度 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #a8edea 0%, #74b9ff 100%)">👦</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalChildren }}</div>
          <div class="stat-label">儿童总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.avgFocusScore }}</div>
          <div class="stat-label">平均专注度</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📖</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalReadingTime }}</div>
          <div class="stat-label">总阅读时长(分钟)</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.avgReadingCount }}</div>
          <div class="stat-label">人均阅读次数</div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 - 平台维度 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fdcb6e 0%, #e17055 100%)">📚</div>
        <div class="stat-info">
          <div class="stat-value">{{ platformStats.bookCount }}</div>
          <div class="stat-label">绘本总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fdcb6e 0%, #e17055 100%)">📝</div>
        <div class="stat-info">
          <div class="stat-value">{{ platformStats.totalReads }}</div>
          <div class="stat-label">总阅读次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fdcb6e 0%, #e17055 100%)">📅</div>
        <div class="stat-info">
          <div class="stat-value">{{ platformStats.todayReads }}</div>
          <div class="stat-label">今日阅读</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fdcb6e 0%, #e17055 100%)">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ platformStats.activeRate }}%</div>
          <div class="stat-label">7日活跃率</div>
        </div>
      </div>
    </div>

    <!-- 图表区 第一行：阅读类型 + 绘本分类 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">🏷️</span>
          <span class="header-title">儿童阅读类型分布</span>
        </div>
        <div ref="typeChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">📚</span>
          <span class="header-title">绘本分类分布</span>
        </div>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 图表区 第二行：专注度 + 年龄段 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">🎯</span>
          <span class="header-title">儿童专注度分布</span>
        </div>
        <div ref="focusChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">👶</span>
          <span class="header-title">年龄段分布</span>
        </div>
        <div ref="ageChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 图表区 第三行：阅读能力 + 分类阅读量 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">📈</span>
          <span class="header-title">儿童阅读能力分布</span>
        </div>
        <div ref="abilityChartRef" class="chart-container ability-chart"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">📊</span>
          <span class="header-title">分类阅读量</span>
        </div>
        <div ref="categoryReadsChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 图表区 第四行：近30天阅读趋势（全宽） -->
    <div class="chart-card full-width">
      <div class="card-header">
        <span class="header-icon">📈</span>
        <span class="header-title">近30天阅读趋势</span>
      </div>
      <div ref="trendChartRef" class="chart-container wide-chart"></div>
    </div>

    <!-- 图表区 第五行：阅读时段热力图（全宽） -->
    <div class="chart-card full-width">
      <div class="card-header">
        <span class="header-icon">🕐</span>
        <span class="header-title">阅读时段热力图</span>
      </div>
      <div ref="heatmapChartRef" class="chart-container wide-chart"></div>
    </div>

    <!-- 图表区 第六行：热门绘本 + 班级排行 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">🔥</span>
          <span class="header-title">热门绘本排行</span>
        </div>
        <div ref="hotBooksChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">🏫</span>
          <span class="header-title">班级阅读排行</span>
        </div>
        <div ref="classRankChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 儿童阅读排行榜 -->
    <div class="ranking-section">
      <div class="card-header">
        <span class="header-icon">🏆</span>
        <span class="header-title">儿童阅读排行榜</span>
        <div class="header-tabs">
          <button :class="['tab-btn', { active: rankingType === 'readingTime' }]" @click="changeRankingType('readingTime')">阅读时长</button>
          <button :class="['tab-btn', { active: rankingType === 'readingCount' }]" @click="changeRankingType('readingCount')">阅读次数</button>
          <button :class="['tab-btn', { active: rankingType === 'focusScore' }]" @click="changeRankingType('focusScore')">专注度</button>
        </div>
      </div>
      <el-table :data="rankingList" stripe style="width: 100%">
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <div class="rank-badge" :class="'rank-' + ($index + 1)">
              {{ $index + 1 }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="childName" label="儿童姓名" width="120" />
        <el-table-column prop="age" label="年龄" width="80">
          <template #default="{ row }">
            {{ row.age }}岁
          </template>
        </el-table-column>
        <el-table-column prop="readingType" label="阅读类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.readingType)" size="small">
              {{ row.readingType || '未分类' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" :label="rankingLabel" width="150">
          <template #default="{ row }">
            <span class="value-text">{{ formatValue(row.value) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="focusScore" label="专注度评分" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.focusScore || 0" :color="getProgressColor(row.focusScore)" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column prop="className" label="所属班级" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getChildrenAnalysis, getChildrenRanking, getBigscreenData } from '@/api/stats'

const loading = ref(false)
const rankingType = ref('readingTime')

const statistics = ref({
  totalChildren: 0,
  avgFocusScore: 0,
  totalReadingTime: 0,
  avgReadingCount: 0
})

const platformStats = ref({
  bookCount: 0,
  totalReads: 0,
  todayReads: 0,
  activeRate: 0
})

const typeChartRef = ref()
const focusChartRef = ref()
const abilityChartRef = ref()
const categoryChartRef = ref()
const ageChartRef = ref()
const categoryReadsChartRef = ref()
const trendChartRef = ref()
const heatmapChartRef = ref()
const hotBooksChartRef = ref()
const classRankChartRef = ref()
const rankingList = ref([])

let typeChart = null
let focusChart = null
let abilityChart = null
let categoryChart = null
let ageChart = null
let categoryReadsChart = null
let trendChart = null
let heatmapChart = null
let hotBooksChart = null
let classRankChart = null

const rankingLabel = computed(() => {
  const labels = {
    readingTime: '阅读时长(分钟)',
    readingCount: '阅读次数',
    focusScore: '专注度评分'
  }
  return labels[rankingType.value]
})

const formatValue = (value) => {
  if (rankingType.value === 'readingTime') {
    return `${value} 分钟`
  } else if (rankingType.value === 'readingCount') {
    return `${value} 次`
  }
  return value
}

const getTypeTagType = (type) => {
  const types = {
    '专注型': 'success',
    '兴趣导向型': 'warning',
    '跳跃型': 'info'
  }
  return types[type] || ''
}

const getProgressColor = (score) => {
  if (score >= 80) return '#1dd1a1'
  if (score >= 60) return '#feca57'
  return '#ff6b6b'
}

const tooltipStyle = {
  backgroundColor: 'rgba(255, 255, 255, 0.95)',
  borderColor: '#ffeaa7',
  borderWidth: 2,
  textStyle: { color: '#2d3436' }
}

const axisLineStyle = { lineStyle: { color: '#f0e6d3' } }
const axisLabelStyle = { color: '#a0937d', fontWeight: 600 }
const splitLineStyle = { lineStyle: { color: '#f0e6d3', type: 'dashed' } }

const initChart = (el) => {
  const chart = echarts.init(el)
  return chart
}

const loadStatistics = async () => {
  try {
    const res = await getChildrenAnalysis()
    if (res.code === 200 && res.data) {
      statistics.value = {
        totalChildren: res.data.totalChildren || 0,
        avgFocusScore: res.data.avgFocusScore || 0,
        totalReadingTime: res.data.totalReadingTime || 0,
        avgReadingCount: res.data.avgReadingCount || 0
      }
      return res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
  return null
}

const loadBigscreenData = async () => {
  try {
    const res = await getBigscreenData()
    if (res.code === 200 && res.data) {
      const ov = res.data.overview || {}
      platformStats.value = {
        bookCount: ov.bookCount || 0,
        totalReads: ov.totalReads || 0,
        todayReads: ov.todayReads || 0,
        activeRate: ov.activeRate || 0
      }
      return res.data
    }
  } catch (error) {
    console.error('加载大屏数据失败:', error)
  }
  return null
}

const loadRanking = async () => {
  try {
    const res = await getChildrenRanking(rankingType.value, 10)
    if (res.code === 200 && res.data) {
      rankingList.value = res.data || []
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
  }
}

const changeRankingType = (type) => {
  rankingType.value = type
  loadRanking()
}

const initTypeChart = (data) => {
  if (!typeChartRef.value) return
  typeChart = initChart(typeChartRef.value)

  const typeData = data?.typeDistribution || [
    { name: '专注型', value: 45 },
    { name: '兴趣导向型', value: 35 },
    { name: '跳跃型', value: 20 }
  ]
  const colors = ['#1dd1a1', '#feca57', '#ff6b6b']

  typeChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { color: '#6d4c41', fontWeight: 600 } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 3 },
      label: { show: true, formatter: '{b}\n{c}人', color: '#6d4c41', fontWeight: 600 },
      data: typeData.map((item, index) => ({
        name: item.name, value: item.value,
        itemStyle: { color: colors[index % colors.length] }
      }))
    }]
  })
}

const initFocusChart = (data) => {
  if (!focusChartRef.value) return
  focusChart = initChart(focusChartRef.value)

  const focusData = data?.focusDistribution || [
    { range: '90-100分', count: 15 },
    { range: '80-89分', count: 28 },
    { range: '70-79分', count: 22 },
    { range: '60-69分', count: 18 },
    { range: '60分以下', count: 8 }
  ]
  const colors = ['#1dd1a1', '#55efc4', '#feca57', '#ff9f43', '#ff6b6b']

  focusChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'axis', formatter: '{b}: {c}人' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: focusData.map(item => item.range), axisLine: axisLineStyle, axisLabel: axisLabelStyle },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: splitLineStyle, axisLabel: { color: '#a0937d' } },
    series: [{
      type: 'bar', barWidth: '50%',
      data: focusData.map((item, index) => ({
        value: item.count, itemStyle: { color: colors[index], borderRadius: [8, 8, 0, 0] }
      })),
      label: { show: true, position: 'top', color: '#6d4c41', fontWeight: 600 }
    }]
  })
}

const initAbilityChart = (data) => {
  if (!abilityChartRef.value) return
  abilityChart = initChart(abilityChartRef.value)

  const abilityData = data?.abilityDistribution || [
    { name: '词汇量', high: 35, medium: 40, low: 25 },
    { name: '阅读速度', high: 28, medium: 45, low: 27 },
    { name: '理解能力', high: 32, medium: 42, low: 26 },
    { name: '记忆能力', high: 30, medium: 44, low: 26 },
    { name: '专注度', high: 38, medium: 38, low: 24 },
    { name: '阅读习惯', high: 25, medium: 42, low: 33 }
  ]

  abilityChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'axis' },
    legend: { bottom: 0, data: ['优秀(≥80)', '中等(60-79)', '待提升(<60)'], textStyle: { color: '#6d4c41', fontWeight: 600 } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: abilityData.map(item => item.name), axisLine: axisLineStyle, axisLabel: axisLabelStyle },
    yAxis: { type: 'value', name: '人数', axisLine: { show: false }, splitLine: splitLineStyle, axisLabel: { color: '#a0937d' } },
    series: [
      { name: '优秀(≥80)', type: 'bar', stack: 'total', data: abilityData.map(item => item.high), itemStyle: { color: '#1dd1a1' } },
      { name: '中等(60-79)', type: 'bar', stack: 'total', data: abilityData.map(item => item.medium), itemStyle: { color: '#feca57' } },
      { name: '待提升(<60)', type: 'bar', stack: 'total', data: abilityData.map(item => item.low), itemStyle: { color: '#ff6b6b', borderRadius: [8, 8, 0, 0] } }
    ]
  })
}

const initCategoryChart = (bsData) => {
  if (!categoryChartRef.value) return
  categoryChart = initChart(categoryChartRef.value)

  const catData = (bsData?.categoryDistribution || []).map(d => ({ name: d.category || d.name, value: d.count || d.value }))

  categoryChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'item', formatter: '{b}: {c}本 ({d}%)' },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { color: '#6d4c41', fontWeight: 600 } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 3 },
      label: { show: true, formatter: '{b}\n{c}本', color: '#6d4c41', fontWeight: 600 },
      data: catData,
      color: ['#74b9ff', '#1dd1a1', '#feca57', '#ff6b6b', '#a29bfe', '#fd79a8', '#fdcb6e', '#55efc4', '#ff9f43', '#6c5ce7']
    }]
  })
}

const initAgeChart = (bsData) => {
  if (!ageChartRef.value) return
  ageChart = initChart(ageChartRef.value)

  const ageData = bsData?.ageDistribution || []

  ageChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'axis' },
    legend: { bottom: 0, data: ['儿童数', '阅读次数'], textStyle: { color: '#6d4c41', fontWeight: 600 } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: ageData.map(a => a.ageRange), axisLine: axisLineStyle, axisLabel: axisLabelStyle },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: splitLineStyle, axisLabel: { color: '#a0937d' } },
    series: [
      { name: '儿童数', type: 'bar', data: ageData.map(a => a.childCount), itemStyle: { color: '#74b9ff', borderRadius: [6, 6, 0, 0] }, barWidth: 20 },
      { name: '阅读次数', type: 'bar', data: ageData.map(a => a.readCount), itemStyle: { color: '#1dd1a1', borderRadius: [6, 6, 0, 0] }, barWidth: 20 }
    ]
  })
}

const initCategoryReadsChart = (bsData) => {
  if (!categoryReadsChartRef.value) return
  categoryReadsChart = initChart(categoryReadsChartRef.value)

  const crData = bsData?.categoryReads || []

  categoryReadsChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: crData.map(c => c.name || c.category), axisLine: axisLineStyle, axisLabel: { ...axisLabelStyle, rotate: 20 } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: splitLineStyle, axisLabel: { color: '#a0937d' } },
    series: [{
      type: 'bar',
      data: crData.map(c => c.readCount || c.count || 0),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 1, 0, 0, [{ offset: 0, color: '#74b9ff' }, { offset: 1, color: '#1dd1a1' }]), borderRadius: [6, 6, 0, 0] }
    }]
  })
}

const initTrendChart = (bsData) => {
  if (!trendChartRef.value) return
  trendChart = initChart(trendChartRef.value)

  const trend = bsData?.readingTrend || []

  trendChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'axis' },
    legend: { bottom: 0, data: ['阅读次数', '阅读时长(分钟)'], textStyle: { color: '#6d4c41', fontWeight: 600 } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: trend.map(t => t.date), axisLine: axisLineStyle, axisLabel: { ...axisLabelStyle, fontSize: 10 } },
    yAxis: [
      { type: 'value', name: '次数', nameTextStyle: { color: '#a0937d' }, axisLine: { show: false }, splitLine: splitLineStyle, axisLabel: { color: '#a0937d' } },
      { type: 'value', name: '分钟', nameTextStyle: { color: '#a0937d' }, axisLine: { show: false }, splitLine: { show: false }, axisLabel: { color: '#a0937d' } }
    ],
    series: [
      { name: '阅读次数', type: 'line', data: trend.map(t => t.count), smooth: true, lineStyle: { color: '#74b9ff' }, itemStyle: { color: '#74b9ff' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(116,185,255,0.3)' }, { offset: 1, color: 'rgba(116,185,255,0.02)' }]) } },
      { name: '阅读时长(分钟)', type: 'line', yAxisIndex: 1, data: trend.map(t => Math.round(t.duration / 60)), smooth: true, lineStyle: { color: '#1dd1a1' }, itemStyle: { color: '#1dd1a1' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(29,209,161,0.3)' }, { offset: 1, color: 'rgba(29,209,161,0.02)' }]) } }
    ]
  })
}

const initHeatmapChart = (bsData) => {
  if (!heatmapChartRef.value) return
  heatmapChart = initChart(heatmapChartRef.value)

  const hmData = bsData?.readingHeatmap || []
  const hours = Array.from({ length: 24 }, (_, i) => i + ':00')
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const maxVal = Math.max(...hmData.map(d => d[2]), 1)

  heatmapChart.setOption({
    tooltip: { ...tooltipStyle, formatter: p => `${days[p.data[0] % 7]} ${hours[p.data[1]]}<br/>阅读次数: ${p.data[2]}` },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: hours, axisLine: axisLineStyle, axisLabel: { color: '#a0937d', fontSize: 9, interval: 2 } },
    yAxis: { type: 'category', data: days, axisLine: axisLineStyle, axisLabel: axisLabelStyle },
    visualMap: { min: 0, max: maxVal, calculable: true, orient: 'horizontal', left: 'center', bottom: 0, textStyle: { color: '#a0937d' }, inRange: { color: ['#f0e6d3', '#fdcb6e', '#feca57', '#ff9f43', '#ff6b6b'] } },
    series: [{ type: 'heatmap', data: hmData, label: { show: false }, emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' } } }]
  })
}

const initHotBooksChart = (bsData) => {
  if (!hotBooksChartRef.value) return
  hotBooksChart = initChart(hotBooksChartRef.value)

  const hotBooks = [...(bsData?.hotBooks || [])].reverse()

  hotBooksChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'axis' },
    grid: { left: 100, right: 30, top: 10, bottom: 20 },
    xAxis: { type: 'value', axisLine: axisLineStyle, axisLabel: { color: '#a0937d' }, splitLine: splitLineStyle },
    yAxis: { type: 'category', data: hotBooks.map(b => b.title?.length > 8 ? b.title.slice(0, 8) + '…' : b.title), axisLine: axisLineStyle, axisLabel: { ...axisLabelStyle, fontSize: 11 } },
    series: [{
      type: 'bar', data: hotBooks.map(b => b.readCount), barWidth: 14,
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#74b9ff' }, { offset: 1, color: '#a29bfe' }]), borderRadius: [0, 6, 6, 0] },
      label: { show: true, position: 'right', color: '#6d4c41', fontWeight: 600, fontSize: 10 }
    }]
  })
}

const initClassRankChart = (bsData) => {
  if (!classRankChartRef.value) return
  classRankChart = initChart(classRankChartRef.value)

  const classRank = [...(bsData?.classRanking || [])].reverse()

  classRankChart.setOption({
    tooltip: { ...tooltipStyle, trigger: 'axis' },
    grid: { left: 80, right: 30, top: 10, bottom: 20 },
    xAxis: { type: 'value', name: '人均次数', nameTextStyle: { color: '#a0937d' }, axisLine: axisLineStyle, axisLabel: { color: '#a0937d' }, splitLine: splitLineStyle },
    yAxis: { type: 'category', data: classRank.map(c => c.className), axisLine: axisLineStyle, axisLabel: { ...axisLabelStyle, fontSize: 11 } },
    series: [{
      type: 'bar', data: classRank.map(c => c.avgReadCount), barWidth: 14,
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#ff9f43' }, { offset: 1, color: '#feca57' }]), borderRadius: [0, 6, 6, 0] },
      label: { show: true, position: 'right', color: '#6d4c41', fontWeight: 600, fontSize: 10 }
    }]
  })
}

const handleResize = () => {
  typeChart?.resize()
  focusChart?.resize()
  abilityChart?.resize()
  categoryChart?.resize()
  ageChart?.resize()
  categoryReadsChart?.resize()
  trendChart?.resize()
  heatmapChart?.resize()
  hotBooksChart?.resize()
  classRankChart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    const [analysisData, bsData] = await Promise.all([loadStatistics(), loadBigscreenData()])

    initTypeChart(analysisData)
    initFocusChart(analysisData)
    initAbilityChart(analysisData)

    if (bsData) {
      initCategoryChart(bsData)
      initAgeChart(bsData)
      initCategoryReadsChart(bsData)
      initTrendChart(bsData)
      initHeatmapChart(bsData)
      initHotBooksChart(bsData)
      initClassRankChart(bsData)
    }

    await loadRanking()
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  focusChart?.dispose()
  abilityChart?.dispose()
  categoryChart?.dispose()
  ageChart?.dispose()
  categoryReadsChart?.dispose()
  trendChart?.dispose()
  heatmapChart?.dispose()
  hotBooksChart?.dispose()
  classRankChart?.dispose()
})
</script>

<style scoped>
.children-analysis {
  font-family: 'Nunito', sans-serif;
}

.page-header {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  border-radius: 20px;
  padding: 24px 32px;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.header-icon {
  font-size: 32px;
}

.header-content h2 {
  font-size: 24px;
  font-weight: 800;
  color: #2d3436;
  margin: 0;
}

.header-desc {
  color: #6d4c41;
  font-size: 14px;
  margin: 0;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 3px solid #f0e6d3;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #2d3436;
}

.stat-label {
  font-size: 13px;
  color: #a0937d;
  font-weight: 600;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  border: 3px solid #f0e6d3;
  margin-bottom: 0;
}

.chart-card.full-width {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px dashed #f0e6d3;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.chart-container {
  height: 280px;
}

.ability-chart {
  height: 320px;
}

.wide-chart {
  height: 300px;
}

/* 排行榜 */
.ranking-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 3px solid #f0e6d3;
  margin-top: 24px;
}

.header-tabs {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 8px 16px;
  border: none;
  background: #f0e6d3;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #a0937d;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  color: #fff;
}

.tab-btn:hover {
  transform: scale(1.05);
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffed4a);
  color: #fff;
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: #fff;
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #daa06d);
  color: #fff;
}

.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) {
  background: #f0e6d3;
  color: #a0937d;
}

.value-text {
  font-weight: 700;
  color: #e17055;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
