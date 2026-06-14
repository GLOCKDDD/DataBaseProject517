<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div
        v-for="(card, idx) in statCards"
        :key="card.title"
        class="stat-card fade-in-up"
        :class="'stagger-' + (idx + 1)"
      >
        <div class="stat-card-inner">
          <div class="stat-info">
            <div class="stat-label">{{ card.title }}</div>
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-sub">{{ card.sub }}</div>
          </div>
          <div class="stat-icon" :style="{ background: card.iconBg }">
            <el-icon :size="24" :color="card.iconColor"><component :is="card.icon" /></el-icon>
          </div>
        </div>
        <div class="stat-bar" :style="{ background: card.barColor }"></div>
      </div>
    </div>

    <div class="dashboard-grid">
      <!-- 房态概览 -->
      <div class="grid-col-left">
        <el-card class="room-card" shadow="never">
          <template #header>
            <div class="card-header-custom">
              <span class="card-title">房态概览</span>
              <span class="card-badge">共 {{ rooms.length }} 间</span>
            </div>
          </template>

          <!-- 房态统计条 -->
          <div class="room-summary">
            <div class="summary-item" v-for="s in roomStatusSummary" :key="s.label">
              <div class="summary-dot" :style="{ background: s.color }"></div>
              <span class="summary-label">{{ s.label }}</span>
              <span class="summary-count">{{ s.count }}</span>
            </div>
          </div>

          <!-- 房态柱状图 -->
          <div class="room-bar-chart">
            <div class="bar-group" v-for="s in roomStatusSummary" :key="s.label">
              <div class="bar-track">
                <div
                  class="bar-fill"
                  :style="{
                    height: (s.count / rooms.length * 100) + '%',
                    background: s.gradient
                  }"
                >
                  <span class="bar-value">{{ s.count }}</span>
                </div>
              </div>
              <div class="bar-label">{{ s.label }}</div>
            </div>
          </div>

          <!-- 房间网格 -->
          <div class="room-status-grid">
            <div
              v-for="room in rooms"
              :key="room.room_id"
              class="room-block"
              :class="'room-' + getStatusClass(room.status)"
            >
              <div class="room-number">{{ room.room_number }}</div>
              <div class="room-type">{{ room.type_name }}</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧面板 -->
      <div class="grid-col-right">
        <!-- 业务概览 -->
        <el-card shadow="never" class="business-card">
          <template #header>
            <div class="card-header-custom">
              <span class="card-title">今日业务</span>
              <span class="card-date">{{ todayStr }}</span>
            </div>
          </template>
          <div class="business-metrics">
            <div class="metric-item" v-for="m in businessMetrics" :key="m.label">
              <div class="metric-header">
                <span class="metric-label">{{ m.label }}</span>
                <span class="metric-value" :style="{ color: m.color }">{{ m.value }}</span>
              </div>
              <div class="metric-bar-track">
                <div
                  class="metric-bar-fill"
                  :style="{ width: m.percent + '%', background: m.color }"
                ></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 最近入住记录 -->
        <el-card shadow="never" class="recent-card">
          <template #header>
            <div class="card-header-custom">
              <span class="card-title">最近入住</span>
              <el-button type="primary" link size="small" @click="$router.push('/checkins')">查看全部</el-button>
            </div>
          </template>
          <div class="recent-list">
            <div v-for="(item, idx) in recentCheckins" :key="idx" class="recent-item">
              <div class="recent-avatar">{{ item.guest_name[0] }}</div>
              <div class="recent-info">
                <div class="recent-name">{{ item.guest_name }}</div>
                <div class="recent-meta">房间 {{ item.room_number }} &middot; {{ item.checkin_time }}</div>
              </div>
              <el-tag :type="checkinStatusType(item.status)" size="small" class="recent-status">{{ item.status }}</el-tag>
            </div>
            <el-empty v-if="recentCheckins.length === 0" description="暂无入住记录" :image-size="80" />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { roomService, roomTypeService, reservationService, checkinService, customerService } from '@/api/services'
