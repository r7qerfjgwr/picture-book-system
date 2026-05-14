<template>
  <div class="reading-page">
    <!-- 顶部状态栏 -->
    <div class="reading-header">
      <button class="back-btn" @click="confirmExit">
        <span>←</span> 退出阅读
      </button>
      <div class="reading-info">
        <span class="book-title">{{ book?.title }}</span>
        <span class="child-name">👶 {{ child?.name }}正在阅读</span>
      </div>
      <div class="header-stats">
        <div class="stat-mini">
          <span>⏱️</span>
          <span>{{ formattedDuration }}</span>
        </div>
        <div class="stat-mini">
          <span>📖 翻页</span>
          <span>{{ pageTurnCount }}次</span>
        </div>
        <div class="stat-mini">
          <span>📊 进度</span>
          <span>{{ progressPercent }}%</span>
        </div>
      </div>
    </div>

    <!-- 绘本阅读区域 -->
    <div class="reading-content">
      <div class="book-display">
        <div class="page-container">
          <div class="current-page" :style="getPageStyle">
            <div class="page-content">
              <div class="illustration-area">
                <span class="illustration-emoji">{{ currentPageData.illustration }}</span>
              </div>
              <div class="story-area">
                <p class="story-text">{{ currentPageData.text }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="page-controls">
          <button class="page-btn prev" :disabled="currentPage <= 1" @click="prevPage">
            <span>◀</span> 上一页
          </button>
          <span class="page-indicator-inline">第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button class="page-btn next" :disabled="currentPage >= totalPages" @click="nextPage">
            下一页 <span>▶</span>
          </button>
        </div>
      </div>

      <!-- 底部操作区 -->
      <div class="bottom-bar">
        <div class="interaction-btns">
          <button class="interact-btn" :class="{ active: isBookmarked }" @click="toggleBookmark">
            <span>{{ isBookmarked ? '❤️' : '🤍' }}</span>
            <span class="btn-text">收藏</span>
          </button>
          <button class="interact-btn" @click="openAnnotationDialog">
            <span>📝</span>
            <span class="btn-text">批注</span>
          </button>
          <button class="interact-btn" :class="{ recording: isRecording }" @click="toggleRecording">
            <span>{{ isRecording ? '⏹️' : '🎤' }}</span>
            <span class="btn-text">{{ isRecording ? `${recordingTime}s` : '录音' }}</span>
          </button>
          <button class="interact-btn" @click="showAnnotationList = true">
            <span>📋</span>
            <span class="btn-text">记录</span>
          </button>
        </div>
        <div class="reading-tips-inline">
          <span>💡</span>
          <span>{{ currentTip }}</span>
        </div>
        <button class="finish-btn" @click="finishReading">
          ✅ 完成阅读
        </button>
      </div>
    </div>

    <div class="confirm-modal" v-if="showExitConfirm">
      <div class="confirm-content">
        <h3>确定要退出阅读吗？</h3>
        <p>当前阅读进度将被保存</p>
        <div class="confirm-actions">
          <button class="cancel-btn" @click="showExitConfirm = false">继续阅读</button>
          <button class="confirm-btn" @click="exitReading">确认退出</button>
        </div>
      </div>
    </div>

    <!-- 批注弹窗 -->
    <div class="confirm-modal" v-if="showAnnotationDialog" @click.self="showAnnotationDialog = false">
      <div class="annotation-dialog">
        <div class="dialog-header">
          <h3>📝 添加批注</h3>
          <button class="close-btn" @click="showAnnotationDialog = false">✕</button>
        </div>
        <div class="dialog-body">
          <p class="page-info">第 {{ currentPage }} 页</p>
          <textarea v-model="annotationText" placeholder="写下你的想法..." rows="4"></textarea>
        </div>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="showAnnotationDialog = false">取消</button>
          <button class="confirm-btn" @click="submitAnnotation">保存</button>
        </div>
      </div>
    </div>

    <!-- 记录列表弹窗 -->
    <div class="confirm-modal" v-if="showAnnotationList" @click.self="showAnnotationList = false">
      <div class="annotation-list-dialog">
        <div class="dialog-header">
          <h3>📋 互动记录</h3>
          <button class="close-btn" @click="showAnnotationList = false">✕</button>
        </div>
        <div class="dialog-body">
          <div class="stats-summary">
            <div class="stat-item">
              <span class="stat-icon">❤️</span>
              <span class="stat-value">{{ bookmarkCount }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">📝</span>
              <span class="stat-value">{{ annotationCount }}</span>
              <span class="stat-label">批注</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">🎤</span>
              <span class="stat-value">{{ voiceRecordCount }}</span>
              <span class="stat-label">录音</span>
            </div>
          </div>
          <div class="annotation-list" v-if="annotations.length > 0">
            <div class="annotation-item" v-for="item in annotations" :key="item.id">
              <div class="item-header">
                <span class="item-page">第{{ item.pageNum }}页</span>
                <span class="item-type">{{ item.annotationType === 1 ? '📝 文字' : '🎤 语音' }}</span>
                <button class="delete-btn" @click="handleDeleteAnnotation(item.id)">🗑️</button>
              </div>
              <!-- 文字批注 -->
              <p v-if="item.annotationType === 1" class="item-content">{{ item.content }}</p>
              <!-- 语音批注 -->
              <div v-else class="audio-player">
                <button class="play-btn" @click="playAudio(item.content)">▶️ 播放录音</button>
              </div>
            </div>
          </div>
          <div class="empty-list" v-else>
            <span>📭</span>
            <p>暂无批注记录</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookById, getBookPages } from '@/api/book'
import { getChildById } from '@/api/child'
import { createReadingLog } from '@/api/readingLog'
import { addBookmark, removeBookmark, checkBookmark } from '@/api/bookmark'
import { addAnnotation, getBookAnnotations, deleteAnnotation } from '@/api/annotation'
// 所有绘本故事内容
const route = useRoute()
const router = useRouter()

const book = ref(null)
const child = ref(null)
const bookPages = ref([])
const startTime = ref(null)
const readingDuration = ref(0)
const currentPage = ref(1)
const totalPages = ref(20)
const pageTurnCount = ref(0)
const pageStayTimes = ref([])
const lastPageTurnTime = ref(null)
const showExitConfirm = ref(false)
const isSaved = ref(false)

// 互动功能状态
const isBookmarked = ref(false)
const bookmarkCount = ref(0)
const annotationCount = ref(0)
const voiceRecordCount = ref(0)
const showAnnotationDialog = ref(false)
const annotationText = ref('')
const annotations = ref([])
const showAnnotationList = ref(false)
const isRecording = ref(false)
const recordingTime = ref(0)
let recordingTimer = null
let mediaRecorder = null
let audioChunks = []

let timer = null

const readingTips = [
  '可以和孩子讨论画面中的细节，培养观察力',
  '试着用不同的声音演绎不同的角色',
  '问问孩子"你觉得接下来会发生什么？"',
  '鼓励孩子描述他们最喜欢的画面',
  '可以暂停一下，让孩子预测故事发展',
  '读完后可以问问孩子学到了什么',
  '让孩子尝试复述故事的主要内容',
  '引导孩子关注角色的情绪变化'
]

const currentTip = ref(readingTips[0])

const formattedDuration = computed(() => {
  const minutes = Math.floor(readingDuration.value / 60)
  const seconds = readingDuration.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

const progressPercent = computed(() => {
  return Math.round((currentPage.value / totalPages.value) * 100)
})

const getPageStyle = computed(() => {
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
    '安全教育': 'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
    'animal': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
  }
  return {
    background: gradients[book.value?.category] || 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)'
  }
})

const currentPageData = computed(() => {
  if (!book.value || bookPages.value.length === 0) {
    return { illustration: '📖✨', text: '正在加载故事内容...' }
  }
  const page = bookPages.value[currentPage.value - 1]
  return page || bookPages.value[0] || { illustration: '📖', text: '加载中...' }
})

const startTimer = () => {
  startTime.value = new Date()
  lastPageTurnTime.value = Date.now()
  timer = setInterval(() => {
    readingDuration.value++
    if (readingDuration.value % 30 === 0) {
      currentTip.value = readingTips[Math.floor(Math.random() * readingTips.length)]
    }
  }, 1000)
}

const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const recordPageStay = () => {
  if (lastPageTurnTime.value) {
    const stayTime = Math.round((Date.now() - lastPageTurnTime.value) / 1000)
    pageStayTimes.value.push(stayTime)
  }
  lastPageTurnTime.value = Date.now()
}

const prevPage = () => {
  if (currentPage.value > 1) {
    recordPageStay()
    currentPage.value--
    pageTurnCount.value++
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    recordPageStay()
    currentPage.value++
    pageTurnCount.value++
  }
}

const confirmExit = () => {
  showExitConfirm.value = true
}

const exitReading = async () => {
  await saveReadingLog(false)
  router.push('/app/reading-logs')
}

const finishReading = async () => {
  recordPageStay()
  await saveReadingLog(true)
  ElMessage.success({ message: '🎉 太棒了！阅读完成！', duration: 2000 })
  router.push('/app/reading-logs')
}

// ============ 互动功能 ============

// 收藏/取消收藏
const toggleBookmark = async () => {
  try {
    if (isBookmarked.value) {
      await removeBookmark(child.value?.id, book.value?.id)
      isBookmarked.value = false
      bookmarkCount.value = Math.max(0, bookmarkCount.value - 1)
      ElMessage.success('已取消收藏')
    } else {
      await addBookmark({
        childId: child.value?.id,
        bookId: book.value?.id,
        bookmarkType: 1,
        note: `第${currentPage.value}页收藏`
      })
      isBookmarked.value = true
      bookmarkCount.value++
      ElMessage.success('❤️ 收藏成功！')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 检查收藏状态
const checkBookmarkStatus = async () => {
  try {
    const res = await checkBookmark(child.value?.id, book.value?.id)
    isBookmarked.value = res.data || false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 打开批注弹窗
const openAnnotationDialog = () => {
  annotationText.value = ''
  showAnnotationDialog.value = true
}

// 提交批注
const submitAnnotation = async () => {
  if (!annotationText.value.trim()) {
    ElMessage.warning('请输入批注内容')
    return
  }
  try {
    await addAnnotation({
      childId: child.value?.id,
      bookId: book.value?.id,
      pageNum: currentPage.value,
      annotationType: 1, // 文字批注
      content: annotationText.value
    })
    annotationCount.value++
    showAnnotationDialog.value = false
    annotationText.value = ''
    ElMessage.success('📝 批注添加成功！')
    loadAnnotations()
  } catch (error) {
    console.error('添加批注失败:', error)
    ElMessage.error('添加失败')
  }
}

// 加载批注列表
const loadAnnotations = async () => {
  try {
    const res = await getBookAnnotations(child.value?.id, book.value?.id)
    annotations.value = res.data || []
    // 统计批注和录音数量
    annotationCount.value = annotations.value.filter(a => a.annotationType === 1).length
    voiceRecordCount.value = annotations.value.filter(a => a.annotationType === 2).length
  } catch (error) {
    console.error('加载批注失败:', error)
  }
}

// 播放录音
const playAudio = (base64Audio) => {
  const audio = new Audio(base64Audio)
  audio.play()
}

// 删除批注
const handleDeleteAnnotation = async (id) => {
  try {
    await deleteAnnotation(id)
    annotationCount.value = Math.max(0, annotationCount.value - 1)
    loadAnnotations()
    ElMessage.success('已删除')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 录音数据存储
const audioRecords = ref([])

// 开始录音
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      audioChunks.push(e.data)
    }

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' })
      // 转为base64存储
      const reader = new FileReader()
      reader.onloadend = async () => {
        const base64Audio = reader.result
        try {
          await addAnnotation({
            childId: child.value?.id,
            bookId: book.value?.id,
            pageNum: currentPage.value,
            annotationType: 2, // 语音批注
            content: base64Audio // 存储base64音频数据
          })
          voiceRecordCount.value++
          ElMessage.success('🎤 语音保存成功！')
          loadAnnotations()
        } catch (error) {
          console.error('保存录音失败:', error)
          ElMessage.error('保存失败')
        }
      }
      reader.readAsDataURL(audioBlob)

      // 停止所有音轨
      stream.getTracks().forEach(track => track.stop())
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0

    recordingTimer = setInterval(() => {
      recordingTime.value++
      // 最长60秒
      if (recordingTime.value >= 60) {
        stopRecording()
      }
    }, 1000)

  } catch (error) {
    console.error('录音失败:', error)
    ElMessage.error('无法启动录音，请检查麦克风权限')
  }
}

// 停止录音
const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) {
      clearInterval(recordingTimer)
      recordingTimer = null
    }
  }
}

// 切换录音状态
const toggleRecording = () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

// 格式化本地时间 (ISO格式)
const formatLocalDateTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

const saveReadingLog = async (isCompleted) => {
  if (isSaved.value) return
  isSaved.value = true

  try {
    const endTime = new Date()
    // 确保 duration 不为负数，如果 startTime 为空或计算异常则使用 readingDuration
    let duration = Math.floor((endTime - startTime.value) / 1000)
    if (!startTime.value || duration < 0) {
      duration = readingDuration.value || 0
    }
    duration = Math.max(0, duration)

    await createReadingLog({
      childId: child.value?.id,
      bookId: book.value?.id,
      startTime: formatLocalDateTime(startTime.value),
      endTime: formatLocalDateTime(endTime),
      duration: duration,
      pageTurnCount: pageTurnCount.value,
      pageStayTimes: pageStayTimes.value,
      isCompleted: isCompleted ? 1 : 0,
      // 添加互动数据
      bookmarkCount: bookmarkCount.value,
      annotationCount: annotationCount.value,
      voiceRecordCount: voiceRecordCount.value
    })
  } catch (error) {
    console.error('保存阅读记录失败:', error)
    isSaved.value = false
  }
}

const loadData = async () => {
  const bookId = route.query.bookId
  const childId = route.query.childId

  if (!bookId || !childId) {
    ElMessage.error('缺少必要参数')
    router.back()
    return
  }

  try {
    const bookRes = await getBookById(bookId)
    if (bookRes.code === 200) {
      book.value = bookRes.data
      totalPages.value = bookRes.data.pageCount || 20
    }

    // 加载绘本页面内容
    const pagesRes = await getBookPages(bookId)
    if (pagesRes.code === 200 && pagesRes.data && pagesRes.data.length > 0) {
      bookPages.value = pagesRes.data
      totalPages.value = pagesRes.data.length
    }

    const childRes = await getChildById(childId)
    if (childRes.code === 200) {
      child.value = childRes.data
    }

    // 加载互动数据
    checkBookmarkStatus()
    loadAnnotations()

    startTimer()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载失败，请重试')
  }
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  stopTimer()
  if (startTime.value) {
    saveReadingLog(false)
  }
})
</script>

<style scoped>
.reading-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fef9f0 0%, #fff5e6 100%);
  display: flex;
  flex-direction: column;
}

