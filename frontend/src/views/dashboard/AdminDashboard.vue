<template>
  <div class="admin-dashboard storybook-dashboard" v-loading="loading">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <div class="banner-text">
          <h1>欢迎回来，管理员</h1>
          <p>今天是美好的一天，让我们一起守护孩子们的阅读世界</p>
        </div>
        <div class="banner-deco">
          <span class="deco-emoji">🌈</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card users-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.totalUsers }}</div>
          <div class="stat-label">用户总数</div>
        </div>
        <div class="stat-decoration">✨</div>
      </div>

      <div class="stat-card books-card">
        <div class="stat-icon">📚</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.totalBooks }}</div>
          <div class="stat-label">绘本总数</div>
        </div>
        <div class="stat-decoration">📖</div>
      </div>

      <div class="stat-card logs-card">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.totalReadingLogs }}</div>
          <div class="stat-label">阅读日志</div>
        </div>
        <div class="stat-decoration">📊</div>
      </div>

      <div class="stat-card children-card">
        <div class="stat-icon">👶</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.totalChildren }}</div>
          <div class="stat-label">儿童总数</div>
        </div>
        <div class="stat-decoration">🚀</div>
      </div>
    </div>

    <!-- 今日/本周统计 -->
    <div class="quick-stats">
      <div class="quick-stat-card today-read">
        <div class="quick-stat-icon">📅</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ todayReadCount }}</div>
          <div class="quick-stat-label">今日阅读</div>
        </div>
      </div>
      <div class="quick-stat-card today-duration">
        <div class="quick-stat-icon">⏱️</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ Math.round(todayDuration / 60) }}</div>
          <div class="quick-stat-label">今日时长(分钟)</div>
        </div>
      </div>
      <div class="quick-stat-card week-read">
        <div class="quick-stat-icon">📆</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ weekReadCount }}</div>
          <div class="quick-stat-label">本周阅读</div>
        </div>
      </div>
      <div class="quick-stat-card active-user">
        <div class="quick-stat-icon">🟢</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ activeUsersToday }}</div>
          <div class="quick-stat-label">今日活跃儿童</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card large">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">📈</span>
            <span class="header-title">阅读趋势</span>
          </div>
          <div class="header-right">
            <button
              :class="['time-btn', { active: timeRange === '7' }]"
              @click="changeTimeRange('7')"
            >近7天</button>
            <button
              :class="['time-btn', { active: timeRange === '30' }]"
              @click="changeTimeRange('30')"
            >近30天</button>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card small">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">🥧</span>
            <span class="header-title">绘本分类分布</span>
          </div>
        </div>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="charts-section">
      <div class="chart-card">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">📊</span>
            <span class="header-title">年龄段绘本分布</span>
          </div>
        </div>
        <div ref="ageChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">📝</span>
            <span class="header-title">阅读时段分布</span>
          </div>
        </div>
        <div ref="hoursChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div class="actions-header">
        <span class="header-emoji">🎯</span>
        <span>快捷操作</span>
      </div>
      <div class="actions-grid">
        <div class="action-btn" @click="$router.push('/app/users')">
          <span class="action-emoji">👤</span>
          <span class="action-text">用户管理</span>
        </div>
        <div class="action-btn" @click="$router.push('/app/books')">
          <span class="action-emoji">📚</span>
          <span class="action-text">绘本管理</span>
        </div>
        <div class="action-btn" @click="$router.push('/app/analysis')">
          <span class="action-emoji">📊</span>
          <span class="action-text">数据分析</span>
        </div>
        <div class="action-btn" @click="$router.push('/app/class')">
          <span class="action-emoji">🏫</span>
          <span class="action-text">班级管理</span>
        </div>
      </div>
    </div>

    <!-- 最近操作日志 -->
    <div class="operation-logs">
      <div class="card-header">
        <div class="header-left">
          <span class="header-emoji">📋</span>
          <span class="header-title">最近操作日志</span>
        </div>
      </div>
      <el-table :data="operationLogs" stripe style="width: 100%">
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="operation" label="操作内容" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  getOverviewStats,
  getReadingTrend,
  getBookCategoryStats,
  getAgeDistribution,
  getReadingHoursDistribution,
  getTodayStats,
  getWeekStats,
  getActiveUsersToday,
  getOperationLogs
} from '@/api/stats'

const loading = ref(false)
const timeRange = ref('7')

const statistics = ref({
  totalUsers: 0,
  totalBooks: 0,
  totalReadingLogs: 0,
  totalChildren: 0
})

const animatedStats = reactive({
  totalUsers: 0,
  totalBooks: 0,
  totalReadingLogs: 0,
  totalChildren: 0
})

// 今日/本周统计
const todayReadCount = ref(0)
const todayDuration = ref(0)
const weekReadCount = ref(0)
const activeUsersToday = ref(0)
const operationLogs = ref([])

const trendChartRef = ref()
const categoryChartRef = ref()
const ageChartRef = ref()
const hoursChartRef = ref()

let trendChart = null
let categoryChart = null
let ageChart = null
let hoursChart = null

