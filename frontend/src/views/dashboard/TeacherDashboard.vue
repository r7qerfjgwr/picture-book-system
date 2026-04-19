<template>
  <div class="teacher-dashboard storybook-dashboard" v-loading="loading">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner teacher-banner">
      <div class="banner-content">
        <div class="banner-text">
          <h1>欢迎回来，老师</h1>
          <p>您的班级孩子们今天又进步了呢！让我们一起查看他们的阅读成长</p>
        </div>
        <div class="banner-actions">
          <div class="class-selector">
            <span class="selector-icon">🏫</span>
            <el-select v-model="selectedClassId" placeholder="选择班级" @change="loadData" class="class-select" clearable>
              <el-option label="全部班级" :value="null">
                <span class="option-content">
                  <span class="option-icon">📚</span>
                  <span>全部班级</span>
                </span>
              </el-option>
              <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id">
                <span class="option-content">
                  <span class="option-icon">🎒</span>
                  <span>{{ cls.className }}</span>
                </span>
              </el-option>
            </el-select>
          </div>
          <div class="banner-deco">
            <span class="deco-emoji">📚</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card children-card">
        <div class="stat-icon">👦👧</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.childCount }}</div>
          <div class="stat-label">班级儿童</div>
        </div>
      </div>

      <div class="stat-card time-card">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <div class="stat-value">{{ Math.round(animatedStats.totalDuration / 60) }}</div>
          <div class="stat-label">总阅读时长(小时)</div>
        </div>
      </div>

      <div class="stat-card avg-card">
        <div class="stat-icon">📖</div>
        <div class="stat-content">
          <div class="stat-value">{{ animatedStats.readingCount }}</div>
          <div class="stat-label">阅读记录数</div>
        </div>
      </div>

      <div class="stat-card active-card">
        <div class="stat-icon">🌟</div>
        <div class="stat-content">
          <div class="stat-value">{{ childRanking.length }}</div>
          <div class="stat-label">有阅读记录</div>
        </div>
      </div>
    </div>

    <!-- 今日/本周统计 -->
    <div class="quick-stats">
      <div class="quick-stat-card today-card">
        <div class="quick-stat-icon">📅</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ todayStats.readCount }}</div>
          <div class="quick-stat-label">今日阅读</div>
        </div>
        <div class="quick-stat-detail">{{ Math.round(todayStats.duration / 60) }}分钟</div>
      </div>
      <div class="quick-stat-card week-card">
        <div class="quick-stat-icon">📆</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ weekStats.readCount }}</div>
          <div class="quick-stat-label">本周阅读</div>
        </div>
        <div class="quick-stat-detail">{{ Math.round(weekStats.duration / 60) }}分钟</div>
      </div>
      <div class="quick-stat-card avg-card">
        <div class="quick-stat-icon">🎯</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ avgFocusScore.toFixed(0) }}</div>
          <div class="quick-stat-label">平均专注度</div>
        </div>
        <div class="quick-stat-detail">满分100</div>
      </div>
      <div class="quick-stat-card type-card">
        <div class="quick-stat-icon">📊</div>
        <div class="quick-stat-info">
          <div class="quick-stat-value">{{ readerTypeStats.focus || 0 }}</div>
          <div class="quick-stat-label">专注型读者</div>
        </div>
        <div class="quick-stat-detail">{{ readerTypeStats.jump || 0 }}跳跃型</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card large">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">📊</span>
            <span class="header-title">能力分布</span>
          </div>
        </div>
        <div ref="abilityChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card small">
        <div class="card-header">
          <div class="header-left">
            <span class="header-emoji">🏆</span>
            <span class="header-title">阅读排行</span>
          </div>
        </div>
        <div ref="rankingChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 活跃儿童排行 -->
    <div class="ranking-section">
      <div class="card-header">
        <div class="header-left">
          <span class="header-emoji">🏅</span>
          <span class="header-title">班级儿童阅读排行</span>
          <span class="ranking-count">共 {{ childRanking.length }} 人</span>
        </div>
      </div>

      <div class="ranking-list" v-if="childRanking.length > 0">
        <div
          v-for="(child, index) in childRanking"
          :key="child.id"
          class="ranking-item"
          :class="`rank-${index + 1}`"
        >
          <div class="rank-badge">
            <span v-if="index < 3" class="medal">{{ ['🥇', '🥈', '🥉'][index] }}</span>
            <span v-else class="rank-num">{{ index + 1 }}</span>
          </div>
          <div class="child-avatar">{{ child.name?.charAt(0) || '?' }}</div>
          <div class="child-info">
            <div class="child-name">{{ child.name }}</div>
            <div class="child-stats">
              <span>📚 {{ child.readCount }}次阅读</span>
            </div>
          </div>
          <div class="focus-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: Math.min(child.readCount * 5, 100) + '%' }"></div>
            </div>
            <span class="progress-value">{{ child.readCount }}次</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无阅读数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getTeacherDashboard } from '@/api/stats'
