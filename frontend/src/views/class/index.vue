<template>
  <div class="class-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <span class="header-icon">🏫</span>
        <h2>班级管理</h2>
      </div>
      <el-button type="primary" @click="showAddClassDialog">
        <span>➕</span> 新增班级
      </el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon">📚</div>
        <div class="stat-content">
          <div class="stat-value">{{ classList.length }}</div>
          <div class="stat-label">班级总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👶</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalChildren }}</div>
          <div class="stat-label">儿童总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👩‍🏫</div>
        <div class="stat-content">
          <div class="stat-value">{{ teacherCount }}</div>
          <div class="stat-label">教师数量</div>
        </div>
      </div>
    </div>

    <!-- 班级卡片列表 -->
    <div class="class-grid" v-loading="loading">
      <div v-for="cls in classList" :key="cls.id" class="class-card">
        <div class="card-banner" :style="getBannerStyle(cls.id)">
          <div class="banner-content">
            <span class="class-emoji">🎒</span>
            <h3>{{ cls.className }}</h3>
          </div>
          <div class="card-actions">
            <el-button type="primary" size="small" @click="viewClassDetail(cls)">
              查看详情
            </el-button>
          </div>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="info-icon">🏢</span>
            <span class="info-value">{{ cls.institutionName || '未设置机构' }}</span>
          </div>
          <div class="info-row">
            <span class="info-icon">👶</span>
            <span class="info-value">{{ cls.actualStudentCount || 0 }} 名儿童</span>
          </div>
          <div class="info-row">
            <span class="info-icon">📅</span>
            <span class="info-value">{{ formatDate(cls.createTime) }}</span>
          </div>
          <div class="stats-mini">
            <div class="mini-stat">
              <span class="mini-value">{{ cls.readingCount || 0 }}</span>
              <span class="mini-label">阅读记录</span>
            </div>
            <div class="mini-stat">
              <span class="mini-value">{{ cls.avgFocus || 0 }}</span>
              <span class="mini-label">平均专注度</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="classList.length === 0 && !loading" description="暂无班级数据" />
    </div>

    <!-- 班级详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" :title="selectedClass?.className + ' - 班级详情'" width="800px">
      <div class="detail-content" v-if="selectedClass">
        <div class="detail-header">
          <div class="detail-banner" :style="getBannerStyle(selectedClass.id)">
            <span class="detail-emoji">🎒</span>
            <h2>{{ selectedClass.className }}</h2>
          </div>
        </div>
        <div class="detail-stats">
          <div class="detail-stat-item">
            <span class="detail-stat-value">{{ children.length }}</span>
            <span class="detail-stat-label">儿童数量</span>
          </div>
          <div class="detail-stat-item">
            <span class="detail-stat-value">{{ classStats.totalReadings }}</span>
            <span class="detail-stat-label">阅读记录</span>
          </div>
          <div class="detail-stat-item">
            <span class="detail-stat-value">{{ classStats.avgFocus }}</span>
            <span class="detail-stat-label">平均专注度</span>
          </div>
        </div>
        <div class="children-section">
          <h4>👶 儿童列表</h4>
          <el-table :data="children" stripe v-loading="childrenLoading" max-height="400">
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="age" label="年龄" width="80">
              <template #default="{ row }">
                {{ calculateAge(row.birthDate) }}岁
              </template>
            </el-table-column>
            <el-table-column prop="gender" label="性别" width="70">
              <template #default="{ row }">
                {{ row.gender === 1 ? '男' : '女' }}
              </template>
            </el-table-column>
            <el-table-column prop="readingType" label="阅读类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getReadingTypeTag(row.readingType)" size="small">
                  {{ row.readingType || '未分析' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="focusScore" label="专注度" width="150">
              <template #default="{ row }">
                <el-progress
                  :percentage="Math.round(row.focusScore || 0)"
                  :stroke-width="8"
                  :color="getProgressColor(row.focusScore)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="readCount" label="阅读次数" width="100">
              <template #default="{ row }">
                {{ row.readCount || 0 }}次
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="children.length === 0 && !childrenLoading" description="该班级暂无儿童" />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleDeleteClass">删除班级</el-button>
      </template>
    </el-dialog>

    <!-- 新增班级弹窗 -->
    <el-dialog v-model="classDialogVisible" title="新增班级" width="450px">
      <el-form ref="classFormRef" :model="classForm" :rules="classRules" label-width="90px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="classForm.className" placeholder="请输入班级名称">
            <template #prefix><span>🎒</span></template>
          </el-input>
        </el-form-item>
        <el-form-item label="机构名称" prop="institutionName">
          <el-input v-model="classForm.institutionName" placeholder="请输入机构名称">
            <template #prefix><span>🏢</span></template>
          </el-input>
        </el-form-item>
        <el-form-item label="班级描述" prop="description">
          <el-input v-model="classForm.description" type="textarea" :rows="3" placeholder="请输入班级描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddClass" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClassList, createClass, deleteClass, getClassChildren } from '@/api/classInfo'

const classList = ref([])
const selectedClass = ref(null)
const children = ref([])
const classDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const classFormRef = ref()
const loading = ref(false)
const childrenLoading = ref(false)
const submitting = ref(false)

const classForm = reactive({
  className: '',
  institutionName: '',
  description: ''
})

const classRules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }]
}

