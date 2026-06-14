<template>
  <div class="page-container">
    <div class="page-header">
      <h2>入住管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>办理入住
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索房间号/客户姓名"
        :prefix-icon="Search"
        clearable
        style="width: 280px;"
        @input="loadData"
      />
      <el-select v-model="searchStatus" placeholder="入住状态" clearable @change="loadData" style="width: 140px;">
        <el-option v-for="s in CHECKIN_STATUS_LIST" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table :data="list" border stripe>
        <el-table-column prop="checkin_id" label="登记编号" width="80" align="center" />
        <el-table-column prop="room_number" label="房间号" width="80" />
        <el-table-column prop="room_type_name" label="房型" width="90" />
        <el-table-column label="入住宾客" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="g in row.guests" :key="g.guest_id" size="small" style="margin: 2px 4px 2px 0;"
              :type="g.is_primary ? '' : 'info'"
            >
              {{ g.customer_name }}{{ g.is_primary ? '(主)' : '' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkin_time" label="入住时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkin_time) }}</template>
        </el-table-column>
        <el-table-column prop="checkout_time" label="退房时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkout_time) || '—' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="CHECKIN_STATUS_TYPE[row.status]" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联预订" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.reservation_id" type="success" size="small">#{{ row.reservation_id }}</el-tag>
            <span v-else style="color:#999;">无</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 办理入住对话框 -->
    <el-dialog v-model="dialogVisible" title="办理入住" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-divider content-position="left">客户查找</el-divider>
        <el-form-item label="查找方式">
          <el-radio-group v-model="searchType">
            <el-radio label="phone">按电话</el-radio>
            <el-radio label="id_number">按身份证</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="查询内容">
          <el-input v-model="searchValue" :placeholder="searchType === 'phone' ? '输入手机号' : '输入身份证号'" style="width: 300px;">
            <template #append>
              <el-button @click="searchCustomer">查询</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="客户信息" v-if="foundCustomer">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="姓名">{{ foundCustomer.name }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ foundCustomer.phone }}</el-descriptions-item>
            <el-descriptions-item label="身份证">{{ foundCustomer.id_number }}</el-descriptions-item>
            <el-descriptions-item label="会员等级">
              <el-tag :type="MEMBER_LEVEL_TYPE[foundCustomer.membership_level]" size="small">
                {{ foundCustomer.membership_level }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-divider content-position="left">预订信息（可选）</el-divider>
        <el-form-item label="关联预订">
          <el-select v-model="form.reservation_id" placeholder="无预订则直接分配房间" clearable style="width: 100%;">
            <el-option
              v-for="r in matchedReservations"
              :key="r.reservation_id"
              :label="`#${r.reservation_id} - ${formatDateTime(r.expected_checkin)} ~ ${formatDateTime(r.expected_checkout)}`"
              :value="r.reservation_id"
            />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">房间分配</el-divider>
        <el-form-item label="房间类型" prop="type_id">
          <el-select v-model="selectedTypeId" placeholder="选择房间类型" @change="loadAvailableRooms">
            <el-option
              v-for="t in roomTypes"
              :key="t.type_id"
              :label="`${t.type_name} (¥${t.base_price}/晚, 最多${t.capacity}人)`"
              :value="t.type_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分配房间" prop="room_id">
          <el-select v-model="form.room_id" placeholder="选择空闲房间">
            <el-option
              v-for="r in availableRooms"
              :key="r.room_id"
              :label="`${r.room_number} (${r.floor || ''})`"
              :value="r.room_id"
            />
          </el-select>
          <span v-if="availableRooms.length === 0 && selectedTypeId" style="color: var(--color-danger); margin-left: 12px;">
            该类型无空闲房间
          </span>
        </el-form-item>

        <el-divider content-position="left">入住宾客</el-divider>
        <div v-for="(guest, idx) in form.guests" :key="idx" style="display: flex; gap: 12px; margin-bottom: 8px; align-items: center;">
          <el-input v-model="guest.id_number" placeholder="身份证号" style="flex: 1;" @blur="autoFillGuest(idx)" />
          <el-input v-model="guest.name" placeholder="姓名" style="width: 120px;" disabled />
          <el-checkbox v-model="guest.is_primary" :disabled="idx === 0">主入住人</el-checkbox>
          <el-button type="danger" :icon="Delete" circle size="small" @click="form.guests.splice(idx, 1)" v-if="form.guests.length > 1" />
        </div>
        <el-button type="primary" link @click="form.guests.push({ id_number: '', name: '', customer_id: null, is_primary: false })">
          <el-icon><Plus /></el-icon>添加入住人
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { checkinService, customerService, roomTypeService, roomService, reservationService } from '@/api/services'
import { formatDateTime, CHECKIN_STATUS_LIST, CHECKIN_STATUS_TYPE, MEMBER_LEVEL_TYPE } from '@/utils/helpers'
import { ElMessage } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'

const list = ref([])
const roomTypes = ref([])
const availableRooms = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const searchKeyword = ref('')
const searchStatus = ref('')

const searchType = ref('phone')
const searchValue = ref('')
const foundCustomer = ref(null)
const matchedReservations = ref([])
const selectedTypeId = ref(null)

const form = reactive({
  reservation_id: null,
  room_id: null,
  guests: [{ id_number: '', name: '', customer_id: null, is_primary: true }]
})

const rules = {
  room_id: [{ required: true, message: '请选择房间', trigger: 'change' }]
}

onMounted(async () => {
  roomTypes.value = await roomTypeService.getList()
  loadData()
})

async function loadData() {
  const params = {}
  if (searchKeyword.value) params.keyword = searchKeyword.value
  if (searchStatus.value) params.status = searchStatus.value
  list.value = await checkinService.getList(params)
}

async function searchCustomer() {
  if (!searchValue.value) {
    ElMessage.warning('请输入查询内容')
    return
  }
  let customer = null
  if (searchType.value === 'phone') {
    customer = await customerService.getByPhone(searchValue.value)
  } else {
    customer = await customerService.getByIdNumber(searchValue.value)
  }

  if (!customer) {
    ElMessage.warning('未找到该客户，请先录入客户信息')
    foundCustomer.value = null
    matchedReservations.value = []
    return
  }

  foundCustomer.value = customer
  form.guests[0].id_number = customer.id_number
  form.guests[0].name = customer.name
  form.guests[0].customer_id = customer.customer_id
  form.guests[0].is_primary = true

  matchedReservations.value = await reservationService.getList({
    status: '已确认',
    customer_id: customer.customer_id
  })

  if (matchedReservations.value.length > 0) {
    ElMessage.info(`找到${matchedReservations.value.length}条已确认预订`)
  }
}

async function autoFillGuest(idx) {
  const guest = form.guests[idx]
  if (!guest.id_number) return
  const customer = await customerService.getByIdNumber(guest.id_number)
  if (customer) {
    guest.name = customer.name
    guest.customer_id = customer.customer_id
  } else {
    guest.name = ''
    guest.customer_id = null
  }
}

async function loadAvailableRooms() {
  if (selectedTypeId.value) {
    availableRooms.value = await roomService.getAvailableRooms(selectedTypeId.value)
  } else {
    availableRooms.value = []
  }
  form.room_id = null
}

function openDialog() {
  searchValue.value = ''
  foundCustomer.value = null
  matchedReservations.value = []
  selectedTypeId.value = null
  availableRooms.value = []
  form.reservation_id = null
  form.room_id = null
  form.guests = [{ id_number: '', name: '', customer_id: null, is_primary: true }]
  dialogVisible.value = true
}

async function handleSubmit() {
  if (form.guests.some(g => !g.customer_id)) {
    ElMessage.warning('请确保所有宾客信息已正确填写')
    return
  }
  if (!form.room_id) {
    ElMessage.warning('请选择房间')
    return
  }

  submitting.value = true
  try {
    const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null')
    await checkinService.create({
      reservation_id: form.reservation_id || null,
      room_id: form.room_id,
      created_by: currentUser?.userId ?? 1,
      guests: form.guests.map(g => ({
        customer_id: g.customer_id,
        is_primary: g.is_primary ? 1 : 0
      }))
    })
    ElMessage.success('入住办理成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}
</script>
