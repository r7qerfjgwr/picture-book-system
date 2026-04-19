<template>
  <div class="storybook-category-page">
    <!-- 分类横幅 -->
    <div class="category-banner" :class="categoryTheme">
      <div class="banner-content">
        <div class="banner-left">
          <span class="category-emoji">{{ categoryInfo.emoji }}</span>
          <div class="category-text">
            <h1>{{ categoryInfo.title }}</h1>
            <p>{{ categoryInfo.description }}</p>
          </div>
        </div>
        <div class="banner-deco">
          <span class="deco-icon">{{ categoryInfo.decoEmoji }}</span>
        </div>
      </div>
      <div class="banner-pattern"></div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-header">
          <span class="filter-icon">🔍</span>
          <span>智能筛选</span>
        </div>
        <div class="filter-grid">
          <div class="filter-item">
            <label>适合年龄</label>
            <select v-model="filterForm.ageRange" @change="applyFilter">
              <option value="">全部年龄</option>
              <option v-for="age in ageRanges" :key="age" :value="age">{{ age }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>知识类型</label>
            <select v-model="filterForm.knowledgeType" @change="applyFilter">
              <option value="">全部类型</option>
              <option v-for="type in knowledgeTypes" :key="type" :value="type">{{ type }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>画风风格</label>
            <select v-model="filterForm.artStyle" @change="applyFilter">
              <option value="">全部画风</option>
              <option v-for="style in artStyles" :key="style" :value="style">{{ style }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>难度等级</label>
            <select v-model="filterForm.difficultyLevel" @change="applyFilter">
              <option value="">全部难度</option>
              <option :value="1">⭐ 简单</option>
              <option :value="2">⭐⭐ 中等</option>
              <option :value="3">⭐⭐⭐ 困难</option>
            </select>
          </div>
        </div>
        <div class="filter-actions">
          <button class="reset-btn" @click="resetFilter">
            <span>🔄</span> 重置筛选
          </button>
        </div>
      </div>
    </div>

    <!-- 快捷标签 -->
    <div class="quick-tags">
      <span class="tags-label">✨ 快捷筛选：</span>
      <div class="tags-list">
        <span
          v-for="tag in quickTags"
          :key="tag.value"
          :class="['tag-item', { active: activeQuickTag === tag.value }]"
          @click="handleQuickFilter(tag.value)"
        >
          {{ tag.emoji }} {{ tag.label }}
        </span>
      </div>
    </div>

    <!-- 绘本网格 -->
    <div class="books-section">
      <div class="section-header">
        <div class="header-left">
          <span class="section-icon">📚</span>
          <span class="section-title">绘本列表</span>
        </div>
        <div class="book-count">
          共找到 <span class="count-num">{{ books.length }}</span> 本绘本
        </div>
      </div>

      <div class="books-grid" v-if="books.length > 0">
        <div
          v-for="book in books"
          :key="book.id"
          class="book-card"
          @click="viewBook(book)"
        >
          <div class="book-cover-wrapper">
            <div class="book-cover" :style="getCoverStyle(book)">
              <span class="cover-emoji">{{ getBookEmoji(book.category) }}</span>
            </div>
            <div class="book-badge" v-if="book.readCount > 10">
              <span>🔥</span> 热门
            </div>
          </div>
          <div class="book-info">
            <h3 class="book-title">{{ book.title }}</h3>
            <p class="book-author">✍️ {{ book.author }}</p>
            <div class="book-tags">
              <span class="tag age-tag" v-if="book.ageRange">{{ book.ageRange }}</span>
              <span class="tag cat-tag" v-if="book.category">{{ book.category }}</span>
            </div>
            <div class="book-footer">
              <div class="book-rating">
                <span v-for="i in 5" :key="i" :class="['star', { filled: i <= Math.round(book.rating || 4) }]">★</span>
              </div>
              <span class="read-count">📖 {{ book.readCount || 0 }}次</span>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-else>
        <div class="empty-icon">🔍</div>
        <p>暂无符合条件的绘本</p>
        <p class="empty-hint">当前分类下没有符合所选条件的内容，试试其他筛选条件吧</p>
        <button class="retry-btn" @click="resetFilter">
          <span>🔄</span> 重置筛选
        </button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="books.length > 0">
        <div class="pagination">
          <button class="page-btn" :disabled="pagination.page === 1" @click="pagination.page--; loadBooks()">
            ◀ 上一页
          </button>
          <span class="page-info">第 {{ pagination.page }} 页</span>
          <button class="page-btn" :disabled="books.length < pagination.size" @click="pagination.page++; loadBooks()">
            下一页 ▶
          </button>
        </div>
      </div>
    </div>

    <!-- 绘本详情弹窗 -->
    <div class="book-modal" v-if="showModal" @click.self="showModal = false">
      <div class="modal-content">
        <button class="close-btn" @click="showModal = false">✕</button>
        <div class="modal-body" v-if="selectedBook">
          <div class="modal-cover">
            <div class="cover-image" :style="getCoverStyle(selectedBook)">
              <span class="cover-emoji-lg">{{ getBookEmoji(selectedBook.category) }}</span>
            </div>
          </div>
          <div class="modal-info">
            <h2 class="modal-title">{{ selectedBook.title }}</h2>
            <p class="modal-author">作者：{{ selectedBook.author }}</p>

            <div class="info-grid">
              <div class="info-item">
                <span class="info-icon">📂</span>
                <span class="info-label">分类</span>
                <span class="info-value">{{ selectedBook.category }}</span>
              </div>
              <div class="info-item">
                <span class="info-icon">👶</span>
                <span class="info-label">适合年龄</span>
                <span class="info-value">{{ selectedBook.ageRange }}</span>
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

            <div class="keywords-section" v-if="selectedBook.keywords">
              <h4>🏷️ 关键词</h4>
              <div class="keywords-list">
                <span class="keyword-tag" v-for="keyword in selectedBook.keywords.split(',')" :key="keyword">
                  {{ keyword }}
                </span>
              </div>
            </div>

            <div class="modal-actions">
              <select v-model="selectedChildId" class="child-selector">
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBooksByCategory, filterBooks } from '@/api/book'
import { getMyChildren } from '@/api/child'
import { createReadingLog } from '@/api/readingLog'
import { addBookmark, checkBookmark, removeBookmark } from '@/api/bookmark'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const books = ref([])
const showModal = ref(false)
const selectedBook = ref(null)
const activeQuickTag = ref('')
const childList = ref([])
const selectedChildId = ref(null)
const isBookmarked = ref(false)

const categoryConfig = {
  animal: {
    title: '动物类绘本',
    description: '认识可爱的动物朋友，探索神奇的动物世界',
    emoji: '🐻',
    decoEmoji: '🦁🐘🦒',
    theme: 'animal',
    dbCategory: '动物'
  },
  science: {
    title: '科普类绘本',
    description: '探索科学的奥秘，培养小小科学家',
    emoji: '🔬',
    decoEmoji: '🚀🌍💡',
    theme: 'science',
    dbCategory: '科普'
  },
  emotion: {
    title: '情感类绘本',
    description: '感受爱与温暖，培养情商与品格',
    emoji: '❤️',
    decoEmoji: '🌸💝🌈',
    theme: 'emotion',
    dbCategory: '情感'
  },
  fairy_tale: {
    title: '童话类绘本',
    description: '走进梦幻童话世界，开启奇妙冒险',
    emoji: '🏰',
    decoEmoji: '👑🧚✨',
    theme: 'fairy',
    dbCategory: '童话'
  }
}

const categoryInfo = computed(() => {
  const category = route.params.category || 'animal'
  return categoryConfig[category] || categoryConfig.animal
})

const categoryTheme = computed(() => {
  return categoryInfo.value.theme
})

const ageRanges = ref(['3-4岁', '4-5岁', '5-6岁'])
const knowledgeTypes = ref(['语言', '数学', '科学', '艺术', '社会'])
const artStyles = ref(['水彩', '卡通', '写实', '剪纸', '拼贴'])

const filterForm = reactive({
  category: '',
  ageRange: '',
  knowledgeType: '',
  artStyle: '',
  difficultyLevel: null
})

const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

const quickTags = [
  { label: '热门推荐', value: 'hot', emoji: '🔥' },
  { label: '3-4岁适合', value: 'age_3_4', emoji: '👶' },
  { label: '科学启蒙', value: 'science', emoji: '🔬' },
  { label: '情感培养', value: 'emotion', emoji: '❤️' },
  { label: '水彩画风', value: 'watercolor', emoji: '🎨' }
]

const getBookEmoji = (category) => {
  const emojis = {
    '动物': '🐻',
    '科普': '🔬',
    '情感': '❤️',
    '童话': '🏰'
  }
  return emojis[category] || '📖'
}

const getCoverStyle = (book) => {
  const gradients = {
    '动物': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    '科普': 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    '情感': 'linear-gradient(135deg, #ffecd2 0%, #ffb88c 100%)',
    '童话': 'linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%)'
  }
  return {
    background: gradients[book.category] || 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)'
  }
}

const getDifficultyText = (level) => {
  const texts = { 1: '⭐ 简单', 2: '⭐⭐ 中等', 3: '⭐⭐⭐ 困难' }
  return texts[level] || '⭐ 简单'
}

const loadBooks = async () => {
  try {
    const categoryKey = route.params.category || 'animal'
    const dbCategory = categoryConfig[categoryKey]?.dbCategory || ''

    // 检查是否有筛选条件（不包括category）
    const hasFilter = filterForm.ageRange || filterForm.knowledgeType ||
                      filterForm.artStyle || filterForm.difficultyLevel

    // 如果是热门推荐，调用热门接口
    if (activeQuickTag.value === 'hot') {
      const res = await filterBooks({ category: dbCategory })
      // 按阅读次数排序，取热门绘本
      books.value = (res.data || []).sort((a, b) => (b.readCount || 0) - (a.readCount || 0))
    } else if (hasFilter) {
      // 构建筛选参数，只传递有值的参数
      const params = {}
      // 不传category，让筛选在整个绘本库中查找
      if (filterForm.ageRange) params.ageRange = filterForm.ageRange
      if (filterForm.knowledgeType) params.knowledgeType = filterForm.knowledgeType
      if (filterForm.artStyle) params.artStyle = filterForm.artStyle
      if (filterForm.difficultyLevel) params.difficultyLevel = Number(filterForm.difficultyLevel)

      console.log('Filter params:', params)
      const res = await filterBooks(params)
      books.value = res.data || []
      console.log('Filter result:', books.value.length)
    } else {
      const res = await getBooksByCategory(dbCategory, '')
      books.value = res.data || []
    }
    pagination.total = books.value.length
  } catch (error) {
    console.error('Load books error:', error)
    books.value = []
  }
}

const generateMockBooks = () => {
  const category = route.params.category || 'animal'
  const mockData = {
    animal: [
      { id: 1, title: '小熊的冒险', author: '张小明', category: '动物', ageRange: '3-4岁', rating: 4.5, readCount: 128, knowledgeType: '语言', artStyle: '水彩', pageCount: 24, description: '一只勇敢的小熊踏上寻找蜂蜜的冒险之旅，途中结识了许多动物朋友。' },
      { id: 2, title: '森林里的音乐会', author: '李小红', category: '动物', ageRange: '4-5岁', rating: 4.8, readCount: 256, knowledgeType: '艺术', artStyle: '卡通', pageCount: 28, description: '森林里的小动物们举办了一场盛大的音乐会，每个动物都展示了自己的才艺。' },
      { id: 3, title: '小兔子的新家', author: '王小刚', category: '动物', ageRange: '3-4岁', rating: 4.2, readCount: 89, knowledgeType: '社会', artStyle: '水彩', pageCount: 20, description: '小兔子要搬新家了，它和朋友们一起布置温馨的小窝。' },
    ],
    science: [
      { id: 4, title: '太空探险记', author: '赵小美', category: '科普', ageRange: '5-6岁', rating: 4.7, readCount: 312, knowledgeType: '科学', artStyle: '写实', pageCount: 32, description: '跟随小宇航员一起探索神秘的太空，认识各种星球。' },
      { id: 5, title: '神奇的水循环', author: '刘小华', category: '科普', ageRange: '4-5岁', rating: 4.4, readCount: 156, knowledgeType: '科学', artStyle: '卡通', pageCount: 26, description: '一滴小水珠的奇妙旅程，了解水的三种形态。' },
    ],
    emotion: [
      { id: 6, title: '爱的抱抱', author: '孙小芳', category: '情感', ageRange: '3-4岁', rating: 4.9, readCount: 428, knowledgeType: '社会', artStyle: '水彩', pageCount: 22, description: '一个温暖的抱抱，传递着最真挚的爱与关怀。' },
      { id: 7, title: '我的好朋友', author: '周小杰', category: '情感', ageRange: '4-5岁', rating: 4.6, readCount: 198, knowledgeType: '社会', artStyle: '卡通', pageCount: 24, description: '关于友情的小故事，学会分享与关心他人。' },
    ],
    fairy_tale: [
      { id: 8, title: '灰姑娘的舞会', author: '吴小燕', category: '童话', ageRange: '4-5岁', rating: 4.8, readCount: 356, knowledgeType: '语言', artStyle: '水彩', pageCount: 30, description: '经典的灰姑娘故事，梦想终会实现。' },
      { id: 9, title: '勇敢的小裁缝', author: '郑小龙', category: '童话', ageRange: '5-6岁', rating: 4.5, readCount: 167, knowledgeType: '语言', artStyle: '卡通', pageCount: 28, description: '一个小裁缝用智慧和勇气战胜了巨人。' },
    ]
  }
  return mockData[category] || mockData.animal
}

const applyFilter = () => {
  pagination.page = 1
  // 设置当前分类到筛选条件
  const categoryKey = route.params.category || 'animal'
  filterForm.category = categoryConfig[categoryKey]?.dbCategory || ''
  loadBooks()
}

const resetFilter = () => {
  filterForm.ageRange = ''
  filterForm.knowledgeType = ''
  filterForm.artStyle = ''
  filterForm.difficultyLevel = null
  filterForm.category = ''
  activeQuickTag.value = ''
  pagination.page = 1
  loadBooks()
}

const handleQuickFilter = (value) => {
  // 如果点击的是当前激活的标签，则取消筛选
  if (activeQuickTag.value === value) {
    activeQuickTag.value = ''
    filterForm.ageRange = ''
    filterForm.knowledgeType = ''
    filterForm.artStyle = ''
    filterForm.difficultyLevel = null
    filterForm.category = ''
    pagination.page = 1
    loadBooks()
    return
  }

  // 设置新的激活标签
  activeQuickTag.value = value

  // 重置筛选条件（不重置activeQuickTag）
  filterForm.ageRange = ''
  filterForm.knowledgeType = ''
  filterForm.artStyle = ''
  filterForm.difficultyLevel = null
  filterForm.category = ''
  pagination.page = 1

  // 设置新的筛选条件
  switch (value) {
    case 'hot':
      // 热门推荐不需要额外筛选条件
      break
    case 'age_3_4':
      filterForm.ageRange = '3-4岁'
      break
    case 'science':
      filterForm.knowledgeType = '科学'
      break
    case 'emotion':
      filterForm.knowledgeType = '社会'
      break
    case 'watercolor':
      filterForm.artStyle = '水彩'
      break
  }

  loadBooks()
}

const viewBook = async (book) => {
  selectedBook.value = book
  showModal.value = true

  // 检查是否已收藏
  await checkBookmarkStatus()
}

// 检查收藏状态
const checkBookmarkStatus = async () => {
  if (selectedChildId.value && selectedBook.value?.id) {
    try {
      const res = await checkBookmark(selectedChildId.value, selectedBook.value.id)
      isBookmarked.value = res.data || false
    } catch (e) {
      isBookmarked.value = false
    }
  } else {
    isBookmarked.value = false
  }
}

// 监听儿童选择变化，更新收藏状态
watch(selectedChildId, () => {
  if (showModal.value) {
    checkBookmarkStatus()
  }
})

const startReading = async () => {
  if (!selectedChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }

  if (!selectedBook.value) return

  // 跳转到阅读页面
  router.push({
    path: '/app/reading',
    query: {
      bookId: selectedBook.value.id,
      childId: selectedChildId.value
    }
  })
  showModal.value = false
}

const addToFavorite = async () => {
  if (!selectedChildId.value) {
    ElMessage.warning('请先选择儿童')
    return
  }

  if (!selectedBook.value) return

  try {
    if (isBookmarked.value) {
      // 取消收藏
      await removeBookmark(selectedChildId.value, selectedBook.value.id)
      isBookmarked.value = false
      ElMessage.success('已取消收藏')
    } else {
      // 添加收藏
      await addBookmark({
        childId: selectedChildId.value,
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

watch(() => route.params.category, () => {
  pagination.page = 1
  resetFilter()
  loadBooks()
})

// 加载儿童列表
const loadChildren = async () => {
  try {
    const res = await getMyChildren()
    childList.value = res.data || []
    if (childList.value.length > 0) {
      selectedChildId.value = childList.value[0].id
    }
  } catch (error) {
    console.error('加载儿童列表失败:', error)
  }
}

onMounted(() => {
  loadBooks()
  loadChildren()
})
</script>

<style scoped>


.storybook-category-page {
  font-family: 'Nunito', sans-serif;
  min-height: 100vh;
}

/* 分类横幅 */
.category-banner {
  border-radius: 24px;
  padding: 32px 40px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.category-banner.animal {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.category-banner.science {
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
}

.category-banner.emotion {
  background: linear-gradient(135deg, #ffeaa7 0%, #ffb88c 100%);
}

.category-banner.fairy {
  background: linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%);
}

.category-banner::before {
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

.banner-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.category-emoji {
  font-size: 72px;
  animation: bounce 2s infinite ease-in-out;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.category-text h1 {
  font-size: 32px;
  font-weight: 800;
  color: #2d3436;
  margin-bottom: 8px;
}

.category-text p {
  font-size: 16px;
  color: #6d4c41;
  font-weight: 600;
}

.deco-icon {
  font-size: 48px;
  animation: float 3s infinite ease-in-out;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-15px) rotate(5deg); }
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 24px;
}

.filter-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 3px solid #f0e6d3;
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
}

.filter-icon {
  font-size: 24px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-size: 13px;
  font-weight: 600;
  color: #6d4c41;
}

.filter-item select {
  padding: 12px 16px;
  border: 2px solid #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #2d3436;
  background: #fefefe;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-item select:hover {
  border-color: #ffd93d;
}

.filter-item select:focus {
  outline: none;
  border-color: #e17055;
  box-shadow: 0 0 0 3px rgba(225, 112, 85, 0.1);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
}

.reset-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

/* 快捷标签 */
.quick-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tags-label {
  font-size: 14px;
  font-weight: 600;
  color: #6d4c41;
}

.tags-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tag-item {
  padding: 8px 16px;
  background: #fff;
  border: 2px solid #f0e6d3;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #6d4c41;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-item:hover {
  border-color: #ffd93d;
  background: #fffdf5;
}

.tag-item.active {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-color: #fdcb6e;
  color: #2d3436;
}

/* 绘本区域 */
.books-section {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  border: 3px solid #f0e6d3;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px dashed #f0e6d3;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  font-size: 28px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #2d3436;
}

.book-count {
  font-size: 14px;
  color: #6d4c41;
  font-weight: 600;
}

.count-num {
  color: #e17055;
  font-weight: 800;
}

/* 绘本网格 */
.books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.book-card {
  background: linear-gradient(145deg, #fff 0%, #fef9f0 100%);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid transparent;
}

.book-card:hover {
  transform: translateY(-8px);
  border-color: #ffd93d;
  box-shadow: 0 12px 32px rgba(253, 203, 110, 0.3);
}

.book-cover-wrapper {
  position: relative;
}

.book-cover {
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-emoji {
  font-size: 64px;
  animation: bookFloat 3s infinite ease-in-out;
}

@keyframes bookFloat {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-8px) rotate(3deg); }
}

.book-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 700;
}

.book-info {
  padding: 16px;
}

.book-title {
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 13px;
  color: #a0937d;
  margin-bottom: 10px;
}

.book-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
}

.age-tag {
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  color: #fff;
}

.cat-tag {
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  color: #fff;
}

.book-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-rating .star {
  color: #f0e6d3;
  font-size: 14px;
}

.book-rating .star.filled {
  color: #ffd93d;
}

.read-count {
  font-size: 12px;
  color: #a0937d;
  font-weight: 600;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #6d4c41;
  font-weight: 600;
}

.empty-hint {
  font-size: 14px;
  color: #a0937d;
  margin-top: 8px;
}

.retry-btn {
  margin-top: 20px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

/* 分页 */
.pagination-wrapper {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 2px dashed #f0e6d3;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.page-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  font-weight: 700;
  color: #6d4c41;
}

/* 弹窗 */
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

.description-section,
.keywords-section {
  margin-bottom: 20px;
}

.description-section h4,
.keywords-section h4 {
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

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  padding: 6px 14px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
  color: #2d3436;
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
@media (max-width: 1200px) {
  .books-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .filter-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .modal-body {
    grid-template-columns: 1fr;
  }

  .banner-left {
    flex-direction: column;
    text-align: center;
  }

  .category-emoji {
    font-size: 48px;
  }

  .category-text h1 {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .books-grid {
    grid-template-columns: 1fr;
  }

  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
