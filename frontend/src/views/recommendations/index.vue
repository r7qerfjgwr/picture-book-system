<template>
  <div class="recommendations-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>⭐ 绘本推荐</h2>
      <p>为每个孩子智能推荐最适合的绘本</p>
    </div>

    <!-- 儿童选择器 -->
    <el-card class="selector-card" shadow="hover">
      <div class="selector-content">
        <div class="selector-left">
          <span class="selector-label">选择儿童：</span>
          <el-select
            v-model="selectedChildId"
            placeholder="请选择儿童"
            @change="onChildChange"
            style="width: 200px"
          >
            <el-option
              v-for="child in childList"
              :key="child.id"
              :label="child.name"
              :value="child.id"
            />
          </el-select>
        </div>
        <el-button type="primary" @click="refreshRecommendations" :loading="loading">
          刷新推荐
        </el-button>
      </div>
    </el-card>

    <!-- 推荐类型切换 -->
    <div class="tabs-wrapper">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-item', { active: activeTab === tab.key }]"
        @click="switchTab(tab.key)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrapper">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>正在加载推荐...</span>
    </div>

    <!-- 推荐列表 -->
    <div v-else class="books-grid">
      <div
        v-for="(item, index) in displayBooks"
        :key="index"
        class="book-card"
        @click="viewBook(item)"
      >
        <div class="book-cover" :style="getCoverStyle(getBookCategory(item))">
          <span class="cover-icon">{{ getBookEmoji(getBookCategory(item)) }}</span>
        </div>
        <div class="book-info">
          <h3 class="book-title">{{ getBookTitle(item) }}</h3>
          <p class="book-author">作者：{{ getBookAuthor(item) }}</p>
          <el-tag size="small" type="info">{{ getBookCategory(item) }}</el-tag>
          <div v-if="activeTab === 'multi' && item.score" class="match-score">
            <span class="score-label">匹配度：</span>
            <el-progress
              :percentage="Number(item.score) || 0"
              :stroke-width="6"
              :show-text="true"
            />
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="displayBooks.length === 0" class="empty-wrapper">
        <el-empty description="暂无推荐数据">
          <el-button type="primary" @click="refreshRecommendations">重新加载</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 绘本详情弹窗 -->
    <div class="book-modal" v-if="showBookDialog" @click.self="showBookDialog = false">
      <div class="modal-content">
        <button class="close-btn" @click="showBookDialog = false">✕</button>
        <div class="modal-body" v-if="selectedBook">
          <div class="modal-cover">
            <div class="cover-image" :style="getCoverStyle(selectedBook.category)">
              <span class="cover-emoji-lg">{{ getBookEmoji(selectedBook.category) }}</span>
            </div>
          </div>
          <div class="modal-info">
            <h2 class="modal-title">{{ selectedBook.title }}</h2>
            <p class="modal-author">作者：{{ selectedBook.author || '未知' }}</p>

            <div class="info-grid">
              <div class="info-item">
                <span class="info-icon">📂</span>
                <span class="info-label">分类</span>
                <span class="info-value">{{ selectedBook.category || '未分类' }}</span>
              </div>
              <div class="info-item">
                <span class="info-icon">👶</span>
                <span class="info-label">适合年龄</span>
                <span class="info-value">{{ selectedBook.ageRange || '3-6岁' }}</span>
              </div>
              <div class="info-item">
                <span class="info-icon">🧠</span>
                <span class="info-label">知识类型</span>
                <span class="info-value">{{ selectedBook.knowledgeType || '综合' }}</span>
              </div>
              <div class="info-item">
                <span class="info-icon">🎨</span>
                <span class="info-label">画风风格</span>
                <span class="info-value">{{ selectedBook.artStyle || '精美' }}</span>
              </div>
              <div class="info-item">
                <span class="info-icon">📊</span>
                <span class="info-label">难度等级</span>
                <span class="info-value">{{ getDifficultyText(selectedBook.difficultyLevel) }}</span>
              </div>
              <div class="info-item">
                <span class="info-icon">📄</span>
                <span class="info-label">页数</span>
                <span class="info-value">{{ selectedBook.pageCount || 32 }}页</span>
              </div>
            </div>

            <div class="description-section">
              <h4>📖 绘本简介</h4>
              <p>{{ selectedBook.description || '这是一本精彩的绘本，快来和小朋友一起阅读吧！' }}</p>
            </div>

            <div class="modal-actions">
              <select v-model="dialogChildId" class="child-selector">
                <option :value="null">选择儿童</option>
                <option v-for="child in childList" :key="child.id" :value="child.id">
                  {{ child.name }}
                </option>
              </select>
              <button class="action-btn primary" @click="startReading">
                <span>📖</span> 开始阅读
              </button>
              <button class="action-btn secondary" :class="{ bookmarked: isBookmarked }" @click="addToFavorite">
                <span>{{ isBookmarked ? '💔' : '❤️' }}</span>
                {{ isBookmarked ? '取消收藏' : '收藏' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { addBookmark, checkBookmark, removeBookmark } from '@/api/bookmark'

const router = useRouter()

// 状态
const childList = ref([])
const selectedChildId = ref(null)
const dialogChildId = ref(null)
const activeTab = ref('hot')
const loading = ref(false)
const showBookDialog = ref(false)
const selectedBook = ref(null)
const isBookmarked = ref(false)

// 各类型数据
const hotBooks = ref([])
const multiBooks = ref([])
const difficultyBooks = ref([])
const ageBooks = ref([])

// 标签配置
const tabs = [
  { key: 'hot', label: '热门绘本', icon: '🔥' },
  { key: 'multi', label: '智能推荐', icon: '🤖' },
  { key: 'difficulty', label: '难度匹配', icon: '📊' },
  { key: 'age', label: '年龄适合', icon: '👶' }
]

// 计算显示的书籍
const displayBooks = computed(() => {
  switch (activeTab.value) {
    case 'hot':
      return hotBooks.value.map(book => ({ book }))
    case 'multi':
      return multiBooks.value || []
    case 'difficulty':
      return difficultyBooks.value.map(book => ({ book }))
    case 'age':
      return ageBooks.value.map(book => ({ book }))
    default:
      return []
  }
})

// 获取书籍信息的辅助函数
const getBookTitle = (item) => {
  if (item.book && item.book.title) return item.book.title
  if (item.title) return item.title
  return '未知书名'
}

const getBookAuthor = (item) => {
  if (item.book && item.book.author) return item.book.author
  if (item.author) return item.author
  return '未知作者'
}

const getBookCategory = (item) => {
  if (item.book && item.book.category) return item.book.category
  if (item.category) return item.category
  return '未分类'
}

const getBookId = (item) => {
  if (item.book && item.book.id) return item.book.id
  if (item.id) return item.id
  return null
}

// 获取分类emoji
const getBookEmoji = (category) => {
  const emojis = {
    '动物': '🐻',
    '科普': '🔬',
    '情感': '❤️',
    '童话': '🏰'
  }
  return emojis[category] || '📖'
}

// 获取封面样式
const getCoverStyle = (category) => {
  const gradients = {
    '动物': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    '科普': 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    '情感': 'linear-gradient(135deg, #ffeaa7 0%, #ffb88c 100%)',
    '童话': 'linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%)'
  }
  return {
    background: gradients[category] || 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)'
  }
}

// 获取难度文字
const getDifficultyText = (level) => {
  const texts = { 1: '⭐ 简单', 2: '⭐⭐ 中等', 3: '⭐⭐⭐ 困难' }
  return texts[level] || '⭐ 简单'
}

// 查看绘本详情
const viewBook = async (item) => {
  selectedBook.value = item.book || item
  showBookDialog.value = true

  // 设置弹窗中的儿童选择
  dialogChildId.value = selectedChildId.value

  // 检查收藏状态
  await checkBookmarkStatus()
}

// 检查收藏状态
const checkBookmarkStatus = async () => {
  if (dialogChildId.value && selectedBook.value?.id) {
    try {
      const res = await checkBookmark(dialogChildId.value, selectedBook.value.id)
      isBookmarked.value = res.data || false
    } catch (e) {
      isBookmarked.value = false
    }
  } else {
    isBookmarked.value = false
  }
}

// 监听儿童选择变化
watch(dialogChildId, () => {
  if (showBookDialog.value) {
    checkBookmarkStatus()
  }
})

// 开始阅读
const startReading = () => {
  if (!dialogChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }
  if (!selectedBook.value || !getBookId(selectedBook.value)) {
    ElMessage.warning('绘本信息不完整')
    return
  }

  router.push({
    path: '/app/reading',
    query: {
      bookId: getBookId(selectedBook.value),
      childId: dialogChildId.value
    }
  })
}

// 添加收藏
const addToFavorite = async () => {
  if (!dialogChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }

  if (!selectedBook.value) return

  try {
    if (isBookmarked.value) {
      await removeBookmark(dialogChildId.value, selectedBook.value.id)
      isBookmarked.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addBookmark({
        childId: dialogChildId.value,
        bookId: selectedBook.value.id,
        bookmarkType: 1,
        note: ''
      })
      isBookmarked.value = true
      ElMessage.success('收藏成功！')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 切换标签
const switchTab = (key) => {
  activeTab.value = key
}

// 儿童选择变化
const onChildChange = () => {
  refreshRecommendations()
}

// 加载儿童列表
const loadChildren = async () => {
  try {
    const res = await request({
      url: '/child/my-children',
      method: 'get'
    })
    if (res.data) {
      childList.value = res.data
      if (childList.value.length > 0 && !selectedChildId.value) {
        selectedChildId.value = childList.value[0].id
      }
    }
  } catch (e) {
    console.log('加载儿童列表失败:', e)
    childList.value = []
  }
}

// 加载推荐数据
const refreshRecommendations = async () => {
  loading.value = true

  try {
    // 加载热门绘本（不需要选择儿童）
    const hotRes = await request({
      url: '/recommend/hot',
      method: 'get',
      params: { limit: 12 }
    })
    hotBooks.value = hotRes.data || []

    // 如果选择了儿童，加载个性化推荐
    if (selectedChildId.value) {
      // 智能推荐
      const multiRes = await request({
        url: `/recommend/multi-strategy/${selectedChildId.value}`,
        method: 'get',
        params: { limit: 12 }
      })
      multiBooks.value = multiRes.data || []

      // 难度匹配
      const diffRes = await request({
        url: `/recommend/difficulty/${selectedChildId.value}`,
        method: 'get',
        params: { limit: 12 }
      })
      difficultyBooks.value = diffRes.data || []

      // 年龄适合
      const ageRes = await request({
        url: `/recommend/age-range/${selectedChildId.value}`,
        method: 'get',
        params: { limit: 12 }
      })
      ageBooks.value = ageRes.data || []
    }
  } catch (e) {
    console.log('加载推荐失败:', e)
  } finally {
    loading.value = false
  }
}

// 页面加载
onMounted(async () => {
  await loadChildren()
  await refreshRecommendations()
})
</script>

<style scoped>
.recommendations-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.selector-card {
  margin-bottom: 20px;
}

.selector-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selector-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selector-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 标签切换 */
.tabs-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
}

.tab-item:hover {
  background: #e6f7ff;
}

.tab-item.active {
  background: #409eff;
  color: #fff;
}

.tab-icon {
  font-size: 16px;
}

.tab-label {
  font-size: 14px;
  font-weight: 500;
}

/* 加载状态 */
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #909399;
}

.loading-icon {
  font-size: 32px;
  margin-bottom: 12px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 书籍网格 */
.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.book-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #ebeef5;
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.book-cover {
  width: 80px;
  height: 100px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cover-icon {
  font-size: 32px;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #909399;
}

.match-score {
  margin-top: 12px;
}

.score-label {
  font-size: 12px;
  color: #606266;
  display: block;
  margin-bottom: 4px;
}

/* 空状态 */
.empty-wrapper {
  grid-column: 1 / -1;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
}

/* 绘本详情弹窗 */
.book-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: #fff;
  border-radius: 24px;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  border: 4px solid #f0e6d3;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
}

.close-btn:hover {
  transform: scale(1.1);
}

.modal-body {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  padding: 32px;
}

.modal-cover {
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 280px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-emoji-lg {
  font-size: 100px;
}

.modal-info {
  padding: 8px 0;
}

.modal-title {
  font-size: 26px;
  font-weight: 800;
  color: #2d3436;
  margin-bottom: 8px;
}

.modal-author {
  font-size: 14px;
  color: #a0937d;
  font-weight: 600;
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fef9f0;
  border-radius: 12px;
}

.info-icon {
  font-size: 20px;
}

.info-label {
  font-size: 12px;
  color: #a0937d;
  font-weight: 600;
}

.info-value {
  font-size: 13px;
  color: #2d3436;
  font-weight: 700;
  margin-left: auto;
}

.description-section {
  margin-bottom: 20px;
}

.description-section h4 {
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
  margin-bottom: 12px;
}

.description-section p {
  font-size: 14px;
  color: #6d4c41;
  line-height: 1.8;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  align-items: center;
}

.child-selector {
  padding: 14px 20px;
  border: 2px solid #f0e6d3;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 600;
  color: #2d3436;
  background: #fefefe;
  cursor: pointer;
  min-width: 120px;
}

.child-selector:focus {
  outline: none;
  border-color: #e17055;
}

.action-btn {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn.primary {
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  color: #fff;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(225, 112, 85, 0.4);
}

.action-btn.secondary {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  color: #2d3436;
}

.action-btn.secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(253, 203, 110, 0.4);
}

.action-btn.secondary.bookmarked {
  background: linear-gradient(135deg, #fd79a8 0%, #e84393 100%);
  color: #fff;
}

/* 响应式 */
@media (max-width: 768px) {
  .books-grid {
    grid-template-columns: 1fr;
  }

  .selector-content {
    flex-direction: column;
    gap: 16px;
  }

  .tabs-wrapper {
    flex-wrap: wrap;
  }

  .modal-body {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