import { formatDateTime, CHECKIN_STATUS_TYPE } from '@/utils/helpers'

const rooms = ref([])
const reservations = ref([])
const checkins = ref([])
const customerTotal = ref(0)
const todayStr = new Date().toISOString().slice(0, 10)

onMounted(() => {
  loadData()
})

async function loadData() {
  const [roomList, types, resList, checkinList, customerPage] = await Promise.all([
    roomService.getList({ pageSize: 200 }),
    roomTypeService.getList(),
    reservationService.getList({}),
    checkinService.getList({}),
    customerService.getList({})
  ])
  rooms.value = (roomList || []).map(r => ({
    ...r,
    type_name: (types || []).find(t => t.type_id === r.type_id)?.type_name || ''
  }))
  reservations.value = resList || []
  checkins.value = checkinList || []
  customerTotal.value = (customerPage || []).length
}

// 统计卡片
const statCards = computed(() => [
  {
    title: '总房间数',
    value: rooms.value.length,
    sub: '全部客房',
    icon: 'OfficeBuilding',
    iconBg: 'linear-gradient(135deg, #E8F0FF, #D6E4FF)',
    iconColor: '#165DFF',
    barColor: 'linear-gradient(90deg, #165DFF, #4080FF)'
  },
  {
    title: '空闲房间',
    value: rooms.value.filter(r => r.status === '空闲').length,
    sub: '可预订',
    icon: 'CircleCheck',
    iconBg: 'linear-gradient(135deg, #E8FFEA, #B7EB8F)',
    iconColor: '#00B42A',
    barColor: 'linear-gradient(90deg, #00B42A, #52C41A)'
  },
  {
    title: '占用房间',
    value: rooms.value.filter(r => r.status === '占用').length,
    sub: '已入住',
    icon: 'User',
    iconBg: 'linear-gradient(135deg, #FFF7E8, #FFD591)',
    iconColor: '#FF7D00',
    barColor: 'linear-gradient(90deg, #FF7D00, #FFA940)'
  },
  {
    title: '总客户数',
    value: customerTotal.value,
    sub: '已注册',
    icon: 'UserFilled',
    iconBg: 'linear-gradient(135deg, #F5E6FF, #D3ADF7)',
    iconColor: '#722ED1',
    barColor: 'linear-gradient(90deg, #722ED1, #B37FEB)'
  }
])

// 房态统计
const roomStatusSummary = computed(() => {
  const counts = { '空闲': 0, '占用': 0, '清洁中': 0, '维修中': 0 }
  rooms.value.forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
  return [
    { label: '空闲', count: counts['空闲'], color: '#00B42A', gradient: 'linear-gradient(180deg, #52C41A, #00B42A)' },
    { label: '占用', count: counts['占用'], color: '#F53F3F', gradient: 'linear-gradient(180deg, #FF7875, #F53F3F)' },
    { label: '清洁中', count: counts['清洁中'], color: '#FF7D00', gradient: 'linear-gradient(180deg, #FFA940, #FF7D00)' },
    { label: '维修中', count: counts['维修中'], color: '#86909C', gradient: 'linear-gradient(180deg, #C9CDD4, #86909C)' }
  ]
})

// 业务指标
const businessMetrics = computed(() => {
  const todayRes = reservations.value.filter(r => r.created_at?.startsWith(todayStr)).length
  const todayIn = checkins.value.filter(c => c.checkin_time?.startsWith(todayStr)).length
  const todayOut = checkins.value.filter(c => c.checkout_time?.startsWith(todayStr)).length
  const pending = reservations.value.filter(r => r.status === '已确认').length
  const cleaning = rooms.value.filter(r => r.status === '清洁中').length
  const maxVal = Math.max(todayRes, todayIn, todayOut, pending, cleaning, 1)
  return [
    { label: '今日新增预订', value: todayRes, color: '#165DFF', percent: Math.max((todayRes / maxVal) * 100, 8) },
    { label: '今日入住', value: todayIn, color: '#00B42A', percent: Math.max((todayIn / maxVal) * 100, 8) },
    { label: '今日退房', value: todayOut, color: '#FF7D00', percent: Math.max((todayOut / maxVal) * 100, 8) },
    { label: '待确认预订', value: pending, color: '#F53F3F', percent: Math.max((pending / maxVal) * 100, 8) },
    { label: '清洁中房间', value: cleaning, color: '#86909C', percent: Math.max((cleaning / maxVal) * 100, 8) }
  ]
})

