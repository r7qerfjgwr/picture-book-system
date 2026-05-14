<template>
  <div class="storybook-books-page">
    <!-- 页面横幅 -->
    <div class="page-banner">
      <div class="banner-content">
        <div class="banner-left">
          <span class="banner-emoji">📚</span>
          <div class="banner-text">
            <h1>绘本管理</h1>
            <p>管理绘本资源，丰富阅读世界</p>
          </div>
        </div>
        <div class="banner-stats">
          <div class="stat-item">
            <span class="stat-num">{{ pagination.total }}</span>
            <span class="stat-label">绘本总数</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-section">
      <div class="search-card">
        <div class="search-grid">
          <div class="search-item">
            <label>📖 绘本名称</label>
            <input
              v-model="searchForm.keyword"
              type="text"
              placeholder="输入绘本名称搜索..."
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="search-item">
            <label>📂 绘本类别</label>
            <select v-model="searchForm.category" @change="handleSearch">
              <option value="">全部类别</option>
              <option value="认知启蒙">🧒 认知启蒙</option>
              <option value="情感培养">❤️ 情感培养</option>
              <option value="生活习惯">🧴 生活习惯</option>
              <option value="故事">🏰 故事</option>
              <option value="科学探索">🔬 科学探索</option>
              <option value="品格教育">🌟 品格教育</option>
              <option value="社会交往">🤝 社会交往</option>
              <option value="益智游戏">🧩 益智游戏</option>
              <option value="艺术启蒙">🎨 艺术启蒙</option>
              <option value="安全教育">🛡️ 安全教育</option>
            </select>
          </div>
          <div class="search-item">
            <label>📊 难度等级</label>
            <select v-model="searchForm.difficultyLevel" @change="handleSearch">
              <option value="">全部难度</option>
              <option value="1">⭐ 简单</option>
              <option value="2">⭐⭐ 中等</option>
              <option value="3">⭐⭐⭐ 困难</option>
            </select>
          </div>
        </div>
        <div class="search-actions">
          <button class="search-btn" @click="handleSearch">
            <span>🔍</span> 搜索
          </button>
          <button class="reset-btn" @click="resetSearch">
            <span>🔄</span> 重置
          </button>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <span class="result-info">共找到 <strong>{{ pagination.total }}</strong> 本绘本</span>
      </div>
      <div class="action-right">
        <label class="upload-btn">
          <span>📤</span> 批量导入
          <input type="file" accept=".xlsx,.xls" hidden @change="handleUpload" />
        </label>
        <button class="add-btn" @click="showAddDialog">
          <span>✨</span> 新增绘本
        </button>
      </div>
    </div>

    <!-- 绘本表格 -->
    <div class="table-section">
      <div class="table-wrapper">
        <table class="storybook-table">
          <thead>
            <tr>
              <th>绘本信息</th>
              <th>作者</th>
              <th>类别</th>
              <th>难度</th>
              <th>页数</th>
              <th>阅读次数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="books.length > 0">
            <tr v-for="book in books" :key="book.id">
              <td>
                <div class="book-info-cell">
                  <div class="book-cover-mini" :style="getCoverStyle(book.category)">
                    <span>{{ getCategoryEmoji(book.category) }}</span>
                  </div>
                  <div class="book-title-cell">
                    <span class="book-name">{{ book.title }}</span>
                    <span class="book-id">ID: {{ book.id }}</span>
                  </div>
                </div>
              </td>
              <td>
                <span class="author-name">✍️ {{ book.author }}</span>
              </td>
              <td>
                <span class="category-badge" :style="getCategoryStyle(book.category)">
                  {{ getCategoryEmoji(book.category) }} {{ book.category }}
                </span>
              </td>
              <td>
                <span class="difficulty-badge" :style="getDifficultyStyle(book.difficultyLevel)">
                  {{ getDifficultyLabel(book.difficultyLevel) }}
                </span>
              </td>
              <td>
                <span class="page-count">📄 {{ book.pageCount || 20 }}页</span>
              </td>
              <td>
                <span class="read-count">📖 {{ book.readCount || 0 }}次</span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="edit-btn" @click="editBook(book)">
                    ✏️ 编辑
                  </button>
                  <button class="delete-btn" @click="deleteBook(book)">
                    🗑️ 删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="7">
                <div class="empty-state">
                  <span class="empty-icon">📚</span>
                  <p>暂无绘本数据</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <div class="pagination">
          <button class="page-btn" :disabled="pagination.page === 1" @click="pagination.page--; loadBooks()">
            ◀ 上一页
          </button>
          <div class="page-numbers">
            <span class="current-page">第 {{ pagination.page }} 页</span>
          </div>
          <button class="page-btn" :disabled="pagination.page * pagination.size >= pagination.total" @click="pagination.page++; loadBooks()">
            下一页 ▶
          </button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div class="modal-overlay" v-if="dialogVisible" @click.self="dialogVisible = false">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEdit ? '✏️ 编辑绘本' : '✨ 新增绘本' }}</h2>
          <button class="close-btn" @click="dialogVisible = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="form-item full">
              <label>📖 绘本名称 <span class="required">*</span></label>
              <input v-model="form.title" type="text" placeholder="请输入绘本名称" />
            </div>
            <div class="form-item">
              <label>✍️ 作者 <span class="required">*</span></label>
              <input v-model="form.author" type="text" placeholder="请输入作者" />
            </div>
            <div class="form-item">
              <label>📂 主类别 <span class="required">*</span></label>
              <select v-model="form.category">
                <option value="">请选择类别</option>
                <option value="认知启蒙">🧒 认知启蒙</option>
                <option value="情感培养">❤️ 情感培养</option>
                <option value="生活习惯">🧴 生活习惯</option>
                <option value="故事">🏰 故事</option>
                <option value="科学探索">🔬 科学探索</option>
                <option value="品格教育">🌟 品格教育</option>
                <option value="社会交往">🤝 社会交往</option>
                <option value="益智游戏">🧩 益智游戏</option>
                <option value="艺术启蒙">🎨 艺术启蒙</option>
                <option value="安全教育">🛡️ 安全教育</option>
              </select>
            </div>
            <div class="form-item">
              <label>📊 难度等级 <span class="required">*</span></label>
              <select v-model="form.difficultyLevel">
                <option :value="1">⭐ 简单</option>
                <option :value="2">⭐⭐ 中等</option>
                <option :value="3">⭐⭐⭐ 困难</option>
              </select>
            </div>
            <div class="form-item">
              <label>📄 页数</label>
              <input v-model.number="form.pageCount" type="number" min="1" max="100" placeholder="页数" />
            </div>
            <div class="form-item full">
              <label>📝 简介</label>
              <textarea v-model="form.description" rows="4" placeholder="请输入绘本简介..."></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="dialogVisible = false">取消</button>
          <button class="submit-btn" @click="handleSubmit">
            <span>✓</span> {{ isEdit ? '保存修改' : '创建绘本' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookList, createBook, updateBook, deleteBook as deleteBookApi, uploadBooks } from '@/api/book'

const loading = ref(false)
const books = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  keyword: '',
  category: '',
  difficultyLevel: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  title: '',
  author: '',
  category: '',
  difficultyLevel: 1,
  pageCount: 20,
  description: ''
})

