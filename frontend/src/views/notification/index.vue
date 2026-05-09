<template>
  <div class="notification-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <span class="header-icon">📢</span>
        <h2>通知发送</h2>
      </div>
    </div>

    <!-- 发送表单 -->
    <div class="send-form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="接收对象" prop="receiverType">
          <el-radio-group v-model="form.receiverType" @change="handleReceiverTypeChange">
            <el-radio value="all">所有用户</el-radio>
            <el-radio value="role">指定角色</el-radio>
            <el-radio value="user">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.receiverType === 'role'" label="选择角色" prop="roleKey">
          <el-select v-model="form.roleKey" placeholder="请选择角色" style="width: 200px">
            <el-option label="家长" value="PARENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.receiverType === 'user'" label="选择用户" prop="userId">
          <el-select
            v-model="form.userId"
            filterable
            placeholder="请搜索并选择用户"
            style="width: 300px"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.realName || user.username} (${getRoleName(user.roleKey)})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="通知类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择通知类型" style="width: 200px">
            <el-option label="📢 系统通知" value="system" />
            <el-option label="🎉 活动通知" value="activity" />
            <el-option label="⏰ 提醒通知" value="reminder" />
          </el-select>
        </el-form-item>

        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请输入通知内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSend" :loading="sending">
            <span class="btn-icon">📨</span>
            发送通知
          </el-button>
          <el-button @click="handleReset">
            <span class="btn-icon">🔄</span>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 预览区域 -->
    <div class="preview-card">
      <div class="preview-header">
        <span class="preview-icon">👁️</span>
        <span>通知预览</span>
      </div>
      <div class="preview-content">
        <div class="preview-notification">
          <div class="preview-icon-badge">{{ getTypeIcon(form.type) }}</div>
          <div class="preview-body">
            <div class="preview-title">{{ form.title || '通知标题' }}</div>
            <div class="preview-text">{{ form.content || '通知内容将显示在这里...' }}</div>
            <div class="preview-time">刚刚</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发送记录提示 -->
    <div class="tips-card">
      <div class="tips-header">
        <span class="tips-icon">💡</span>
        <span>温馨提示</span>
      </div>
      <ul class="tips-list">
        <li>系统通知：用于重要系统公告、功能更新等</li>
        <li>活动通知：用于活动推广、节日祝福等</li>
        <li>提醒通知：用于阅读提醒、任务提醒等</li>
        <li>发送后通知将实时推送到用户的通知中心</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sendNotification, getUsersForNotification } from '@/api/notification'

const formRef = ref()
const userList = ref([])
const sending = ref(false)

const form = reactive({
  receiverType: 'all',
  roleKey: '',
  userId: null,
  type: 'system',
  title: '',
  content: ''
})

const rules = {
  receiverType: [{ required: true, message: '请选择接收对象', trigger: 'change' }],
  roleKey: [{ required: true, message: '请选择角色', trigger: 'change' }],
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

const getRoleName = (roleKey) => {
  const names = { ADMIN: '管理员', TEACHER: '教师', PARENT: '家长' }
  return names[roleKey] || '用户'
}

const getTypeIcon = (type) => {
  const icons = { system: '📢', activity: '🎉', reminder: '⏰' }
  return icons[type] || '📌'
}

const handleReceiverTypeChange = () => {
  form.roleKey = ''
  form.userId = null
}

const loadUsers = async () => {
  try {
    const res = await getUsersForNotification()
    if (res.code === 200) {
      userList.value = res.data || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const handleSend = async () => {
  try {
    await formRef.value.validate()

    const receiverText = {
      all: '所有用户',
      role: getRoleName(form.roleKey),
      user: userList.value.find(u => u.id === form.userId)?.realName || '该用户'
    }

    await ElMessageBox.confirm(
      `确定要发送通知给"${receiverText[form.receiverType]}"吗？`,
      '确认发送',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )

    sending.value = true
    const res = await sendNotification(form)

    if (res.code === 200) {
      ElMessage.success(`通知发送成功！共发送 ${res.data.sentCount} 条`)
      handleReset()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发送通知失败:', error)
      ElMessage.error('发送失败，请重试')
    }
  } finally {
    sending.value = false
  }
}

const handleReset = () => {
  formRef.value?.resetFields()
  form.receiverType = 'all'
  form.roleKey = ''
  form.userId = null
  form.type = 'system'
  form.title = ''
  form.content = ''
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.notification-page {
  padding: 20px;
}

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
  font-size: 28px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2d3436;
}

.send-form-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  border: 2px solid #f0e6d3;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.btn-icon {
  margin-right: 6px;
}

.preview-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  border: 2px solid #f0e6d3;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px dashed #f0e6d3;
  font-weight: 700;
  color: #2d3436;
}

.preview-icon {
  font-size: 20px;
}

.preview-content {
  background: linear-gradient(135deg, #fef9f3 0%, #fff5e6 100%);
  border-radius: 12px;
  padding: 20px;
}

.preview-notification {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 2px solid #ffeaa7;
}

.preview-icon-badge {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.preview-body {
  flex: 1;
}

.preview-title {
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
  margin-bottom: 8px;
}

.preview-text {
  font-size: 14px;
  color: #636e72;
  line-height: 1.6;
  margin-bottom: 8px;
}

.preview-time {
  font-size: 12px;
  color: #b2bec3;
}

.tips-card {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border-radius: 16px;
  padding: 20px;
  border: 2px solid #ffeaa7;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 700;
  color: #e17055;
}

.tips-icon {
  font-size: 20px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #6d4c41;
  font-size: 14px;
  line-height: 1.8;
}
</style>
