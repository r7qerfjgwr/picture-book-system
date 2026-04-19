<template>
  <div class="reading-logs-page">
    <div class="page-header">
      <h2>阅读记录</h2>
      <div class="header-actions">
        <el-button type="success" @click="exportLogs">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
        <el-button type="primary" @click="showRecordDialog">
          <el-icon><Plus /></el-icon>
          记录阅读
        </el-button>
      </div>
    </div>

    <el-card>
      <template #header>
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="儿童" v-if="!userStore.isParent">
            <el-select v-model="searchForm.childId" placeholder="选择儿童" clearable>
              <el-option v-for="child in childList" :key="child.id" :label="child.name" :value="child.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <el-table :data="logs" stripe v-loading="loading">
        <el-table-column prop="childName" label="儿童姓名" width="100">
          <template #default="{ row }">
            {{ row.childName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="bookTitle" label="绘本名称" min-width="150">
          <template #default="{ row }">
            {{ row.bookTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="阅读时长" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="pageTurnCount" label="翻页次数" width="100">
          <template #default="{ row }">
            {{ row.pageTurnCount || 0 }}次
          </template>
        </el-table-column>
        <el-table-column prop="focusScore" label="专注度" width="140">
          <template #default="{ row }">
            <el-progress
              :percentage="Number(row.focusScore) || 0"
              :stroke-width="8"
              :color="getFocusColor(row.focusScore)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="isCompleted" label="是否完成" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isCompleted === 1 ? 'success' : 'warning'">
              {{ row.isCompleted === 1 ? '完成' : '未完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.isCompleted !== 1" type="primary" size="small" @click="completeReading(row)">
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </el-card>

    <!-- 记录阅读弹窗 -->
    <el-dialog v-model="recordDialogVisible" title="记录阅读" width="500px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="选择儿童" required>
          <el-select v-model="recordForm.childId" placeholder="请选择儿童" style="width: 100%">
            <el-option v-for="child in childList" :key="child.id" :label="child.name" :value="child.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择绘本" required>
          <el-select v-model="recordForm.bookId" placeholder="请选择绘本" filterable style="width: 100%">
            <el-option v-for="book in bookList" :key="book.id" :label="book.title" :value="book.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="阅读时长" required>
          <el-input-number v-model="recordForm.duration" :min="1" :max="180" placeholder="分钟" style="width: 100%" />
          <span style="margin-left: 10px;">分钟</span>
        </el-form-item>
        <el-form-item label="翻页次数">
          <el-input-number v-model="recordForm.pageTurnCount" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否完成">
          <el-switch v-model="recordForm.isCompleted" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.note" type="textarea" :rows="2" placeholder="记录阅读心得..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getReadingLogList, createReadingLog, updateReadingLog, getReadingLogById } from '@/api/readingLog'
import { getMyChildren } from '@/api/child'
import { getBookList } from '@/api/book'
import { ElMessage } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const logs = ref([])
const childList = ref([])
const bookList = ref([])
const recordDialogVisible = ref(false)
const submitting = ref(false)

const searchForm = reactive({
  childId: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const recordForm = reactive({
  childId: null,
  bookId: null,
  duration: 10,
  pageTurnCount: 20,
  isCompleted: true,
  note: ''
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 16)
  }
  return time
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '0秒'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (minutes > 0) {
    return `${minutes}分${secs}秒`
  }
  return `${secs}秒`
}

// 获取专注度颜色
const getFocusColor = (score) => {
  const s = Number(score) || 0
  if (s >= 80) return '#67c23a'
  if (s >= 60) return '#409eff'
  if (s >= 40) return '#e6a23c'
  return '#f56c6c'
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.page,
      size: pagination.size
    }

    if (searchForm.childId) {
      params.childId = searchForm.childId
    }

    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const res = await getReadingLogList(params)
    if (res.code === 200) {
      logs.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    }
  } catch (error) {
    console.error('Load logs error:', error)
    ElMessage.error('加载阅读记录失败')
  } finally {
    loading.value = false
  }
}

const loadBooks = async () => {
  try {
    const res = await getBookList({ current: 1, size: 100 })
    if (res.code === 200) {
      bookList.value = res.data?.records || []
    }
  } catch (error) {
    console.error('Load books error:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadLogs()
}

const resetSearch = () => {
  searchForm.childId = ''
  searchForm.dateRange = []
  handleSearch()
}

const showRecordDialog = () => {
  // 重置表单
  recordForm.childId = childList.value.length > 0 ? childList.value[0].id : null
  recordForm.bookId = null
  recordForm.duration = 10
  recordForm.pageTurnCount = 20
  recordForm.isCompleted = true
  recordForm.note = ''
  recordDialogVisible.value = true
}

const submitRecord = async () => {
  if (!recordForm.childId) {
    ElMessage.warning('请选择儿童')
    return
  }
  if (!recordForm.bookId) {
    ElMessage.warning('请选择绘本')
    return
  }

  submitting.value = true
  try {
    const now = new Date()
    const startTime = new Date(now.getTime() - recordForm.duration * 60 * 1000)

    await createReadingLog({
      childId: recordForm.childId,
      bookId: recordForm.bookId,
      startTime: startTime.toISOString(),
      endTime: now.toISOString(),
      pageTurnCount: recordForm.pageTurnCount,
      isCompleted: recordForm.isCompleted ? 1 : 0
    })

    ElMessage.success('阅读记录提交成功！')
    recordDialogVisible.value = false
    loadLogs()
  } catch (error) {
    console.error('Submit record error:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 完成阅读
const completeReading = async (row) => {
  try {
    const now = new Date()
    const startTime = new Date(row.startTime)

    await updateReadingLog(row.id, {
      endTime: now.toISOString(),
      isCompleted: 1
    })

    ElMessage.success('已标记为完成！')
    loadLogs()
  } catch (error) {
    console.error('Complete reading error:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 导出阅读记录为Excel
const exportLogs = () => {
  if (!logs.value || logs.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  // 生成CSV内容
  const headers = ['儿童姓名', '绘本名称', '绘本分类', '开始时间', '结束时间', '阅读时长(秒)', '翻页次数', '专注度', '是否完成']
  const rows = logs.value.map(log => [
    log.childName || '-',
    log.bookTitle || '-',
    log.bookCategory || '-',
    formatTime(log.startTime),
    formatTime(log.endTime),
    log.duration || 0,
    log.pageTurnCount || 0,
    (log.focusScore || 0).toFixed(1),
    log.isCompleted === 1 ? '完成' : '未完成'
  ])

  // 添加BOM头以支持中文
  let csvContent = '\uFEFF' + headers.join(',') + '\n'
  rows.forEach(row => {
    csvContent += row.join(',') + '\n'
  })

  // 创建Blob并下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  const dateStr = new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')
  link.download = `阅读记录_${dateStr}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}

onMounted(async () => {
  try {
    // 加载孩子列表
    const res = await getMyChildren()
    childList.value = res.data || []

    // 家长自动选择第一个孩子
    if (userStore.isParent && childList.value.length > 0) {
      searchForm.childId = childList.value[0].id
    }

    // 加载绘本列表
    await loadBooks()
  } catch (error) {
    console.error('Load children error:', error)
  }

  loadLogs()
})
</script>

<style scoped>
.reading-logs-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