// 数字动画
const animateNumbers = () => {
  const duration = 1500
  const steps = 60
  const interval = duration / steps

  Object.keys(statistics.value).forEach(key => {
    const target = statistics.value[key]
    if (target === 0) return

    const increment = target / steps
    let step = 0

    const timer = setInterval(() => {
      step++
      animatedStats[key] = Math.min(Math.round(increment * step), target)

      if (step >= steps) {
        clearInterval(timer)
        animatedStats[key] = target
      }
    }, interval)
  })
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getOverviewStats()
    if (res.code === 200 && res.data) {
      statistics.value = {
        totalUsers: res.data.totalUsers || 0,
        totalBooks: res.data.totalBooks || 0,
        totalReadingLogs: res.data.totalReadingLogs || 0,
        totalChildren: res.data.totalChildren || 0
      }
      animateNumbers()
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载今日统计
const loadTodayStats = async () => {
  try {
    const res = await getTodayStats()
    if (res.code === 200 && res.data) {
      todayReadCount.value = res.data.readCount || 0
      todayDuration.value = res.data.duration || 0
    }
  } catch (error) {
    console.error('加载今日统计失败:', error)
  }
}

// 加载本周统计
const loadWeekStats = async () => {
  try {
    const res = await getWeekStats()
    if (res.code === 200 && res.data) {
      weekReadCount.value = res.data.readCount || 0
    }
  } catch (error) {
    console.error('加载本周统计失败:', error)
  }
}

// 加载今日活跃用户
const loadActiveUsers = async () => {
  try {
    const res = await getActiveUsersToday()
    if (res.code === 200 && res.data) {
      activeUsersToday.value = res.data.count || 0
    }
  } catch (error) {
    console.error('加载活跃用户失败:', error)
  }
}

// 加载操作日志
const loadOperationLogs = async () => {
  try {
    const res = await getOperationLogs(1, 10)
    if (res.code === 200 && res.data) {
      operationLogs.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载操作日志失败:', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 19)
  }
  return time
}

// 加载阅读趋势
const loadTrendChart = async () => {
  try {
    const res = await getReadingTrend(parseInt(timeRange.value))
    if (res.code === 200 && res.data) {
      updateTrendChart(res.data)
    }
  } catch (error) {
    console.error('加载阅读趋势失败:', error)
  }
}

// 加载分类统计
const loadCategoryChart = async () => {
  try {
    const res = await getBookCategoryStats()
    if (res.code === 200 && res.data) {
      updateCategoryChart(res.data)
    }
  } catch (error) {
    console.error('加载分类统计失败:', error)
  }
}

// 加载年龄段分布
const loadAgeChart = async () => {
  try {
    const res = await getAgeDistribution()
    if (res.code === 200 && res.data) {
      updateAgeChart(res.data)
    }
  } catch (error) {
    console.error('加载年龄段分布失败:', error)
  }
}

// 加载阅读时段分布
const loadHoursChart = async () => {
  try {
    const res = await getReadingHoursDistribution()
    if (res.code === 200 && res.data) {
      updateHoursChart(res.data)
    }
  } catch (error) {
    console.error('加载阅读时段分布失败:', error)
  }
}

// 更新趋势图
const updateTrendChart = (data) => {
  if (!trendChartRef.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const dates = data.map(item => item.date)
  const counts = data.map(item => item.count || 0)
  const durations = data.map(item => item.duration || 0)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2,
      textStyle: { color: '#2d3436' }
    },
    legend: {
      data: ['阅读次数', '阅读时长(分钟)'],
      bottom: 0
    },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#f0e6d3' } },
      axisLabel: { color: '#a0937d', fontWeight: 600 }
    },
    yAxis: [
      {
        type: 'value',
        name: '次数',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#f0e6d3', type: 'dashed' } },
        axisLabel: { color: '#a0937d' }
      },
      {
        type: 'value',
        name: '分钟',
        axisLine: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#a0937d' }
      }
    ],
    series: [
      {
        name: '阅读次数',
        type: 'line',
        smooth: true,
        data: counts,
        lineStyle: { width: 4, color: '#ff6b6b' },
        itemStyle: { color: '#ff6b6b', borderWidth: 3, borderColor: '#fff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 107, 107, 0.3)' },
            { offset: 1, color: 'rgba(255, 107, 107, 0.05)' }
          ])
        }
      },
      {
        name: '阅读时长(分钟)',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: durations,
        lineStyle: { width: 3, color: '#48dbfb' },
        itemStyle: { color: '#48dbfb' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(72, 219, 251, 0.3)' },
            { offset: 1, color: 'rgba(72, 219, 251, 0.05)' }
          ])
        }
      }
    ]
  })
}

// 更新分类图
const updateCategoryChart = (data) => {
  if (!categoryChartRef.value) return

  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  const colors = ['#ff6b6b', '#feca57', '#48dbfb', '#1dd1a1', '#ff9ff3', '#54a0ff']
  const pieData = data.map((item, index) => ({
    value: item.count || 0,
    name: item.category || '未分类',
    itemStyle: { color: colors[index % colors.length] }
  }))

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2,
      formatter: '{b}: {c}本 ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        color: '#6d4c41',
        fontWeight: 600
      },
      data: pieData
    }]
  })
}