import { getMyClasses } from '@/api/classInfo'

const userStore = useUserStore()
const loading = ref(false)
const classList = ref([])
const selectedClassId = ref(null)

const statistics = ref({
  childCount: 0,
  readingCount: 0,
  totalDuration: 0
})

const animatedStats = reactive({
  childCount: 0,
  readingCount: 0,
  totalDuration: 0
})

const childRanking = ref([])
const abilityDistribution = ref([])

// 今日和本周统计
const todayStats = ref({ readCount: 0, duration: 0 })
const weekStats = ref({ readCount: 0, duration: 0 })
const avgFocusScore = ref(0)
const readerTypeStats = ref({ focus: 0, jump: 0, interest: 0 })

const abilityChartRef = ref()
const rankingChartRef = ref()

let abilityChart = null
let rankingChart = null

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

// 加载班级列表
const loadClassList = async () => {
  try {
    const res = await getMyClasses()
    if (res.code === 200 && res.data) {
      classList.value = res.data || []
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  try {
    // 如果选择了班级，传入 classId；否则后端会返回所有班级数据
    const res = await getTeacherDashboard(selectedClassId.value)
    if (res.code === 200 && res.data) {
      statistics.value = {
        childCount: res.data.childCount || 0,
        readingCount: res.data.readingCount || 0,
        totalDuration: res.data.totalDuration || 0
      }
      childRanking.value = res.data.childRanking || []
      abilityDistribution.value = res.data.abilityDistribution || []

      // 新增统计数据
      todayStats.value = res.data.todayStats || { readCount: 0, duration: 0 }
      weekStats.value = res.data.weekStats || { readCount: 0, duration: 0 }
      avgFocusScore.value = res.data.avgFocusScore || 0
      readerTypeStats.value = res.data.readerTypeStats || { focus: 0, jump: 0, interest: 0 }

      animateNumbers()
      updateAbilityChart()
      updateRankingChart()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 更新能力分布图
const updateAbilityChart = () => {
  if (!abilityChartRef.value) return

  if (!abilityChart) {
    abilityChart = echarts.init(abilityChartRef.value)
  }

  if (abilityDistribution.value.length === 0) {
    abilityChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999', fontSize: 14 }
      }
    })
    return
  }

  abilityChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#74b9ff',
      borderWidth: 2,
      formatter: '{b}: {c}人'
    },
    legend: {
      bottom: 0,
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
      data: abilityDistribution.value.map(item => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: item.color }
      }))
    }]
  })
}

// 更新排行图
const updateRankingChart = () => {
  if (!rankingChartRef.value) return

  if (!rankingChart) {
    rankingChart = echarts.init(rankingChartRef.value)
  }

  if (childRanking.value.length === 0) {
    rankingChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999', fontSize: 14 }
      }
    })
    return
  }

  const top5 = childRanking.value.slice(0, 5).reverse()
  const names = top5.map(item => item.name)
  const counts = top5.map(item => item.readCount)

  rankingChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#74b9ff',
      borderWidth: 2
    },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', show: false },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#6d4c41', fontWeight: 600, fontSize: 12 }
    },
    series: [{
      type: 'bar',
      data: counts,
      barWidth: 16,
      itemStyle: {
        borderRadius: [0, 8, 8, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#74b9ff' },
          { offset: 1, color: '#48dbfb' }
        ])
      }
    }]
  })
}

