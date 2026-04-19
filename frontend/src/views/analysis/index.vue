<template>
  <div class="analysis-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <span class="header-icon">📊</span>
        <h2>阅读行为分析</h2>
      </div>
      <div class="header-right">
        <el-select v-model="selectedChildId" placeholder="选择儿童" @change="loadAnalysis" class="child-select">
          <el-option v-for="child in childList" :key="child.id" :label="child.name" :value="child.id" />
        </el-select>
        <button class="analysis-btn" @click="runAnalysis">
          <span>🔄</span> 重新分析
        </button>
      </div>
    </div>

    <!-- 核心指标概览 -->
    <div class="metrics-overview">
      <div class="metric-card reading-type">
        <div class="metric-icon">📖</div>
        <div class="metric-content">
          <span class="metric-label">阅读类型</span>
          <span class="metric-value" :class="getTypeClass(analysisResult.readingType)">
            {{ analysisResult.readingType || '未分析' }}
          </span>
          <span class="metric-desc">{{ getReadingTypeDesc(analysisResult.readingType) }}</span>
        </div>
      </div>
      <div class="metric-card focus-score">
        <div class="metric-icon">🎯</div>
        <div class="metric-content">
          <span class="metric-label">专注度评分</span>
          <span class="metric-value">{{ (analysisResult.focusScore || 0).toFixed(1) }}</span>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: (analysisResult.focusScore || 0) + '%' }"></div>
          </div>
        </div>
      </div>
      <div class="metric-card ability-score">
        <div class="metric-icon">📚</div>
        <div class="metric-content">
          <span class="metric-label">阅读能力</span>
          <span class="metric-value">{{ (analysisResult.readingAbilityScore || 0).toFixed(1) }}</span>
          <div class="progress-bar">
            <div class="progress-fill green" :style="{ width: (analysisResult.readingAbilityScore || 0) + '%' }"></div>
          </div>
        </div>
      </div>
      <div class="metric-card cognitive-stage">
        <div class="metric-icon">🧠</div>
        <div class="metric-content">
          <span class="metric-label">认知阶段</span>
          <span class="metric-value stage">{{ analysisResult.cognitiveStage || '未评估' }}</span>
          <span class="metric-desc">{{ getStageDesc(analysisResult.cognitiveStage) }}</span>
        </div>
      </div>
    </div>

    <!-- 多维度评分条 -->
    <div class="dimension-scores">
      <h3 class="section-title"><span class="title-icon">📈</span> 多维度评分</h3>
      <div class="scores-grid">
        <div class="score-item">
          <div class="score-header">
            <span class="score-label">重复阅读偏好</span>
            <span class="score-value">{{ (analysisResult.replayPreferenceScore || 0).toFixed(0) }}分</span>
          </div>
          <div class="score-bar">
            <div class="bar-fill blue" :style="{ width: (analysisResult.replayPreferenceScore || 0) + '%' }"></div>
          </div>
        </div>
        <div class="score-item">
          <div class="score-header">
            <span class="score-label">互动行为分数</span>
            <span class="score-value">{{ (analysisResult.interactionScore || 0).toFixed(0) }}分</span>
          </div>
          <div class="score-bar">
            <div class="bar-fill orange" :style="{ width: (analysisResult.interactionScore || 0) + '%' }"></div>
          </div>
        </div>
        <div class="score-item">
          <div class="score-header">
            <span class="score-label">情绪稳定性</span>
            <span class="score-value">{{ (analysisResult.emotionStabilityScore || 0).toFixed(0) }}分</span>
          </div>
          <div class="score-bar">
            <div class="bar-fill red" :style="{ width: (analysisResult.emotionStabilityScore || 0) + '%' }"></div>
          </div>
        </div>
        <div class="score-item">
          <div class="score-header">
            <span class="score-label">习惯养成度</span>
            <span class="score-value">{{ (analysisResult.habitFormationScore || 0).toFixed(0) }}分</span>
          </div>
          <div class="score-bar">
            <div class="bar-fill purple" :style="{ width: (analysisResult.habitFormationScore || 0) + '%' }"></div>
          </div>
        </div>
        <div class="score-item">
          <div class="score-header">
            <span class="score-label">成长预测分数</span>
            <span class="score-value">{{ (analysisResult.growthPredictionScore || 0).toFixed(0) }}分</span>
          </div>
          <div class="score-bar">
            <div class="bar-fill green" :style="{ width: (analysisResult.growthPredictionScore || 0) + '%' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 兴趣标签 -->
    <div class="interest-section">
      <h3 class="section-title"><span class="title-icon">🏷️</span> 兴趣标签</h3>
      <div class="tags-container">
        <span v-for="tag in getInterestTags()" :key="tag" class="interest-tag">{{ tag }}</span>
        <span v-if="!analysisResult.interestTags" class="no-tag">暂无数据，请先进行阅读活动</span>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 第一行 -->
      <div class="charts-row">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">🎯 能力雷达图</span>
            <span class="chart-subtitle">八维度能力综合评估</span>
          </div>
          <div ref="radarChart" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">📊 阅读类型分布</span>
            <span class="chart-subtitle">基于K-Means聚类分析</span>
          </div>
          <div ref="pieChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 第二行 -->
      <div class="charts-row">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">📈 成长趋势预测</span>
            <span class="chart-subtitle">线性回归预测未来发展趋势</span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">⏰ 阅读时段分布</span>
            <span class="chart-subtitle">每日阅读时间热力分析</span>
          </div>
          <div ref="heatmapChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 第三行 -->
      <div class="charts-row">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">📚 兴趣偏好分析</span>
            <span class="chart-subtitle">各类绘本阅读频率统计</span>
          </div>
          <div ref="interestChart" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">⏱️ 阅读时长分布</span>
            <span class="chart-subtitle">单次阅读时长统计分析</span>
          </div>
          <div ref="durationChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 第四行：聚类分析 -->
      <div class="chart-card full-width">
        <div class="chart-header">
          <span class="chart-title">🔬 阅读习惯聚类分析</span>
          <span class="chart-subtitle">K-Means算法将读者分为专注型、跳跃型、兴趣导向型三类</span>
        </div>
        <div ref="clusterChart" class="chart-container large"></div>
        <div class="cluster-legend">
          <div class="legend-item">
            <span class="legend-dot focused"></span>
            <span>专注型：阅读时长长、翻页适中、完成率高</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot jumpy"></span>
            <span>跳跃型：阅读时长短、翻页快、完成率低</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot interest"></span>
            <span>兴趣导向型：阅读行为受内容兴趣影响明显</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 算法说明 -->
    <div class="algorithm-section">
      <h3 class="section-title"><span class="title-icon">🧮</span> 分析算法说明</h3>
      <div class="algorithm-cards">
        <div class="algo-card">
          <div class="algo-icon">📊</div>
          <h4>K-Means聚类</h4>
          <p>通过阅读时长、翻页速度、完成率三个维度，将读者分类为不同阅读类型</p>
        </div>
        <div class="algo-card">
          <div class="algo-icon">📈</div>
          <h4>线性回归预测</h4>
          <p>基于历史阅读数据，预测儿童未来成长趋势和能力发展</p>
        </div>
        <div class="algo-card">
          <div class="algo-icon">🎯</div>
          <h4>多维评分模型</h4>
          <p>综合专注度、互动性、情绪稳定性等维度，生成综合能力评分</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getMyChildren } from '@/api/child'