// 更新年龄段图
const updateAgeChart = (data) => {
  if (!ageChartRef.value) return

  if (!ageChart) {
    ageChart = echarts.init(ageChartRef.value)
  }

  const colors = ['#ff9f43', '#feca57', '#ff6b6b', '#ff9ff3']
  const barData = data.map((item, index) => ({
    value: item.value || 0,
    itemStyle: { color: colors[index % colors.length], borderRadius: [8, 8, 0, 0] }
  }))

  ageChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name),
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
      data: barData,
      barWidth: '50%'
    }]
  })
}

// 更新阅读时段图
const updateHoursChart = (data) => {
  if (!hoursChartRef.value) return

  if (!hoursChart) {
    hoursChart = echarts.init(hoursChartRef.value)
  }

  const hours = data.map(item => `${item.hour}:00`)
  const counts = data.map(item => item.count || 0)

  hoursChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#f0e6d3' } },
      axisLabel: { color: '#a0937d', rotate: 45, interval: 2 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0e6d3', type: 'dashed' } },
      axisLabel: { color: '#a0937d' }
    },
    series: [{
      data: counts,
      type: 'line',
      smooth: true,
      lineStyle: { width: 3, color: '#48dbfb' },
      itemStyle: { color: '#48dbfb' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(72, 219, 251, 0.3)' },
          { offset: 1, color: 'rgba(72, 219, 251, 0.05)' }
        ])
      }
    }]
  })
}

const changeTimeRange = (range) => {
  timeRange.value = range
  loadTrendChart()
}

const handleResize = () => {
  trendChart?.resize()
  categoryChart?.resize()
  ageChart?.resize()
  hoursChart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadStatistics(),
      loadTrendChart(),
      loadCategoryChart(),
      loadAgeChart(),
      loadHoursChart(),
      loadTodayStats(),
      loadWeekStats(),
      loadActiveUsers(),
      loadOperationLogs()
    ])
  } finally {
    loading.value = false
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  categoryChart?.dispose()
  ageChart?.dispose()
  hoursChart?.dispose()
})
</script>

<style scoped>


.storybook-dashboard {
  font-family: 'Nunito', sans-serif;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 20px;
  padding: 24px 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(253, 203, 110, 0.3);
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.banner-text h1 {
  font-size: 26px;
  font-weight: 800;
  color: #2d3436;
  margin-bottom: 8px;
}

.banner-text p {
  font-size: 15px;
  color: #6d4c41;
  font-weight: 600;
}

.deco-emoji {
  font-size: 64px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-10px) rotate(5deg); }
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 3px solid transparent;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.users-card { border-color: #ff6b6b; }
.books-card { border-color: #feca57; }
.logs-card { border-color: #48dbfb; }
.children-card { border-color: #1dd1a1; }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.users-card .stat-icon { background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%); }
.books-card .stat-icon { background: linear-gradient(135deg, #ffeaa7 0%, #feca57 100%); }
.logs-card .stat-icon { background: linear-gradient(135deg, #74b9ff 0%, #48dbfb 100%); }
.children-card .stat-icon { background: linear-gradient(135deg, #55efc4 0%, #1dd1a1 100%); }

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #2d3436;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #a0937d;
  font-weight: 600;
  margin-top: 4px;
}

.stat-decoration {
  position: absolute;
  right: 16px;
  bottom: 16px;
  font-size: 32px;
  opacity: 0.2;
}

/* 快速统计 */
.quick-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.quick-stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 2px solid #f0e6d3;
  transition: all 0.3s ease;
}

.quick-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.quick-stat-icon {
  font-size: 28px;
}

.quick-stat-info {
  flex: 1;
}

.quick-stat-value {
  font-size: 24px;
  font-weight: 800;
  color: #2d3436;
}

.quick-stat-label {
  font-size: 12px;
  color: #a0937d;
  font-weight: 600;
}

.today-read { border-left: 4px solid #ff6b6b; }
.today-duration { border-left: 4px solid #feca57; }
.week-read { border-left: 4px solid #48dbfb; }
.active-user { border-left: 4px solid #1dd1a1; }

/* 操作日志 */
.operation-logs {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 3px solid #f0e6d3;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  border: 3px solid #f0e6d3;
}

.chart-card.large {
  grid-column: span 1;
}

.chart-card.small {
  grid-column: span 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px dashed #f0e6d3;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-emoji {
  font-size: 24px;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.header-right {
  display: flex;
  gap: 8px;
}

.time-btn {
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

.time-btn.active {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  color: #fff;
}

.time-btn:hover {
  transform: scale(1.05);
}

.chart-container {
  width: 100%;
  height: 280px;
}

/* 快捷操作 */
.quick-actions {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 3px solid #f0e6d3;
}

.actions-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(253, 203, 110, 0.4);
}

.action-emoji {
  font-size: 36px;
}

.action-text {
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }

  .welcome-banner {
    padding: 20px;
  }

  .banner-text h1 {
    font-size: 20px;
  }
}
</style>
