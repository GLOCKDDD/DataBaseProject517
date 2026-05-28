<template>
  <div class="page-container">
    <div class="page-header">
      <h2>客房类型管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增类型
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" border stripe>
        <el-table-column prop="type_id" label="类型编号" width="80" align="center" />
        <el-table-column prop="type_name" label="类型名称" width="120" />
        <el-table-column prop="base_price" label="基础价格（元/晚）" width="160" align="right">
          <template #default="{ row }">
            <span class="price-value">¥{{ row.base_price?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="最大入住人数" width="130" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="客房数量" width="100" align="center">
          <template #default="{ row }">
            <span class="count-value">{{ roomCounts[row.type_id] || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确认删除该类型？删除前请确保无关联客房。"
              @confirm="handleDelete(row.type_id)"
            >
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
      :title="isEdit ? '编辑客房类型' : '新增客房类型'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="类型名称" prop="type_name">
          <el-input v-model="form.type_name" placeholder="如：单人间、双人间、套房" />
        </el-form-item>
        <el-form-item label="基础价格" prop="base_price">
          <el-input-number v-model="form.base_price" :min="0.01" :precision="2" :step="50" />
          <span style="margin-left: 8px; color: var(--text-tertiary);">元/晚</span>
        </el-form-item>
        <el-form-item label="最大入住人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="房间描述信息" />
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
import { roomTypeService } from '@/api/services'
import { db } from '@/api/mock'
import { positiveNumberValidator, positiveIntValidator } from '@/utils/validators'
import { ElMessage } from 'element-plus'

const list = ref([])
const roomCounts = ref({})
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({
  type_name: '',
  base_price: 280,
  capacity: 1,
  description: ''
})

const rules = {
  type_name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  base_price: [{ required: true, validator: positiveNumberValidator, trigger: 'blur' }],
  capacity: [{ required: true, validator: positiveIntValidator, trigger: 'change' }]
}

onMounted(() => {
  loadData()
})

function loadData() {
  list.value = db.getTable('room_types')
  const rooms = db.getTable('rooms')
  const counts = {}
  rooms.forEach(r => {
    counts[r.type_id] = (counts[r.type_id] || 0) + 1
  })
  roomCounts.value = counts
}

function openDialog(type = null) {
  isEdit.value = !!type
  editId.value = type?.type_id || null
  form.type_name = type?.type_name || ''
  form.base_price = type?.base_price || 280
  form.capacity = type?.capacity || 1
  form.description = type?.description || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await roomTypeService.update(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await roomTypeService.create({ ...form })
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

async function handleDelete(typeId) {
  try {
    await roomTypeService.remove(typeId)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>

<style scoped>
.price-value {
  color: var(--color-danger);
  font-weight: 600;
  font-family: var(--font-mono);
}

.count-value {
  font-weight: 600;
  color: var(--color-primary);
  font-family: var(--font-mono);
}
</style>