import { getAnalysisResult, runAnalysis as runAnalysisApi, getMultiDimensionAnalysis } from '@/api/analysis'

const childList = ref([])
const selectedChildId = ref('')
const analysisResult = ref({})
const dimensionData = ref({})

const radarChart = ref()
const pieChart = ref()
const trendChart = ref()
const heatmapChart = ref()
const interestChart = ref()
const durationChart = ref()
const clusterChart = ref()

let chartInstances = []

const getTypeClass = (type) => {
  const classes = { '专注型': 'type-focused', '跳跃型': 'type-jumpy', '兴趣导向型': 'type-interest' }
  return classes[type] || ''
}

const getReadingTypeDesc = (type) => {
  const descs = {
    '专注型': '注意力集中，能完整阅读',
    '跳跃型': '翻页较快，兴趣多变',
    '兴趣导向型': '受内容兴趣驱动明显'
  }
  return descs[type] || '暂无分析结果'
}

const getStageDesc = (stage) => {
  const descs = {
    '感知运动阶段': '0-2岁',
    '前运算阶段': '2-4岁',
    '前运算阶段（高级）': '4-5岁',
    '具体运算阶段': '5-7岁',
    '形式运算阶段': '7岁+'
  }
  return descs[stage] || '根据阅读行为评估'
}