const handleResize = () => {
  abilityChart?.resize()
  rankingChart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    await loadClassList()
    await loadData()
  } finally {
    loading.value = false
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  abilityChart?.dispose()
  rankingChart?.dispose()
})
</script>

<style scoped>


.storybook-dashboard {
  font-family: 'Nunito', sans-serif;
}

/* 欢迎横幅 */
.welcome-banner {
  border-radius: 20px;
  padding: 24px 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(116, 185, 255, 0.2);
}

.teacher-banner {
  background: linear-gradient(135deg, #74b9ff 0%, #48dbfb 100%);
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

.banner-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.class-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 8px 16px;
  border-radius: 14px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.selector-icon {
  font-size: 22px;
}

.class-select {
  width: 160px;
}

.class-select :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none;
  padding: 0;
}

.class-select :deep(.el-input__inner) {
  font-weight: 600;
  color: #2d3436;
}

.class-select :deep(.el-select__caret) {
  color: #48dbfb;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-icon {
  font-size: 16px;
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

.children-card { border-color: #48dbfb; }
.time-card { border-color: #ff9f43; }
.avg-card { border-color: #1dd1a1; }
.active-card { border-color: #ff6b6b; }

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

.stat-label {
  font-size: 14px;
  color: #a0937d;
  font-weight: 600;
  margin-top: 4px;
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

.quick-stat-detail {
  font-size: 12px;
  color: #48dbfb;
  font-weight: 700;
  background: rgba(72, 219, 251, 0.1);
  padding: 4px 10px;
  border-radius: 8px;
}

.today-card { border-left: 4px solid #ff6b6b; }
.week-card { border-left: 4px solid #feca57; }
.avg-card { border-left: 4px solid #1dd1a1; }
.type-card { border-left: 4px solid #74b9ff; }

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

/* 排行榜 */
.ranking-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  border: 3px solid #f0e6d3;
}

.ranking-count {
  font-size: 13px;
  color: #a0937d;
  font-weight: 500;
  margin-left: auto;
  background: #fef9f0;
  padding: 4px 12px;
  border-radius: 10px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.ranking-list::-webkit-scrollbar {
  width: 6px;
}

.ranking-list::-webkit-scrollbar-track {
  background: #f0e6d3;
  border-radius: 3px;
}

.ranking-list::-webkit-scrollbar-thumb {
  background: #ffeaa7;
  border-radius: 3px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff9f0 0%, #fff 100%);
  border-radius: 16px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.ranking-item:hover {
  border-color: #ffeaa7;
  transform: translateX(4px);
}

.ranking-item.rank-1 {
  background: linear-gradient(135deg, #ffeaa7 0%, #fff 100%);
  border-color: #ffd93d;
}

.rank-badge {
  width: 36px;
  text-align: center;
}

.medal {
  font-size: 28px;
}

.rank-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #f0e6d3;
  border-radius: 8px;
  font-weight: 700;
  color: #a0937d;
}

.child-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #74b9ff 0%, #48dbfb 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 800;
  color: #fff;
}

.child-info {
  flex: 1;
}

.child-name {
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.child-stats {
  display: flex;
  gap: 16px;
  margin-top: 4px;
  font-size: 13px;
  color: #a0937d;
}

.focus-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 150px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #f0e6d3;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #1dd1a1 0%, #55efc4 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-value {
  font-size: 13px;
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
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }

  .ranking-item {
    flex-wrap: wrap;
  }

  .focus-progress {
    width: 100%;
    order: 3;
    margin-top: 8px;
  }
}
</style>
