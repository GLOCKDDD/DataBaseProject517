<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="userList" border stripe>
        <el-table-column prop="user_id" label="编号" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-cell-avatar">{{ row.username[0].toUpperCase() }}</div>
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="身份" width="130">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? '' : 'info'" size="small" :effect="row.role === 'admin' ? 'dark' : 'light'">
              {{ row.role === 'admin' ? '管理员' : '前台操作员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permissions" label="权限" min-width="200">
          <template #default="{ row }">
            <template v-if="row.role === 'admin'">
              <el-tag type="danger" size="small" effect="dark">全部权限</el-tag>
            </template>
            <template v-else-if="row.permissions">
              <el-tag
                v-for="perm in parsePerms(row.permissions)"
                :key="perm"
                size="small"
                style="margin: 2px 4px 2px 0;"
              >{{ permLabel(perm) }}</el-tag>
            </template>
            <span v-else style="color: var(--text-tertiary);">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column prop="last_login" label="最后登录" width="170">
          <template #default="{ row }">{{ formatDateTime(row.last_login) || '从未登录' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="openPermDialog(row)" v-if="row.role !== 'admin'">
              权限设置
            </el-button>
            <el-popconfirm
              title="确认删除该用户？"
              @confirm="handleDelete(row.user_id)"
              v-if="row.user_id !== authStore.currentUser?.user_id"
            >
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password_hash" v-if="!isEdit">
          <el-input v-model="form.password_hash" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="身份" prop="role">
          <el-select v-model="form.role" placeholder="请选择身份">
            <el-option label="管理员" value="admin" />
            <el-option label="前台操作员" value="frontdesk" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog v-model="permDialogVisible" title="权限设置" width="500px" destroy-on-close>
      <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 14px;">
        为用户 <strong style="color: var(--text-primary);">{{ permUser?.username }}</strong> 分配操作权限：
      </p>
      <div class="perm-list">
        <el-checkbox-group v-model="selectedPerms">
          <div v-for="perm in allPerms" :key="perm.value" class="perm-item">
            <el-checkbox :label="perm.value">{{ perm.label }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userService } from '@/api/services'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/helpers'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const userList = ref([])
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)
const permUser = ref(null)
const selectedPerms = ref([])

const form = reactive({
  username: '',
  password_hash: '',
  role: 'frontdesk'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password_hash: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

const allPerms = [
  { label: '客户信息录入', value: 'customer_add' },
  { label: '预订管理', value: 'reservation_manage' },
  { label: '入住管理', value: 'checkin_manage' },
  { label: '换房管理', value: 'room_change' },
  { label: '结账管理', value: 'billing' },
  { label: '客户查询', value: 'customer_query' }
]

const permLabelMap = {
  customer_add: '客户录入',
  reservation_manage: '预订管理',
  checkin_manage: '入住管理',
  room_change: '换房管理',
  billing: '结账管理',
  customer_query: '客户查询'
}

function permLabel(key) {
  return permLabelMap[key] || key
}

function parsePerms(perms) {
  if (!perms) return []
  try {
    return typeof perms === 'string' ? JSON.parse(perms) : perms
  } catch {
    return []
  }
}

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  userList.value = await userService.getList()
}

function openDialog(user = null) {
  isEdit.value = !!user
  editId.value = user?.user_id || null
  form.username = user?.username || ''
  form.password_hash = ''
  form.role = user?.role || 'frontdesk'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await userService.update(editId.value, {
        username: form.username,
        role: form.role
      })
      ElMessage.success('更新成功')
    } else {
      await userService.create({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(userId) {
  try {
    await userService.remove(userId)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function openPermDialog(user) {
  permUser.value = user
  selectedPerms.value = parsePerms(user.permissions)
  permDialogVisible.value = true
}

async function handlePermSave() {
  try {
    await userService.update(permUser.value.user_id, {
      permissions: JSON.stringify(selectedPerms.value)
    })
    ElMessage.success('权限设置成功')
    permDialogVisible.value = false
    loadUsers()
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-cell-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-lighter), #D6E4FF);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.perm-list {
  background: var(--bg-page);
  border-radius: var(--radius-md);
  padding: 16px;
}

.perm-item {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.perm-item:hover {
  background: var(--bg-hover);
}
</style>
