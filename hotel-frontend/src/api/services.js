/**
 * API 服务层
 * 模拟后端接口，所有业务逻辑在此层处理
 * 包含完整的业务规则校验
 */

import { db } from './mock'
import { daysBetween, nextId, isValidRoomTransition, isValidReservationTransition, MEMBER_DISCOUNTS, MEMBER_UPGRADE_THRESHOLDS, MEMBER_LEVEL_LIST } from '@/utils/helpers'

// 模拟网络延迟
const delay = (ms = 200) => new Promise(resolve => setTimeout(resolve, ms))

// ==================== 用户服务 ====================
export const userService = {
  async getList() {
    await delay()
    return db.getTable('users').map(u => {
      const { password_hash, ...rest } = u
      return rest
    })
  },

  async create(user) {
    await delay()
    const users = db.getTable('users')
    // 用户名唯一性校验
    if (users.some(u => u.username === user.username)) {
      throw new Error('用户名已存在')
    }
    const record = {
      ...user,
      created_at: new Date().toISOString(),
      last_login: null
    }
    return db.addRecord('users', record, 'user_id')
  },

  async update(userId, updates) {
    await delay()
    const users = db.getTable('users')
    // 用户名唯一性校验（排除自身）
    if (updates.username && users.some(u => u.username === updates.username && u.user_id !== userId)) {
      throw new Error('用户名已存在')
    }
    return db.updateRecord('users', userId, updates, 'user_id')
  },

  async remove(userId) {
    await delay()
    db.deleteRecord('users', userId, 'user_id')
  }
}

// ==================== 客户服务 ====================
export const customerService = {
  async getList(params = {}) {
    await delay()
    let list = db.getTable('customers')

    // 模糊查询支持
    if (params.keyword) {
      const kw = params.keyword.toLowerCase()
      list = list.filter(c =>
        c.name.toLowerCase().includes(kw) ||
        c.id_number.includes(kw) ||
        c.phone.includes(kw)
      )
    }
    return list
  },

  async getById(id) {
    await delay()
    return db.findById('customers', id, 'customer_id')
  },

  async getByIdNumber(idNumber) {
    await delay()
    return db.getTable('customers').find(c => c.id_number === idNumber) || null
  },

  async getByPhone(phone) {
    await delay()
    return db.getTable('customers').find(c => c.phone === phone) || null
  },

  async create(customer) {
    await delay()
    const customers = db.getTable('customers')
    // 身份证号唯一性校验
    if (customers.some(c => c.id_number === customer.id_number)) {
      throw new Error('该身份证号已存在')
    }
    const record = {
      ...customer,
      membership_level: '普通',
      points: 0,
      created_at: new Date().toISOString(),
      updated_at: null
    }
    return db.addRecord('customers', record, 'customer_id')
  },

  async update(customerId, updates) {
    await delay()
    updates.updated_at = new Date().toISOString()
    return db.updateRecord('customers', customerId, updates, 'customer_id')
  },

  // 获取客户历史入住记录
  async getHistory(customerId) {
    await delay()
    const checkins = db.getTable('checkins')
    const guests = db.getTable('checkin_guests')
    const customerGuestRecords = guests.filter(g => g.customer_id === customerId)
    const checkinIds = customerGuestRecords.map(g => g.checkin_id)
    return checkins.filter(c => checkinIds.includes(c.checkin_id))
  },

  // 更新积分并检查升级
  async addPoints(customerId, points) {
    await delay()
    const customer = db.findById('customers', customerId, 'customer_id')
    if (!customer) throw new Error('客户不存在')

    customer.points += points
    // 自动升级检查
    const currentLevelIdx = MEMBER_LEVEL_LIST.findIndex(l => l.value === customer.membership_level)
    for (let i = MEMBER_LEVEL_LIST.length - 1; i > currentLevelIdx; i--) {
      if (customer.points >= MEMBER_UPGRADE_THRESHOLDS[MEMBER_LEVEL_LIST[i].value]) {
        customer.membership_level = MEMBER_LEVEL_LIST[i].value
        break
      }
    }
    return db.updateRecord('customers', customerId, customer, 'customer_id')
  }
}