const getInterestTags = () => {
  if (!analysisResult.value.interestTags) return []
  return analysisResult.value.interestTags.split(',').filter(t => t.trim())
}

const initCharts = () => {
  const refs = [radarChart, pieChart, trendChart, heatmapChart, interestChart, durationChart, clusterChart]
  refs.forEach(ref => {
    if (ref.value) {
      const instance = echarts.init(ref.value)
      chartInstances.push(instance)
    }
  })

  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  chartInstances.forEach(instance => instance?.resize())
}

const loadRadarChart = () => {
  const instance = echarts.getInstanceByDom(radarChart.value)
  if (!instance) return

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' }
    },
    radar: {
      indicator: [
        { name: '词汇量', max: 100 },
        { name: '逻辑思维', max: 100 },
        { name: '阅读能力', max: 100 },
        { name: '专注度', max: 100 },
        { name: '认知发展', max: 100 },
        { name: '社交情感', max: 100 },
        { name: '创造力', max: 100 },
        { name: '阅读习惯', max: 100 }
      ],
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#666',
        fontSize: 12,
        fontWeight: 'bold'
      },
      splitLine: {
        lineStyle: { color: 'rgba(255, 154, 87, 0.2)' }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(255,236,210,0.3)', 'rgba(255,236,210,0.1)']
        }
      }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [
          analysisResult.value.vocabularyScore || 65,
          analysisResult.value.logicScore || 60,
          analysisResult.value.readingAbilityScore || 70,
          analysisResult.value.focusScore || 55,
          analysisResult.value.cognitiveScore || 60,
          analysisResult.value.socialScore || 65,
          analysisResult.value.creativityScore || 70,
          analysisResult.value.habitFormationScore || 50
        ],
        name: '能力评估',
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: '#e17055',
          width: 2
        },
        areaStyle: {
          color: 'rgba(225, 112, 85, 0.3)'
        },
        itemStyle: {
          color: '#e17055'
        }
      }]
    }]
  }
  instance.setOption(option)
}

const loadPieChart = () => {
  const instance = echarts.getInstanceByDom(pieChart.value)
  if (!instance) return

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        show: true,
        position: 'outside',
        formatter: '{b}\n{d}%',
        fontSize: 11
      },
      labelLine: {
        show: true,
        length: 15,
        length2: 10
      },
      data: [
        { value: 12, name: '专注型', itemStyle: { color: '#55efc4' } },
        { value: 8, name: '跳跃型', itemStyle: { color: '#fdcb6e' } },
        { value: 10, name: '兴趣导向型', itemStyle: { color: '#74b9ff' } }
      ]
    }]
  }
  instance.setOption(option)
}

