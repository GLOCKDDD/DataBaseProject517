<template>
  <div class="page-container">
    <div class="page-header">
      <h2>客房信息管理</h2>
      <div class="header-actions">
        <el-button @click="handleBatchClean" :disabled="selectedRooms.length === 0" class="btn-ghost">
          <el-icon><Brush /></el-icon>批量设为空闲
        </el-button>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增客房
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索房间号"
        :prefix-icon="Search"
        clearable
        style="width: 200px;"
        @input="loadData"
      />
      <el-select v-model="searchType" placeholder="房型" clearable @change="loadData" style="width: 140px;">
        <el-option v-for="t in roomTypes" :key="t.type_id" :label="t.type_name" :value="t.type_id" />
      </el-select>
      <el-select v-model="searchStatus" placeholder="房间状态" clearable @change="loadData" style="width: 140px;">
        <el-option v-for="s in ROOM_STATUS_LIST" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table
        :data="list"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column prop="room_id" label="编号" width="60" align="center" />
        <el-table-column prop="room_number" label="房间号" width="90" />
        <el-table-column prop="type_name" label="房型" width="100" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="ROOM_STATUS_TYPE[row.status]" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status === '清洁中'"
              type="success"
              link
              size="small"
              @click="handleSetAvailable(row.room_id)"
            >
              设为空闲
            </el-button>
            <el-popconfirm title="确认删除该客房？" @confirm="handleDelete(row.room_id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑客房' : '新增客房'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="房间号" prop="room_number">
          <el-input v-model="form.room_number" placeholder="如：101、202、A301" />
        </el-form-item>
        <el-form-item label="所属类型" prop="type_id">
          <el-select v-model="form.type_id" placeholder="选择房型" style="width: 100%;">
            <el-option
              v-for="t in roomTypes"
              :key="t.type_id"
              :label="`${t.type_name} (¥${t.base_price}/晚)`"
              :value="t.type_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-select v-model="form.status" style="width: 100%;">
            <el-option v-for="s in ROOM_STATUS_LIST" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input v-model="form.floor" placeholder="如：1楼、2楼" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { roomService, roomTypeService } from '@/api/services'
import { ROOM_STATUS_LIST, ROOM_STATUS_TYPE } from '@/utils/helpers'
import { ElMessage } from 'element-plus'
import { Search, Brush } from '@element-plus/icons-vue'

const list = ref([])
const roomTypes = ref([])
const selectedRooms = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)
const searchKeyword = ref('')
const searchType = ref('')
const searchStatus = ref('')

const form = reactive({
  room_number: '',
  type_id: '',
  status: '空闲',
  floor: '',
  remark: ''
})

const rules = {
  room_number: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  type_id: [{ required: true, message: '请选择房型', trigger: 'change' }]
}

onMounted(async () => {
  roomTypes.value = await roomTypeService.getList()
  loadData()
})

async function loadData() {
  const params = {}
  if (searchKeyword.value) params.keyword = searchKeyword.value
  if (searchType.value) params.type_id = searchType.value
  if (searchStatus.value) params.status = searchStatus.value
  list.value = await roomService.getList(params)
}

function handleSelectionChange(selection) {
  selectedRooms.value = selection
}

function openDialog(room = null) {
  isEdit.value = !!room
  editId.value = room?.room_id || null
  form.room_number = room?.room_number || ''
  form.type_id = room?.type_id || ''
  form.status = room?.status || '空闲'
  form.floor = room?.floor || ''
  form.remark = room?.remark || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await roomService.update(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await roomService.create({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(roomId) {
  try {
    await roomService.remove(roomId)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function handleSetAvailable(roomId) {
  try {
    await roomService.update(roomId, { status: '空闲' })
    ElMessage.success('已设为空闲')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function handleBatchClean() {
  const ids = selectedRooms.value.map(r => r.room_id)
  try {
    await roomService.batchUpdateStatus(ids, '空闲')
    ElMessage.success(`已将${ids.length}间房间设为空闲`)
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 8px;
}

.btn-ghost {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-color) !important;
  color: var(--text-secondary) !important;
}

.btn-ghost:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}
</style>
