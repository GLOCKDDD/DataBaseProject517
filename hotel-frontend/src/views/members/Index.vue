<template>
  <div class="page-container">
    <div class="page-header">
      <h2>会员管理</h2>
    </div>

    <!-- 会员等级规则说明 -->
    <el-card shadow="never" class="rules-card">
      <template #header>
        <div class="card-header-custom">
          <span class="card-title">会员等级规则</span>
        </div>
      </template>
      <div class="level-cards">
        <div v-for="rule in levelRules" :key="rule.level" class="level-card" :class="'level-' + rule.level">
          <div class="level-badge">
            <el-tag :type="MEMBER_LEVEL_TYPE[rule.level]" size="small" effect="dark">{{ rule.level }}</el-tag>
          </div>
          <div class="level-info">
            <div class="level-row">
              <span class="level-label">升级阈值</span>
              <span class="level-val">{{ rule.threshold }} 积分</span>
            </div>
            <div class="level-row">
              <span class="level-label">房价折扣</span>
              <span class="level-val discount">{{ rule.discount }}</span>
            </div>
            <div class="level-desc">{{ rule.benefits }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名/身份证/电话"
        :prefix-icon="Search"
        clearable
        style="width: 300px;"
        @input="loadData"
      />
      <el-select v-model="searchLevel" placeholder="会员等级" clearable @change="loadData" style="width: 140px;">
        <el-option v-for="l in MEMBER_LEVEL_LIST" :key="l.value" :label="l.label" :value="l.value" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table :data="list" border stripe>
        <el-table-column prop="customer_id" label="编号" width="60" align="center" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="id_number" label="身份证号" width="190" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="membership_level" label="会员等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="MEMBER_LEVEL_TYPE[row.membership_level]" size="small">
              {{ row.membership_level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" align="center">
          <template #default="{ row }">
            <span class="points-value">{{ row.points }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前折扣" width="90" align="center">
          <template #default="{ row }">
            <span class="discount-text">
              {{ MEMBER_DISCOUNTS[row.membership_level] < 1 ? (MEMBER_DISCOUNTS[row.membership_level] * 10).toFixed(0) + '折' : '无折扣' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openAdjustDialog(row)">调整积分/等级</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 调整积分/等级对话框 -->
    <el-dialog v-model="adjustVisible" title="调整积分与等级" width="500px" destroy-on-close>
      <el-descriptions :column="1" border v-if="currentCustomer" style="margin-bottom: 20px;">
        <el-descriptions-item label="客户姓名">{{ currentCustomer.name }}</el-descriptions-item>
        <el-descriptions-item label="当前等级">
          <el-tag :type="MEMBER_LEVEL_TYPE[currentCustomer.membership_level]" size="small">
            {{ currentCustomer.membership_level }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前积分">{{ currentCustomer.points }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="会员等级">
          <el-select v-model="adjustForm.membership_level" style="width: 100%;">
            <el-option v-for="l in MEMBER_LEVEL_LIST" :key="l.value" :label="l.label" :value="l.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整积分">
          <el-input-number v-model="adjustForm.points" :min="0" style="width: 200px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjust">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { customerService } from '@/api/services'
import { MEMBER_LEVEL_LIST, MEMBER_LEVEL_TYPE, MEMBER_DISCOUNTS, MEMBER_UPGRADE_THRESHOLDS } from '@/utils/helpers'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const list = ref([])
const adjustVisible = ref(false)
const currentCustomer = ref(null)
const searchKeyword = ref('')
const searchLevel = ref('')

const adjustForm = reactive({
  membership_level: '普通',
  points: 0
})

const levelRules = [
  { level: '普通', threshold: '0', discount: '无折扣', benefits: '基础入住服务' },
  { level: 'VIP', threshold: '1000', discount: '9折', benefits: '房价9折优惠，优先升级房型' },
  { level: '贵宾', threshold: '5000', discount: '8折', benefits: '房价8折优惠，优先升级房型，赠送欢迎礼品' }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  const params = {}
  if (searchKeyword.value) params.keyword = searchKeyword.value
  let data = await customerService.getList(params)
  if (searchLevel.value) {
    data = data.filter(c => c.membership_level === searchLevel.value)
  }
  list.value = data
}

function openAdjustDialog(customer) {
  currentCustomer.value = customer
  adjustForm.membership_level = customer.membership_level
  adjustForm.points = customer.points
  adjustVisible.value = true
}

async function handleAdjust() {
  try {
    await customerService.update(currentCustomer.value.customer_id, {
      membership_level: adjustForm.membership_level,
      points: adjustForm.points
    })
    ElMessage.success('调整成功')
    adjustVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>

<style scoped>
.rules-card {
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

.level-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.level-card {
  padding: 16px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  background: var(--bg-page);
  transition: all var(--transition-fast);
}

.level-card:hover {
  box-shadow: var(--shadow-md);
}

.level-badge {
  margin-bottom: 12px;
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.level-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.level-label {
  font-size: 12.5px;
  color: var(--text-tertiary);
}

.level-val {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.level-val.discount {
  color: var(--color-gold);
}

.level-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1.5;
  margin-top: 4px;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
}

.points-value {
  font-weight: 600;
  color: var(--color-gold);
  font-family: var(--font-mono);
}

.discount-text {
  font-weight: 500;
  color: var(--color-primary);
}
</style>
