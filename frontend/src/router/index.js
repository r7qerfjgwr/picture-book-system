import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页', public: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/app',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/app/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页看板' }
      },
      {
        path: 'children',
        name: 'Children',
        component: () => import('@/views/children/index.vue'),
        meta: { title: '儿童管理', roles: ['PARENT'] }
      },
      {
        path: 'children/detail/:id',
        name: 'ChildDetail',
        component: () => import('@/views/children/detail.vue'),
        meta: { title: '儿童详情', roles: ['PARENT'] }
      },
      {
        path: 'books',
        name: 'Books',
        component: () => import('@/views/books/index.vue'),
        meta: { title: '绘本管理', roles: ['ADMIN'] }
      },
      {
        path: 'books/category/:category',
        name: 'BooksCategory',
        component: () => import('@/views/books/category.vue'),
        meta: { title: '绘本分类' }
      },
      {
        path: 'reading',
        name: 'Reading',
        component: () => import('@/views/reading/index.vue'),
        meta: { title: '阅读中' }
      },
      {
        path: 'reading-logs',
        name: 'ReadingLogs',
        component: () => import('@/views/reading-logs/index.vue'),
        meta: { title: '阅读记录' }
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/analysis/index.vue'),
        meta: { title: '行为分析' }
      },
      {
        path: 'growth',
        name: 'Growth',
        component: () => import('@/views/growth/index.vue'),
        meta: { title: '成长跟踪' }
      },
      {
        path: 'recommendations',
        name: 'Recommendations',
        component: () => import('@/views/recommendations/index.vue'),
        meta: { title: '绘本推荐' }
      },
      {
        path: 'class',
        name: 'ClassManagement',
        component: () => import('@/views/class/index.vue'),
        meta: { title: '班级管理', roles: ['TEACHER', 'ADMIN'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/index.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/notification/index.vue'),
        meta: { title: '通知发送', roles: ['ADMIN'] }
      },
      {
        path: 'children-analysis',
        name: 'ChildrenAnalysis',
        component: () => import('@/views/children-analysis/index.vue'),
        meta: { title: '儿童分析概览', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 绘本阅读系统` : '绘本阅读系统'

  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 公开页面直接访问
  if (to.meta.public) {
    // 已登录用户访问首页时跳转到仪表盘
    if (to.path === '/' && token) {
      next('/app/dashboard')
      return
    }
    next()
    return
  }

  // 需要登录的页面
  if (!token) {
    next('/login')
    return
  }

  // 检查权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (to.meta.roles.includes(role)) {
      next()
    } else {
      next('/app/dashboard')
    }
  } else {
    next()
  }
})

export default router
