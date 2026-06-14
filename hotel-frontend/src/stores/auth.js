import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import http from '@/api/http'

export const useAuthStore = defineStore('auth', () => {
  // 从 sessionStorage 恢复登录状态（刷新页面不丢失）
  const currentUser = ref(JSON.parse(sessionStorage.getItem('currentUser') || 'null'))

  const isLoggedIn = computed(() => !!currentUser.value)

  // 登录：调用后端 /api/user/login，后端返回 { user: {...} }
  async function login(username, password) {
    try {
      const data = await http.post('/user/login', { username, password })
      // 后端返回 Map，user 字段包含用户对象
      const userInfo = data?.user ?? data
      if (!userInfo) return { success: false, message: '登录失败，未获取到用户信息' }

      currentUser.value = userInfo
      sessionStorage.setItem('currentUser', JSON.stringify(userInfo))
      return { success: true, user: userInfo }
    } catch (e) {
      return { success: false, message: e.message || '用户名或密码错误' }
    }
  }

  function logout() {
    currentUser.value = null
    sessionStorage.removeItem('currentUser')
  }

  // 权限检查：admin 拥有全部权限，前台人员按 permissions 字段判断
  function hasPermission(perm) {
    if (!currentUser.value) return false
    if (currentUser.value.role === 'admin') return true
    const perms = currentUser.value.permissions
    if (!perms) return true
    try {
      const permList = typeof perms === 'string' ? JSON.parse(perms) : perms
      return permList.includes(perm)
    } catch {
      return true
    }
  }

  return { currentUser, isLoggedIn, login, logout, hasPermission }
})