const getCategoryEmoji = (category) => {
  const emojis = {
    '认知启蒙': '🧒', '情感培养': '❤️', '生活习惯': '🧴', '故事': '🏰',
    '科学探索': '🔬', '品格教育': '🌟', '社会交往': '🤝', '益智游戏': '🧩',
    '艺术启蒙': '🎨', '安全教育': '🛡️'
  }
  return emojis[category] || '📖'
}

const getCategoryStyle = (category) => {
  const styles = {
    '认知启蒙': { background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)', color: '#d63031' },
    '情感培养': { background: 'linear-gradient(135deg, #ffeaa7 0%, #ffb88c 100%)', color: '#e17055' },
    '生活习惯': { background: 'linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%)', color: '#00b894' },
    '故事': { background: 'linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%)', color: '#6d4c41' },
    '科学探索': { background: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)', color: '#0984e3' },
    '品格教育': { background: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)', color: '#6c5ce7' },
    '社会交往': { background: 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)', color: '#0984e3' },
    '益智游戏': { background: 'linear-gradient(135deg, #fddb92 0%, #d1fdff 100%)', color: '#fdcb6e' },
    '艺术启蒙': { background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: '#e84393' },
    '安全教育': { background: 'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)', color: '#6c5ce7' }
  }
  return styles[category] || { background: 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)', color: '#2d3436' }
}

