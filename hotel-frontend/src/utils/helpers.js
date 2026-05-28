/**
 * 通用工具函数
 */

// 房间状态枚举及映射
export const ROOM_STATUS = {
  AVAILABLE: '空闲',
  OCCUPIED: '占用',
  CLEANING: '清洁中',
  MAINTENANCE: '维修中'
}

export const ROOM_STATUS_LIST = [
  { label: '空闲', value: '空闲' },
  { label: '占用', value: '占用' },
  { label: '清洁中', value: '清洁中' },
  { label: '维修中', value: '维修中' }
]

// 房间状态对应标签类型
export const ROOM_STATUS_TYPE = {
  '空闲': 'success',
  '占用': 'danger',
  '清洁中': 'warning',
  '维修中': 'info'
}

// 预订状态枚举
export const RESERVATION_STATUS = {
  CONFIRMED: '已确认',
  CHECKED_IN: '已入住',
  CANCELLED: '已取消',
  COMPLETED: '已完成'
}

export const RESERVATION_STATUS_LIST = [
  { label: '已确认', value: '已确认' },
  { label: '已入住', value: '已入住' },
  { label: '已取消', value: '已取消' },
  { label: '已完成', value: '已完成' }
]

export const RESERVATION_STATUS_TYPE = {
  '已确认': 'primary',
  '已入住': 'success',
  '已取消': 'info',
  '已完成': 'warning'
}

// 入住登记状态枚举
export const CHECKIN_STATUS = {
  CHECKED_IN: '入住中',
  CHECKED_OUT: '已退房',
  ROOM_CHANGED: '已换房'
}

export const CHECKIN_STATUS_LIST = [
  { label: '入住中', value: '入住中' },
  { label: '已退房', value: '已退房' },
  { label: '已换房', value: '已换房' }
]

export const CHECKIN_STATUS_TYPE = {
  '入住中': 'success',
  '已退房': 'info',
  '已换房': 'warning'
}

// 会员等级枚举
export const MEMBER_LEVELS = {
  NORMAL: '普通',
  VIP: 'VIP',
  SVIP: '贵宾'
}

export const MEMBER_LEVEL_LIST = [
  { label: '普通', value: '普通' },
  { label: 'VIP', value: 'VIP' },
  { label: '贵宾', value: '贵宾' }
]

export const MEMBER_LEVEL_TYPE = {
  '普通': 'info',
  'VIP': 'warning',
  '贵宾': 'danger'
}

// 会员折扣配置
export const MEMBER_DISCOUNTS = {
  '普通': 1.0,
  'VIP': 0.9,
  '贵宾': 0.8
}

// 会员升级阈值（积分）
export const MEMBER_UPGRADE_THRESHOLDS = {
  '普通': 0,
  'VIP': 1000,
  '贵宾': 5000
}

// 角色枚举
export const ROLES = {
  ADMIN: 'admin',
  FRONTDESK: 'frontdesk'
}

export const ROLE_LIST = [
  { label: '管理员', value: 'admin' },
  { label: '前台操作员', value: 'frontdesk' }
]

export const ROLE_LABEL = {
  admin: '管理员',
  frontdesk: '前台操作员'
}

// 格式化日期时间
export function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化日期
export function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 计算两个日期之间的天数
export function daysBetween(start, end) {
  const s = new Date(start)
  const e = new Date(end)
  const diff = Math.ceil((e - s) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 1
}

// 生成唯一ID（自增模拟）
export function nextId(list, idKey = 'id') {
  if (!list || list.length === 0) return 1
  return Math.max(...list.map(item => item[idKey] || 0)) + 1
}

// 房间状态流转校验：空闲→占用/维修中→清洁中→空闲
export function isValidRoomTransition(from, to) {
  const transitions = {
    '空闲': ['占用', '维修中'],
    '占用': ['清洁中'],
    '清洁中': ['空闲'],
    '维修中': ['清洁中']
  }
  return transitions[from]?.includes(to) || false
}

// 预订状态流转校验：已确认→已入住(或已取消)→已完成
export function isValidReservationTransition(from, to) {
  const transitions = {
    '已确认': ['已入住', '已取消'],
    '已入住': ['已完成']
  }
  return transitions[from]?.includes(to) || false
}