.reading-header {
  background: #fff;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
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

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

.reading-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.book-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3436;
}

.child-name {
  font-size: 13px;
  color: #a0937d;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #6d4c41;
}

.reading-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.book-display {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.page-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 0;
}

.current-page {
  width: 100%;
  max-width: 800px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.page-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.illustration-area {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(255, 255, 255, 0.3);
}

.illustration-emoji {
  font-size: 72px;
  letter-spacing: 8px;
  animation: bounce 2s infinite ease-in-out;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-6px) scale(1.03); }
}

.story-area {
  padding: 20px 28px 28px;
  background: linear-gradient(180deg, transparent 0%, rgba(255, 255, 255, 0.85) 20%);
}

.story-text {
  font-size: 18px;
  color: #3a3a3a;
  font-weight: 500;
  line-height: 2;
  text-align: justify;
  letter-spacing: 0.5px;
  margin: 0;
}

.page-indicator-inline {
  font-size: 14px;
  color: #6d4c41;
  font-weight: 600;
}

.page-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 16px 0;
}

.page-btn {
  display: flex;
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

.page-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 底部操作栏 */
.bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
  gap: 12px;
}

/* 互动按钮组 */
.interaction-btns {
  display: flex;
  gap: 8px;
}

.interact-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  background: #fef9f0;
  border: 2px solid #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #6d4c41;
  cursor: pointer;
  transition: all 0.3s ease;
}

