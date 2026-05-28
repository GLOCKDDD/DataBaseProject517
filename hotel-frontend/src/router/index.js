import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '控制台', icon: 'Odometer', roles: ['admin', 'frontdesk'] }
      },
      // === 权限管理（仅管理员） ===
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/auth/Users.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['admin'] }
      },
      // === 前台业务模块 ===
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/customers/Index.vue'),
        meta: { title: '客户信息管理', icon: 'UserFilled', roles: ['admin', 'frontdesk'] }
      },
      {
        path: 'reservations',
        name: 'Reservations',
        component: () => import('@/views/reservations/Index.vue'),
        meta: { title: '预订管理', icon: 'Calendar', roles: ['admin', 'frontdesk'] }
      },
      {
        path: 'checkins',
        name: 'Checkins',
        component: () => import('@/views/checkins/Index.vue'),
        meta: { title: '入住管理', icon: 'House', roles: ['admin', 'frontdesk'] }
      },
      {
        path: 'room-changes',
        name: 'RoomChanges',
        component: () => import('@/views/roomChanges/Index.vue'),
        meta: { title: '换房管理', icon: 'Switch', roles: ['admin', 'frontdesk'] }
      },
      {
        path: 'billing',
        name: 'Billing',
        component: () => import('@/views/billing/Index.vue'),
        meta: { title: '结账管理', icon: 'Money', roles: ['admin', 'frontdesk'] }
      },
      // === 后台管理模块 ===
      {
        path: 'room-types',
        name: 'RoomTypes',
        component: () => import('@/views/roomTypes/Index.vue'),
        meta: { title: '客房类型管理', icon: 'Grid', roles: ['admin'] }
      },
      {
        path: 'rooms',
        name: 'Rooms',
        component: () => import('@/views/rooms/Index.vue'),
        meta: { title: '客房信息管理', icon: 'OfficeBuilding', roles: ['admin'] }
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('@/views/members/Index.vue'),
        meta: { title: '会员管理', icon: 'Medal', roles: ['admin'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：登录鉴权 + 角色权限
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 酒店管理系统` : '酒店管理系统'

  const authStore = useAuthStore()

  if (to.meta.requiresAuth !== false && !authStore.isLoggedIn) {
    next('/login')
    return
  }

  // 已登录用户访问登录页则跳转首页
  if (to.path === '/login' && authStore.isLoggedIn) {
    next('/dashboard')
    return
  }

  // 角色权限校验
  if (to.meta.roles && !to.meta.roles.includes(authStore.currentUser?.role)) {
    next('/dashboard')
    return
  }

  next()
})

export default router
