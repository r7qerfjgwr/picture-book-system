<template>
  <el-container class="storybook-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '72px' : '240px'" class="storybook-aside">
      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo-icon" :class="{ collapsed: isCollapse }">
          <span class="logo-emoji">📚</span>
        </div>
        <transition name="fade">
          <span v-if="!isCollapse" class="logo-text">绘本阅读系统</span>
        </transition>
      </div>

      <!-- 菜单区域 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="storybook-menu"
      >
        <el-menu-item index="/app/dashboard" class="menu-item">
          <span class="menu-emoji">🏠</span>
          <template #title>首页看板</template>
        </el-menu-item>

        <el-sub-menu index="children" v-if="userStore.isParent" class="sub-menu">
          <template #title>
            <span class="menu-emoji">👦</span>
            <span>儿童管理</span>
          </template>
          <el-menu-item index="/app/children" class="sub-menu-item">我的孩子</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="books" v-if="userStore.isAdmin" class="sub-menu">
          <template #title>
            <span class="menu-emoji">📖</span>
            <span>绘本管理</span>
          </template>
          <el-menu-item index="/app/books" class="sub-menu-item">绘本列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="category" class="sub-menu">
          <template #title>
            <span class="menu-emoji">🏷️</span>
            <span>绘本分类</span>
          </template>
          <el-menu-item index="/app/books/category/animal" class="sub-menu-item">
            <span class="category-emoji">🐻</span>动物类
          </el-menu-item>
          <el-menu-item index="/app/books/category/science" class="sub-menu-item">
            <span class="category-emoji">🔬</span>科普类
          </el-menu-item>
          <el-menu-item index="/app/books/category/emotion" class="sub-menu-item">
            <span class="category-emoji">❤️</span>情感类
          </el-menu-item>
          <el-menu-item index="/app/books/category/fairy_tale" class="sub-menu-item">
            <span class="category-emoji">🏰</span>童话类
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/app/reading-logs" class="menu-item">
          <span class="menu-emoji">📝</span>
          <template #title>阅读记录</template>
        </el-menu-item>

        <el-menu-item index="/app/analysis" class="menu-item">
          <span class="menu-emoji">📊</span>
          <template #title>行为分析</template>
        </el-menu-item>

        <el-menu-item index="/app/growth" class="menu-item">
          <span class="menu-emoji">🌱</span>
          <template #title>成长跟踪</template>
        </el-menu-item>

        <el-menu-item index="/app/recommendations" class="menu-item">
          <span class="menu-emoji">⭐</span>
          <template #title>绘本推荐</template>
        </el-menu-item>

        <el-menu-item index="/app/class" v-if="userStore.isTeacher || userStore.isAdmin" class="menu-item">
          <span class="menu-emoji">🏫</span>
          <template #title>班级管理</template>
        </el-menu-item>

        <el-menu-item index="/app/users" v-if="userStore.isAdmin" class="menu-item">
          <span class="menu-emoji">👥</span>
          <template #title>用户管理</template>
        </el-menu-item>

        <el-menu-item index="/app/profile" class="menu-item">
          <span class="menu-emoji">👤</span>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>

      <!-- 底部装饰 -->
      <div class="aside-footer" v-if="!isCollapse">
        <div class="deco-stars">
          <span>✨</span><span>🌟</span><span>✨</span>
        </div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="storybook-header">
        <div class="header-left">
          <button class="collapse-btn" @click="toggleCollapse">
            <span v-if="!isCollapse">◀</span>
            <span v-else>▶</span>
          </button>
          <div class="breadcrumb">
            <span class="breadcrumb-icon">📍</span>
            <span>{{ currentPageTitle }}</span>
          </div>
        </div>

        <div class="header-right">
          <!-- 通知图标 -->
          <div class="header-icon-btn">
            <span>🔔</span>
          </div>

          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <div class="avatar-wrapper">
                <span class="avatar-emoji">{{ roleEmoji }}</span>
              </div>
              <div class="user-details">
                <span class="user-name">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
                <span class="user-role" :class="roleClass">{{ roleLabel }}</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <el-dropdown-item command="logout">
                  <span class="dropdown-emoji">🚪</span> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="storybook-main">
        <div class="page-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const currentPageTitle = computed(() => {
  const titles = {
    '/app/dashboard': '首页看板',
    '/app/children': '儿童管理',
    '/app/books': '绘本管理',
    '/app/reading-logs': '阅读记录',
    '/app/analysis': '行为分析',
    '/app/growth': '成长跟踪',
    '/app/recommendations': '绘本推荐',
    '/app/class': '班级管理',
    '/app/users': '用户管理'
  }
  return titles[route.path] || '绘本阅读系统'
})

const roleLabel = computed(() => {
  const roles = { ADMIN: '管理员', TEACHER: '教师', PARENT: '家长' }
  return roles[userStore.role] || '用户'
})