// ==================== 客房类型服务 ====================
export const roomTypeService = {
  async getList() {
    await delay()
    return db.getTable('room_types')
  },

  async create(roomType) {
    await delay()
    const types = db.getTable('room_types')
    if (types.some(t => t.type_name === roomType.type_name)) {
      throw new Error('客房类型名称已存在')
    }
    return db.addRecord('room_types', roomType, 'type_id')
  },

  async update(typeId, updates) {
    await delay()
    return db.updateRecord('room_types', typeId, updates, 'type_id')
  },

  async remove(typeId) {
    await delay()
    // 检查是否有客房引用此类型
    const rooms = db.getTable('rooms')
    if (rooms.some(r => r.type_id === typeId)) {
      throw new Error('该类型下存在客房，无法删除')
    }
    db.deleteRecord('room_types', typeId, 'type_id')
  }
}

// ==================== 客房服务 ====================
export const roomService = {
  async getList(params = {}) {
    await delay()
    let list = db.getTable('rooms')
    const types = db.getTable('room_types')

    // 附加类型名称
    list = list.map(r => ({
      ...r,
      type_name: types.find(t => t.type_id === r.type_id)?.type_name || '未知'
    }))

    if (params.status) {
      list = list.filter(r => r.status === params.status)
    }
    if (params.type_id) {
      list = list.filter(r => r.type_id === params.type_id)
    }
    if (params.keyword) {
      list = list.filter(r => r.room_number.includes(params.keyword))
    }
    return list
  },

  async create(room) {
    await delay()
    const rooms = db.getTable('rooms')
    if (rooms.some(r => r.room_number === room.room_number)) {
      throw new Error('房间号已存在')
    }
    room.status = room.status || '空闲'
    return db.addRecord('rooms', room, 'room_id')
  },

  async update(roomId, updates) {
    await delay()
    return db.updateRecord('rooms', roomId, updates, 'room_id')
  },

  async remove(roomId) {
    await delay()
    // 检查房间是否被入住记录引用
    const checkins = db.getTable('checkins')
    if (checkins.some(c => c.room_id === roomId && c.status === '入住中')) {
      throw new Error('该房间正在使用中，无法删除')
    }
    db.deleteRecord('rooms', roomId, 'room_id')
  },

  // 批量更新状态（如清洁完毕置为空闲）
  async batchUpdateStatus(roomIds, newStatus) {
    await delay()
    roomIds.forEach(id => {
      db.updateRecord('rooms', id, { status: newStatus }, 'room_id')
    })
  },

  // 查询可预订房间数
  async getAvailableCount(typeId, checkinDate, checkoutDate) {
    await delay()
    const rooms = db.getTable('rooms').filter(r => r.type_id === typeId)
    const totalRooms = rooms.length

    // 当前已占用的房间数
    const occupiedCount = rooms.filter(r => r.status === '占用').length

    // 已确认预订中，与查询时间段重叠的预订间数
    const reservations = db.getTable('reservations')
    const details = db.getTable('reservation_details')
    const overlappingReservations = reservations.filter(r => {
      if (r.status !== '已确认') return false
      // 时间段重叠判断
      return new Date(r.expected_checkin) < new Date(checkoutDate) &&
             new Date(r.expected_checkout) > new Date(checkinDate)
    })
    const reservedCount = overlappingReservations.reduce((sum, res) => {
      const resDetails = details.filter(d => d.reservation_id === res.reservation_id && d.type_id === typeId)
      return sum + resDetails.reduce((s, d) => s + d.room_count, 0)
    }, 0)

    return totalRooms - occupiedCount - reservedCount
  },

  // 获取可分配的空闲房间列表
  getAvailableRooms(typeId) {
    const rooms = db.getTable('rooms')
    return rooms.filter(r => r.type_id === typeId && r.status === '空闲')
  }
}