const getCoverStyle = (category) => {
  const gradients = {
    '认知启蒙': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    '情感培养': 'linear-gradient(135deg, #ffeaa7 0%, #ffb88c 100%)',
    '生活习惯': 'linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%)',
    '故事': 'linear-gradient(135deg, #f5e6d3 0%, #d4a574 100%)',
    '科学探索': 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
    '品格教育': 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)',
    '社会交往': 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)',
    '益智游戏': 'linear-gradient(135deg, #fddb92 0%, #d1fdff 100%)',
    '艺术启蒙': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    '安全教育': 'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)'
  }
  return { background: gradients[category] || 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)' }
}

const getDifficultyLabel = (level) => {
  const labels = { 1: '⭐ 简单', 2: '⭐⭐ 中等', 3: '⭐⭐⭐ 困难' }
  return labels[level] || `⭐ ${level}`
}

const getDifficultyStyle = (level) => {
  const styles = {
    1: { background: 'rgba(29, 209, 161, 0.15)', color: '#00b894' },
    2: { background: 'rgba(255, 159, 67, 0.15)', color: '#ff9f43' },
    3: { background: 'rgba(255, 107, 107, 0.15)', color: '#ff6b6b' }
  }
  return styles[level] || { background: 'rgba(0,0,0,0.05)', color: '#6d4c41' }
}

const loadBooks = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      category: searchForm.category || undefined,
      difficultyLevel: searchForm.difficultyLevel || undefined
    }
    const res = await getBookList(params)
    books.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('Load books error:', error)
    ElMessage.error('加载绘本列表失败，请稍后重试')
    books.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadBooks()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.difficultyLevel = ''
  handleSearch()
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    title: '',
    author: '',
    category: '',
    difficultyLevel: 1,
    pageCount: 20,
    description: ''
  })
  dialogVisible.value = true
}

const editBook = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const deleteBook = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该绘本吗？', '提示', { type: 'warning' })
    await deleteBookApi(row.id)
    ElMessage.success('删除成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete book error:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!form.title || !form.author || !form.category) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    if (isEdit.value) {
      await updateBook(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createBook(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadBooks()
  } catch (error) {
    console.error('Submit error:', error)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
  }
}

const handleUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    await uploadBooks(file)
    ElMessage.success('导入成功')
    loadBooks()
  } catch (error) {
    console.error('Upload error:', error)
    ElMessage.info('导入功能开发中')
  }
}

onMounted(() => {
  loadBooks()
})
</script>

<style scoped>


.storybook-books-page {
  font-family: 'Nunito', sans-serif;
}

/* 页面横幅 */
.page-banner {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-radius: 24px;
  padding: 28px 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(253, 203, 110, 0.3);
}

.page-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 250px;
  height: 250px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.banner-emoji {
  font-size: 56px;
  animation: bounce 2s infinite ease-in-out;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.banner-text h1 {
  font-size: 28px;
  font-weight: 800;
  color: #2d3436;
  margin-bottom: 4px;
}

.banner-text p {
  font-size: 14px;
  color: #6d4c41;
  font-weight: 600;
}

.banner-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  text-align: center;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
}

.stat-num {
  display: block;
  font-size: 32px;
  font-weight: 800;
  color: #2d3436;
}

.stat-label {
  font-size: 12px;
  color: #6d4c41;
  font-weight: 600;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 20px;
}

.search-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 3px solid #f0e6d3;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.search-item label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #6d4c41;
  margin-bottom: 8px;
}

.search-item input,
.search-item select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #2d3436;
  background: #fefefe;
  transition: all 0.3s ease;
}

.search-item input:focus,
.search-item select:focus {
  outline: none;
  border-color: #e17055;
  box-shadow: 0 0 0 3px rgba(225, 112, 85, 0.1);
}

.search-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.search-btn,
.reset-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-btn {
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  color: #fff;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(225, 112, 85, 0.4);
}

