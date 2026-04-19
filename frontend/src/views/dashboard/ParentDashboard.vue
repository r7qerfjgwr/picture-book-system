<template>
  <div class="parent-dashboard storybook-dashboard" v-loading="loading">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner parent-banner">
      <div class="banner-content">
        <div class="banner-text">
          <h1>欢迎回来，家长</h1>
          <p>看着孩子一天天进步，是最幸福的事！让我们看看宝贝今天的阅读表现吧</p>
        </div>
        <div class="banner-deco">
          <span class="deco-emoji">🌈</span>
        </div>
      </div>
    </div>

    <!-- 核心数据卡片 -->
    <div class="stats-grid">
      <div class="stat-card books-card">
        <div class="stat-icon">📚</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.weekReadingCount }}</div>
          <div class="stat-label">本周阅读次数</div>
        </div>
      </div>

      <div class="stat-card time-card">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <div class="stat-value">{{ Math.round(animatedStats.totalDuration / 60) }}</div>
          <div class="stat-label">阅读总时长(小时)</div>
        </div>
      </div>

      <div class="stat-card focus-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.focusScore || 0 }}</div>
          <div class="stat-label">专注度评分</div>
        </div>
        <div class="progress-ring" v-if="dashboardData.focusScore">
          <svg viewBox="0 0 36 36">
            <path class="ring-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"/>
            <path class="ring-fill" :stroke-dasharray="`${dashboardData.focusScore || 0}, 100`" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"/>
          </svg>
        </div>
      </div>

      <div class="stat-card type-card">
        <div class="stat-icon">✨</div>
        <div class="stat-content">
          <div class="stat-value type-text">{{ dashboardData.readerType || '-' }}</div>
          <div class="stat-label">阅读类型</div>
        </div>
        <div class="type-icon">{{ getTypeEmoji(dashboardData.readerType) }}</div>
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
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card small">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">🥧</span>
            <span class="header-title">绘本类别偏好</span>
          </div>
        </div>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="charts-section">
      <div class="chart-card">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">🌟</span>
            <span class="header-title">成长维度雷达图</span>
          </div>
        </div>
        <div ref="growthChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">📚</span>
            <span class="header-title">推荐绘本</span>
          </div>
          <button class="refresh-btn" @click="refreshRecommendations">
            换一批
          </button>
        </div>
        <div class="recommendation-list" v-if="recommendations.length > 0">
          <div v-for="book in recommendations.slice(0, 4)" :key="book.id" class="book-card">
            <div class="book-cover">
              <span class="book-emoji">📖</span>
            </div>
            <div class="book-info">
              <div class="book-title">{{ book.title }}</div>
              <div class="book-author">{{ book.author }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无推荐" :image-size="60" />
      </div>
    </div>

    <!-- 今日小贴士 -->
    <div class="tips-section">
      <div class="card-header">
        <div class="header-left">
          <span class="header-emoji">💡</span>
          <span class="header-title">今日育儿小贴士</span>
        </div>
      </div>
      <div class="tips-content">
        <div class="tip-card">
          <span class="tip-icon">🌙</span>
          <div class="tip-text">
            <strong>睡前阅读</strong>
            <p>每天睡前15-20分钟的亲子阅读，有助于培养孩子的阅读习惯和安全感</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getMyChildren } from '@/api/child'
import { getParentDashboard } from '@/api/stats'
import { getRecommendations } from '@/api/recommendation'

const loading = ref(false)
const currentChildId = ref(null)

const dashboardData = ref({
  weekReadingCount: 0,
  totalDuration: 0,
  focusScore: 0,
  readerType: '-'
})

const animatedStats = reactive({
  weekReadingCount: 0,
  totalDuration: 0,
  focusScore: 0
})

const recommendations = ref([])

const trendChartRef = ref()
const categoryChartRef = ref()
const growthChartRef = ref()

let trendChart = null
let categoryChart = null
let growthChart = null

const getTypeEmoji = (type) => {
  const emojis = { '专注型': '🦊', '跳跃型': '🐰', '兴趣导向型': '🦋' }
  return emojis[type] || '🌟'
}

// 数字动画
const animateNumbers = () => {
  const duration = 1500
  const steps = 60
  const interval = duration / steps

  const keys = ['weekReadingCount', 'totalDuration', 'focusScore']
  keys.forEach(key => {
    const target = dashboardData.value[key] || 0
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

// 加载数据
const loadData = async () => {
  try {
    const childrenRes = await getMyChildren().catch(() => ({ data: [] }))
    if (!childrenRes.data || childrenRes.data.length === 0) {
      ElMessage.warning('未绑定儿童信息')
      return
    }

    currentChildId.value = childrenRes.data[0].id

    // 获取仪表盘数据 - 添加错误处理
    try {
      const res = await getParentDashboard(currentChildId.value)
      if (res.code === 200 && res.data) {
        dashboardData.value = {
          weekReadingCount: res.data.weekReadingCount || 0,
          totalDuration: res.data.totalDuration || 0,
          focusScore: res.data.readingAbilityScore || res.data.focusScore || 0,
          readerType: res.data.readerType || '-'
        }

        animateNumbers()
        updateTrendChart(res.data.recentTrend || [])
        updateCategoryChart(res.data.categoryPreference || [])
      }
    } catch (e) {
      console.error('获取仪表盘数据失败:', e)
      // 继续执行，不要完全失败
    }

    // 获取推荐
    try {
      const recRes = await getRecommendations(currentChildId.value).catch(() => ({ data: [] }))
      recommendations.value = recRes.data || []
    } catch (e) {
      console.error('获取推荐失败:', e)
    }

  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 更新趋势图
const updateTrendChart = (data) => {
  if (!trendChartRef.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  if (data.length === 0) {
    trendChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }

  const dates = data.map(item => item.date)
  const counts = data.map(item => item.count || 0)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#1dd1a1',
      borderWidth: 2
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#f0e6d3' } },
      axisLabel: { color: '#a0937d', fontWeight: 600 }
    },
    yAxis: {
      type: 'value',
      name: '次数',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0e6d3', type: 'dashed' } },
      axisLabel: { color: '#a0937d' }
    },
    series: [{
      data: counts,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      lineStyle: { width: 4, color: '#1dd1a1' },
      itemStyle: { color: '#1dd1a1', borderWidth: 3, borderColor: '#fff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(29, 209, 161, 0.3)' },
          { offset: 1, color: 'rgba(29, 209, 161, 0.05)' }
        ])
      }
    }]
  })
}