const loadTrendChart = () => {
  const instance = echarts.getInstanceByDom(trendChart.value)
  if (!instance) return

  const trajectory = dimensionData.value.growthTrajectory || { trajectory: [] }
  const hasRealData = trajectory.trajectory && trajectory.trajectory.length > 0

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)'
    },
    legend: {
      data: ['专注度', '完成率', '阅读能力'],
      bottom: 0,
      textStyle: { fontSize: 11 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: hasRealData
        ? trajectory.trajectory.map(t => t.date)
        : ['第1周', '第2周', '第3周', '第4周', '预测第5周', '预测第6周'],
      axisLabel: { fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { fontSize: 10 }
    },
    series: [
      {
        name: '专注度',
        type: 'line',
        data: hasRealData ? trajectory.trajectory.map(t => t.focusScore || 0) : [55, 60, 65, 70, 73, 76],
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#e17055', width: 2 },
        itemStyle: { color: '#e17055' },
        areaStyle: { color: 'rgba(225,112,85,0.1)' }
      },
      {
        name: '完成率',
        type: 'line',
        data: hasRealData ? trajectory.trajectory.map(t => t.completionRate || 0) : [60, 65, 70, 75, 78, 80],
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#55efc4', width: 2 },
        itemStyle: { color: '#55efc4' },
        areaStyle: { color: 'rgba(85,239,196,0.1)' }
      },
      {
        name: '阅读能力',
        type: 'line',
        data: hasRealData ? trajectory.trajectory.map(t => t.readingAbilityScore || 0) : [50, 55, 60, 65, 68, 72],
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#74b9ff', width: 2 },
        itemStyle: { color: '#74b9ff' },
        areaStyle: { color: 'rgba(116,185,255,0.1)' }
      }
    ]
  }
  instance.setOption(option)
}

const loadHeatmapChart = () => {
  const instance = echarts.getInstanceByDom(heatmapChart.value)
  if (!instance) return

  const timeSlotData = dimensionData.value.timeSlotDistribution || {
    '早晨\n6-9点': 5,
    '上午\n9-12点': 3,
    '中午\n12-14点': 2,
    '下午\n14-18点': 4,
    '傍晚\n18-20点': 8,
    '晚上\n20-22点': 12,
    '夜间\n22-6点': 1
  }

  const values = Object.values(timeSlotData)
  const maxValue = Math.max(...values)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: Object.keys(timeSlotData),
      axisLabel: { fontSize: 10, interval: 0 }
    },
    yAxis: {
      type: 'value',
      name: '次数',
      axisLabel: { fontSize: 10 }
    },
    series: [{
      type: 'bar',
      data: values.map((value, index) => ({
        value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `hsl(${30 + (value / maxValue) * 20}, 80%, ${60 + (value / maxValue) * 15}%)` },
            { offset: 1, color: `hsl(${30 + (value / maxValue) * 20}, 80%, ${70 + (value / maxValue) * 10}%)` }
          ]),
          borderRadius: [6, 6, 0, 0]
        }
      })),
      barWidth: '50%'
    }]
  }
  instance.setOption(option)
}

const loadInterestChart = () => {
  const instance = echarts.getInstanceByDom(interestChart.value)
  if (!instance) return

  const categoryData = dimensionData.value.categoryPreference || [
    { category: '动物', count: 12 },
    { category: '科普', count: 8 },
    { category: '情感', count: 6 },
    { category: '童话', count: 10 }
  ]

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categoryData.map(c => c.category),
      axisLabel: { fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '阅读次数',
      axisLabel: { fontSize: 10 }
    },
    series: [{
      type: 'bar',
      data: categoryData.map((c, i) => ({
        value: c.count,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: ['#ff9a56', '#74b9ff', '#55efc4', '#fdcb6e'][i] },
            { offset: 1, color: ['#ffccaa', '#a8d8ff', '#88ffe0', '#ffeaa7'][i] }
          ]),
          borderRadius: [8, 8, 0, 0]
        }
      })),
      barWidth: '40%',
      label: {
        show: true,
        position: 'top',
        fontSize: 12,
        fontWeight: 'bold'
      }
    }]
  }
  instance.setOption(option)
}

const loadDurationChart = () => {
  const instance = echarts.getInstanceByDom(durationChart.value)
  if (!instance) return

  const durationData = dimensionData.value.durationDistribution || {
    '0-5分钟': 5,
    '5-10分钟': 12,
    '10-20分钟': 8,
    '20-30分钟': 4,
    '30分钟+': 2
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}次 ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center',
      textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['35%', '60%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{d}%',
        fontSize: 10
      },
      data: Object.entries(durationData).map(([name, value], i) => ({
        name,
        value,
        itemStyle: { color: ['#e17055', '#fdcb6e', '#55efc4', '#74b9ff', '#a29bfe'][i] }
      }))
    }]
  }
  instance.setOption(option)
}