// ==================== 预订服务 ====================
export const reservationService = {
  async getList(params = {}) {
    await delay()
    let list = db.getTable('reservations')
    const customers = db.getTable('customers')
    const users = db.getTable('users')
    const details = db.getTable('reservation_details')
    const types = db.getTable('room_types')

    list = list.map(r => ({
      ...r,
      customer_name: customers.find(c => c.customer_id === r.customer_id)?.name || '未知',
      created_by_name: users.find(u => u.user_id === r.created_by)?.username || '未知',
      details: details.filter(d => d.reservation_id === r.reservation_id).map(d => ({
        ...d,
        type_name: types.find(t => t.type_id === d.type_id)?.type_name || '未知'
      }))
    }))

    if (params.status) {
      list = list.filter(r => r.status === params.status)
    }
    if (params.keyword) {
      list = list.filter(r => r.customer_name.includes(params.keyword))
    }
    return list.sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
  },

  async create(reservation) {
    await delay()
    const { details, ...resData } = reservation

    // 校验时间逻辑
    if (new Date(resData.expected_checkout) <= new Date(resData.expected_checkin)) {
      throw new Error('预计离开时间必须晚于预计入住时间')
    }

    // 校验预订不超售规则
    for (const detail of details) {
      const available = await roomService.getAvailableCount(
        detail.type_id,
        resData.expected_checkin,
        resData.expected_checkout
      )
      if (detail.room_count > available) {
        const typeName = db.getTable('room_types').find(t => t.type_id === detail.type_id)?.type_name
        throw new Error(`${typeName}可预订数量不足，当前可预订${available}间`)
      }
    }

    resData.status = '已确认'
    resData.created_at = new Date().toISOString()
    const savedRes = db.addRecord('reservations', resData, 'reservation_id')

    // 保存需求明细
    details.forEach(d => {
      db.addRecord('reservation_details', {
        reservation_id: savedRes.reservation_id,
        type_id: d.type_id,
        room_count: d.room_count
      }, 'detail_id')
    })

    return savedRes
  },

  async cancel(reservationId) {
    await delay()
    const res = db.findById('reservations', reservationId, 'reservation_id')
    if (!res) throw new Error('预订不存在')
    if (res.status !== '已确认') throw new Error('只有"已确认"状态的预订才能取消')
    if (!isValidReservationTransition(res.status, '已取消')) {
      throw new Error('状态流转不合法')
    }
    return db.updateRecord('reservations', reservationId, { status: '已取消' }, 'reservation_id')
  }
}

// ==================== 入住服务 ====================
export const checkinService = {
  async getList(params = {}) {
    await delay()
    let list = db.getTable('checkins')
    const rooms = db.getTable('rooms')
    const types = db.getTable('room_types')
    const guests = db.getTable('checkin_guests')
    const customers = db.getTable('customers')
    const reservations = db.getTable('reservations')

    list = list.map(c => {
      const room = rooms.find(r => r.room_id === c.room_id)
      const roomType = room ? types.find(t => t.type_id === room.type_id) : null
      const checkinGuests = guests.filter(g => g.checkin_id === c.checkin_id).map(g => ({
        ...g,
        customer_name: customers.find(ct => ct.customer_id === g.customer_id)?.name || '未知'
      }))
      const reservation = c.reservation_id ? reservations.find(r => r.reservation_id === c.reservation_id) : null

      return {
        ...c,
        room_number: room?.room_number || '未知',
        room_type_name: roomType?.type_name || '未知',
        base_price: roomType?.base_price || 0,
        guests: checkinGuests,
        reservation_status: reservation?.status || null
      }
    })

    if (params.status) {
      list = list.filter(c => c.status === params.status)
    }
    if (params.keyword) {
      list = list.filter(c =>
        c.room_number.includes(params.keyword) ||
        c.guests.some(g => g.customer_name.includes(params.keyword))
      )
    }
    return list.sort((a, b) => new Date(b.checkin_time) - new Date(a.checkin_time))
  },

  // 入住办理：有预订时关联预订，无预订时直接分配房间
  async create(checkinData) {
    await delay()
    const { guests, reservation_id, room_id } = checkinData

    // 房间必须为"空闲"状态
    const room = db.findById('rooms', room_id, 'room_id')
    if (!room) throw new Error('房间不存在')
    if (room.status !== '空闲') throw new Error('该房间当前不可分配，状态为：' + room.status)

    // 如果关联预订，校验预订状态
    if (reservation_id) {
      const reservation = db.findById('reservations', reservation_id, 'reservation_id')
      if (!reservation) throw new Error('预订不存在')
      if (!['已确认', '已入住'].includes(reservation.status)) {
        throw new Error('预订状态不合法，当前状态：' + reservation.status)
      }
    }

    // 校验入住人数不超过房间容量
    const roomType = db.findById('room_types', room.type_id, 'type_id')
    if (guests.length > roomType.capacity) {
      throw new Error(`入住人数(${guests.length})超过房间最大容量(${roomType.capacity})`)
    }

    // 创建入住记录
    const checkin = {
      reservation_id: reservation_id || null,
      room_id,
      checkin_time: new Date().toISOString(),
      checkout_time: null,
      status: '入住中'
    }
    const savedCheckin = db.addRecord('checkins', checkin, 'checkin_id')

    // 保存入住宾客明细
    guests.forEach(g => {
      db.addRecord('checkin_guests', {
        checkin_id: savedCheckin.checkin_id,
        customer_id: g.customer_id,
        is_primary: g.is_primary ? 1 : 0
      }, 'guest_id')
    })

    // 更新房间状态为"占用"
    db.updateRecord('rooms', room_id, { status: '占用' }, 'room_id')

    // 如果关联预订，更新预订状态为"已入住"
    if (reservation_id) {
      db.updateRecord('reservations', reservation_id, { status: '已入住' }, 'reservation_id')
    }

    return savedCheckin
  }
}

