<template>
  <div class="profile-page">
    <!-- 顶部横幅 -->
    <div class="profile-banner">
      <div class="banner-content">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <span class="avatar-emoji">{{ roleEmoji }}</span>
          </div>
          <div class="user-basic">
            <h1>{{ userInfo.realName || '用户' }}</h1>
            <div class="role-tag" :class="roleClass">
              <span>{{ getRoleLabel(userInfo.roleKey) }}</span>
            </div>
          </div>
        </div>
        <div class="banner-deco">
          <span class="deco-emoji">🌟</span>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <!-- 左侧信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <span class="header-icon">📋</span>
          <span class="header-title">基本信息</span>
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="info-icon">👤</span>
            <div class="info-content">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userInfo.username || '-' }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="info-icon">📱</span>
            <div class="info-content">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="info-icon">📧</span>
            <div class="info-content">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userInfo.email || '未设置' }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="info-icon">📅</span>
            <div class="info-content">
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatDate(userInfo.lastLoginTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧编辑区域 -->
      <div class="edit-section">
        <!-- 修改个人信息 -->
        <div class="edit-card">
          <div class="card-header">
            <span class="header-icon">✏️</span>
            <span class="header-title">修改信息</span>
          </div>
          <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-position="top" class="edit-form">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="infoForm.realName" placeholder="请输入真实姓名">
                <template #prefix>
                  <span class="input-icon">👤</span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="infoForm.phone" placeholder="请输入手机号">
                <template #prefix>
                  <span class="input-icon">📱</span>
                </template>
              </el-input>
            </el-form-item>
            <el-button type="primary" class="save-btn" @click="updateInfo">
              <span>💾</span> 保存修改
            </el-button>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div class="edit-card">
          <div class="card-header">
            <span class="header-icon">🔐</span>
            <span class="header-title">修改密码</span>
          </div>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top" class="edit-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password>
                <template #prefix>
                  <span class="input-icon">🔑</span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password>
                <template #prefix>
                  <span class="input-icon">🔒</span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password>
                <template #prefix>
                  <span class="input-icon">🔐</span>
                </template>
              </el-input>
            </el-form-item>
            <el-button type="primary" class="save-btn" @click="updatePassword">
              <span>🔄</span> 修改密码
            </el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updatePassword as updatePasswordApi } from '@/api/auth'

const userStore = useUserStore()

const userInfo = ref({})
const infoFormRef = ref()
const passwordFormRef = ref()

const infoForm = reactive({
  realName: '',
  phone: ''
})

const infoRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const roleEmoji = computed(() => {
  const emojis = { ADMIN: '👑', TEACHER: '👩‍🏫', PARENT: '👨‍👩‍👧' }
  return emojis[userInfo.value.roleKey] || '👤'
})

const roleClass = computed(() => {
  const classes = { ADMIN: 'role-admin', TEACHER: 'role-teacher', PARENT: 'role-parent' }
  return classes[userInfo.value.roleKey] || ''
})

const getRoleLabel = (role) => {
  const labels = { ADMIN: '管理员', TEACHER: '教师', PARENT: '家长' }
  return labels[role] || role
}

const formatDate = (date) => {
  if (!date) return '-'
  if (typeof date === 'string') return date.substring(0, 10)
  return date
}

const loadUserInfo = () => {
  userInfo.value = userStore.userInfo
  infoForm.realName = userInfo.value.realName
  infoForm.phone = userInfo.value.phone
}

const updateInfo = async () => {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return

  ElMessage.success('信息更新成功')
  userStore.getUserInfo()
}

const updatePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await updatePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordFormRef.value.resetFields()
  } catch (error) {
    console.error('Update password error:', error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fef9f3 0%, #fff5eb 100%);
}

/* 顶部横幅 */
.profile-banner {
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  border-radius: 0 0 30px 30px;
  padding: 32px 40px;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(0, 184, 148, 0.2);
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-wrapper {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.avatar-emoji {
  font-size: 40px;
}

.user-basic h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #fff;
}

.role-tag {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  margin-top: 8px;
}

.role-admin {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  color: #fff;
}

.role-teacher {
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  color: #fff;
}

.role-parent {
  background: rgba(255, 255, 255, 0.9);
  color: #00b894;
}

.deco-emoji {
  font-size: 64px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-10px) rotate(5deg); }
}

/* 内容区域 */
.profile-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 24px;
  padding: 0 20px;
}

/* 信息卡片 */
.info-card, .edit-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px dashed #f0e6d3;
}

.header-icon {
  font-size: 24px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3436;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fef9f0;
  border-radius: 14px;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.2);
}

.info-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-content {
  flex: 1;
}

.info-label {
  display: block;
  font-size: 12px;
  color: #a0937d;
  margin-bottom: 4px;
}

.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #2d3436;
}

/* 编辑区域 */
.edit-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.edit-form {
  margin-top: 10px;
}

.edit-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #6d4c41;
}

.edit-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: none;
  border: 2px solid #f0e6d3;
}

.edit-form :deep(.el-input__wrapper:focus-within) {
  border-color: #00b894;
}

.input-icon {
  margin-right: 8px;
}

.save-btn {
  width: 100%;
  padding: 14px;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  border: none;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 184, 148, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .banner-content {
    flex-direction: column;
    text-align: center;
  }

  .avatar-section {
    flex-direction: column;
  }
}
</style>
