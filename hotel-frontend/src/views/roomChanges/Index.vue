<template>
  <div class="page-container">
    <div class="page-header">
      <h2>换房管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Switch /></el-icon>办理换房
      </el-button>
    </div>

    <!-- 换房记录列表 -->
    <div class="table-card">
      <el-table :data="list" border stripe>
        <el-table-column prop="change_id" label="换房编号" width="80" align="center" />
        <el-table-column prop="checkin_id" label="入住编号" width="80" align="center" />
        <el-table-column prop="old_room_number" label="原房间" width="80" />
        <el-table-column prop="new_room_number" label="新房间" width="80" />
        <el-table-column prop="change_time" label="换房时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.change_time) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="换房原因" min-width="200" />
      </el-table>
      <el-empty v-if="list.length === 0" description="暂无换房记录" />
    </div>

    <!-- 办理换房对话框 -->
    <el-dialog v-model="dialogVisible" title="办理换房" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="入住记录" prop="checkin_id">
          <el-select v-model="form.checkin_id" placeholder="选择入住中的记录" style="width: 100%;" @change="onCheckinChange">
            <el-option
              v-for="c in activeCheckins"
              :key="c.checkin_id"
              :label="`#${c.checkin_id} - 房间${c.room_number} (${c.guests?.find(g=>g.is_primary)?.customer_name || c.guests?.[0]?.customer_name || '未知'})`"
              :value="c.checkin_id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="当前房间">
          <el-input :value="currentRoomInfo" disabled />
        </el-form-item>

        <el-form-item label="新房型">
          <el-select v-model="newTypeId" placeholder="选择目标房型" @change="loadNewRooms" style="width: 100%;">
            <el-option
              v-for="t in roomTypes"
              :key="t.type_id"
              :label="`${t.type_name} (¥${t.base_price}/晚)`"
              :value="t.type_id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="新房间" prop="new_room_id">
          <el-select v-model="form.new_room_id" placeholder="选择空闲房间" style="width: 100%;">
            <el-option
              v-for="r in newRooms"
              :key="r.room_id"
              :label="r.floor ? `${r.room_number} (${r.floor})` : r.room_number"
              :value="r.room_id"
            />
          </el-select>
          <span v-if="newRooms.length === 0 && newTypeId" style="color: var(--color-danger); margin-left: 0; margin-top: 4px;">
            该类型无空闲房间
          </span>
        </el-form-item>

        <el-form-item label="换房原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" placeholder="请输入换房原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认换房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { roomChangeService, roomTypeService, roomService, checkinService } from '@/api/services'
import { formatDateTime } from '@/utils/helpers'
import { ElMessage } from 'element-plus'

const list = ref([])
const activeCheckins = ref([])
const roomTypes = ref([])
const newRooms = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const newTypeId = ref(null)

const form = reactive({
  checkin_id: null,
  new_room_id: null,
  reason: ''
})

const rules = {
  checkin_id: [{ required: true, message: '请选择入住记录', trigger: 'change' }],
  new_room_id: [{ required: true, message: '请选择新房间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入换房原因', trigger: 'blur' }]
}

const currentRoomInfo = computed(() => {
  if (!form.checkin_id) return ''
  const checkin = activeCheckins.value.find(c => c.checkin_id === form.checkin_id)
  return checkin ? `${checkin.room_number} (${checkin.room_type_name})` : ''
})

onMounted(async () => {
  roomTypes.value = await roomTypeService.getList()
  loadData()
})

async function loadData() {
  list.value = await roomChangeService.getList()
  const checkins = await checkinService.getList({ status: '入住中' })
  activeCheckins.value = checkins
}

function onCheckinChange() {
  newTypeId.value = null
  newRooms.value = []
  form.new_room_id = null
}

async function loadNewRooms() {
  if (newTypeId.value) {
    newRooms.value = await roomService.getAvailableRooms(newTypeId.value)
  } else {
    newRooms.value = []
  }
  form.new_room_id = null
}

function openDialog() {
  form.checkin_id = null
  form.new_room_id = null
  form.reason = ''
  newTypeId.value = null
  newRooms.value = []
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null')
    await roomChangeService.create({
      checkin_id: form.checkin_id,
      new_room_id: form.new_room_id,
      reason: form.reason,
      created_by: currentUser?.userId ?? 1
    })
    ElMessage.success('换房办理成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}
</script>
