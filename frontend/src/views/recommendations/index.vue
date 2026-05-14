<template>
  <div class="recommendations-page">
    <!-- 页面横幅 -->
    <div class="page-banner">
      <div class="banner-content">
        <div class="banner-text">
          <h1>绘本推荐</h1>
          <p>智能推荐，让每个孩子找到最适合的绘本</p>
        </div>
        <div class="banner-icon">📚</div>
      </div>
      <div class="banner-deco"></div>
    </div>

    <!-- 儿童选择器 -->
    <div class="selector-section">
      <div class="selector-card">
        <div class="selector-icon">👦</div>
        <div class="selector-info">
          <span class="selector-label">选择儿童</span>
          <el-select
            v-model="selectedChildId"
            placeholder="请选择儿童"
            @change="onChildChange"
            class="child-select"
          >
            <el-option
              v-for="child in childList"
              :key="child.id"
              :label="child.name"
              :value="child.id"
            />
          </el-select>
        </div>
      </div>
      <button class="refresh-btn" @click="refreshRecommendations" :disabled="loading">
        <span v-if="loading" class="loading-spinner"></span>
        <span v-else>🔄</span>
        <span>刷新推荐</span>
      </button>
    </div>

    <!-- 推荐类型切换 -->
    <div class="tabs-section">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-card', { active: activeTab === tab.key }]"
        @click="switchTab(tab.key)"
      >
        <div class="tab-icon-wrapper" :class="tab.key">
          <span class="tab-icon">{{ tab.icon }}</span>
        </div>
        <div class="tab-info">
          <span class="tab-label">{{ tab.label }}</span>
          <span class="tab-desc">{{ tab.desc }}</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <div class="loading-animation">
        <div class="book-loader">
          <span>📖</span>
        </div>
        <p>正在寻找最适合的绘本...</p>
      </div>
    </div>

    <!-- 推荐列表 -->
    <div v-else-if="displayBooks.length > 0" class="books-section">
      <div class="section-header">
        <span class="section-icon">✨</span>
        <span class="section-title">为你推荐</span>
        <span class="book-count">共 {{ displayBooks.length }} 本</span>
      </div>
      <div class="books-grid">
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
            <p class="book-author">✍️ {{ getBookAuthor(item) }}</p>
            <div class="book-tags">
              <span class="tag category-tag">{{ getBookCategory(item) }}</span>
              <span v-if="item.book?.ageRange" class="tag age-tag">{{ item.book.ageRange }}</span>
            </div>
            <div v-if="activeTab === 'multi' && item.score" class="match-score">
              <div class="score-bar">
                <div class="score-fill" :style="{ width: (item.score || 0) + '%' }"></div>
              </div>
              <span class="score-text">匹配度 {{ Math.round(item.score || 0) }}%</span>
            </div>
          </div>
          <div class="book-action">
            <span class="action-icon">👉</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-section">
      <div class="empty-icon">📭</div>
      <h3>暂无推荐</h3>
      <p>请先选择儿童，或点击刷新获取推荐</p>
      <button class="retry-btn" @click="refreshRecommendations">
        <span>🔄</span> 重新加载
      </button>
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
  { key: 'hot', label: '热门绘本', icon: '🔥', desc: '最受欢迎' },
  { key: 'multi', label: '智能推荐', icon: '🤖', desc: '个性化' },
  { key: 'difficulty', label: '难度匹配', icon: '📊', desc: '能力适配' },
  { key: 'age', label: '年龄适合', icon: '👶', desc: '同龄最爱' }
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
    '认知启蒙': '🧒', '情感培养': '❤️', '生活习惯': '🧴',
    '故事': '🏰', '科学探索': '🔬', '品格教育': '🌟',
    '社会交往': '🤝', '益智游戏': '🧩', '艺术启蒙': '🎨',
    '安全教育': '🛡️'
  }
  return emojis[category] || '📖'
}