// ==================== 换房服务 ====================
export const roomChangeService = {
  async getList() {
    await delay()
    const changes = db.getTable('room_changes')
    const rooms = db.getTable('rooms')
    const checkins = db.getTable('checkins')

    return changes.map(c => ({
      ...c,
      old_room_number: rooms.find(r => r.room_id === c.old_room_id)?.room_number || '未知',
      new_room_number: rooms.find(r => r.room_id === c.new_room_id)?.room_number || '未知',
      checkin_info: checkins.find(ch => ch.checkin_id === c.checkin_id) || null
    })).sort((a, b) => new Date(b.change_time) - new Date(a.change_time))
  },

  async create(changeData) {
    await delay()
    const { checkin_id, new_room_id, reason } = changeData

    // 校验入住记录
    const checkin = db.findById('checkins', checkin_id, 'checkin_id')
    if (!checkin) throw new Error('入住记录不存在')
    if (checkin.status !== '入住中') throw new Error('只有"入住中"状态才能换房')

    const oldRoom = db.findById('rooms', checkin.room_id, 'room_id')
    if (!oldRoom || oldRoom.status !== '占用') {
      throw new Error('原房间状态异常，必须为"占用"')
    }

    const newRoom = db.findById('rooms', new_room_id, 'room_id')
    if (!newRoom) throw new Error('新房间不存在')
    if (newRoom.status !== '空闲') {
      throw new Error('新房间必须为"空闲"状态')
    }

    // 创建换房记录
    const change = {
      checkin_id,
      old_room_id: checkin.room_id,
      new_room_id,
      change_time: new Date().toISOString(),
      reason: reason || ''
    }
    db.addRecord('room_changes', change, 'change_id')

    // 更新入住登记的房间
    db.updateRecord('checkins', checkin_id, { room_id: new_room_id }, 'checkin_id')

    // 原房间→清洁中，新房间→占用
    db.updateRecord('rooms', oldRoom.room_id, { status: '清洁中' }, 'room_id')
    db.updateRecord('rooms', newRoom.room_id, { status: '占用' }, 'room_id')

    return change
  }
}

