<template>
  <div class="storybook-login">
    <!-- 装饰性背景元素 -->
    <div class="decorations">
      <div class="cloud cloud-1"></div>
      <div class="cloud cloud-2"></div>
      <div class="cloud cloud-3"></div>
      <div class="star star-1">✦</div>
      <div class="star star-2">✦</div>
      <div class="star star-3">✦</div>
      <div class="star star-4">✦</div>
      <div class="star star-5">✧</div>
      <div class="floating-book book-1">📚</div>
      <div class="floating-book book-2">📖</div>
      <div class="floating-book book-3">📕</div>
      <div class="floating-book book-4">📗</div>
    </div>

    <!-- 左侧插画区域 -->
    <div class="illustration-panel">
      <div class="illustration-content">
        <div class="moon">🌙</div>
        <div class="owl">🦉</div>
        <div class="tree">🌳</div>
        <div class="grass">
          <span>🌱</span><span>🌿</span><span>🌱</span><span>🌿</span><span>🌱</span>
        </div>
        <div class="welcome-text">
          <h2>开启阅读之旅</h2>
          <p>记录孩子的每一个成长瞬间</p>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-panel">
      <div class="login-card">
        <!-- Logo区域 -->
        <div class="logo-section">
          <div class="logo-icon">
            <span class="book-stack">
              <span class="book-icon">📖</span>
            </span>
          </div>
          <h1 class="system-title">儿童绘本阅读系统</h1>
          <p class="system-subtitle">阅读行为分析与成长跟踪平台</p>
        </div>

        <!-- 登录表单 -->
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="storybook-form">
          <el-form-item prop="username">
            <div class="input-wrapper">
              <span class="input-icon">👤</span>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                class="storybook-input"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <span class="input-icon">🔐</span>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                class="storybook-input"
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <div class="form-extras">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.remember" />
              <span class="checkmark"></span>
              <span class="label-text">记住密码</span>
            </label>
          </div>

          <el-form-item>
            <button
              type="button"
              class="magic-login-btn"
              :class="{ loading: loading }"
              :disabled="loading"
              @click="handleLogin"
            >
              <span class="btn-content">
                <span class="btn-icon">✨</span>
                <span class="btn-text">{{ loading ? '登录中...' : '开始阅读冒险' }}</span>
                <span class="btn-arrow">→</span>
              </span>
              <div class="btn-sparkles">
                <span></span><span></span><span></span>
              </div>
            </button>
          </el-form-item>
        </el-form>

        <!-- 测试账号提示 -->
        <div class="test-accounts">
          <div class="accounts-header">
            <span class="header-icon">🎭</span>
            <span>体验账号</span>
          </div>
          <div class="accounts-list">
            <div class="account-item" @click="fillAccount('admin', 'admin123')">
              <span class="role-icon">👨‍💼</span>
              <span class="role-name">管理员</span>
            </div>
            <div class="account-item" @click="fillAccount('teacher', 'teacher123')">
              <span class="role-icon">👩‍🏫</span>
              <span class="role-name">教师</span>
            </div>
            <div class="account-item" @click="fillAccount('parent', 'parent123')">
              <span class="role-icon">👨‍👩‍👧</span>
              <span class="role-name">家长</span>
            </div>
          </div>
        </div>

        <!-- 底部装饰 -->
        <div class="card-footer">
          <div class="footer-deco">
            <span>🌸</span>
            <span>☀️</span>
            <span>🌸</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权信息 -->
    <div class="footer-info">
      <p>🎓 毕业设计作品 · 儿童绘本阅读行为分析与成长跟踪系统</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  const savedUsername = localStorage.getItem('savedUsername')
  const savedPassword = localStorage.getItem('savedPassword')
  if (savedUsername && savedPassword) {
    loginForm.username = savedUsername
    loginForm.password = savedPassword
    loginForm.remember = true
  }
})

const fillAccount = (username, password) => {
  loginForm.username = username
  loginForm.password = password
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm)

    if (loginForm.remember) {
      localStorage.setItem('savedUsername', loginForm.username)
      localStorage.setItem('savedPassword', loginForm.password)
    } else {
      localStorage.removeItem('savedUsername')
      localStorage.removeItem('savedPassword')
    }

    ElMessage({
      message: '🎉 欢迎回来！',
      type: 'success',
      customClass: 'storybook-message'
    })

    router.push('/app/dashboard')
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>