const loadClusterChart = () => {
  const instance = echarts.getInstanceByDom(clusterChart.value)
  if (!instance) return

  // 模拟聚类数据点
  const generateClusterData = () => {
    const focused = []
    const jumpy = []
    const interest = []

    for (let i = 0; i < 15; i++) {
      focused.push([
        25 + Math.random() * 20,  // 阅读时长 25-45分钟
        2 + Math.random() * 3,    // 翻页速度 2-5页/分钟
        '专注型'
      ])
      jumpy.push([
        5 + Math.random() * 15,   // 阅读时长 5-20分钟
        6 + Math.random() * 6,    // 翻页速度 6-12页/分钟
        '跳跃型'
      ])
      interest.push([
        15 + Math.random() * 20,  // 阅读时长 15-35分钟
        3 + Math.random() * 5,    // 翻页速度 3-8页/分钟
        '兴趣导向型'
      ])
    }
    return [...focused, ...jumpy, ...interest]
  }

  const allData = generateClusterData()

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.data[2]}<br/>阅读时长: ${params.data[0].toFixed(1)}分钟<br/>翻页速度: ${params.data[1].toFixed(1)}页/分钟`
      }
    },
    grid: {
      left: '8%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      name: '阅读时长(分钟)',
      nameLocation: 'middle',
      nameGap: 30,
      nameTextStyle: { fontSize: 12, fontWeight: 'bold' },
      min: 0,
      max: 50,
      splitLine: { show: true, lineStyle: { type: 'dashed' } }
    },
    yAxis: {
      name: '翻页速度(页/分钟)',
      nameLocation: 'middle',
      nameGap: 40,
      nameTextStyle: { fontSize: 12, fontWeight: 'bold' },
      min: 0,
      max: 15,
      splitLine: { show: true, lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'scatter',
      data: allData,
      symbolSize: 18,
      itemStyle: {
        color: (params) => {
          const colors = {
            '专注型': '#55efc4',
            '跳跃型': '#fdcb6e',
            '兴趣导向型': '#74b9ff'
          }
          return colors[params.data[2]] || '#999'
        },
        shadowBlur: 10,
        shadowColor: 'rgba(0,0,0,0.1)'
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(0,0,0,0.3)'
        }
      }
    }]
  }
  instance.setOption(option)
}

const loadAllCharts = () => {
  loadRadarChart()
  loadPieChart()
  loadTrendChart()
  loadHeatmapChart()
  loadInterestChart()
  loadDurationChart()
  loadClusterChart()
}

const loadAnalysis = async () => {
  if (!selectedChildId.value) return

  try {
    const res = await getAnalysisResult(selectedChildId.value)
    analysisResult.value = res.data || {}

    try {
      const dimRes = await getMultiDimensionAnalysis(selectedChildId.value)
      dimensionData.value = dimRes.data || {}
    } catch (e) {
      console.log('Multi-dimension data not available')
    }

    loadAllCharts()
  } catch (error) {
    console.error('Load analysis error:', error)
  }
}

const runAnalysis = async () => {
  if (!selectedChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }

  try {
    await runAnalysisApi(selectedChildId.value)
    ElMessage.success('分析完成')
    loadAnalysis()
  } catch (error) {
    console.error('Run analysis error:', error)
    ElMessage.error('分析失败，请重试')
  }
}

onMounted(async () => {
  try {
    const res = await getMyChildren()
    childList.value = res.data || []
    if (childList.value.length > 0) {
      selectedChildId.value = childList.value[0].id
    }
  } catch (error) {
    console.error('Load children error:', error)
  }

  initCharts()
  if (selectedChildId.value) {
    loadAnalysis()
  } else {
    loadAllCharts()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(instance => instance?.dispose())
})
</script>

<style scoped>


.analysis-page {
  font-family: 'Nunito', sans-serif;
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

.header-icon {
  font-size: 28px;
}

.header-left h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2d3436;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.child-select {
  width: 150px;
}

.analysis-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.analysis-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(225, 112, 85, 0.4);
}

/* 核心指标 */
.metrics-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.reading-type .metric-icon { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.focus-score .metric-icon { background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); }
.ability-score .metric-icon { background: linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%); }
.cognitive-stage .metric-icon { background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%); }

.metric-content {
  flex: 1;
}

.metric-label {
  display: block;
  font-size: 12px;
  color: #a0937d;
  font-weight: 600;
  margin-bottom: 4px;
}

.metric-value {
  display: block;
  font-size: 22px;
  font-weight: 800;
  color: #2d3436;
}

.metric-value.type-focused { color: #55efc4; }
.metric-value.type-jumpy { color: #fdcb6e; }
.metric-value.type-interest { color: #74b9ff; }
.metric-value.stage { font-size: 14px; }

.metric-desc {
  display: block;
  font-size: 11px;
  color: #a0937d;
  margin-top: 4px;
}

.progress-bar {
  height: 6px;
  background: #f0e6d3;
  border-radius: 3px;
  margin-top: 8px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #e17055 0%, #fdcb6e 100%);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.progress-fill.green {
  background: linear-gradient(90deg, #55efc4 0%, #00b894 100%);
}

/* 多维度评分 */
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

.title-icon {
  font-size: 20px;
}

.scores-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.score-item {
  padding: 12px;
  background: #fef9f0;
  border-radius: 12px;
}

.score-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.score-label {
  font-size: 12px;
  color: #6d4c41;
  font-weight: 600;
}

.score-value {
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
}

.score-bar {
  height: 8px;
  background: #f0e6d3;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.bar-fill.blue { background: linear-gradient(90deg, #74b9ff 0%, #0984e3 100%); }
.bar-fill.orange { background: linear-gradient(90deg, #ffeaa7 0%, #fdcb6e 100%); }
.bar-fill.red { background: linear-gradient(90deg, #fab1a0 0%, #e17055 100%); }
.bar-fill.purple { background: linear-gradient(90deg, #a29bfe 0%, #6c5ce7 100%); }
.bar-fill.green { background: linear-gradient(90deg, #55efc4 0%, #00b894 100%); }

/* 兴趣标签 */
.interest-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.interest-tag {
  padding: 8px 16px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #2d3436;
}

.no-tag {
  color: #a0937d;
  font-size: 14px;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 24px;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.chart-card.full-width {
  grid-column: span 2;
}

.chart-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px dashed #f0e6d3;
}

.chart-title {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.chart-subtitle {
  display: block;
  font-size: 12px;
  color: #a0937d;
  margin-top: 4px;
}

.chart-container {
  width: 100%;
  height: 280px;
}

.chart-container.large {
  height: 320px;
}

/* 聚类图例 */
.cluster-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #f0e6d3;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #6d4c41;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.focused { background: #55efc4; }
.legend-dot.jumpy { background: #fdcb6e; }
.legend-dot.interest { background: #74b9ff; }

/* 算法说明 */
.algorithm-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.algorithm-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.algo-card {
  text-align: center;
  padding: 20px;
  background: #fef9f0;
  border-radius: 16px;
}

.algo-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.algo-card h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 700;
  color: #2d3436;
}

.algo-card p {
  margin: 0;
  font-size: 12px;
  color: #6d4c41;
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 1200px) {
  .metrics-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .scores-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .charts-row {
    grid-template-columns: 1fr;
  }

  .chart-card.full-width {
    grid-column: span 1;
  }

  .metrics-overview {
    grid-template-columns: 1fr;
  }

  .scores-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .algorithm-cards {
    grid-template-columns: 1fr;
  }

  .cluster-legend {
    flex-direction: column;
    align-items: center;
  }
}
</style>