// ==================== 结账服务 ====================
export const billingService = {
  async getList() {
    await delay()
    const bills = db.getTable('bills')
    const checkins = db.getTable('checkins')
    const rooms = db.getTable('rooms')
    const types = db.getTable('room_types')
    const users = db.getTable('users')

    return bills.map(b => {
      const checkin = checkins.find(c => c.checkin_id === b.checkin_id)
      const room = checkin ? rooms.find(r => r.room_id === checkin.room_id) : null
      const roomType = room ? types.find(t => t.type_id === room.type_id) : null
      return {
        ...b,
        room_number: room?.room_number || '未知',
        room_type_name: roomType?.type_name || '未知',
        operator: users.find(u => u.user_id === b.created_by)?.username || '未知'
      }
    }).sort((a, b) => new Date(b.payment_time) - new Date(a.payment_time))
  },

  // 结账处理
  async checkout(checkinId, extraCharges = []) {
    await delay()
    const checkin = db.findById('checkins', checkinId, 'checkin_id')
    if (!checkin) throw new Error('入住记录不存在')
    if (checkin.status !== '入住中') throw new Error('只有"入住中"状态才能结账')

    // 防止重复结账
    const existingBills = db.getTable('bills')
    if (existingBills.some(b => b.checkin_id === checkinId)) {
      throw new Error('该入住记录已结账，不可重复结账')
    }

    // 获取房间和类型信息
    const room = db.findById('rooms', checkin.room_id, 'room_id')
    const roomType = db.findById('room_types', room.type_id, 'type_id')

    // 计算费用
    const checkinTime = new Date(checkin.checkin_time)
    const checkoutTime = new Date()
    const nights = daysBetween(checkinTime, checkoutTime)

    // 获取主要入住人的会员折扣
    const guests = db.getTable('checkin_guests').filter(g => g.checkin_id === checkinId)
    const primaryGuest = guests.find(g => g.is_primary === 1) || guests[0]
    const customer = primaryGuest ? db.findById('customers', primaryGuest.customer_id, 'customer_id') : null
    const discount = customer ? (MEMBER_DISCOUNTS[customer.membership_level] || 1.0) : 1.0

    const roomFee = nights * roomType.base_price * discount
    const extraTotal = extraCharges.reduce((sum, c) => sum + c.amount, 0)
    const totalAmount = roomFee + extraTotal

    // 生成账单
    const bill = {
      checkin_id: checkinId,
      total_amount: Math.round(totalAmount * 100) / 100,
      payment_time: checkoutTime.toISOString(),
      details: JSON.stringify({
        nights,
        base_price: roomType.base_price,
        discount,
        room_fee: Math.round(roomFee * 100) / 100,
        extra_charges: extraCharges
      }),
      created_by: JSON.parse(sessionStorage.getItem('currentUser'))?.user_id || 1
    }
    const savedBill = db.addRecord('bills', bill, 'bill_id')

    // 更新入住登记状态为"已退房"
    db.updateRecord('checkins', checkinId, {
      status: '已退房',
      checkout_time: checkoutTime.toISOString()
    }, 'checkin_id')

    // 房间状态改为"清洁中"
    db.updateRecord('rooms', checkin.room_id, { status: '清洁中' }, 'room_id')

    // 有关联预订时，更新预订状态为"已完成"
    if (checkin.reservation_id) {
      db.updateRecord('reservations', checkin.reservation_id, { status: '已完成' }, 'reservation_id')
    }

    // 累计客户积分（1元=1积分）
    if (customer) {
      await customerService.addPoints(customer.customer_id, Math.floor(bill.total_amount))
    }

    return savedBill
  },

  // 获取账单详情
  async getDetail(billId) {
    await delay()
    const bill = db.findById('bills', billId, 'bill_id')
    if (!bill) return null
    const checkin = db.findById('checkins', bill.checkin_id, 'checkin_id')
    const room = checkin ? db.findById('rooms', checkin.room_id, 'room_id') : null
    const roomType = room ? db.findById('room_types', room.type_id, 'type_id') : null
    const guests = db.getTable('checkin_guests').filter(g => g.checkin_id === bill.checkin_id)
    const customers = db.getTable('customers')

    return {
      ...bill,
      details: JSON.parse(bill.details || '{}'),
      checkin,
      room_number: room?.room_number,
      room_type_name: roomType?.type_name,
      guests: guests.map(g => ({
        ...g,
        customer_name: customers.find(c => c.customer_id === g.customer_id)?.name || '未知'
      }))
    }
  }
}