// 统计数据
const totalChildren = computed(() => {
  return classList.value.reduce((sum, cls) => sum + (cls.actualStudentCount || 0), 0)
})

const teacherCount = computed(() => {
  const teacherIds = new Set(classList.value.map(cls => cls.teacherId).filter(Boolean))
  return teacherIds.size
})

// 班级统计
const classStats = ref({
  totalReadings: 0,
  avgFocus: 0
})

const getBannerStyle = (id) => {
  const gradients = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  ]
  return { background: gradients[id % gradients.length] }
}

const getReadingTypeTag = (type) => {
  const types = { '专注型': 'success', '跳跃型': 'warning', '兴趣导向型': 'primary' }
  return types[type] || 'info'
}

const getProgressColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#409eff'
  if (score >= 40) return '#e6a23c'
  return '#f56c6c'
}

const formatDate = (date) => {
  if (!date) return '-'
  if (typeof date === 'string') return date.substring(0, 10)
  return date
}

const calculateAge = (birthDate) => {
  if (!birthDate) return '-'
  const birth = new Date(birthDate)
  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return age > 0 ? age : '-'
}

const loadClassList = async () => {
  loading.value = true
  try {
    const res = await getClassList({ current: 1, size: 100 })
    classList.value = res.data?.records || []

    // 加载每个班级的实际儿童数量
    for (const cls of classList.value) {
      try {
        const childrenRes = await getClassChildren(cls.id)
        cls.actualStudentCount = (childrenRes.data || []).length

        // 统计阅读次数
        const childList = childrenRes.data || []
        cls.readingCount = childList.reduce((sum, c) => sum + (c.readCount || 0), 0)
        const focusScores = childList.filter(c => c.focusScore).map(c => c.focusScore)
        cls.avgFocus = focusScores.length > 0
          ? Math.round(focusScores.reduce((a, b) => a + b, 0) / focusScores.length)
          : 0
      } catch (e) {
        cls.actualStudentCount = 0
        cls.readingCount = 0
        cls.avgFocus = 0
      }
    }
  } catch (error) {
    console.error('Load class list error:', error)
  } finally {
    loading.value = false
  }
}

const viewClassDetail = async (cls) => {
  selectedClass.value = cls
  detailDialogVisible.value = true
  childrenLoading.value = true
  try {
    const res = await getClassChildren(cls.id)
    children.value = res.data || []

    // 计算班级统计
    classStats.value.totalReadings = children.value.reduce((sum, c) => sum + (c.readCount || 0), 0)
    const focusScores = children.value.filter(c => c.focusScore).map(c => c.focusScore)
    classStats.value.avgFocus = focusScores.length > 0
      ? Math.round(focusScores.reduce((a, b) => a + b, 0) / focusScores.length)
      : 0
  } catch (error) {
    console.error('Load children error:', error)
    children.value = []
  } finally {
    childrenLoading.value = false
  }
}

const showAddClassDialog = () => {
  classForm.className = ''
  classForm.institutionName = ''
  classForm.description = ''
  classDialogVisible.value = true
}

const handleAddClass = async () => {
  const valid = await classFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createClass(classForm)
    ElMessage.success('班级创建成功')
    classDialogVisible.value = false
    loadClassList()
  } catch (error) {
    console.error('Create class error:', error)
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const handleDeleteClass = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该班级吗？删除后无法恢复。', '提示', { type: 'warning' })
    await deleteClass(selectedClass.value.id)
    ElMessage.success('删除成功')
    detailDialogVisible.value = false
    selectedClass.value = null
    children.value = []
    loadClassList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete class error:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadClassList()
})
</script>

<style scoped>
.class-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fef9f3 0%, #fff5eb 100%);
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 32px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #2d3436;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 3px solid #f0e6d3;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 36px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #2d3436;
}

.stat-label {
  font-size: 13px;
  color: #a0937d;
  font-weight: 600;
}

/* 班级卡片网格 */
.class-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.class-card {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.class-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.card-banner {
  padding: 24px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.class-emoji {
  font-size: 32px;
}

.card-banner h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.card-body {
  padding: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #6d4c41;
}

.info-icon {
  font-size: 18px;
}

.stats-mini {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px dashed #f0e6d3;
}

.mini-stat {
  text-align: center;
}

.mini-value {
  font-size: 22px;
  font-weight: 800;
  color: #e17055;
}

.mini-label {
  font-size: 12px;
  color: #a0937d;
}

/* 详情弹窗 */
.detail-banner {
  padding: 30px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 16px;
  border-radius: 16px;
  margin-bottom: 24px;
}

.detail-emoji {
  font-size: 48px;
}

.detail-banner h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.detail-stat-item {
  background: #fef9f0;
  padding: 16px;
  border-radius: 12px;
  text-align: center;
}

.detail-stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #e17055;
}

.detail-stat-label {
  font-size: 13px;
  color: #a0937d;
}

.children-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #2d3436;
}

/* 响应式 */
@media (max-width: 1200px) {
  .class-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .class-grid {
    grid-template-columns: 1fr;
  }
}
</style>
