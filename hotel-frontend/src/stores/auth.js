import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { db } from '@/api/mock'

export const useAuthStore = defineStore('auth', () => {
  // 当前登录用户信息
  const currentUser = ref(JSON.parse(sessionStorage.getItem('currentUser') || 'null'))

  // 是否已登录
  const isLoggedIn = computed(() => !!currentUser.value)

  // 登录
  function login(username, password) {
    const users = db.getTable('users')
    const user = users.find(u => u.username === username && u.password_hash === password)
    if (!user) {
      return { success: false, message: '用户名或密码错误' }
    }
    // 更新最后登录时间
    user.last_login = new Date().toISOString()
    db.updateRecord('users', user.user_id, { last_login: user.last_login })

    const userInfo = { ...user }
    delete userInfo.password_hash // 不在前端存储密码
    currentUser.value = userInfo
    sessionStorage.setItem('currentUser', JSON.stringify(userInfo))
    return { success: true, user: userInfo }
  }

  // 登出
  function logout() {
    currentUser.value = null
    sessionStorage.removeItem('currentUser')
  }

  // 检查是否有某操作权限
  function hasPermission(perm) {
    if (!currentUser.value) return false
    if (currentUser.value.role === 'admin') return true // 管理员拥有全部权限
    const perms = currentUser.value.permissions
    if (!perms) return true // 未设置权限则默认允许前台功能
    try {
      const permList = typeof perms === 'string' ? JSON.parse(perms) : perms
      return permList.includes(perm)
    } catch {
      return true
    }
  }

  return { currentUser, isLoggedIn, login, logout, hasPermission }
})