// 获取封面样式
const getCoverStyle = (category) => {
  const gradients = {
    '认知启蒙': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    '情感培养': 'linear-gradient(135deg, #ffeaa7 0%, #ffb88c 100%)',
    '生活习惯': 'linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%)',
    '故事': 'linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%)',
    '科学探索': 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    '品格教育': 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)',
    '社会交往': 'linear-gradient(135deg, #fdcbf1 0%, #e6dee9 100%)',
    '益智游戏': 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)',
    '艺术启蒙': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    '安全教育': 'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)'
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
  dialogChildId.value = selectedChildId.value
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
    const hotRes = await request({
      url: '/recommend/hot',
      method: 'get',
      params: { limit: 12 }
    })
    hotBooks.value = hotRes.data || []

    if (selectedChildId.value) {
      const multiRes = await request({
        url: `/recommend/multi-strategy/${selectedChildId.value}`,
        method: 'get',
        params: { limit: 12 }
      })
      multiBooks.value = multiRes.data || []

      const diffRes = await request({
        url: `/recommend/difficulty/${selectedChildId.value}`,
        method: 'get',
        params: { limit: 12 }
      })
      difficultyBooks.value = diffRes.data || []

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
  min-height: calc(100vh - 100px);
  background: linear-gradient(180deg, #fef9f3 0%, #fff5eb 100%);
  padding: 24px;
}

/* 页面横幅 */
.page-banner {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  border-radius: 24px;
  padding: 32px 40px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(255, 107, 107, 0.3);
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.banner-text h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 800;
  color: #fff;
}

.banner-text p {
  margin: 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.banner-icon {
  font-size: 64px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-10px) rotate(5deg); }
}

.banner-deco {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
}

/* 选择器区域 */
.selector-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.selector-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 16px 24px;
  border-radius: 20px;
  border: 3px solid #f0e6d3;
  flex: 1;
  max-width: 400px;
}

.selector-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.selector-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.selector-label {
  font-size: 12px;
  color: #a0937d;
  font-weight: 600;
}

.child-select {
  width: 180px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  border: none;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 184, 148, 0.3);
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 184, 148, 0.4);
}

.refresh-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 标签切换 */
.tabs-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.tab-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid #f0e6d3;
}

.tab-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(253, 203, 110, 0.25);
  border-color: #fdcb6e;
}

.tab-card.active {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-color: #f0b429;
  box-shadow: 0 8px 24px rgba(253, 203, 110, 0.4);
}

.tab-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.6);
}

.tab-card.active .tab-icon-wrapper {
  background: rgba(255, 255, 255, 0.8);
}

.tab-icon {
  font-size: 24px;
}

.tab-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tab-label {
  font-size: 15px;
  font-weight: 700;
  color: #2d3436;
}

.tab-desc {
  font-size: 12px;
  color: #a0937d;
  font-weight: 500;
}

/* 加载状态 */
.loading-section {
  display: flex;
  justify-content: center;
  padding: 80px 20px;
}

.loading-animation {
  text-align: center;
}

.book-loader {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin: 0 auto 16px;
  animation: bounce 1s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.loading-animation p {
  font-size: 15px;
  color: #a0937d;
  font-weight: 500;
}

/* 书籍列表 */
.books-section {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  border: 3px solid #f0e6d3;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 3px dashed #f0e6d3;
}

.section-icon {
  font-size: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 800;
  color: #2d3436;
}

.book-count {
  margin-left: auto;
  font-size: 14px;
  color: #a0937d;
  font-weight: 600;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.book-card {
  background: linear-gradient(145deg, #fff 0%, #fef9f0 100%);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid #f0e6d3;
  align-items: center;
}

.book-card:hover {
  transform: translateY(-6px);
  border-color: #fdcb6e;
  box-shadow: 0 12px 32px rgba(253, 203, 110, 0.3);
}

.book-cover {
  width: 80px;
  height: 100px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cover-icon {
  font-size: 36px;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #a0937d;
}

.book-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.tag {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
}

.category-tag {
  background: #ffeaa7;
  color: #e17055;
}

.age-tag {
  background: #dfe6e9;
  color: #636e72;
}

.match-score {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score-bar {
  flex: 1;
  height: 8px;
  background: #f0e6d3;
  border-radius: 4px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  background: linear-gradient(90deg, #55efc4 0%, #00b894 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.score-text {
  font-size: 12px;
  color: #00b894;
  font-weight: 700;
  white-space: nowrap;
}

.book-action {
  width: 36px;
  height: 36px;
  background: #ffeaa7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.book-card:hover .book-action {
  background: #fdcb6e;
  transform: scale(1.1);
}

.action-icon {
  font-size: 16px;
}

/* 空状态 */
.empty-section {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 24px;
  border: 3px solid #f0e6d3;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-section h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #2d3436;
}

.empty-section p {
  margin: 0 0 24px 0;
  font-size: 14px;
  color: #a0937d;
}

.retry-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  color: #2d3436;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(253, 203, 110, 0.4);
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
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  color: #fff;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 184, 148, 0.4);
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
@media (max-width: 1200px) {
  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .tabs-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .books-grid {
    grid-template-columns: 1fr;
  }

  .selector-section {
    flex-direction: column;
    align-items: stretch;
  }

  .selector-card {
    max-width: none;
  }

  .refresh-btn {
    width: 100%;
    justify-content: center;
  }

  .modal-body {
    grid-template-columns: 1fr;
    padding: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .modal-actions {
    flex-wrap: wrap;
  }
}
</style>