const roleClass = computed(() => {
  const classes = { ADMIN: 'role-admin', TEACHER: 'role-teacher', PARENT: 'role-parent' }
  return classes[userStore.role] || ''
})

const roleEmoji = computed(() => {
  const emojis = { ADMIN: '👑', TEACHER: '👩‍🏫', PARENT: '👨‍👩‍👧' }
  return emojis[userStore.role] || '👤'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.storybook-layout {
  height: 100vh;
  font-family: 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 侧边栏样式 */
.storybook-aside {
  background: linear-gradient(180deg, #ffecd2 0%, #fcb69f 50%, #ffecd2 100%);
  position: relative;
  overflow: hidden;
  transition: width 0.3s ease;
}

.storybook-aside::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 40px;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3));
  pointer-events: none;
}

/* Logo区域 */
.logo-section {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.4);
  border-bottom: 3px dashed rgba(255, 255, 255, 0.6);
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: rotate(-5deg) scale(1.05);
}

.logo-icon.collapsed {
  width: 40px;
  height: 40px;
}

.logo-emoji {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: #e17055;
  text-shadow: 1px 1px 0 rgba(255, 255, 255, 0.8);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 菜单样式 */
.storybook-menu {
  background: transparent;
  border: none;
  padding: 12px 8px;
}

.storybook-menu :deep(.el-menu-item),
.storybook-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
  border-radius: 12px;
  color: #6d4c41;
  font-weight: 600;
  transition: all 0.3s ease;
}

.storybook-menu :deep(.el-menu-item:hover),
.storybook-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.5);
  transform: translateX(4px);
}

.storybook-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.menu-emoji {
  font-size: 20px;
  margin-right: 10px;
  transition: transform 0.3s ease;
}

.storybook-menu :deep(.el-menu-item:hover .menu-emoji) {
  transform: scale(1.2);
}

.sub-menu :deep(.el-sub-menu__icon-arrow) {
  color: #6d4c41;
}

.sub-menu-item {
  padding-left: 52px !important;
  height: 44px !important;
  line-height: 44px !important;
  font-size: 14px;
}

/* 子菜单弹出层样式 - 匹配侧边栏暖色调 */
:deep(.el-menu--popup) {
  background: linear-gradient(180deg, #ffecd2 0%, #fcb69f 50%, #ffecd2 100%) !important;
  border-radius: 12px !important;
  padding: 8px !important;
  box-shadow: 0 4px 20px rgba(252, 182, 159, 0.3) !important;
  border: 2px dashed rgba(255, 255, 255, 0.5) !important;
}

:deep(.el-menu--popup .el-menu-item) {
  background: transparent !important;
  color: #6d4c41 !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
  margin: 4px 0 !important;
}

:deep(.el-menu--popup .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.5) !important;
  transform: translateX(4px);
}

:deep(.el-menu--popup .el-menu-item.is-active) {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4) !important;
}

/* 内联展开的子菜单样式 */
:deep(.el-sub-menu.is-opened > .el-menu) {
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px;
  margin: 4px 8px;
  padding: 8px 0;
}

:deep(.el-sub-menu.is-opened > .el-menu .el-menu-item) {
  background: transparent !important;
  min-width: auto !important;
}

:deep(.el-sub-menu.is-opened > .el-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.5) !important;
}

:deep(.el-sub-menu.is-opened > .el-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%) !important;
  color: #fff !important;
}

.category-emoji {
  margin-right: 8px;
}

/* 侧边栏底部 */
.aside-footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
}

.deco-stars span {
  font-size: 16px;
  animation: twinkle 2s infinite ease-in-out;
}

.deco-stars span:nth-child(2) {
  animation-delay: 0.3s;
}

.deco-stars span:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes twinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* 主内容区 */
.main-container {
  background: #fef9f3;
}

/* 顶部导航 */
.storybook-header {
  background: linear-gradient(135deg, #ffffff 0%, #fff9f0 100%);
  box-shadow: 0 2px 12px rgba(255, 107, 107, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  border-bottom: 3px solid #ffeaa7;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e17055;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.5);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #e17055;
}

.breadcrumb-icon {
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 234, 167, 0.5);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.header-icon-btn:hover {
  background: #ffeaa7;
  transform: translateY(-2px);
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.user-info:hover {
  background: #fff;
  border-color: #ffeaa7;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-wrapper {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
}

.user-role {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
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
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  color: #fff;
}

/* 下拉菜单 */
.user-dropdown-menu :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-weight: 600;
}

.dropdown-emoji {
  font-size: 18px;
}

/* 主内容区 */
.storybook-main {
  padding: 20px;
  overflow-y: auto;
  background: linear-gradient(180deg, #fef9f3 0%, #fff5eb 100%);
}

.page-wrapper {
  min-height: calc(100vh - 124px);
}

/* 响应式 */
@media (max-width: 768px) {
  .storybook-aside {
    position: fixed;
    z-index: 100;
    height: 100vh;
  }

  .user-details {
    display: none;
  }
}
</style>
