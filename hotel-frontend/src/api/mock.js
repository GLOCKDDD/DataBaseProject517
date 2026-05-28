/**
 * Mock 数据层
 * 使用 localStorage 模拟数据库持久化
 * 所有表结构严格匹配《系统需求分析》文档
 */

// 初始数据
const INITIAL_DATA = {
  users: [
    { user_id: 1, username: 'admin', password_hash: 'admin123', role: 'admin', permissions: null, created_at: '2026-01-01T08:00:00', last_login: null },
    { user_id: 2, username: 'front1', password_hash: '123456', role: 'frontdesk', permissions: JSON.stringify(['customer_add', 'reservation_manage', 'checkin_manage', 'room_change', 'billing']), created_at: '2026-01-15T08:00:00', last_login: null }
  ],
  customers: [
    { customer_id: 1, id_number: '110101199001011234', name: '张三', address: '北京市朝阳区建国路88号', phone: '13800138001', membership_level: 'VIP', points: 1500, created_at: '2026-02-01T10:00:00', updated_at: '2026-04-15T14:30:00' },
    { customer_id: 2, id_number: '310101198505052345', name: '李四', address: '上海市浦东新区陆家嘴路168号', phone: '13900139002', membership_level: '普通', points: 200, created_at: '2026-03-10T09:00:00', updated_at: null },
    { customer_id: 3, id_number: '440101199206063456', name: '王五', address: '广州市天河区天河路385号', phone: '13700137003', membership_level: '贵宾', points: 6200, created_at: '2025-12-20T11:00:00', updated_at: '2026-05-01T16:00:00' }
  ],
  room_types: [
    { type_id: 1, type_name: '单人间', base_price: 280.00, capacity: 1, description: '标准单人客房，配备1.2米单人床' },
    { type_id: 2, type_name: '双人间', base_price: 420.00, capacity: 2, description: '标准双人客房，配备1.5米双人床或两张单人床' },
    { type_id: 3, type_name: '套房', base_price: 680.00, capacity: 3, description: '豪华套房，配备客厅、卧室、独立卫浴' }
  ],
  rooms: [
    { room_id: 1, room_number: '101', type_id: 1, status: '空闲', floor: '1楼', remark: '' },
    { room_id: 2, room_number: '102', type_id: 1, status: '空闲', floor: '1楼', remark: '' },
    { room_id: 3, room_number: '103', type_id: 2, status: '占用', floor: '1楼', remark: '' },
    { room_id: 4, room_number: '201', type_id: 2, status: '空闲', floor: '2楼', remark: '' },
    { room_id: 5, room_number: '202', type_id: 2, status: '清洁中', floor: '2楼', remark: '' },
    { room_id: 6, room_number: '203', type_id: 3, status: '空闲', floor: '2楼', remark: '' },
    { room_id: 7, room_number: '301', type_id: 1, status: '维修中', floor: '3楼', remark: '空调维修' },
    { room_id: 8, room_number: '302', type_id: 3, status: '空闲', floor: '3楼', remark: '' }
  ],
  reservations: [
    {
      reservation_id: 1, customer_id: 1, created_by: 2,
      expected_checkin: '2026-05-20T14:00:00', expected_checkout: '2026-05-23T12:00:00',
      guest_count: 1, status: '已确认', created_at: '2026-05-18T09:00:00', remark: '需要高楼层'
    }
  ],
  reservation_details: [
    { detail_id: 1, reservation_id: 1, type_id: 2, room_count: 1 }
  ],
  checkins: [],
  checkin_guests: [],
  bills: [],
  room_changes: []
}

// 数据库操作封装
const STORAGE_KEY = 'hotel_db'

function getDB() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(INITIAL_DATA))
    return JSON.parse(JSON.stringify(INITIAL_DATA))
  }
  return JSON.parse(raw)
}

function saveDB(data) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

export const db = {
  // 获取整个表数据
  getTable(tableName) {
    const data = getDB()
    return data[tableName] || []
  },

  // 保存整个表
  saveTable(tableName, records) {
    const data = getDB()
    data[tableName] = records
    saveDB(data)
  },

  // 新增记录（自增ID）
  addRecord(tableName, record, idField) {
    const data = getDB()
    const table = data[tableName] || []
    const maxId = table.reduce((max, item) => Math.max(max, item[idField] || 0), 0)
    record[idField] = maxId + 1
    table.push(record)
    data[tableName] = table
    saveDB(data)
    return record
  },

  // 更新记录
  updateRecord(tableName, id, updates, idField) {
    const data = getDB()
    const table = data[tableName] || []
    const idx = table.findIndex(item => item[idField] === id)
    if (idx !== -1) {
      table[idx] = { ...table[idx], ...updates }
      data[tableName] = table
      saveDB(data)
      return table[idx]
    }
    return null
  },

  // 删除记录
  deleteRecord(tableName, id, idField) {
    const data = getDB()
    const table = data[tableName] || []
    data[tableName] = table.filter(item => item[idField] !== id)
    saveDB(data)
  },

  // 按ID查找
  findById(tableName, id, idField) {
    const table = this.getTable(tableName)
    return table.find(item => item[idField] === id) || null
  },

  // 重置数据到初始状态
  reset() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(INITIAL_DATA))
  }
}