.interact-btn:hover {
  background: #ffeaa7;
  border-color: #fdcb6e;
  transform: translateY(-2px);
}

.interact-btn.active {
  background: #ff6b6b;
  border-color: #ff6b6b;
  color: #fff;
}

.interact-btn.recording {
  background: #e17055;
  border-color: #e17055;
  color: #fff;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.interact-btn .btn-text {
  font-size: 12px;
}

.reading-tips-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #6d4c41;
  flex: 1;
  margin-right: 20px;
}

.finish-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.finish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 184, 148, 0.4);
}

.confirm-modal {
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
}

.confirm-content {
  background: #fff;
  padding: 32px;
  border-radius: 20px;
  text-align: center;
  max-width: 400px;
}

.confirm-content h3 {
  font-size: 20px;
  color: #2d3436;
  margin-bottom: 12px;
}

.confirm-content p {
  color: #6d4c41;
  margin-bottom: 24px;
}

.confirm-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.cancel-btn {
  padding: 12px 24px;
  background: #f0e6d3;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #6d4c41;
  cursor: pointer;
}

.confirm-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
}

/* 批注弹窗 */
.annotation-dialog,
.annotation-list-dialog {
  background: #fff;
  border-radius: 20px;
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-bottom: 2px dashed #f0e6d3;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2d3436;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  color: #6d4c41;
}