// 最近入住
const recentCheckins = computed(() => {
  return [...checkins.value]
    .sort((a, b) => new Date(b.checkin_time) - new Date(a.checkin_time))
    .slice(0, 5)
    .map(c => {
      const primaryGuest = (c.guests || []).find(g => g.is_primary) || (c.guests || [])[0]
      return {
        room_number: c.room_number || '未知',
        guest_name: primaryGuest?.customer_name || '未知',
        checkin_time: formatDateTime(c.checkin_time),
        status: c.status
      }
    })
})

function getStatusClass(status) {
  const map = { '空闲': 'available', '占用': 'occupied', '清洁中': 'cleaning', '维修中': 'maintenance' }
  return map[status] || ''
}

function checkinStatusType(status) {
  return CHECKIN_STATUS_TYPE[status] || ''
}
</script>

<style scoped>
/* 统计卡片行 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  overflow: hidden;
  position: relative;
  transition: all var(--transition-normal);
}

.stat-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.stat-card-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -0.5px;
}

.stat-sub {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 6px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
}

/* 主网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
}

.grid-col-left,
.grid-col-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片头部 */
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

.card-badge {
  font-size: 12px;
  color: var(--text-tertiary);
  background: var(--bg-page);
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.card-date {
  font-size: 12px;
  color: var(--text-tertiary);
  font-family: var(--font-mono);
}

/* 房态统计条 */
.room-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.summary-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.summary-label {
  font-size: 12.5px;
  color: var(--text-secondary);
}

.summary-count {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: var(--font-mono);
}

/* 房态柱状图 */
.room-bar-chart {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 20px;
  padding: 16px 0;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-track {
  width: 36px;
  height: 100px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  border-radius: var(--radius-md);
  min-height: 4px;
  transition: height 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 6px;
}

.bar-value {
  font-size: 11px;
  font-weight: 700;
  color: #fff;
}

.bar-label {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 500;
}

/* 房间网格 */
.room-status-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}

.room-block {
  padding: 8px 6px;
  border-radius: var(--radius-md);
  text-align: center;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.room-block:hover {
  transform: scale(1.04);
}

.room-available {
  background: var(--color-success-light);
  border-color: #B7EB8F;
}
.room-occupied {
  background: var(--color-danger-light);
  border-color: #FFCCC7;
}
.room-cleaning {
  background: var(--color-warning-light);
  border-color: #FFD591;
}
.room-maintenance {
  background: var(--color-info-light);
  border-color: #E5E6EB;
}

.room-number {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-primary);
}

.room-type {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

/* 业务指标 */
.business-metrics {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.metric-value {
  font-size: 16px;
  font-weight: 700;
  font-family: var(--font-mono);
}

.metric-bar-track {
  height: 6px;
  background: var(--bg-page);
  border-radius: 3px;
  overflow: hidden;
}

.metric-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 最近入住 */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.recent-item:hover {
  background: var(--bg-hover);
}

.recent-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-lighter), #D6E4FF);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.recent-info {
  flex: 1;
  min-width: 0;
}

.recent-name {
  font-size: 13.5px;
  font-weight: 500;
  color: var(--text-primary);
}

.recent-meta {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.recent-status {
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .room-status-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-row {
    grid-template-columns: 1fr;
  }
}
</style>
