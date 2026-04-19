<template>
  <div class="class-page">
    <div class="page-header">
      <h2>班级管理</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>班级列表</span>
              <el-button type="primary" size="small" @click="showAddClassDialog">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>

          <div class="class-list" v-loading="loading">
            <div
              v-for="cls in classList"
              :key="cls.id"
              :class="['class-item', { active: selectedClass?.id === cls.id }]"
              @click="selectClass(cls)"
            >
              <div class="class-name">{{ cls.className }}</div>
              <div class="class-info">
                {{ cls.institutionName || '未设置机构' }} | 儿童数：{{ cls.studentCount || 0 }}
              </div>
            </div>
            <el-empty v-if="classList.length === 0" description="暂无班级数据" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card v-if="selectedClass">
          <template #header>
            <div class="card-header">
              <span>{{ selectedClass.className }} - 儿童列表</span>
              <el-button type="danger" size="small" @click="handleDeleteClass">删除班级</el-button>
            </div>
          </template>

          <el-table :data="children" stripe v-loading="childrenLoading">
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="birthDate" label="出生日期" width="120" />
            <el-table-column prop="gender" label="性别" width="80">
              <template #default="{ row }">
                {{ row.gender === 1 ? '男' : '女' }}
              </template>
            </el-table-column>
            <el-table-column prop="readingType" label="阅读类型">
              <template #default="{ row }">
                <el-tag :type="getReadingTypeTag(row.readingType)" size="small">
                  {{ row.readingType || '未分析' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="focusScore" label="专注度" width="120">
              <template #default="{ row }">
                <el-progress :percentage="row.focusScore || 0" :stroke-width="8" />
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="children.length === 0 && !childrenLoading" description="该班级暂无儿童" />
        </el-card>

        <el-card v-else>
          <el-empty description="请选择班级" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="classDialogVisible" title="新增班级" width="400px">
      <el-form ref="classFormRef" :model="classForm" :rules="classRules" label-width="80px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="classForm.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="机构名称" prop="institutionName">
          <el-input v-model="classForm.institutionName" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="班级描述" prop="description">
          <el-input v-model="classForm.description" type="textarea" placeholder="请输入班级描述" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClassList, createClass, deleteClass, getClassChildren } from '@/api/classInfo'

const classList = ref([])
const selectedClass = ref(null)
const children = ref([])
const classDialogVisible = ref(false)
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

const getReadingTypeTag = (type) => {
  const types = { '专注型': 'success', '跳跃型': 'warning', '兴趣导向型': 'primary' }
  return types[type] || 'info'
}

const loadClassList = async () => {
  loading.value = true
  try {
    const res = await getClassList({ current: 1, size: 100 })
    classList.value = res.data?.records || []
  } catch (error) {
    console.error('Load class list error:', error)
  } finally {
    loading.value = false
  }
}

const selectClass = async (cls) => {
  selectedClass.value = cls
  childrenLoading.value = true
  try {
    const res = await getClassChildren(cls.id)
    children.value = res.data || []
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
  } finally {
    submitting.value = false
  }
}

const handleDeleteClass = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该班级吗？', '提示', { type: 'warning' })
    await deleteClass(selectedClass.value.id)
    ElMessage.success('删除成功')
    selectedClass.value = null
    children.value = []
    loadClassList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete class error:', error)
    }
  }
}

onMounted(() => {
  loadClassList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.class-list {
  max-height: 500px;
  overflow-y: auto;
}

.class-item {
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}

.class-item:hover {
  background: #f5f7fa;
}

.class-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.class-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.class-info {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