/* 主容器 */
.storybook-login {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, sans-serif;
  background: linear-gradient(135deg, #fef6e4 0%, #f8e8d6 50%, #ffeaa7 100%);
}

/* 背景装饰 */
.decorations {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.cloud {
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 100px;
  animation: floatCloud 20s infinite ease-in-out;
}

.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
}

.cloud-1 {
  width: 120px;
  height: 40px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.cloud-1::before {
  width: 50px;
  height: 50px;
  top: -25px;
  left: 20px;
}

.cloud-1::after {
  width: 60px;
  height: 60px;
  top: -35px;
  left: 50px;
}

.cloud-2 {
  width: 100px;
  height: 35px;
  top: 25%;
  right: 15%;
  animation-delay: -5s;
}

.cloud-2::before {
  width: 40px;
  height: 40px;
  top: -20px;
  left: 15px;
}

.cloud-2::after {
  width: 50px;
  height: 50px;
  top: -30px;
  left: 40px;
}

.cloud-3 {
  width: 80px;
  height: 30px;
  top: 60%;
  left: 5%;
  animation-delay: -10s;
}

.cloud-3::before {
  width: 35px;
  height: 35px;
  top: -18px;
  left: 10px;
}

.cloud-3::after {
  width: 40px;
  height: 40px;
  top: -22px;
  left: 30px;
}

@keyframes floatCloud {
  0%, 100% { transform: translateX(0) translateY(0); }
  25% { transform: translateX(30px) translateY(-10px); }
  50% { transform: translateX(20px) translateY(5px); }
  75% { transform: translateX(-10px) translateY(-5px); }
}

.star {
  position: absolute;
  font-size: 24px;
  color: #ffd93d;
  animation: twinkle 3s infinite ease-in-out;
  text-shadow: 0 0 10px rgba(255, 217, 61, 0.5);
}

.star-1 { top: 8%; left: 25%; animation-delay: 0s; }
.star-2 { top: 15%; right: 30%; animation-delay: 0.5s; }
.star-3 { top: 40%; left: 8%; animation-delay: 1s; }
.star-4 { top: 70%; right: 10%; animation-delay: 1.5s; }
.star-5 { top: 85%; left: 30%; animation-delay: 2s; }

@keyframes twinkle {
  0%, 100% { opacity: 1; transform: scale(1) rotate(0deg); }
  50% { opacity: 0.5; transform: scale(0.8) rotate(180deg); }
}

.floating-book {
  position: absolute;
  font-size: 32px;
  animation: floatBook 6s infinite ease-in-out;
}

.book-1 { top: 12%; right: 20%; animation-delay: 0s; }
.book-2 { top: 35%; left: 15%; animation-delay: 1.5s; }
.book-3 { bottom: 25%; right: 8%; animation-delay: 3s; }
.book-4 { bottom: 15%; left: 25%; animation-delay: 4.5s; }

@keyframes floatBook {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

/* 左侧插画区域 */
.illustration-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  padding: 40px;
}

.illustration-content {
  position: relative;
  text-align: center;
}

.moon {
  font-size: 80px;
  animation: moonGlow 4s infinite ease-in-out;
  filter: drop-shadow(0 0 20px rgba(255, 217, 61, 0.4));
}

@keyframes moonGlow {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.owl {
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 48px;
  animation: owlBlink 4s infinite;
}

@keyframes owlBlink {
  0%, 90%, 100% { transform: scaleY(1); }
  95% { transform: scaleY(0.1); }
}

.tree {
  font-size: 100px;
  margin-top: -20px;
  filter: drop-shadow(0 10px 20px rgba(0, 0, 0, 0.1));
}

.grass {
  display: flex;
  justify-content: center;
  gap: 8px;
  font-size: 32px;
  margin-top: -15px;
}

.grass span {
  animation: grassWave 2s infinite ease-in-out;
}

.grass span:nth-child(1) { animation-delay: 0s; }
.grass span:nth-child(2) { animation-delay: 0.2s; }
.grass span:nth-child(3) { animation-delay: 0.4s; }
.grass span:nth-child(4) { animation-delay: 0.6s; }
.grass span:nth-child(5) { animation-delay: 0.8s; }

@keyframes grassWave {
  0%, 100% { transform: rotate(-5deg); }
  50% { transform: rotate(5deg); }
}

.welcome-text {
  margin-top: 30px;
}

.welcome-text h2 {
  font-size: 32px;
  font-weight: 800;
  color: #e17055;
  text-shadow: 2px 2px 0 rgba(255, 255, 255, 0.8);
  margin-bottom: 10px;
}

.welcome-text p {
  font-size: 18px;
  color: #74b9ff;
  font-weight: 600;
}

/* 右侧登录面板 */
.login-panel {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 2;
}

.login-card {
  width: 100%;
  background: linear-gradient(145deg, #ffffff 0%, #fff9f0 100%);
  border-radius: 32px;
  padding: 48px 40px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.08),
    0 8px 25px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 3px solid #ffeaa7;
  position: relative;
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 217, 61, 0.1) 0%, transparent 70%);
  pointer-events: none;
}

/* Logo区域 */
.logo-section {
  text-align: center;
  margin-bottom: 36px;
  position: relative;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(253, 203, 110, 0.4);
  animation: logoBounce 3s infinite ease-in-out;
}

@keyframes logoBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.book-icon {
  font-size: 40px;
}

.system-title {
  font-size: 26px;
  font-weight: 800;
  color: #e17055;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.system-subtitle {
  font-size: 14px;
  color: #a0937d;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 表单样式 */
.storybook-form {
  margin-top: 24px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 20px;
  z-index: 2;
  transition: transform 0.3s ease;
}

.input-wrapper:focus-within .input-icon {
  transform: scale(1.2);
}

.storybook-form :deep(.el-input__wrapper) {
  background: #fefefe;
  border: 2px solid #f0e6d3;
  border-radius: 16px;
  padding: 4px 16px 4px 48px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

.storybook-form :deep(.el-input__wrapper:hover) {
  border-color: #ffd93d;
}

.storybook-form :deep(.el-input__wrapper.is-focus) {
  border-color: #e17055;
  box-shadow: 0 4px 16px rgba(225, 112, 85, 0.15);
}

.storybook-form :deep(.el-input__inner) {
  font-size: 15px;
  color: #2d3436;
  font-weight: 500;
  font-family: 'Nunito', sans-serif;
}

.storybook-form :deep(.el-input__inner::placeholder) {
  color: #b2bec3;
}

.storybook-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.storybook-form :deep(.el-form-item__error) {
  font-size: 12px;
  padding-top: 4px;
  color: #e17055;
}

/* 记住密码 */
.form-extras {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.remember-me {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.remember-me input {
  display: none;
}

.remember-me .checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid #f0e6d3;
  border-radius: 6px;
  margin-right: 8px;
  position: relative;
  transition: all 0.3s ease;
  background: #fff;
}

.remember-me input:checked + .checkmark {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-color: #fdcb6e;
}

.remember-me input:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-size: 12px;
  font-weight: bold;
}

.remember-me .label-text {
  font-size: 14px;
  color: #636e72;
  font-weight: 600;
}

/* 登录按钮 */
.magic-login-btn {
  width: 100%;
  padding: 16px 32px;
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  border: none;
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.4s ease;
  box-shadow: 0 8px 24px rgba(225, 112, 85, 0.35);
}

.magic-login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(225, 112, 85, 0.45);
}

.magic-login-btn:active {
  transform: translateY(-1px);
}

.magic-login-btn.loading {
  pointer-events: none;
  opacity: 0.8;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  position: relative;
  z-index: 2;
}

.btn-icon {
  font-size: 20px;
  animation: sparkle 2s infinite;
}

@keyframes sparkle {
  0%, 100% { transform: rotate(0deg) scale(1); }
  50% { transform: rotate(15deg) scale(1.2); }
}

.btn-text {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  font-family: 'Nunito', sans-serif;
  letter-spacing: 1px;
}

.btn-arrow {
  font-size: 20px;
  color: #fff;
  transition: transform 0.3s ease;
}

.magic-login-btn:hover .btn-arrow {
  transform: translateX(5px);
}

.btn-sparkles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.btn-sparkles span {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: btnSparkle 3s infinite;
}

.btn-sparkles span:nth-child(1) { top: 20%; left: 20%; animation-delay: 0s; }
.btn-sparkles span:nth-child(2) { top: 60%; right: 25%; animation-delay: 1s; }
.btn-sparkles span:nth-child(3) { bottom: 25%; left: 30%; animation-delay: 2s; }

@keyframes btnSparkle {
  0%, 100% { opacity: 0; transform: scale(0); }
  50% { opacity: 1; transform: scale(1); }
}

/* 测试账号 */
.test-accounts {
  margin-top: 28px;
  padding-top: 24px;
  border-top: 2px dashed #f0e6d3;
}

.accounts-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #a0937d;
  font-weight: 700;
}

.header-icon {
  font-size: 18px;
}

.accounts-list {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.account-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 20px;
  background: linear-gradient(145deg, #fff 0%, #fef9f0 100%);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.account-item:hover {
  border-color: #ffd93d;
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(253, 203, 110, 0.25);
}

.role-icon {
  font-size: 28px;
}

.role-name {
  font-size: 12px;
  color: #636e72;
  font-weight: 700;
}

/* 卡片底部 */
.card-footer {
  margin-top: 24px;
  text-align: center;
}

.footer-deco {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 20px;
  animation: footerFloat 4s infinite ease-in-out;
}

@keyframes footerFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

/* 页脚信息 */
.footer-info {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
  padding: 16px;
  z-index: 10;
}

.footer-info p {
  font-size: 13px;
  color: #a0937d;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .illustration-panel {
    display: none;
  }

  .login-panel {
    width: 100%;
    max-width: 480px;
  }
}

@media (max-width: 520px) {
  .login-card {
    padding: 32px 24px;
    border-radius: 24px;
  }

  .system-title {
    font-size: 22px;
  }

  .accounts-list {
    flex-wrap: wrap;
  }

  .account-item {
    padding: 10px 16px;
  }
}

/* 全局消息样式覆盖 */
:global(.storybook-message) {
  background: linear-gradient(135deg, #00b894 0%, #00cec9 100%) !important;
  border-radius: 12px !important;
  padding: 16px 24px !important;
}
</style>
