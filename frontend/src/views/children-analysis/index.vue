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

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon">📚</div>
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

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 儿童阅读类型分布 -->
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">🏷️</span>
          <span class="header-title">儿童阅读类型分布</span>
        </div>
        <div ref="typeChartRef" class="chart-container"></div>
      </div>

      <!-- 儿童专注度分布 -->
      <div class="chart-card">
        <div class="card-header">
          <span class="header-icon">🎯</span>
          <span class="header-title">儿童专注度分布</span>
        </div>
        <div ref="focusChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 阅读能力分布 -->
    <div class="chart-card full-width">
      <div class="card-header">
        <span class="header-icon">📈</span>
        <span class="header-title">儿童阅读能力分布</span>
      </div>
      <div ref="abilityChartRef" class="chart-container ability-chart"></div>
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
import { getChildrenAnalysis, getChildrenRanking } from '@/api/stats'

const loading = ref(false)
const rankingType = ref('readingTime')

const statistics = ref({
  totalChildren: 0,
  avgFocusScore: 0,
  totalReadingTime: 0,
  avgReadingCount: 0
})

const typeChartRef = ref()
const focusChartRef = ref()
const abilityChartRef = ref()
const rankingList = ref([])

let typeChart = null
let focusChart = null
let abilityChart = null

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

  typeChart = echarts.init(typeChartRef.value)

  const typeData = data?.typeDistribution || [
    { name: '专注型', value: 45 },
    { name: '兴趣导向型', value: 35 },
    { name: '跳跃型', value: 20 }
  ]

  const colors = ['#1dd1a1', '#feca57', '#ff6b6b']

  typeChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2,
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      bottom: 0,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#6d4c41', fontWeight: 600 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        show: true,
        formatter: '{b}\n{c}人',
        color: '#6d4c41',
        fontWeight: 600
      },
      data: typeData.map((item, index) => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: colors[index % colors.length] }
      }))
    }]
  })
}

const initFocusChart = (data) => {
  if (!focusChartRef.value) return

  focusChart = echarts.init(focusChartRef.value)

  const focusData = data?.focusDistribution || [
    { range: '90-100分', count: 15 },
    { range: '80-89分', count: 28 },
    { range: '70-79分', count: 22 },
    { range: '60-69分', count: 18 },
    { range: '60分以下', count: 8 }
  ]

  const colors = ['#1dd1a1', '#55efc4', '#feca57', '#ff9f43', '#ff6b6b']

  focusChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2,
      formatter: '{b}: {c}人'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: focusData.map(item => item.range),
      axisLine: { lineStyle: { color: '#f0e6d3' } },
      axisLabel: { color: '#a0937d', fontWeight: 600 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0e6d3', type: 'dashed' } },
      axisLabel: { color: '#a0937d' }
    },
    series: [{
      type: 'bar',
      data: focusData.map((item, index) => ({
        value: item.count,
        itemStyle: { color: colors[index], borderRadius: [8, 8, 0, 0] }
      })),
      barWidth: '50%',
      label: {
        show: true,
        position: 'top',
        color: '#6d4c41',
        fontWeight: 600
      }
    }]
  })
}

const initAbilityChart = (data) => {
  if (!abilityChartRef.value) return

  abilityChart = echarts.init(abilityChartRef.value)

  const abilityData = data?.abilityDistribution || [
    { name: '词汇量', avg: 75, high: 35, medium: 40, low: 25 },
    { name: '阅读速度', avg: 68, high: 28, medium: 45, low: 27 },
    { name: '理解能力', avg: 72, high: 32, medium: 42, low: 26 },
    { name: '记忆能力', avg: 70, high: 30, medium: 44, low: 26 },
    { name: '专注度', avg: 78, high: 38, medium: 38, low: 24 },
    { name: '阅读习惯', avg: 65, high: 25, medium: 42, low: 33 }
  ]

  abilityChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2
    },
    legend: {
      bottom: 0,
      data: ['优秀(≥80)', '中等(60-79)', '待提升(<60)'],
      textStyle: { color: '#6d4c41', fontWeight: 600 }
    },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: abilityData.map(item => item.name),
      axisLine: { lineStyle: { color: '#f0e6d3' } },
      axisLabel: { color: '#a0937d', fontWeight: 600 }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0e6d3', type: 'dashed' } },
      axisLabel: { color: '#a0937d' }
    },
    series: [
      {
        name: '优秀(≥80)',
        type: 'bar',
        stack: 'total',
        data: abilityData.map(item => item.high),
        itemStyle: { color: '#1dd1a1', borderRadius: [0, 0, 0, 0] }
      },
      {
        name: '中等(60-79)',
        type: 'bar',
        stack: 'total',
        data: abilityData.map(item => item.medium),
        itemStyle: { color: '#feca57' }
      },
      {
        name: '待提升(<60)',
        type: 'bar',
        stack: 'total',
        data: abilityData.map(item => item.low),
        itemStyle: { color: '#ff6b6b', borderRadius: [8, 8, 0, 0] }
      }
    ]
  })
}

const handleResize = () => {
  typeChart?.resize()
  focusChart?.resize()
  abilityChart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await loadStatistics()
    initTypeChart(data)
    initFocusChart(data)
    initAbilityChart(data)
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
}

.chart-card.full-width {
  grid-column: span 2;
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

/* 排行榜 */
.ranking-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 3px solid #f0e6d3;
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

  .chart-card.full-width {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
