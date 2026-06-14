<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预订管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新建预订
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索客户姓名"
        :prefix-icon="Search"
        clearable
        style="width: 250px;"
        @input="loadData"
      />
      <el-select v-model="searchStatus" placeholder="预订状态" clearable @change="loadData" style="width: 140px;">
        <el-option v-for="s in RESERVATION_STATUS_LIST" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table :data="list" border stripe>
        <el-table-column prop="reservation_id" label="预订编号" width="80" align="center" />
        <el-table-column prop="customer_name" label="客户姓名" width="100" />
        <el-table-column label="需求房型/间数" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="d in row.details" :key="d.detail_id" size="small" style="margin: 2px 4px 2px 0;">
              {{ d.type_name }} × {{ d.room_count }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expected_checkin" label="预计入住" width="170">
          <template #default="{ row }">{{ formatDateTime(row.expected_checkin) }}</template>
        </el-table-column>
        <el-table-column prop="expected_checkout" label="预计离开" width="170">
          <template #default="{ row }">{{ formatDateTime(row.expected_checkout) }}</template>
        </el-table-column>
        <el-table-column prop="guest_count" label="人数" width="60" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="RESERVATION_STATUS_TYPE[row.status]" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_by_name" label="操作员" width="90" />
        <el-table-column prop="remark" label="备注" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '已确认'">
              <el-popconfirm title="确认取消该预订？" @confirm="handleCancel(row.reservation_id)">
                <template #reference>
                  <el-button type="danger" link size="small">取消预订</el-button>
                </template>
              </el-popconfirm>
            </template>
            <span v-else style="color:#999; font-size: 12px;">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新建预订对话框 -->
    <el-dialog v-model="dialogVisible" title="新建预订" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="客户" prop="customer_id">
          <el-select
            v-model="form.customer_id"
            filterable
            placeholder="选择已有客户"
            style="width: 100%;"
          >
            <el-option
              v-for="c in customers"
              :key="c.customer_id"
              :label="`${c.name} (${c.id_number})`"
              :value="c.customer_id"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="预计入住" prop="expected_checkin">
              <el-date-picker
                v-model="form.expected_checkin"
                type="datetime"
                placeholder="选择入住时间"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计离开" prop="expected_checkout">
              <el-date-picker
                v-model="form.expected_checkout"
                type="datetime"
                placeholder="选择离开时间"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="入住人数" prop="guest_count">
          <el-input-number v-model="form.guest_count" :min="1" />
        </el-form-item>

        <el-form-item label="房间需求">
          <div style="width: 100%;">
            <div v-for="(detail, idx) in form.details" :key="idx" style="display: flex; gap: 12px; margin-bottom: 8px; align-items: center;">
              <el-select v-model="detail.type_id" placeholder="选择房型" style="flex: 1;">
                <el-option
                  v-for="t in roomTypes"
                  :key="t.type_id"
                  :label="`${t.type_name} (¥${t.base_price}/晚, 最多${t.capacity}人)`"
                  :value="t.type_id"
                />
              </el-select>
              <el-input-number v-model="detail.room_count" :min="1" :max="10" style="width: 120px;" />
              <el-button type="danger" :icon="Delete" circle size="small" @click="form.details.splice(idx, 1)" v-if="form.details.length > 1" />
            </div>
            <el-button type="primary" link @click="form.details.push({ type_id: '', room_count: 1 })">
              <el-icon><Plus /></el-icon>添加房型需求
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { reservationService, customerService, roomTypeService } from '@/api/services'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime, RESERVATION_STATUS_LIST, RESERVATION_STATUS_TYPE } from '@/utils/helpers'
import { reservationTimeValidator } from '@/utils/validators'
import { ElMessage } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const list = ref([])
const customers = ref([])
const roomTypes = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const searchKeyword = ref('')
const searchStatus = ref('')

const form = reactive({
  customer_id: '',
  expected_checkin: '',
  expected_checkout: '',
  guest_count: 1,
  details: [{ type_id: '', room_count: 1 }],
  remark: ''
})

const rules = {
  customer_id: [{ required: true, message: '请选择客户', trigger: 'change' }],
  expected_checkin: [{ required: true, message: '请选择预计入住时间', trigger: 'change' }],
  expected_checkout: [
    { required: true, message: '请选择预计离开时间', trigger: 'change' },
    { validator: reservationTimeValidator(() => form), trigger: 'change' }
  ],
  guest_count: [{ required: true, message: '请输入入住人数', trigger: 'change' }]
}

onMounted(async () => {
  customers.value = await customerService.getList()
  roomTypes.value = await roomTypeService.getList()
  loadData()
})

async function loadData() {
  const params = {}
  if (searchStatus.value) params.status = searchStatus.value
  let data = await reservationService.getList(params)
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    data = data.filter(r => r.customer_name?.toLowerCase().includes(kw))
  }
  list.value = data
}

function openDialog() {
  form.customer_id = ''
  form.expected_checkin = ''
  form.expected_checkout = ''
  form.guest_count = 1
  form.details = [{ type_id: '', room_count: 1 }]
  form.remark = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (form.details.some(d => !d.type_id)) {
    ElMessage.warning('请选择房间类型')
    return
  }

  submitting.value = true
  try {
    await reservationService.create({
      customer_id: form.customer_id,
      created_by: authStore.currentUser?.userId,
      expected_checkin: new Date(form.expected_checkin).toISOString(),
      expected_checkout: new Date(form.expected_checkout).toISOString(),
      guest_count: form.guest_count,
      details: form.details,
      remark: form.remark
    })
    ElMessage.success('预订创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

async function handleCancel(id) {
  try {
    await reservationService.cancel(id)
    ElMessage.success('预订已取消')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>
