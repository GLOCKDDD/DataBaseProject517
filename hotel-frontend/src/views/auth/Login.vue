<template>
  <div class="login-container">
    <!-- 装饰线条 -->
    <div class="deco-line deco-line-1"></div>
    <div class="deco-line deco-line-2"></div>

    <div class="login-card">
      <!-- 顶部装饰条 -->
      <div class="card-accent"></div>

      <div class="login-header">
        <div class="login-logo">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 21V7l9-4 9 4v14H3z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
            <path d="M9 21V13h6v8" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
            <path d="M1 21h22" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            <circle cx="12" cy="9" r="1.5" fill="currentColor"/>
          </svg>
        </div>
        <h1>Grand Hotel</h1>
        <p class="login-subtitle">酒店管理系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        @keyup.enter="handleLogin"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-hint">
        <div class="hint-divider">
          <span>测试账号</span>
        </div>
        <div class="hint-accounts">
          <div class="hint-account">
            <span class="hint-role">管理员</span>
            <span class="hint-cred">admin / admin123</span>
          </div>
          <div class="hint-account">
            <span class="hint-role">前台</span>
            <span class="hint-cred">front1 / 123456</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="login-footer">
      &copy; 2026 Grand Hotel Management System
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  setTimeout(async () => {
    const result = await authStore.login(form.username, form.password)
    if (result.success) {
      ElMessage.success(`欢迎回来，${result.user.username}！`)
      router.push('/dashboard')
    } else {
      ElMessage.error(result.message)
    }
    loading.value = false
  }, 500)
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--sidebar-bg);
  position: relative;
  overflow: hidden;
}

/* 装饰线条 */
.deco-line {
  position: absolute;
  pointer-events: none;
}

.deco-line-1 {
  top: -10%;
  right: 15%;
  width: 1px;
  height: 140%;
  background: linear-gradient(180deg, transparent, rgba(22,93,255,0.12), transparent);
  transform: rotate(15deg);
}

.deco-line-2 {
  top: -10%;
  right: 25%;
  width: 1px;
  height: 140%;
  background: linear-gradient(180deg, transparent, rgba(197,165,90,0.08), transparent);
  transform: rotate(15deg);
}

/* 卡片 */
.login-card {
  width: 400px;
  padding: 40px 36px 32px;
  background: rgba(255,255,255,0.97);
  border-radius: var(--radius-xl);
  box-shadow: 0 16px 48px rgba(0,0,0,0.25), 0 4px 12px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(20px);
  animation: cardEnter 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-gold), var(--color-primary));
}

/* 头部 */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  border-radius: var(--radius-lg);
  color: #fff;
  box-shadow: 0 4px 12px rgba(22,93,255,0.25);
}

.login-header h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.login-subtitle {
  color: var(--text-tertiary);
  font-size: 13px;
  margin-top: 4px;
  letter-spacing: 2px;
}

/* 表单 */
.login-form {
  margin-bottom: 8px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-secondary);
  font-size: 13px;
  padding-bottom: 4px;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--border-color) inset;
  padding: 4px 12px;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary-light) inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 3px rgba(22,93,255,0.08);
}

.login-btn {
  width: 100%;
  margin-top: 4px;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  border: none;
  box-shadow: 0 4px 12px rgba(22,93,255,0.2);
  transition: all var(--transition-fast);
}

.login-btn:hover {
  box-shadow: 0 6px 16px rgba(22,93,255,0.3);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

/* 提示区 */
.login-hint {
  margin-top: 20px;
}

.hint-divider {
  text-align: center;
  position: relative;
  margin-bottom: 16px;
}

.hint-divider::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  height: 1px;
  background: var(--border-color);
}

.hint-divider span {
  position: relative;
  background: rgba(255,255,255,0.97);
  padding: 0 12px;
  font-size: 12px;
  color: var(--text-tertiary);
  letter-spacing: 0.5px;
}

.hint-accounts {
  display: flex;
  gap: 12px;
}

.hint-account {
  flex: 1;
  padding: 10px 12px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  text-align: center;
  border: 1px solid var(--border-light);
}

.hint-role {
  display: block;
  font-size: 11px;
  color: var(--text-tertiary);
  margin-bottom: 3px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.hint-cred {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: var(--font-mono);
  font-weight: 500;
}

/* 底部 */
.login-footer {
  position: absolute;
  bottom: 20px;
  text-align: center;
  color: rgba(255,255,255,0.2);
  font-size: 12px;
  letter-spacing: 0.5px;
}
</style>
