/**
 * API 服务层 - 对接真实后端 REST 接口
 * 所有请求通过 vite proxy 转发至 http://localhost:8080/api
 */

import http from './http'

// ==================== 用户服务 ====================
export const userService = {
  async getList(params = {}) {
    const page = await http.get('/user/list', {
      params: { pageNum: 1, pageSize: 100, role: params.role || '' }
    })
    return page?.records ?? (Array.isArray(page) ? page : [])
  },

  async create(user) {
    return http.post('/user/register', user)
  },

  async update(userId, updates) {
    return http.put('/user/update', { ...updates, user_id: userId })
  },

  async remove(userId) {
    return http.delete(`/user/${userId}`)
  }
}

// ==================== 客户服务 ====================
export const customerService = {
  async getList(params = {}) {
    const page = await http.get('/customer/search', {
      params: {
        pageNum: 1,
        pageSize: 100,
        keyword: params.keyword || ''
      }
    })
    return page?.records ?? (Array.isArray(page) ? page : [])
  },

  async getById(id) {
    return http.get(`/customer/${id}`)
  },

  async getByIdNumber(idNumber, silent = false) {
    return http.get(`/customer/idnumber/${idNumber}`, { silent })
  },

  // 后端无按电话精确查询接口，使用关键字搜索后在前端过滤
  async getByPhone(phone) {
    const page = await http.get('/customer/search', {
      params: { pageNum: 1, pageSize: 20, keyword: phone }
    })
    const list = page?.records ?? (Array.isArray(page) ? page : [])
    return list.find(c => c.phone === phone) || null
  },

  async create(customer) {
    return http.post('/customer/save', customer)
  },

  async update(customerId, updates) {
    return http.post('/customer/save', { ...updates, customer_id: customerId })
  },

  // 累加积分并自动触发会员升级（后端 /customer/membership 负责校验升级）
  async addPoints(customerId, points) {
    const customer = await http.get(`/customer/${customerId}`)
    const newPoints = (customer?.points ?? 0) + points
    return http.put('/customer/membership', null, {
      params: {
        customerId,
        level: customer?.membership_level ?? '普通',
        points: newPoints
      }
    })
  },

  async getHistory(customerId) {
    return http.get(`/customer/${customerId}/checkins`)
  },

  async setMembership(customerId, level, points) {
    return http.put('/customer/membership', null, {
      params: { customerId, level, points }
    })
  },

  async remove(customerId) {
    return http.delete(`/customer/${customerId}`)
  }
}

// ==================== 客房类型服务 ====================
export const roomTypeService = {
  async getList() {
    return http.get('/roomtype/list')
  },

  async create(roomType) {
    return http.post('/roomtype/add', roomType)
  },

  async update(typeId, updates) {
    return http.put('/roomtype/update', { ...updates, type_id: typeId })
  },

  async remove(typeId) {
    return http.delete(`/roomtype/${typeId}`)
  }
}

// ==================== 客房服务 ====================
export const roomService = {
  async getList(params = {}) {
    const page = await http.get('/room/list', {
      params: {
        pageNum: 1,
        pageSize: 200,
        status: params.status || '',
        floor: params.floor || '',
        typeId: params.type_id || '',
        keyword: params.keyword || ''
      }
    })
    return page?.records ?? (Array.isArray(page) ? page : [])
  },

  async create(room) {
    return http.post('/room/add', room)
  },

  async update(roomId, updates) {
    return http.put('/room/update', { ...updates, room_id: roomId })
  },

  async remove(roomId) {
    return http.delete(`/room/${roomId}`)
  },

  // 手动构造多值 query string，避免 axios 数组序列化与 Spring 不兼容
  async batchUpdateStatus(roomIds, status) {
    const qs = roomIds.map(id => `roomIds=${id}`).join('&') + `&status=${encodeURIComponent(status)}`
    return http.put(`/room/batch-status?${qs}`)
  },

  async getAvailableRooms(typeId) {
    return http.get(`/room/free/${typeId}`)
  },

  async getRoomOverview() {
    return http.get('/room/overview')
  }
}

// ==================== 预订服务 ====================
export const reservationService = {
  async getList(params = {}) {
    const page = await http.get('/reservation/list', {
      params: {
        pageNum: 1,
        pageSize: 100,
        status: params.status || '',
        customerId: params.customer_id || ''
      }
    })
    return page?.records ?? (Array.isArray(page) ? page : [])
  },

  async create(reservation) {
    return http.post('/reservation/create', reservation)
  },

  async cancel(reservationId) {
    return http.put(`/reservation/cancel/${reservationId}`)
  },

  async getDetail(reservationId) {
    return http.get(`/reservation/${reservationId}`)
  },

  async getDetails(reservationId) {
    return http.get(`/reservation/${reservationId}/details`)
  }
}

// ==================== 入住服务 ====================
export const checkinService = {
  async getList(params = {}) {
    const list = await http.get('/checkin/listfull', {
      params: { status: params.status || '' }
    })
    return Array.isArray(list) ? list : []
  },

  async create(checkinData) {
    return http.post('/checkin/create', checkinData)
  },

  async getDetail(checkinId) {
    return http.get(`/checkin/${checkinId}`)
  },

  async getGuests(checkinId) {
    return http.get(`/checkin/${checkinId}/guests`)
  }
}

// ==================== 换房服务 ====================
export const roomChangeService = {
  // 不传 checkinId 时获取全部记录（含房间号富化）
  async getList(checkinId) {
    if (checkinId != null) {
      return http.get(`/roomchange/list/${checkinId}`)
    }
    return http.get('/roomchange/listall')
  },

  async create(changeData) {
    return http.post('/roomchange/create', changeData)
  }
}

// ==================== 结账服务 ====================
export const billingService = {
  async getList() {
    const page = await http.get('/bill/list', {
      params: { pageNum: 1, pageSize: 200 }
    })
    return page?.records ?? (Array.isArray(page) ? page : [])
  },

  // CheckoutDTO 只需要 checkin_id 和 created_by（后端自动计算费用）
  async checkout(checkinId) {
    const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || 'null')
    return http.post('/bill/checkout', {
      checkin_id: checkinId,
      created_by: currentUser?.userId ?? 1
    })
  },

  async getDetail(billId) {
    return http.get(`/bill/${billId}`)
  },

  async getByCheckinId(checkinId) {
    return http.get(`/bill/checkin/${checkinId}`)
  }
}
