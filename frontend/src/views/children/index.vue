<template>
  <div class="children-page">
    <div class="page-header">
      <h2>我的孩子</h2>
    </div>

    <!-- 孩子列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>孩子列表</span>
          <el-button type="primary" @click="showBindDialog">
            <el-icon><Plus /></el-icon>
            绑定孩子
          </el-button>
        </div>
      </template>

      <el-table :data="children" stripe v-loading="loading">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级">
          <template #default="{ row }">
            {{ row.className || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="readingType" label="阅读类型">
          <template #default="{ row }">
            <el-tag :type="getReadingTypeTag(row.readingType)">
              {{ row.readingType || '未分析' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewGrowth(row)">成长报告</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 孩子详情卡片 -->
    <div v-for="child in children" :key="child.id" class="child-detail-card">
      <el-card>
        <template #header>
          <div class="detail-header">
            <div class="child-basic">
              <el-avatar :size="50" icon="UserFilled" />
              <div class="child-name-info">
                <h3>{{ child.name }}</h3>
                <span class="child-meta">{{ child.age }}岁 · {{ child.className || '未分班' }}</span>
              </div>
              <el-tag :type="getReadingTypeTag(child.readingType)" size="large">
                {{ child.readingType || '未分析' }}
              </el-tag>
            </div>
            <el-button type="primary" @click="startReading(child)">
              📖 开始阅读
            </el-button>
          </div>
        </template>

        <el-row :gutter="20">
          <!-- 阅读统计 -->
          <el-col :span="12">
            <div class="stats-section">
              <h4>📊 阅读统计</h4>
              <div class="stats-grid">
                <div class="stat-box">
                  <span class="stat-value">{{ childStats[child.id]?.totalBooks || 0 }}</span>
                  <span class="stat-label">阅读绘本</span>
                </div>
                <div class="stat-box">
                  <span class="stat-value">{{ Math.round((childStats[child.id]?.totalDuration || 0) / 60) }}</span>
                  <span class="stat-label">分钟</span>
                </div>
                <div class="stat-box">
                  <span class="stat-value">{{ (childStats[child.id]?.avgFocusScore || 0).toFixed(0) }}</span>
                  <span class="stat-label">专注度</span>
                </div>
                <div class="stat-box">
                  <span class="stat-value">{{ childStats[child.id]?.totalLogs || 0 }}</span>
                  <span class="stat-label">记录数</span>
                </div>
              </div>
            </div>

            <!-- 最近阅读 -->
            <div class="recent-logs" v-if="childLogs[child.id]?.length > 0">
              <h4>📚 最近阅读</h4>
              <div class="log-list">
                <div class="log-item" v-for="log in childLogs[child.id].slice(0, 3)" :key="log.id">
                  <span class="log-title">{{ log.bookTitle }}</span>
                  <span class="log-time">{{ formatTime(log.startTime) }}</span>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 收藏绘本 -->
          <el-col :span="12">
            <div class="bookmarks-section">
              <h4>❤️ 收藏的绘本</h4>
              <div class="bookmarked-books" v-if="childBookmarks[child.id]?.length > 0">
                <div class="book-item" v-for="item in childBookmarks[child.id]" :key="item.id" @click="readBook(child.id, item.bookId)">
                  <div class="book-cover" :style="getCoverStyle(item.bookInfo?.category)">
                    <span class="cover-emoji">{{ getBookEmoji(item.bookInfo?.category) }}</span>
                  </div>
                  <div class="book-info">
                    <div class="book-title">{{ item.bookInfo?.title }}</div>
                    <el-tag size="small" type="info">{{ item.bookInfo?.category }}</el-tag>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无收藏" :image-size="50" />
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 绑定孩子弹窗 -->
    <el-dialog v-model="bindDialogVisible" title="绑定孩子" width="500px">
      <el-form ref="bindFormRef" :model="bindForm" :rules="bindRules" label-width="80px">
        <el-form-item label="孩子姓名" prop="name">
          <el-input v-model="bindForm.name" placeholder="请输入孩子姓名" />
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="bindForm.birthDate"
            type="date"
            placeholder="选择出生日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="bindForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="bindForm.classId" placeholder="选择班级" style="width: 100%">
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBind">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyChildren, bindChild } from '@/api/child'
import { getAllClasses } from '@/api/classInfo'
import { getBookmarks } from '@/api/bookmark'
import { getBookById } from '@/api/book'
import { getChildReadingLogs, getReadingStatistics } from '@/api/readingLog'

const router = useRouter()
const loading = ref(false)
const children = ref([])
const classList = ref([])
const bindDialogVisible = ref(false)
const bindFormRef = ref()

// 每个孩子的数据
const childStats = ref({})
const childLogs = ref({})
const childBookmarks = ref({})

const bindForm = reactive({
  name: '',
  birthDate: '',
  gender: 1,
  classId: ''
})

const bindRules = {
  name: [{ required: true, message: '请输入孩子姓名', trigger: 'blur' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }]
}

const getReadingTypeTag = (type) => {
  const types = { '专注型': 'success', '跳跃型': 'warning', '兴趣导向型': 'primary' }
  return types[type] || 'info'
}

// 根据分类获取绘本图标
const getBookEmoji = (category) => {
  const emojis = {
    '动物': '🐻',
    '科普': '🔬',
    '情感': '❤️',
    '童话': '🏰'
  }
  return emojis[category] || '📖'
}

// 根据分类获取封面样式
const getCoverStyle = (category) => {
  const gradients = {
    '动物': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    '科普': 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    '情感': 'linear-gradient(135deg, #ffecd2 0%, #ffb88c 100%)',
    '童话': 'linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%)'
  }
  return { background: gradients[category] || 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)' }
}

const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 10)
  return time
}

const loadChildren = async () => {
  loading.value = true
  try {
    const res = await getMyChildren()
    children.value = res.data || []

    // 加载每个孩子的详情数据
    children.value.forEach(child => {
      loadChildDetail(child.id)
    })
  } catch (error) {
    console.error('Load children error:', error)
    ElMessage.error('加载孩子列表失败')
  } finally {
    loading.value = false
  }
}

// 加载单个孩子的详情数据
const loadChildDetail = async (childId) => {
  // 加载统计
  try {
    const statsRes = await getReadingStatistics(childId)
    childStats.value[childId] = statsRes.data || {}
  } catch (e) {
    childStats.value[childId] = {}
  }

  // 加载阅读记录
  try {
    const logsRes = await getChildReadingLogs(childId, { current: 1, size: 5 })
    childLogs.value[childId] = logsRes.data?.records || []
  } catch (e) {
    childLogs.value[childId] = []
  }

  // 加载收藏
  try {
    const bookmarkRes = await getBookmarks(childId)
    const bookmarks = bookmarkRes.data || []

    // 获取绘本详情
    const bookPromises = bookmarks.map(async (bookmark) => {
      try {
        const bookRes = await getBookById(bookmark.bookId)
        return { ...bookmark, bookInfo: bookRes.data }
      } catch (e) {
        return null
      }
    })

    const results = await Promise.all(bookPromises)
    childBookmarks.value[childId] = results.filter(b => b && b.bookInfo)
  } catch (e) {
    childBookmarks.value[childId] = []
  }
}

const loadClasses = async () => {
  try {
    const res = await getAllClasses()
    if (res.code === 200) {
      classList.value = res.data || []
    }
  } catch (error) {
    console.error('Load classes error:', error)
  }
}

const showBindDialog = () => {
  bindForm.name = ''
  bindForm.birthDate = ''
  bindForm.gender = 1
  bindForm.classId = ''
  bindDialogVisible.value = true
}

const handleBind = async () => {
  const valid = await bindFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await bindChild(bindForm)
    ElMessage.success('绑定成功')
    bindDialogVisible.value = false
    loadChildren()
  } catch (error) {
    console.error('Bind child error:', error)
    ElMessage.error('绑定失败，请稍后重试')
  }
}

const viewGrowth = (row) => {
  router.push({ path: '/app/growth', query: { childId: row.id } })
}

const startReading = (child) => {
  router.push({
    path: '/app/books/category/animal',
    query: { childId: child.id }
  })
}

const readBook = (childId, bookId) => {
  router.push({
    path: '/app/reading',
    query: { childId, bookId }
  })
}

onMounted(() => {
  loadChildren()
  loadClasses()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2d3436;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 孩子详情卡片 */
.child-detail-card {
  margin-top: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.child-basic {
  display: flex;
  align-items: center;
  gap: 16px;
}

.child-name-info h3 {
  margin: 0;
  font-size: 18px;
  color: #2d3436;
}

.child-meta {
  font-size: 13px;
  color: #a0937d;
}

/* 统计区域 */
.stats-section, .bookmarks-section, .recent-logs {
  background: #fef9f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.stats-section h4, .bookmarks-section h4, .recent-logs h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #6d4c41;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-box {
  text-align: center;
  padding: 12px;
  background: #fff;
  border-radius: 10px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #e17055;
}

.stat-label {
  font-size: 12px;
  color: #a0937d;
}

/* 最近阅读 */
.log-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  background: #fff;
  border-radius: 8px;
  font-size: 13px;
}

.log-title {
  color: #2d3436;
  font-weight: 500;
}

.log-time {
  color: #a0937d;
  font-size: 12px;
}

/* 收藏绘本 */
.bookmarked-books {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.book-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.book-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.book-cover {
  width: 40px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cover-emoji {
  font-size: 20px;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 13px;
  font-weight: 600;
  color: #2d3436;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .bookmarked-books {
    grid-template-columns: 1fr;
  }
}
</style>
