<template>
  <div class="apple-home">
    <!-- 导航栏 -->
    <nav class="nav-bar" :class="{ scrolled: isScrolled }">
      <div class="nav-container">
        <a href="#" class="nav-logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">绘本阅读</span>
        </a>

        <div class="nav-center">
          <a href="#features" class="nav-link">特色功能</a>
          <a href="#stats" class="nav-link">数据概览</a>
          <a href="#books" class="nav-link">热门绘本</a>
          <a href="#about" class="nav-link">关于我们</a>
        </div>

        <div class="nav-right">
          <button class="nav-link-btn" @click="showLogin = true">
            登录
          </button>
          <button class="nav-primary-btn" @click="showRegister = true">
            免费注册
          </button>
        </div>

        <!-- 移动端菜单按钮 -->
        <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
          <span class="menu-line" :class="{ open: mobileMenuOpen }"></span>
          <span class="menu-line" :class="{ open: mobileMenuOpen }"></span>
        </button>
      </div>

      <!-- 移动端菜单 -->
      <div class="mobile-menu" :class="{ open: mobileMenuOpen }">
        <a href="#features" class="mobile-link" @click="mobileMenuOpen = false">特色功能</a>
        <a href="#stats" class="mobile-link" @click="mobileMenuOpen = false">数据概览</a>
        <a href="#books" class="mobile-link" @click="mobileMenuOpen = false">热门绘本</a>
        <a href="#about" class="mobile-link" @click="mobileMenuOpen = false">关于我们</a>
        <div class="mobile-actions">
          <button class="mobile-login" @click="showLogin = true; mobileMenuOpen = false">登录</button>
          <button class="mobile-register" @click="showRegister = true; mobileMenuOpen = false">免费注册</button>
        </div>
      </div>
    </nav>

    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="gradient-orb orb-1"></div>
        <div class="gradient-orb orb-2"></div>
        <div class="gradient-orb orb-3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-icon">✨</span>
          <span>AI驱动的儿童阅读成长平台</span>
        </div>
        <h1 class="hero-title">
          让每一次阅读<br />
          <span class="gradient-text">都有成长的意义</span>
        </h1>
        <p class="hero-desc">
          基于大数据分析与AI算法，为3-6岁儿童提供个性化绘本推荐，
          科学追踪阅读行为，记录成长每一个精彩瞬间。
        </p>
        <div class="hero-actions">
          <button class="primary-btn" @click="startReading">
            <span>开始阅读之旅</span>
            <span class="btn-arrow">→</span>
          </button>
          <button class="secondary-btn" @click="scrollTo('features')">
            了解更多
          </button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-num">{{ animatedStats.totalBooks }}</span>
            <span class="stat-label">精选绘本</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ animatedStats.totalUsers }}</span>
            <span class="stat-label">活跃家庭</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ animatedStats.totalReadingTime }}</span>
            <span class="stat-label">累计阅读时长</span>
          </div>
        </div>
      </div>
      <div class="hero-visual">
        <div class="device-frame">
          <div class="device-screen">
            <div class="screen-content">
              <div class="book-preview">
                <span class="preview-emoji">🐻</span>
              </div>
              <div class="preview-info">
                <div class="preview-title">小熊的冒险</div>
                <div class="preview-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" style="width: 75%"></div>
                  </div>
                  <span>75% 已阅读</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="floating-cards">
          <div class="float-card card-1">
            <span>🎯</span>
            <span>专注型读者</span>
          </div>
          <div class="float-card card-2">
            <span>📈</span>
            <span>成长+15%</span>
          </div>
          <div class="float-card card-3">
            <span>⭐</span>
            <span>推荐绘本</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据总览 -->
    <section id="stats" class="stats-section">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">📊 实时数据</span>
          <h2>今日数据总览</h2>
          <p>实时追踪平台运营数据，见证孩子们的阅读成长</p>
        </div>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="card-icon blue">
              <span>⏱️</span>
            </div>
            <div class="card-content">
              <div class="card-value">{{ todayStats.readingTime }}<span class="unit">小时</span></div>
              <div class="card-label">今日阅读时长</div>
            </div>
            <div class="card-trend up">
              <span>↑ 12%</span>
              <span>较昨日</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="card-icon green">
              <span>👥</span>
            </div>
            <div class="card-content">
              <div class="card-value">{{ todayStats.activeUsers }}<span class="unit">人</span></div>
              <div class="card-label">活跃读者</div>
            </div>
            <div class="card-trend up">
              <span>↑ 8%</span>
              <span>较昨日</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="card-icon orange">
              <span>📚</span>
            </div>
            <div class="card-content">
              <div class="card-value">{{ todayStats.booksRead }}<span class="unit">本</span></div>
              <div class="card-label">今日阅读绘本</div>
            </div>
            <div class="card-trend up">
              <span>↑ 23%</span>
              <span>较昨日</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="card-icon purple">
              <span>⭐</span>
            </div>
            <div class="card-content">
              <div class="card-value">{{ todayStats.newReports }}<span class="unit">份</span></div>
              <div class="card-label">新增成长报告</div>
            </div>
            <div class="card-trend up">
              <span>↑ 5%</span>
              <span>较昨日</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门绘本推荐 -->
    <section id="books" class="books-section">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">🔥 热门推荐</span>
          <h2>精选绘本推荐</h2>
          <p>基于大数据分析，为您推荐最适合的优质绘本</p>
        </div>
        <div class="books-tabs">
          <button
            v-for="tab in bookTabs"
            :key="tab.value"
            :class="['tab-btn', { active: activeTab === tab.value }]"
            @click="activeTab = tab.value"
          >
            {{ tab.icon }} {{ tab.label }}
          </button>
        </div>
        <div class="books-grid">
          <div
            v-for="(book, index) in filteredBooks"
            :key="book.id"
            class="book-card"
            :style="{ animationDelay: index * 0.1 + 's' }"
            @click="viewBook(book)"
          >
            <div class="book-cover" :class="book.category">
              <span class="cover-emoji">{{ book.emoji }}</span>
              <div class="book-rank" v-if="index < 3">
                <span class="rank-medal">{{ ['🥇', '🥈', '🥉'][index] }}</span>
              </div>
            </div>
            <div class="book-info">
              <h3 class="book-title">{{ book.title }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <div class="book-meta">
                <span class="meta-item">
                  <span class="meta-icon">👶</span>
                  {{ book.ageRange }}
                </span>
                <span class="meta-item">
                  <span class="meta-icon">📖</span>
                  {{ book.readCount }}次阅读
                </span>
              </div>
              <div class="book-rating">
                <span class="stars">
                  <span v-for="i in 5" :key="i" :class="['star', { filled: i <= book.rating }]">★</span>
                </span>
                <span class="rating-value">{{ book.rating }}.0</span>
              </div>
            </div>
          </div>
        </div>
        <div class="books-more">
          <button class="more-btn" @click="showLogin = true">
            查看更多绘本
            <span class="more-arrow">→</span>
          </button>
        </div>
      </div>
    </section>

    <!-- 快速入口 -->
    <section class="quick-entry-section">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">🚀 快速开始</span>
          <h2>选择您的角色</h2>
          <p>不同角色，专属功能，为您定制最佳体验</p>
        </div>
        <div class="role-cards">
          <div class="role-card parent" @click="selectRole('parent')">
            <div class="role-icon">
              <span>👨‍👩‍👧</span>
            </div>
            <h3>我是家长</h3>
            <p>记录孩子的阅读成长，获取个性化推荐</p>
            <ul class="role-features">
              <li><span>✓</span> 绑定孩子信息</li>
              <li><span>✓</span> 查看阅读报告</li>
              <li><span>✓</span> 获取绘本推荐</li>
              <li><span>✓</span> 成长轨迹追踪</li>
            </ul>
            <button class="role-btn">开始使用</button>
          </div>
          <div class="role-card teacher" @click="selectRole('teacher')">
            <div class="role-badge">推荐</div>
            <div class="role-icon">
              <span>👩‍🏫</span>
            </div>
            <h3>我是教师</h3>
            <p>管理班级阅读，了解每个孩子的阅读情况</p>
            <ul class="role-features">
              <li><span>✓</span> 班级儿童管理</li>
              <li><span>✓</span> 阅读数据分析</li>
              <li><span>✓</span> 班级报告生成</li>
              <li><span>✓</span> 家校互动</li>
            </ul>
            <button class="role-btn">开始使用</button>
          </div>
          <div class="role-card admin" @click="selectRole('admin')">
            <div class="role-icon">
              <span>👨‍💼</span>
            </div>
            <h3>我是管理员</h3>
            <p>系统全面管理，数据一目了然</p>
            <ul class="role-features">
              <li><span>✓</span> 用户管理</li>
              <li><span>✓</span> 绘本管理</li>
              <li><span>✓</span> 数据统计</li>
              <li><span>✓</span> 系统配置</li>
            </ul>
            <button class="role-btn">开始使用</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 系统特色 -->
    <section id="features" class="features-section">
      <div class="section-container">
        <div class="section-header center">
          <span class="section-badge">✨ 核心特色</span>
          <h2>科技赋能阅读成长</h2>
          <p>融合前沿技术，打造智能化儿童阅读生态系统</p>
        </div>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <div class="icon-bg blue">
                <span>🧠</span>
              </div>
            </div>
            <h3>大数据行为分析</h3>
            <p>采用K-Means聚类算法，分析阅读时长、翻页速率、重复阅读等多维度数据，精准识别儿童阅读类型。</p>
            <div class="feature-tags">
              <span>K-Means</span>
              <span>行为画像</span>
              <span>多维度分析</span>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <div class="icon-bg green">
                <span>📈</span>
              </div>
            </div>
            <h3>成长轨迹追踪</h3>
            <p>8大维度成长评估体系，从词汇量到创造力全方位记录，生成专业成长报告与同龄对比分析。</p>
            <div class="feature-tags">
              <span>8维评估</span>
              <span>成长报告</span>
              <span>同龄对比</span>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <div class="icon-bg orange">
                <span>🎯</span>
              </div>
            </div>
            <h3>个性化推荐引擎</h3>
            <p>基于协同过滤与内容推荐算法，结合年龄、兴趣、能力三重匹配，推荐最适合的绘本。</p>
            <div class="feature-tags">
              <span>协同过滤</span>
              <span>内容推荐</span>
              <span>三重匹配</span>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <div class="icon-bg purple">
                <span>📊</span>
              </div>
            </div>
            <h3>可视化数据看板</h3>
            <p>丰富的图表展示，阅读热力图、能力雷达图、成长趋势图，让数据一目了然。</p>
            <div class="feature-tags">
              <span>ECharts</span>
              <span>热力图</span>
              <span>雷达图</span>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <div class="icon-bg pink">
                <span>🔐</span>
              </div>
            </div>
            <h3>安全可靠架构</h3>
            <p>Spring Boot 3 + Vue 3现代化技术栈，JWT认证、BCrypt加密，保障数据安全。</p>
            <div class="feature-tags">
              <span>JWT认证</span>
              <span>BCrypt</span>
              <span>权限控制</span>
            </div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <div class="icon-bg cyan">
                <span>📱</span>
              </div>
            </div>
            <h3>响应式设计</h3>
            <p>完美适配PC、平板、手机多端设备，随时随地记录孩子的阅读成长。</p>
            <div class="feature-tags">
              <span>多端适配</span>
              <span>流畅体验</span>
              <span>离线支持</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 技术架构 -->
    <section class="tech-section">
      <div class="section-container">
        <div class="tech-content">
          <div class="tech-left">
            <span class="section-badge">⚙️ 技术架构</span>
            <h2>现代化技术栈</h2>
            <p>采用业界领先的技术方案，确保系统稳定、高效、可扩展</p>
            <div class="tech-list">
              <div class="tech-item">
                <div class="tech-icon">☕</div>
                <div class="tech-info">
                  <h4>Spring Boot 3</h4>
                  <p>Java 17 + MyBatis-Plus，RESTful API设计</p>
                </div>
              </div>
              <div class="tech-item">
                <div class="tech-icon">💚</div>
                <div class="tech-info">
                  <h4>Vue 3 + Vite</h4>
                  <p>Composition API，响应式前端框架</p>
                </div>
              </div>
              <div class="tech-item">
                <div class="tech-icon">🗄️</div>
                <div class="tech-info">
                  <h4>MySQL 8.0</h4>
                  <p>关系型数据库，MyBatis-Plus ORM</p>
                </div>
              </div>
              <div class="tech-item">
                <div class="tech-icon">📈</div>
                <div class="tech-info">
                  <h4>ECharts</h4>
                  <p>专业数据可视化图表库</p>
                </div>
              </div>
            </div>
          </div>
          <div class="tech-right">
            <div class="architecture-diagram">
              <div class="layer frontend">
                <span class="layer-title">前端层</span>
                <div class="layer-items">
                  <span>Vue 3</span>
                  <span>Pinia</span>
                  <span>ECharts</span>
                  <span>Element Plus</span>
                </div>
              </div>
              <div class="layer-arrow">↓</div>
              <div class="layer gateway">
                <span class="layer-title">网关层</span>
                <div class="layer-items">
                  <span>JWT认证</span>
                  <span>权限控制</span>
                  <span>API限流</span>
                </div>
              </div>
              <div class="layer-arrow">↓</div>
              <div class="layer service">
                <span class="layer-title">服务层</span>
                <div class="layer-items">
                  <span>Spring Boot</span>
                  <span>行为分析</span>
                  <span>推荐引擎</span>
                  <span>成长追踪</span>
                </div>
              </div>
              <div class="layer-arrow">↓</div>
              <div class="layer data">
                <span class="layer-title">数据层</span>
                <div class="layer-items">
                  <span>MySQL</span>
                  <span>MyBatis-Plus</span>
                  <span>Redis缓存</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 关于我们 -->
    <section id="about" class="about-section">
      <div class="section-container">
        <div class="about-content">
          <div class="about-text">
            <span class="section-badge">🎓 毕业设计作品</span>
            <h2>儿童绘本阅读行为分析与成长跟踪系统</h2>
            <p class="about-intro">
              本系统是一个面向3-6岁儿童及其家长、教师的绘本阅读管理与成长跟踪平台。
              通过分析儿童的阅读行为数据，运用机器学习算法进行阅读类型分类，
              实现个性化绘本推荐，全方位跟踪儿童阅读成长。
            </p>
            <div class="about-goals">
              <h4>🎯 系统目标</h4>
              <ul>
                <li>帮助家长了解孩子的阅读习惯与兴趣偏好</li>
                <li>为教师提供班级阅读管理工具</li>
                <li>通过数据驱动促进儿童阅读能力发展</li>
                <li>构建科学的儿童阅读成长评价体系</li>
              </ul>
            </div>
            <div class="about-problems">
              <h4>💡 解决的问题</h4>
              <ul>
                <li>解决家长选择绘本困难的问题</li>
                <li>解决缺乏阅读数据记录的问题</li>
                <li>解决成长过程难以量化的问题</li>
                <li>解决家校阅读信息不对称的问题</li>
              </ul>
            </div>
          </div>
          <div class="about-visual">
            <div class="visual-card">
              <div class="visual-header">
                <span class="dot red"></span>
                <span class="dot yellow"></span>
                <span class="dot green"></span>
              </div>
              <div class="visual-body">
                <div class="code-line"><span class="keyword">const</span> system = {</div>
                <div class="code-line indent">name: <span class="string">'绘本阅读系统'</span>,</div>
                <div class="code-line indent">version: <span class="string">'1.0.0'</span>,</div>
                <div class="code-line indent">features: [</div>
                <div class="code-line indent-2"><span class="string">'行为分析'</span>,</div>
                <div class="code-line indent-2"><span class="string">'成长追踪'</span>,</div>
                <div class="code-line indent-2"><span class="string">'智能推荐'</span></div>
                <div class="code-line indent">]</div>
                <div class="code-line">};</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-main">
          <div class="footer-brand">
            <div class="brand-logo">
              <span class="logo-icon">📚</span>
              <span class="logo-text">绘本阅读系统</span>
            </div>
            <p class="brand-desc">
              让每一次阅读都有成长的意义<br />
              科学记录，智能推荐，陪伴成长
            </p>
          </div>
          <div class="footer-links">
            <div class="link-group">
              <h4>功能介绍</h4>
              <a href="#features">系统特色</a>
              <a href="#stats">数据概览</a>
              <a href="#books">热门绘本</a>
            </div>
            <div class="link-group">
              <h4>角色入口</h4>
              <a href="#" @click="selectRole('parent')">家长登录</a>
              <a href="#" @click="selectRole('teacher')">教师登录</a>
              <a href="#" @click="selectRole('admin')">管理员登录</a>
            </div>
            <div class="link-group">
              <h4>技术支持</h4>
              <a href="#about">关于系统</a>
              <a href="#">使用帮助</a>
              <a href="#">常见问题</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <div class="copyright">
            <p>© 2024 儿童绘本阅读行为分析与成长跟踪系统 | 毕业设计作品</p>
          </div>
          <div class="footer-legal">
            <a href="#">隐私政策</a>
            <a href="#">用户协议</a>
            <a href="#">联系我们</a>
          </div>
        </div>
      </div>
    </footer>

    <!-- 登录弹窗 -->
    <div class="modal-overlay" v-if="showLogin" @click.self="showLogin = false">
      <div class="modal-content login-modal">
        <button class="modal-close" @click="showLogin = false">✕</button>
        <div class="modal-header">
          <span class="modal-icon">🔐</span>
          <h2>欢迎回来</h2>
          <p>登录您的账户，继续阅读之旅</p>
        </div>
        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" v-model="loginForm.username" placeholder="请输入用户名" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input type="password" v-model="loginForm.password" placeholder="请输入密码" required />
          </div>
          <div class="form-options">
            <label class="checkbox">
              <input type="checkbox" v-model="loginForm.remember" />
              <span>记住我</span>
            </label>
            <a href="#" class="forgot">忘记密码？</a>
          </div>
          <button type="submit" class="submit-btn">
            登录
          </button>
        </form>
        <div class="modal-footer">
          <p>还没有账户？ <a href="#" @click="showLogin = false; showRegister = true">立即注册</a></p>
        </div>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <div class="modal-overlay" v-if="showRegister" @click.self="showRegister = false">
      <div class="modal-content register-modal">
        <button class="modal-close" @click="showRegister = false">✕</button>
        <div class="modal-header">
          <span class="modal-icon">✨</span>
          <h2>创建账户</h2>
          <p>选择您的角色，开启阅读之旅</p>
        </div>
        <div class="role-selector">
          <div
            v-for="role in registerRoles"
            :key="role.value"
            :class="['role-option', { active: registerForm.role === role.value }]"
            @click="registerForm.role = role.value"
          >
            <span class="role-icon">{{ role.icon }}</span>
            <span class="role-name">{{ role.label }}</span>
            <span class="role-desc">{{ role.desc }}</span>
          </div>
        </div>
        <form class="register-form" @submit.prevent="handleRegister">
          <div class="form-row">
            <div class="form-group">
              <label>用户名</label>
              <input type="text" v-model="registerForm.username" placeholder="请输入用户名" required />
            </div>
            <div class="form-group">
              <label>真实姓名</label>
              <input type="text" v-model="registerForm.realName" placeholder="请输入真实姓名" required />
            </div>
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="email" v-model="registerForm.email" placeholder="请输入邮箱" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="registerForm.password" placeholder="请输入密码" required />
            </div>
            <div class="form-group">
              <label>确认密码</label>
              <input type="password" v-model="registerForm.confirmPassword" placeholder="请确认密码" required />
            </div>
          </div>
          <button type="submit" class="submit-btn">
            注册
          </button>
        </form>
        <div class="modal-footer">
          <p>已有账户？ <a href="#" @click="showRegister = false; showLogin = true">立即登录</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const isScrolled = ref(false)
const showLogin = ref(false)
const showRegister = ref(false)
const activeTab = ref('hot')
const mobileMenuOpen = ref(false)

const animatedStats = reactive({
  totalBooks: 0,
  totalUsers: 0,
  totalReadingTime: 0
})

const todayStats = reactive({
  readingTime: 128,
  activeUsers: 856,
  booksRead: 2340,
  newReports: 45
})

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const registerForm = reactive({
  username: '',
  realName: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'PARENT'
})

const registerRoles = [
  { value: 'PARENT', label: '家长', icon: '👨‍👩‍👧', desc: '管理孩子阅读' },
  { value: 'TEACHER', label: '教师', icon: '👩‍🏫', desc: '管理班级阅读' },
  { value: 'ADMIN', label: '管理员', icon: '👨‍💼', desc: '系统管理' }
]

const bookTabs = [
  { value: 'hot', label: '热门绘本', icon: '🔥' },
  { value: 'age3', label: '3-4岁', icon: '👶' },
  { value: 'age4', label: '4-5岁', icon: '🧒' },
  { value: 'age5', label: '5-6岁', icon: '👦' }
]

const books = ref([
  { id: 1, title: '小熊的冒险', author: '张小明', category: 'animal', emoji: '🐻', ageRange: '3-4岁', readCount: 1256, rating: 5 },
  { id: 2, title: '太空探险记', author: '李小红', category: 'science', emoji: '🚀', ageRange: '5-6岁', readCount: 986, rating: 5 },
  { id: 3, title: '爱的抱抱', author: '王小刚', category: 'emotion', emoji: '❤️', ageRange: '3-4岁', readCount: 856, rating: 4 },
  { id: 4, title: '灰姑娘', author: '赵小美', category: 'fairy', emoji: '🏰', ageRange: '4-5岁', readCount: 756, rating: 5 },
  { id: 5, title: '森林音乐会', author: '刘小华', category: 'animal', emoji: '🐰', ageRange: '4-5岁', readCount: 654, rating: 4 },
  { id: 6, title: '神奇的水', author: '孙小芳', category: 'science', emoji: '💧', ageRange: '5-6岁', readCount: 542, rating: 4 },
  { id: 7, title: '我的好朋友', author: '周小杰', category: 'emotion', emoji: '🤝', ageRange: '4-5岁', readCount: 432, rating: 4 },
  { id: 8, title: '勇敢的小裁缝', author: '吴小燕', category: 'fairy', emoji: '✨', ageRange: '5-6岁', readCount: 398, rating: 4 }
])

const filteredBooks = computed(() => {
  if (activeTab.value === 'hot') return books.value
  const ageMap = { age3: '3-4岁', age4: '4-5岁', age5: '5-6岁' }
  return books.value.filter(b => b.ageRange === ageMap[activeTab.value])
})

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

const scrollTo = (id) => {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}

const animateNumbers = () => {
  const targets = { totalBooks: 280, totalUsers: 2560, totalReadingTime: 12800 }
  const duration = 2000
  const steps = 60
  const interval = duration / steps

  Object.keys(targets).forEach(key => {
    const target = targets[key]
    const increment = target / steps
    let step = 0
    const timer = setInterval(() => {
      step++
      animatedStats[key] = Math.min(Math.round(increment * step), target)
      if (step >= steps) {
        clearInterval(timer)
        animatedStats[key] = target
      }
    }, interval)
  })
}

const startReading = () => {
  showLogin.value = true
}

const selectRole = (role) => {
  registerForm.role = role.toUpperCase()
  showLogin.value = false
  showRegister.value = true
}

const viewBook = (book) => {
  // 如果已登录，跳转到绘本详情
  if (userStore.isLoggedIn) {
    router.push(`/app/books/category/${book.category}`)
  } else {
    // 未登录时提示登录
    showLogin.value = true
  }
}

const handleLogin = async () => {
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功！')
    router.push('/app/dashboard')
  } catch (error) {
    console.error('Login failed:', error)
  }
}