.dialog-body {
  padding: 20px;
}

.page-info {
  font-size: 13px;
  color: #a0937d;
  margin-bottom: 12px;
}

.dialog-body textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  resize: none;
  font-family: inherit;
}

.dialog-body textarea:focus {
  outline: none;
  border-color: #fdcb6e;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0e6d3;
}

/* 统计摘要 */
.stats-summary {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background: #fef9f0;
  border-radius: 12px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-item .stat-icon {
  font-size: 24px;
}

.stat-item .stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #2d3436;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #a0937d;
}

/* 批注列表 */
.annotation-list {
  max-height: 300px;
  overflow-y: auto;
}

.annotation-item {
  padding: 12px;
  background: #fef9f0;
  border-radius: 10px;
  margin-bottom: 8px;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.item-page {
  font-size: 12px;
  font-weight: 600;
  color: #e17055;
  background: #ffeaa7;
  padding: 2px 8px;
  border-radius: 6px;
}

.item-type {
  font-size: 12px;
  color: #6d4c41;
}

.delete-btn {
  margin-left: auto;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  opacity: 0.6;
}

.delete-btn:hover {
  opacity: 1;
}

.item-content {
  margin: 0;
  font-size: 13px;
  color: #2d3436;
  line-height: 1.6;
}

.empty-list {
  text-align: center;
  padding: 40px;
  color: #a0937d;
}

.empty-list span {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
}

/* 音频播放器 */
.audio-player {
  padding: 8px 0;
}

.play-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.play-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(9, 132, 227, 0.4);
}

@media (max-width: 768px) {
  .reading-header { flex-wrap: wrap; gap: 10px; padding: 12px 16px; }
  .reading-info { order: -1; width: 100%; }
  .book-title { font-size: 16px; }
  .header-stats { flex-wrap: wrap; justify-content: center; }
  .stat-mini { padding: 8px 12px; font-size: 13px; }
  .reading-content { padding: 16px; }
  .illustration-emoji { font-size: 56px; }
  .story-text { font-size: 16px; line-height: 1.9; }
  .page-controls { flex-wrap: wrap; gap: 12px; }
  .page-btn { padding: 10px 18px; font-size: 14px; }
  .bottom-bar { flex-wrap: wrap; gap: 12px; }
  .reading-tips-inline { font-size: 13px; }
  .finish-btn { padding: 12px 24px; font-size: 15px; }
}
</style>
