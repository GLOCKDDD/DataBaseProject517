<template>
  <div class="page-container">
    <div class="page-header">
      <h2>结账管理</h2>
    </div>

    <!-- 待结账列表 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-custom">
          <span class="card-title">待结账入住记录</span>
          <el-tag type="warning" size="small" v-if="activeCheckins.length">{{ activeCheckins.length }} 条</el-tag>
        </div>
      </template>
      <el-table :data="activeCheckins" border stripe size="small">
        <el-table-column prop="checkin_id" label="登记编号" width="80" align="center" />
        <el-table-column prop="room_number" label="房间号" width="80" />
        <el-table-column prop="room_type_name" label="房型" width="90" />
        <el-table-column label="入住宾客" width="120">
          <template #default="{ row }">
            {{ row.guests.map(g => g.customer_name).join(', ') }}
          </template>
        </el-table-column>
        <el-table-column prop="checkin_time" label="入住时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkin_time) }}</template>
        </el-table-column>
        <el-table-column label="已住天数" width="80" align="center">
          <template #default="{ row }">
            <span class="days-value">{{ daysBetween(row.checkin_time, new Date()) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预估费用" width="100" align="right">
          <template #default="{ row }">
            <span class="fee-value">¥{{ estimateFee(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openCheckout(row)">结账</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="activeCheckins.length === 0" description="暂无待结账记录" />
    </el-card>

    <!-- 已结账账单列表 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-custom">
          <span class="card-title">账单记录</span>
        </div>
      </template>
      <el-table :data="billList" border stripe size="small">
        <el-table-column prop="bill_id" label="账单编号" width="80" align="center" />
        <el-table-column prop="checkin_id" label="入住编号" width="80" align="center" />
        <el-table-column prop="room_number" label="房间号" width="80" />
        <el-table-column prop="room_type_name" label="房型" width="90" />
        <el-table-column prop="total_amount" label="费用合计" width="100" align="right">
          <template #default="{ row }">
            <span class="fee-value">¥{{ row.total_amount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payment_time" label="结账时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.payment_time) }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="操作员" width="90" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row.bill_id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="billList.length === 0" description="暂无账单记录" />
    </el-card>

    <!-- 结账对话框 -->
    <el-dialog v-model="checkoutVisible" title="结账确认" width="600px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentCheckin">
        <el-descriptions-item label="登记编号">#{{ currentCheckin.checkin_id }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentCheckin.room_number }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ currentCheckin.room_type_name }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDateTime(currentCheckin.checkin_time) }}</el-descriptions-item>
        <el-descriptions-item label="入住宾客" :span="2">
          {{ currentCheckin.guests.map(g => g.customer_name).join(', ') }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">费用计算</el-divider>
      <el-descriptions :column="1" border v-if="feeDetail">
        <el-descriptions-item label="入住天数">{{ feeDetail.nights }} 晚</el-descriptions-item>
        <el-descriptions-item label="基础房价">¥{{ feeDetail.base_price }}/晚</el-descriptions-item>
        <el-descriptions-item label="会员折扣">
          {{ feeDetail.discount < 1 ? (feeDetail.discount * 10).toFixed(0) + '折' : '无折扣' }}
          <el-tag v-if="feeDetail.customer_level" :type="MEMBER_LEVEL_TYPE[feeDetail.customer_level]" size="small" style="margin-left: 8px;">
            {{ feeDetail.customer_level }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="房费小计">
          <span class="fee-highlight">¥{{ feeDetail.room_fee.toFixed(2) }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">附加费用（可选）</el-divider>
      <div v-for="(charge, idx) in extraCharges" :key="idx" style="display: flex; gap: 12px; margin-bottom: 8px;">
        <el-input v-model="charge.name" placeholder="费用名称" style="flex: 1;" />
        <el-input-number v-model="charge.amount" :min="0" :precision="2" style="width: 140px;" />
        <el-button type="danger" :icon="Delete" circle size="small" @click="extraCharges.splice(idx, 1)" />
      </div>
      <el-button type="primary" link @click="extraCharges.push({ name: '', amount: 0 })">
        <el-icon><Plus /></el-icon>添加附加费用
      </el-button>

      <el-divider />
      <div class="total-row">
        <span>费用合计：</span>
        <span class="total-amount">¥{{ totalAmount }}</span>
      </div>

      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCheckout">确认结账</el-button>
      </template>
    </el-dialog>

    <!-- 账单详情对话框 -->
    <el-dialog v-model="detailVisible" title="账单详情" width="600px">
      <div v-if="billDetail" class="bill-detail">
        <div class="bill-header">
          <h3>酒店结算账单</h3>
          <p>账单编号：#{{ billDetail.bill_id }}</p>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房间号">{{ billDetail.room_number }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ billDetail.room_type_name }}</el-descriptions-item>
          <el-descriptions-item label="入住宾客" :span="2">
            {{ billDetail.guests.map(g => g.customer_name).join(', ') }}
          </el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ formatDateTime(billDetail.checkin?.checkin_time) }}</el-descriptions-item>
          <el-descriptions-item label="退房时间">{{ formatDateTime(billDetail.checkin?.checkout_time) }}</el-descriptions-item>
          <el-descriptions-item label="入住天数">{{ billDetail.details?.nights }} 晚</el-descriptions-item>
          <el-descriptions-item label="基础房价">¥{{ billDetail.details?.base_price }}/晚</el-descriptions-item>
          <el-descriptions-item label="会员折扣">
            {{ billDetail.details?.discount < 1 ? (billDetail.details.discount * 10).toFixed(0) + '折' : '无折扣' }}
          </el-descriptions-item>
          <el-descriptions-item label="房费小计">¥{{ billDetail.details?.room_fee?.toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="billDetail.details?.extra_charges?.length" style="margin-top: 12px;">
          <p style="font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">附加费用：</p>
          <div v-for="c in billDetail.details.extra_charges" :key="c.name" style="display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid var(--border-light);">
            <span>{{ c.name }}</span><span class="fee-value">¥{{ c.amount?.toFixed(2) }}</span>
          </div>
        </div>
        <div class="bill-total">
          <span>费用合计：</span>
          <span class="total-amount">¥{{ billDetail.total_amount?.toFixed(2) }}</span>
        </div>
        <p style="text-align: center; color: var(--text-tertiary); margin-top: 16px; font-size: 13px;">
          结账时间：{{ formatDateTime(billDetail.payment_time) }}
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { billingService, checkinService } from '@/api/services'
import { formatDateTime, daysBetween, MEMBER_LEVEL_TYPE } from '@/utils/helpers'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const activeCheckins = ref([])
const billList = ref([])
const checkoutVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const currentCheckin = ref(null)
const feeDetail = ref(null)
const billDetail = ref(null)
const extraCharges = ref([])

const totalAmount = computed(() => {
  if (!feeDetail.value) return 0
  const extra = extraCharges.value.reduce((sum, c) => sum + (c.amount || 0), 0)
  return (feeDetail.value.room_fee + extra).toFixed(2)
})

onMounted(() => {
  loadData()
})

async function loadData() {
  const allCheckins = await checkinService.getList({ status: '入住中' })
  activeCheckins.value = allCheckins
  billList.value = await billingService.getList()
}

function estimateFee(checkin) {
  const nights = Math.max(1, daysBetween(checkin.checkin_time, new Date()))
  return (nights * (checkin.base_price || 0)).toFixed(2)
}

function openCheckout(checkin) {
  currentCheckin.value = checkin
  const nights = Math.max(1, daysBetween(checkin.checkin_time, new Date()))
  const level = checkin.primary_customer_level || null
  const discountMap = { 'VIP': 0.9, '贵宾': 0.8 }
  const discount = discountMap[level] || 1.0
  const roomFee = nights * (checkin.base_price || 0) * discount

  feeDetail.value = {
    nights,
    base_price: checkin.base_price || 0,
    discount,
    customer_level: level,
    room_fee: Math.round(roomFee * 100) / 100
  }

  extraCharges.value = []
  checkoutVisible.value = true
}

async function handleCheckout() {
  submitting.value = true
  try {
    const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null')
    await billingService.checkout(
      currentCheckin.value.checkin_id,
      currentUser?.userId ?? 1
    )
    ElMessage.success('结账成功')
    checkoutVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

async function viewDetail(billId) {
  const bill = await billingService.getDetail(billId)
  if (bill) {
    if (typeof bill.details === 'string') {
      try {
        const d = JSON.parse(bill.details)
        bill.details = {
          nights: d['入住天数'],
          base_price: d['基础单价'],
          discount: d['折扣'],
          room_fee: d['房费小计'],
          extra_charges: d['附加费用'] || []
        }
      } catch(e) {
        bill.details = {}
      }
    }
    bill.guests = bill.guests || []
  }
  billDetail.value = bill
  detailVisible.value = true
}
</script>

<style scoped>
.section-card {
  margin-bottom: 20px;
}

.card-header-custom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.days-value {
  font-weight: 600;
  color: var(--text-primary);
  font-family: var(--font-mono);
}

.fee-value {
  color: var(--color-danger);
  font-weight: 600;
  font-family: var(--font-mono);
}

.fee-highlight {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}

.total-row {
  text-align: right;
  font-size: 16px;
  font-weight: 500;
}

.total-amount {
  color: var(--color-danger);
  font-weight: 700;
  font-size: 24px;
  margin-left: 8px;
  font-family: var(--font-mono);
}

.bill-detail {
  padding: 0;
}

.bill-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

.bill-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.bill-header p {
  color: var(--text-tertiary);
  font-size: 13px;
}

.bill-total {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
  font-weight: 500;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}
</style>
