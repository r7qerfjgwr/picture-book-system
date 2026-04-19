<template>
  <!-- 根据角色显示不同的看板 -->
  <AdminDashboard v-if="isAdmin" />
  <TeacherDashboard v-else-if="isTeacher" />
  <ParentDashboard v-else />
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import AdminDashboard from './AdminDashboard.vue'
import TeacherDashboard from './TeacherDashboard.vue'
import ParentDashboard from './ParentDashboard.vue'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.role === 'ADMIN')
const isTeacher = computed(() => userStore.role === 'TEACHER')
</script>

<style scoped>
.dashboard {
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4ecf7 100%);
  padding: 20px;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: #fff;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.welcome-content h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
}

.welcome-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  opacity: 0.85;
}

.welcome-date {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.2);
  padding: 10px 18px;
  border-radius: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: #fff;
}

.stat-card-blue .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-green .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-card-orange .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card-purple .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.chart-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title .el-icon {
  color: #667eea;
}

.hot-books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.hot-book-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;
  border: 2px solid transparent;
}

.hot-book-item:hover {
  background: #fff;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.book-cover {
  width: 60px;
  height: 80px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.hot-book-item:hover .book-cover {
  transform: scale(1.05);
}

.cover-emoji {
  font-size: 32px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

/* 动物类 - 橙红渐变 */
.book-cover.animal {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
}

/* 科普类 - 蓝色渐变 */
.book-cover.science {
  background: linear-gradient(135deg, #48dbfb 0%, #0984e3 100%);
}

/* 情感类 - 粉紫渐变 */
.book-cover.emotion {
  background: linear-gradient(135deg, #fd79a8 0%, #e84393 100%);
}

/* 童话类 - 紫色渐变 */
.book-cover.fairy {
  background: linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%);
}

/* 默认 */
.book-cover.default {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.book-author {
  font-size: 12px;
  color: #909399;
}

.book-stats {
  font-size: 12px;
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.book-stats .el-icon {
  font-size: 14px;
}

@media (max-width: 1400px) {
  .hot-books-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1100px) {
  .hot-books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
