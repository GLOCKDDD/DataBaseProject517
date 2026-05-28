<template>
  <el-container class="layout-container">
    <!-- 左侧导航菜单 -->
    <el-aside :width="isCollapse ? 'var(--sidebar-collapsed-width)' : 'var(--sidebar-width)'" class="layout-aside">
      <div class="logo-area">
        <div class="logo-icon-wrap">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 21V7l9-4 9 4v14H3z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M9 21V13h6v8" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M1 21h22" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="9" r="1.5" fill="currentColor"/>
          </svg>
        </div>
        <transition name="fade-text">
          <span v-show="!isCollapse" class="logo-text">
            <span class="logo-main">Grand Hotel</span>
            <span class="logo-sub">管理后台</span>
          </span>
        </transition>
      </div>

      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapse"
        router
        background-color="transparent"
        text-color="var(--sidebar-text)"
        active-text-color="var(--sidebar-text-active)"
        class="side-menu"
        :collapse-transition="false"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-menu-item :index="item.path" class="menu-item-custom">
            <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>

      <!-- 侧边栏底部 -->
      <div class="sidebar-footer" v-show="!isCollapse">
        <div class="sidebar-footer-text">v1.0.0</div>
      </div>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container class="main-wrapper">
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="isCollapse = !isCollapse"
            :size="18"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag
            :type="authStore.currentUser?.role === 'admin' ? '' : 'info'"
            size="small"
            class="role-tag"
            :effect="authStore.currentUser?.role === 'admin' ? 'dark' : 'light'"
          >
            {{ authStore.currentUser?.role === 'admin' ? '管理员' : '前台操作员' }}
          </el-tag>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <div class="user-avatar">{{ (authStore.currentUser?.username || 'U')[0].toUpperCase() }}</div>
              <span class="user-name">{{ authStore.currentUser?.username }}</span>
              <el-icon class="el-icon--right" :size="12"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapse = ref(false)

// 根据用户角色动态生成菜单
const menuItems = computed(() => {
  const role = authStore.currentUser?.role
  const allItems = [
    { path: '/dashboard', title: '控制台', icon: 'Odometer', roles: ['admin', 'frontdesk'] },
    { path: '/users', title: '用户管理', icon: 'User', roles: ['admin'] },
    { path: '/customers', title: '客户信息管理', icon: 'UserFilled', roles: ['admin', 'frontdesk'] },
    { path: '/reservations', title: '预订管理', icon: 'Calendar', roles: ['admin', 'frontdesk'] },
    { path: '/checkins', title: '入住管理', icon: 'House', roles: ['admin', 'frontdesk'] },
    { path: '/room-changes', title: '换房管理', icon: 'Switch', roles: ['admin', 'frontdesk'] },
    { path: '/billing', title: '结账管理', icon: 'Money', roles: ['admin', 'frontdesk'] },
    { path: '/room-types', title: '客房类型管理', icon: 'Grid', roles: ['admin'] },
    { path: '/rooms', title: '客房信息管理', icon: 'OfficeBuilding', roles: ['admin'] },
    { path: '/members', title: '会员管理', icon: 'Medal', roles: ['admin'] }
  ]
  return allItems.filter(item => item.roles.includes(role))
})

const currentRoute = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

function handleCommand(command) {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: var(--sidebar-bg);
  transition: width var(--transition-slow);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
  box-shadow: 2px 0 8px rgba(0,0,0,0.08);
}

.logo-area {
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}

.logo-icon-wrap {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  border-radius: var(--radius-md);
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
  overflow: hidden;
}

.logo-main {
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.logo-sub {
  color: var(--color-gold);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 1px;
  margin-top: 1px;
}

.fade-text-enter-active,
.fade-text-leave-active {
  transition: opacity 0.2s;
}
.fade-text-enter-from,
.fade-text-leave-to {
  opacity: 0;
}

.side-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.side-menu:not(.el-menu--collapse) {
  width: var(--sidebar-width);
}

/* 菜单项自定义样式 */
:deep(.menu-item-custom) {
  margin: 2px 8px;
  border-radius: var(--radius-md);
  height: 42px;
  line-height: 42px;
  transition: all var(--transition-fast);
}

:deep(.menu-item-custom:hover) {
  background: rgba(255,255,255,0.06) !important;
}

:deep(.menu-item-custom.is-active) {
  background: var(--sidebar-active-bg) !important;
  color: #fff !important;
  position: relative;
}

:deep(.menu-item-custom.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: var(--color-primary);
  border-radius: 0 2px 2px 0;
}

:deep(.menu-item-custom .menu-icon) {
  font-size: 17px;
}

:deep(.el-menu--collapse .menu-item-custom) {
  margin: 2px 6px;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}

.sidebar-footer-text {
  color: rgba(255,255,255,0.25);
  font-size: 11px;
  text-align: center;
  letter-spacing: 0.5px;
}

/* 右侧容器 */
.main-wrapper {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-card);
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  padding: 0 24px;
  height: 60px;
  flex-shrink: 0;
  z-index: 5;
  border-bottom: 1px solid var(--border-light);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: var(--text-tertiary);
  transition: color var(--transition-fast);
  padding: 4px;
  border-radius: var(--radius-sm);
}

.collapse-btn:hover {
  color: var(--color-primary);
  background: var(--color-primary-lighter);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.role-tag {
  font-size: 12px;
  letter-spacing: 0.3px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-primary);
  font-size: 14px;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.user-info:hover {
  background: var(--bg-hover);
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-weight: 500;
  font-size: 13.5px;
}

.layout-main {
  background: var(--bg-page);
  padding: 0;
  overflow-y: auto;
  flex: 1;
}

/* 页面切换动画 */
.page-fade-enter-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-leave-active {
  transition: opacity 0.15s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-fade-leave-to {
  opacity: 0;
}
</style>
