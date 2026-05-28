<template>
  <div class="page-container">
    <div class="page-header">
      <h2>客户信息管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>录入客户
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名/身份证号/电话"
        :prefix-icon="Search"
        clearable
        style="width: 300px;"
        @input="handleSearch"
      />
      <el-select v-model="searchLevel" placeholder="会员等级" clearable @change="handleSearch" style="width: 140px;">
        <el-option label="普通" value="普通" />
        <el-option label="VIP" value="VIP" />
        <el-option label="贵宾" value="贵宾" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table :data="customerList" border stripe>
        <el-table-column prop="customer_id" label="编号" width="60" align="center" />
        <el-table-column prop="id_number" label="身份证号" width="190" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="membership_level" label="会员等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="memberLevelType(row.membership_level)" size="small">
              {{ row.membership_level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" align="center">
          <template #default="{ row }">
            <span class="points-value">{{ row.points }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="录入时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="viewHistory(row)">历史记录</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑客户' : '客户信息录入'"
      width="550px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="身份证号" prop="id_number">
          <el-input v-model="form.id_number" placeholder="18位身份证号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="11位手机号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="会员等级" v-if="isEdit">
          <el-select v-model="form.membership_level">
            <el-option label="普通" value="普通" />
            <el-option label="VIP" value="VIP" />
            <el-option label="贵宾" value="贵宾" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分" v-if="isEdit">
          <el-input-number v-model="form.points" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 历史入住记录对话框 -->
    <el-dialog v-model="historyVisible" title="历史入住记录" width="700px">
      <el-table :data="historyData" border stripe size="small">
        <el-table-column prop="checkin_id" label="登记编号" width="80" />
        <el-table-column prop="room_number" label="房间号" width="80" />
        <el-table-column prop="checkin_time" label="入住时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkin_time) }}</template>
        </el-table-column>
        <el-table-column prop="checkout_time" label="退房时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkout_time) || '未退房' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="checkinStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="historyData.length === 0" description="暂无入住记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { customerService } from '@/api/services'
import { db } from '@/api/mock'
import { formatDateTime, MEMBER_LEVEL_TYPE, CHECKIN_STATUS_TYPE } from '@/utils/helpers'
import { phoneValidator, idNumberValidator } from '@/utils/validators'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const customerList = ref([])
const dialogVisible = ref(false)
const historyVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)
const historyData = ref([])
const searchKeyword = ref('')
const searchLevel = ref('')

const form = reactive({
  id_number: '',
  name: '',
  phone: '',
  address: '',
  membership_level: '普通',
  points: 0
})

const rules = {
  id_number: [{ required: true, validator: idNumberValidator, trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, validator: phoneValidator, trigger: 'blur' }]
}

function memberLevelType(level) {
  return MEMBER_LEVEL_TYPE[level] || ''
}

function checkinStatusType(status) {
  return CHECKIN_STATUS_TYPE[status] || ''
}

onMounted(() => {
  loadCustomers()
})

async function loadCustomers() {
  const params = {}
  if (searchKeyword.value) params.keyword = searchKeyword.value
  let list = await customerService.getList(params)
  if (searchLevel.value) {
    list = list.filter(c => c.membership_level === searchLevel.value)
  }
  customerList.value = list
}

function handleSearch() {
  loadCustomers()
}

function openDialog(customer = null) {
  isEdit.value = !!customer
  editId.value = customer?.customer_id || null
  form.id_number = customer?.id_number || ''
  form.name = customer?.name || ''
  form.phone = customer?.phone || ''
  form.address = customer?.address || ''
  form.membership_level = customer?.membership_level || '普通'
  form.points = customer?.points || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await customerService.update(editId.value, {
        name: form.name,
        phone: form.phone,
        address: form.address,
        membership_level: form.membership_level,
        points: form.points
      })
      ElMessage.success('更新成功')
    } else {
      await customerService.create({ ...form })
      ElMessage.success('录入成功')
    }
    dialogVisible.value = false
    loadCustomers()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

async function viewHistory(customer) {
  const history = await customerService.getHistory(customer.customer_id)
  const rooms = db.getTable('rooms')
  historyData.value = history.map(h => ({
    ...h,
    room_number: rooms.find(r => r.room_id === h.room_id)?.room_number || '未知'
  }))
  historyVisible.value = true
}
</script>

<style scoped>
.points-value {
  font-weight: 600;
  color: var(--color-gold);
  font-family: var(--font-mono);
}
</style>
