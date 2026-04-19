<template>
  <div class="child-detail">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>儿童详情</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>基本信息</span>
          </template>
          <div class="child-info">
            <el-avatar :size="80" icon="UserFilled" />
            <h3>{{ childInfo.name || '-' }}</h3>
            <p>年龄：{{ childInfo.age || '-' }}岁</p>
            <p>性别：{{ childInfo.gender === 1 ? '男' : childInfo.gender === 2 ? '女' : '-' }}</p>
            <p>班级：{{ childInfo.className || '-' }}</p>
            <el-tag :type="getReadingTypeTag(childInfo.readingType)" size="large">
              {{ childInfo.readingType || '未分析' }}
            </el-tag>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>兴趣标签</span>
          </template>
          <div class="interest-tags" v-if="interestTags.length > 0">
            <el-tag v-for="tag in interestTags" :key="tag" class="tag-item">
              {{ tag }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无兴趣标签" :image-size="60" />
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <span>阅读统计</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ readingStats.totalBooks || 0 }}</div>
                <div class="stat-label">阅读绘本数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ Math.round((readingStats.totalDuration || 0) / 60) }}</div>
                <div class="stat-label">阅读时长(分钟)</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ (readingStats.avgFocusScore || 0).toFixed(1) }}</div>
                <div class="stat-label">平均专注度</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ readingStats.totalLogs || 0 }}</div>
                <div class="stat-label">阅读记录数</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>最近阅读记录</span>
          </template>
          <el-table :data="recentLogs" stripe v-if="recentLogs.length > 0">
            <el-table-column prop="bookTitle" label="绘本名称">
              <template #default="{ row }">
                {{ row.bookTitle || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="阅读时长" width="120">
              <template #default="{ row }">
                {{ formatDuration(row.duration) }}
              </template>
            </el-table-column>
            <el-table-column prop="focusScore" label="专注度" width="140">
              <template #default="{ row }">
                <el-progress :percentage="Number(row.focusScore) || 0" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column prop="isCompleted" label="是否完成" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isCompleted === 1 ? 'success' : 'warning'">
                  {{ row.isCompleted === 1 ? '完成' : '未完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="阅读时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.startTime) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无阅读记录" :image-size="80" />
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>❤️ 收藏的绘本 ({{ bookmarkedBooks.length }})</span>
          </template>
          <div class="bookmarked-books" v-if="bookmarkedBooks.length > 0">
            <div class="book-item" v-for="item in bookmarkedBooks" :key="item.id">
              <div class="book-cover">
                <span class="book-emoji">📖</span>
              </div>
              <div class="book-info">
                <div class="book-title">{{ item.bookInfo?.title || '未知绘本' }}</div>
                <div class="book-author">{{ item.bookInfo?.author || '-' }}</div>
                <div class="book-category">
                  <el-tag size="small" type="info">{{ item.bookInfo?.category || '-' }}</el-tag>
                </div>
              </div>
              <div class="book-actions">
                <el-button type="primary" size="small" @click="readBook(item.bookId)">
                  开始阅读
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收藏的绘本" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getChildById } from '@/api/child'
import { getChildReadingLogs, getReadingStatistics } from '@/api/readingLog'
import { getAnalysisResult } from '@/api/analysis'
import { getBookmarks } from '@/api/bookmark'
import { getBookById } from '@/api/book'

const route = useRoute()
const router = useRouter()

const childInfo = ref({})
const interestTags = ref([])
const readingStats = ref({
  totalBooks: 0,
  totalDuration: 0,
  avgFocusScore: 0,
  totalLogs: 0
})
const recentLogs = ref([])
const bookmarkedBooks = ref([])

const getReadingTypeTag = (type) => {
  const types = { '专注型': 'success', '跳跃型': 'warning', '兴趣导向型': 'primary' }
  return types[type] || 'info'
}

const formatDuration = (seconds) => {
  if (!seconds) return '0秒'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (minutes > 0) return `${minutes}分${secs}秒`
  return `${secs}秒`
}

const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 16)
  }
  return time
}

const loadChildInfo = async () => {
  const childId = route.params.id
  if (!childId) {
    ElMessage.error('儿童ID不存在')
    return
  }

  try {
    // 加载儿童基本信息
    const res = await getChildById(childId)
    childInfo.value = res.data || {}

    // 加载阅读统计
    try {
      const statsRes = await getReadingStatistics(childId)
      readingStats.value = statsRes.data || {}
    } catch (e) {
      console.log('No reading stats')
    }

    // 加载分析结果获取兴趣标签
    try {
      const analysisRes = await getAnalysisResult(childId)
      if (analysisRes.data?.interestTags) {
        interestTags.value = analysisRes.data.interestTags.split(',').filter(t => t.trim())
      }
    } catch (e) {
      console.log('No analysis data')
    }

    // 加载阅读记录
    try {
      const logsRes = await getChildReadingLogs(childId, { current: 1, size: 10 })
      recentLogs.value = logsRes.data?.records || []
    } catch (e) {
      console.log('No reading logs')
    }

    // 加载收藏的绘本
    loadBookmarks(childId)
  } catch (error) {
    console.error('Load child info error:', error)
    ElMessage.error('加载儿童信息失败')
  }
}

// 加载收藏绘本
const loadBookmarks = async (childId) => {
  try {
    const res = await getBookmarks(childId)
    const bookmarks = res.data || []

    // 获取绘本详情
    const bookPromises = bookmarks.map(async (bookmark) => {
      try {
        const bookRes = await getBookById(bookmark.bookId)
        return {
          ...bookmark,
          bookInfo: bookRes.data
        }
      } catch (e) {
        return null
      }
    })

    const results = await Promise.all(bookPromises)
    bookmarkedBooks.value = results.filter(b => b && b.bookInfo)
  } catch (error) {
    console.error('Load bookmarks error:', error)
  }
}

// 开始阅读收藏的绘本
const readBook = (bookId) => {
  router.push({
    path: '/app/reading',
    query: { bookId, childId: route.params.id }
  })
}

onMounted(() => {
  loadChildInfo()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.child-info {
  text-align: center;
  padding: 20px 0;
}

.child-info h3 {
  margin: 15px 0 10px;
}

.child-info p {
  color: #606266;
  margin: 8px 0;
}

.interest-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  margin: 0;
}

.stat-item {
  text-align: center;
  padding: 15px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 收藏绘本列表 */
.bookmarked-books {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.book-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fef9f0;
  border-radius: 12px;
  border: 2px solid #f0e6d3;
  transition: all 0.3s ease;
}

.book-item:hover {
  border-color: #fdcb6e;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.3);
}

.book-cover {
  width: 50px;
  height: 60px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.book-emoji {
  font-size: 24px;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3436;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 12px;
  color: #a0937d;
  margin-top: 4px;
}

.book-category {
  margin-top: 6px;
}

.book-actions {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .bookmarked-books {
    grid-template-columns: 1fr;
  }
}
</style>