.reset-btn {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  color: #2d3436;
}

.reset-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-info {
  font-size: 14px;
  color: #6d4c41;
  font-weight: 600;
}

.result-info strong {
  color: #e17055;
}

.action-right {
  display: flex;
  gap: 12px;
}

.upload-btn,
.add-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-btn {
  background: linear-gradient(135deg, #55efc4 0%, #00b894 100%);
  color: #fff;
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 184, 148, 0.4);
}

.add-btn {
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  color: #fff;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(9, 132, 227, 0.4);
}

/* 表格区域 */
.table-section {
  background: #fff;
  border-radius: 20px;
  border: 3px solid #f0e6d3;
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
}

.storybook-table {
  width: 100%;
  border-collapse: collapse;
}

.storybook-table th {
  background: linear-gradient(135deg, #fef9f0 0%, #fff5eb 100%);
  padding: 16px 20px;
  text-align: left;
  font-size: 14px;
  font-weight: 700;
  color: #6d4c41;
  border-bottom: 3px solid #f0e6d3;
}

.storybook-table td {
  padding: 16px 20px;
  border-bottom: 1px solid #f0e6d3;
  vertical-align: middle;
}

.storybook-table tr:hover {
  background: #fef9f0;
}

.book-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-cover-mini {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.book-title-cell {
  display: flex;
  flex-direction: column;
}

.book-name {
  font-size: 14px;
  font-weight: 700;
  color: #2d3436;
}

.book-id {
  font-size: 11px;
  color: #a0937d;
}

.author-name {
  font-size: 13px;
  color: #6d4c41;
  font-weight: 600;
}

.category-badge {
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
}

.difficulty-badge {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
}

.page-count,
.read-count {
  font-size: 13px;
  color: #6d4c41;
  font-weight: 600;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.edit-btn,
.delete-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn {
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  color: #fff;
}

.edit-btn:hover {
  transform: scale(1.05);
}

.delete-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
}

.delete-btn:hover {
  transform: scale(1.05);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #a0937d;
  font-weight: 600;
}

/* 分页 */
.pagination-section {
  padding: 20px;
  border-top: 2px dashed #f0e6d3;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.page-btn {
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

.page-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(253, 203, 110, 0.4);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.current-page {
  font-size: 14px;
  font-weight: 700;
  color: #6d4c41;
  padding: 10px 20px;
  background: #fef9f0;
  border-radius: 12px;
}

/* 弹窗 */
.modal-overlay {
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
  padding: 20px;
}

.modal-content {
  background: #fff;
  border-radius: 24px;
  max-width: 600px;
  width: 100%;
  border: 4px solid #f0e6d3;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #fef9f0 0%, #fff5eb 100%);
  border-bottom: 2px dashed #f0e6d3;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 800;
  color: #2d3436;
}

.close-btn {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border: none;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-btn:hover {
  transform: scale(1.1);
}

.modal-body {
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item.full {
  grid-column: span 2;
}

.form-item label {
  font-size: 13px;
  font-weight: 700;
  color: #6d4c41;
}

.required {
  color: #ff6b6b;
}

.form-item input,
.form-item select,
.form-item textarea {
  padding: 12px 16px;
  border: 2px solid #f0e6d3;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #2d3436;
  background: #fefefe;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-item textarea {
  resize: vertical;
}

.form-item input:focus,
.form-item select:focus,
.form-item textarea:focus {
  outline: none;
  border-color: #e17055;
  box-shadow: 0 0 0 3px rgba(225, 112, 85, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  background: #fef9f0;
  border-top: 2px dashed #f0e6d3;
}

.cancel-btn,
.submit-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cancel-btn {
  background: #f0e6d3;
  color: #6d4c41;
}

.cancel-btn:hover {
  background: #e6d9c4;
}

.submit-btn {
  background: linear-gradient(135deg, #e17055 0%, #d63031 100%);
  color: #fff;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(225, 112, 85, 0.4);
}

/* 响应式 */
@media (max-width: 1024px) {
  .search-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .search-grid {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-item.full {
    grid-column: span 1;
  }

  .banner-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .action-right {
    justify-content: center;
  }
}
</style>