// 更新分类图
const updateCategoryChart = (data) => {
  if (!categoryChartRef.value) return

  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  if (data.length === 0) {
    categoryChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }

  const colors = ['#ff9f43', '#48dbfb', '#ff6b6b', '#1dd1a1', '#ff9ff3']
  const pieData = data.map((item, index) => ({
    value: item.count || item.total_duration || 0,
    name: item.category || '未分类',
    itemStyle: { color: colors[index % colors.length] }
  }))

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ffeaa7',
      borderWidth: 2,
      formatter: '{b}: {c}次'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
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
        fontWeight: 600,
        fontSize: 11
      },
      data: pieData
    }]
  })
}

// 更新成长雷达图
const updateGrowthChart = () => {
  if (!growthChartRef.value) return

  if (!growthChart) {
    growthChart = echarts.init(growthChartRef.value)
  }

  const focusScore = dashboardData.value.focusScore || 0

  growthChart.setOption({
    tooltip: {},
    radar: {
      indicator: [
        { name: '词汇量', max: 100 },
        { name: '逻辑理解', max: 100 },
        { name: '阅读量', max: 100 },
        { name: '专注度', max: 100 }
      ],
      splitArea: { areaStyle: { color: ['rgba(255, 234, 167, 0.2)', 'rgba(255, 234, 167, 0.1)'] } },
      axisLine: { lineStyle: { color: '#f0e6d3' } },
      splitLine: { lineStyle: { color: '#f0e6d3' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [
          Math.min(focusScore + 10, 100),
          Math.min(focusScore - 5, 100),
          Math.min(focusScore + 5, 100),
          focusScore
        ],
        name: '成长维度',
        areaStyle: { color: 'rgba(29, 209, 161, 0.3)' },
        lineStyle: { color: '#1dd1a1', width: 3 },
        itemStyle: { color: '#1dd1a1' }
      }]
    }]
  })
}

const refreshRecommendations = async () => {
  if (!currentChildId.value) return

  try {
    const res = await getRecommendations(currentChildId.value)
    recommendations.value = res.data || []
  } catch (error) {
    ElMessage.error('获取推荐失败')
  }
}

const handleResize = () => {
  trendChart?.resize()
  categoryChart?.resize()
  growthChart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    await loadData()
    updateGrowthChart()
  } finally {
    loading.value = false
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  categoryChart?.dispose()
  growthChart?.dispose()
})
</script>

<style scoped>
.storybook-dashboard {
  font-family: 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 欢迎横幅 */
.welcome-banner {
  border-radius: 20px;
  padding: 24px 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(29, 209, 161, 0.2);
}

.parent-banner {
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 250px;
  height: 250px;
  background: rgba(255, 255, 255, 0.15);
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
  color: #fff;
  margin-bottom: 8px;
}

.banner-text p {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
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
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 3px solid transparent;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.books-card { border-color: #ff9f43; }
.time-card { border-color: #48dbfb; }
.focus-card { border-color: #1dd1a1; }
.type-card { border-color: #ff6b6b; }

.stat-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #2d3436;
  line-height: 1;
}

.stat-value.type-text {
  font-size: 18px;
}

.stat-label {
  font-size: 14px;
  color: #a0937d;
  font-weight: 600;
  margin-top: 4px;
}

.type-icon {
  position: absolute;
  right: 16px;
  bottom: 16px;
  font-size: 40px;
  opacity: 0.3;
}

/* 进度环 */
.progress-ring {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 50px;
  height: 50px;
}

.progress-ring svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: #f0e6d3;
  stroke-width: 3;
}

.ring-fill {
  fill: none;
  stroke: #1dd1a1;
  stroke-width: 3;
  stroke-linecap: round;
  transition: stroke-dasharray 0.5s ease;
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

.chart-container {
  width: 100%;
  height: 280px;
}

.refresh-btn {
  padding: 8px 16px;
  border: none;
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  transform: translateY(-2px);
}

/* 绘本推荐 */
.recommendation-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.book-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.book-card:hover {
  transform: translateY(-4px);
}

.book-cover {
  width: 100%;
  aspect-ratio: 3/4;
  background: linear-gradient(135deg, #ffeaa7 0%, #f0e6d3 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.book-emoji {
  font-size: 32px;
}

.book-info {
  padding: 0 4px;
}

.book-title {
  font-size: 12px;
  font-weight: 700;
  color: #2d3436;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 10px;
  color: #a0937d;
  margin-top: 2px;
}

/* 小贴士 */
.tips-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 3px solid #f0e6d3;
}

.tips-content {
  display: flex;
  gap: 16px;
}

.tip-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #fff9f0;
  border-radius: 16px;
  flex: 1;
}

.tip-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.tip-text strong {
  display: block;
  font-size: 15px;
  color: #2d3436;
  margin-bottom: 8px;
}

.tip-text p {
  font-size: 13px;
  color: #6d4c41;
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .recommendation-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .recommendation-list {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