const handleRegister = async () => {
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }

  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      realName: registerForm.realName,
      email: registerForm.email,
      roleId: registerForm.role === 'PARENT' ? 3 : registerForm.role === 'TEACHER' ? 2 : 1
    })
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
    showLogin.value = true
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请重试')
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  animateNumbers()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>


.apple-home {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: #000;
  color: #fff;
  min-height: 100vh;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: all 0.4s cubic-bezier(0.28, 0.11, 0.32, 1);
  background: rgba(0, 0, 0, 0);
}

.nav-bar.scrolled {
  background: rgba(29, 29, 31, 0.72);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 40px;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  font-size: 32px;
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.3));
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  color: #f5f5f7;
  letter-spacing: -0.02em;
}

.nav-center {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  font-size: 17px;
  font-weight: 400;
  color: #f5f5f7;
  text-decoration: none;
  padding: 12px 20px;
  border-radius: 10px;
  transition: all 0.3s ease;
  opacity: 0.85;
}

.nav-link:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.1);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-link-btn {
  font-size: 17px;
  font-weight: 500;
  color: #f5f5f7;
  background: transparent;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0.85;
}

.nav-link-btn:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.1);
}

.nav-primary-btn {
  font-size: 17px;
  font-weight: 500;
  color: #1d1d1f;
  background: #f5f5f7;
  border: none;
  padding: 12px 28px;
  border-radius: 980px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-primary-btn:hover {
  background: #fff;
  transform: scale(1.02);
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  cursor: pointer;
  position: relative;
  z-index: 1001;
}

.menu-line {
  display: block;
  width: 24px;
  height: 2px;
  background: #f5f5f7;
  margin: 7px auto;
  transition: all 0.3s ease;
}

.menu-line.open:first-child {
  transform: rotate(45deg) translate(5px, 5px);
}

.menu-line.open:last-child {
  transform: rotate(-45deg) translate(5px, -5px);
}

/* 移动端菜单 */
.mobile-menu {
  display: none;
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.95);
  backdrop-filter: blur(20px);
  padding: 28px;
  transform: translateY(-100%);
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.28, 0.11, 0.32, 1);
}

.mobile-menu.open {
  transform: translateY(0);
  opacity: 1;
}

.mobile-link {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #f5f5f7;
  text-decoration: none;
  padding: 20px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-actions {
  display: flex;
  gap: 16px;
  margin-top: 28px;
}

.mobile-login,
.mobile-register {
  flex: 1;
  padding: 18px;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mobile-login {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #f5f5f7;
}

.mobile-register {
  background: #0071e3;
  border: none;
  color: #fff;
}

@media (max-width: 834px) {
  .nav-center {
    display: none;
  }

  .nav-right {
    display: none;
  }

  .mobile-menu-btn {
    display: block;
  }

  .mobile-menu {
    display: block;
  }
}

/* Hero 区域 */
.hero-section {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 120px 48px 80px;
  position: relative;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #ff6b6b, #feca57);
  top: -200px;
  left: -200px;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #48dbfb, #0984e3);
  bottom: -100px;
  right: 20%;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #a29bfe, #6c5ce7);
  top: 30%;
  right: 10%;
}

.hero-content {
  max-width: 600px;
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 980px;
  font-size: 14px;
  color: #a1a1a6;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 24px;
  letter-spacing: -0.02em;
}

.gradient-text {
  background: linear-gradient(90deg, #ff6b6b, #feca57, #48dbfb, #ff6b6b);
  background-size: 300% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradient 5s ease infinite;
}

@keyframes gradient {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.hero-desc {
  font-size: 18px;
  color: #a1a1a6;
  line-height: 1.6;
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 32px;
  background: #0071e3;
  border: none;
  border-radius: 980px;
  color: #fff;
  font-size: 17px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.primary-btn:hover {
  background: #0077ed;
  transform: scale(1.02);
}

.btn-arrow {
  transition: transform 0.3s;
}

.primary-btn:hover .btn-arrow {
  transform: translateX(4px);
}

.secondary-btn {
  padding: 16px 32px;
  background: transparent;
  border: 1px solid #424245;
  border-radius: 980px;
  color: #2997ff;
  font-size: 17px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.secondary-btn:hover {
  border-color: #2997ff;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 32px;
}

.hero-stats .stat-item {
  text-align: center;
}

.hero-stats .stat-num {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
}

.hero-stats .stat-label {
  font-size: 14px;
  color: #86868b;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #424245;
}

.hero-visual {
  position: relative;
  width: 500px;
  height: 500px;
}

.device-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 320px;
  height: 480px;
  background: linear-gradient(145deg, #1d1d1f, #2d2d2f);
  border-radius: 40px;
  padding: 12px;
  box-shadow: 0 50px 100px rgba(0, 0, 0, 0.5);
}

.device-screen {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #2c3e50, #1a1a2e);
  border-radius: 32px;
  overflow: hidden;
}

.screen-content {
  padding: 20px;
}

.book-preview {
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #ffecd2, #fcb69f);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.preview-emoji {
  font-size: 80px;
}

.preview-info {
  text-align: center;
}

.preview-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 12px;
}

.preview-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #a1a1a6;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: #424245;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #0071e3;
  border-radius: 2px;
}

.floating-cards {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.float-card {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  animation: float 6s ease-in-out infinite;
}

.card-1 { top: 10%; right: 0; animation-delay: 0s; }
.card-2 { top: 40%; right: -20%; animation-delay: 2s; }
.card-3 { bottom: 20%; right: 10%; animation-delay: 4s; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

/* 通用区块样式 */
.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 48px;
}

.section-header {
  margin-bottom: 48px;
}

.section-header.center {
  text-align: center;
}

.section-badge {
  display: inline-block;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 980px;
  font-size: 14px;
  color: #a1a1a6;
  margin-bottom: 16px;
}

.section-header h2 {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: -0.02em;
}

.section-header p {
  font-size: 18px;
  color: #86868b;
}

/* 数据总览 */
.stats-section {
  padding: 100px 0;
  background: linear-gradient(180deg, #000, #0a0a0a);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: linear-gradient(145deg, #1c1c1e, #2c2c2e);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.card-icon.blue { background: linear-gradient(135deg, #0071e3, #48dbfb); }
.card-icon.green { background: linear-gradient(135deg, #34c759, #55efc4); }
.card-icon.orange { background: linear-gradient(135deg, #ff9500, #feca57); }
.card-icon.purple { background: linear-gradient(135deg, #af52de, #a29bfe); }

.card-value {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
}

.card-value .unit {
  font-size: 16px;
  font-weight: 400;
  color: #86868b;
  margin-left: 4px;
}

.card-label {
  font-size: 14px;
  color: #86868b;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.card-trend.up {
  color: #34c759;
}

/* 热门绘本 */
.books-section {
  padding: 100px 0;
  background: #0a0a0a;
}

.books-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.tab-btn {
  padding: 10px 20px;
  background: transparent;
  border: 1px solid #424245;
  border-radius: 980px;
  color: #a1a1a6;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.tab-btn:hover {
  border-color: #0071e3;
  color: #0071e3;
}

.tab-btn.active {
  background: #0071e3;
  border-color: #0071e3;
  color: #fff;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.book-card {
  background: linear-gradient(145deg, #1c1c1e, #2c2c2e);
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s;
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.book-cover {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
}

.book-cover.animal {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
}
.book-cover.science {
  background: linear-gradient(135deg, #48dbfb 0%, #0984e3 100%);
}
.book-cover.emotion {
  background: linear-gradient(135deg, #fd79a8 0%, #e84393 100%);
}
.book-cover.fairy {
  background: linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%);
}

.cover-emoji {
  font-size: 64px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
  transition: transform 0.3s ease;
}

.book-card:hover .cover-emoji {
  transform: scale(1.1);
}

.book-rank {
  position: absolute;
  top: 12px;
  left: 12px;
}

.rank-medal {
  font-size: 24px;
}

.book-info {
  padding: 20px;
}

.book-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 12px;
}

.book-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #a1a1a6;
}

.book-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stars .star {
  color: #424245;
  font-size: 14px;
}

.stars .star.filled {
  color: #ffd93d;
}

.rating-value {
  font-size: 13px;
  color: #a1a1a6;
}

.books-more {
  text-align: center;
  margin-top: 48px;
}

.more-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: transparent;
  border: 1px solid #424245;
  border-radius: 980px;
  color: #2997ff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.more-btn:hover {
  border-color: #2997ff;
  background: rgba(41, 151, 255, 0.1);
}

.more-arrow {
  transition: transform 0.3s;
}

.more-btn:hover .more-arrow {
  transform: translateX(4px);
}

/* 快速入口 */
.quick-entry-section {
  padding: 100px 0;
  background: linear-gradient(180deg, #0a0a0a, #000);
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.role-card {
  background: linear-gradient(145deg, #1c1c1e, #2c2c2e);
  border-radius: 24px;
  padding: 32px;
  text-align: center;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.role-card:hover {
  transform: translateY(-8px);
  border-color: #424245;
}

.role-card.teacher {
  background: linear-gradient(145deg, #1a2a3a, #2a3a4a);
  border-color: #0071e3;
}

.role-badge {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 12px;
  background: #0071e3;
  border-radius: 980px;
  font-size: 12px;
  font-weight: 500;
}

.role-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.role-card h3 {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
}

.role-card > p {
  font-size: 14px;
  color: #86868b;
  margin-bottom: 24px;
}

.role-features {
  text-align: left;
  margin-bottom: 24px;
}

.role-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  font-size: 14px;
  color: #a1a1a6;
}

.role-features li span {
  color: #34c759;
}

.role-btn {
  width: 100%;
  padding: 14px;
  background: #0071e3;
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.role-btn:hover {
  background: #0077ed;
}

/* 系统特色 */
.features-section {
  padding: 100px 0;
  background: #000;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.feature-card {
  background: linear-gradient(145deg, #1c1c1e, #2c2c2e);
  border-radius: 24px;
  padding: 32px;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  margin-bottom: 20px;
}

.icon-bg {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.icon-bg.blue { background: linear-gradient(135deg, #0071e3, #48dbfb); }
.icon-bg.green { background: linear-gradient(135deg, #34c759, #55efc4); }
.icon-bg.orange { background: linear-gradient(135deg, #ff9500, #feca57); }
.icon-bg.purple { background: linear-gradient(135deg, #af52de, #a29bfe); }
.icon-bg.pink { background: linear-gradient(135deg, #ff2d55, #ff6b6b); }
.icon-bg.cyan { background: linear-gradient(135deg, #5ac8fa, #48dbfb); }

.feature-card h3 {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 12px;
}

.feature-card p {
  font-size: 14px;
  color: #86868b;
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-tags span {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  font-size: 12px;
  color: #a1a1a6;
}

/* 技术架构 */
.tech-section {
  padding: 100px 0;
  background: linear-gradient(180deg, #000, #0a0a0a);
}

.tech-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
}

.tech-left h2 {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
}

.tech-left > p {
  font-size: 18px;
  color: #86868b;
  margin-bottom: 40px;
}

.tech-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tech-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(145deg, #1c1c1e, #2c2c2e);
  border-radius: 16px;
}

.tech-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.tech-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.tech-info p {
  font-size: 13px;
  color: #86868b;
}

.architecture-diagram {
  background: linear-gradient(145deg, #1c1c1e, #2c2c2e);
  border-radius: 24px;
  padding: 32px;
}

.layer {
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 8px;
}

.layer-title {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #86868b;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.layer-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.layer-items span {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  font-size: 12px;
  color: #a1a1a6;
}

.layer.frontend { background: rgba(0, 113, 227, 0.2); }
.layer.gateway { background: rgba(52, 199, 89, 0.2); }
.layer.service { background: rgba(255, 149, 0, 0.2); }
.layer.data { background: rgba(175, 82, 222, 0.2); }

.layer-arrow {
  text-align: center;
  color: #424245;
  font-size: 20px;
  margin: 8px 0;
}

/* 关于我们 */
.about-section {
  padding: 100px 0;
  background: #0a0a0a;
}

.about-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
}

.about-text h2 {
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 16px;
}

.about-intro {
  font-size: 16px;
  color: #a1a1a6;
  line-height: 1.8;
  margin-bottom: 32px;
}

.about-goals,
.about-problems {
  margin-bottom: 24px;
}

.about-goals h4,
.about-problems h4 {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 16px;
}

.about-goals ul,
.about-problems ul {
  list-style: none;
}

.about-goals li,
.about-problems li {
  position: relative;
  padding-left: 24px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #86868b;
}

.about-goals li::before,
.about-problems li::before {
  content: '→';
  position: absolute;
  left: 0;
  color: #0071e3;
}

.visual-card {
  background: #1c1c1e;
  border-radius: 16px;
  overflow: hidden;
}

.visual-header {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #2c2c2e;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.red { background: #ff5f57; }
.dot.yellow { background: #febc2e; }
.dot.green { background: #28c840; }

.visual-body {
  padding: 24px;
  font-family: 'SF Mono', 'Monaco', monospace;
  font-size: 14px;
}

.code-line {
  line-height: 1.8;
  color: #86868b;
}

.code-line.indent { padding-left: 24px; }
.code-line.indent-2 { padding-left: 48px; }

.keyword { color: #ff9500; }
.string { color: #34c759; }

/* 页脚 */
.footer {
  background: #000;
  border-top: 1px solid #1d1d1f;
  padding: 48px 0 24px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 48px;
}

.footer-main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 80px;
  margin-bottom: 48px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.brand-desc {
  font-size: 14px;
  color: #86868b;
  line-height: 1.6;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.link-group h4 {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.link-group a {
  display: block;
  font-size: 14px;
  color: #86868b;
  text-decoration: none;
  margin-bottom: 12px;
  transition: color 0.3s;
}

.link-group a:hover {
  color: #fff;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid #1d1d1f;
}

.copyright p {
  font-size: 13px;
  color: #86868b;
}

.footer-legal {
  display: flex;
  gap: 24px;
}

.footer-legal a {
  font-size: 13px;
  color: #86868b;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-legal a:hover {
  color: #fff;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.modal-content {
  background: #1c1c1e;
  border-radius: 24px;
  width: 100%;
  max-width: 440px;
  position: relative;
  padding: 40px;
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: #86868b;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.modal-header {
  text-align: center;
  margin-bottom: 32px;
}

.modal-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.modal-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
}

.modal-header p {
  font-size: 14px;
  color: #86868b;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #a1a1a6;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  background: #2c2c2e;
  border: 1px solid #3a3a3c;
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  transition: all 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #0071e3;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #a1a1a6;
}

.forgot {
  font-size: 14px;
  color: #2997ff;
  text-decoration: none;
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: #0071e3;
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 17px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  background: #0077ed;
}

.modal-footer {
  text-align: center;
  margin-top: 24px;
}

.modal-footer p {
  font-size: 14px;
  color: #86868b;
}

.modal-footer a {
  color: #2997ff;
  text-decoration: none;
}

/* 注册弹窗角色选择 */
.role-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.role-option {
  padding: 16px;
  background: #2c2c2e;
  border: 2px solid transparent;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.role-option:hover {
  border-color: #424245;
}

.role-option.active {
  border-color: #0071e3;
  background: rgba(0, 113, 227, 0.1);
}

.role-option .role-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

.role-option .role-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.role-option .role-desc {
  display: block;
  font-size: 11px;
  color: #86868b;
}

/* 响应式 */
@media (max-width: 1024px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding-top: 100px;
  }

  .hero-content {
    max-width: 100%;
  }

  .hero-visual {
    display: none;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .role-cards {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .tech-content {
    grid-template-columns: 1fr;
  }

  .about-content {
    grid-template-columns: 1fr;
  }

  .footer-main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .hero-title {
    font-size: 36px;
  }

  .section-header h2 {
    font-size: 32px;
  }

  .stats-grid,
  .books-grid,
  .features-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .role-selector {
    grid-template-columns: 1fr;
  }
}
</style>
