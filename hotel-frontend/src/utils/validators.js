/**
 * 表单校验规则集合
 * 匹配《系统需求分析》文档中的用户定义完整性约束
 */

// 手机号校验：11位数字
export const phoneValidator = (rule, value, callback) => {
  if (!value) return callback(new Error('请输入联系电话'))
  if (!/^1\d{10}$/.test(value)) {
    return callback(new Error('手机号必须为11位数字'))
  }
  callback()
}

// 身份证号校验：18位，末位可为X
export const idNumberValidator = (rule, value, callback) => {
  if (!value) return callback(new Error('请输入身份证号'))
  if (!/^\d{17}[\dXx]$/.test(value)) {
    return callback(new Error('身份证号必须为18位，末位可为X'))
  }
  callback()
}

// 预订时间校验：离开时间 > 入住时间
export const reservationTimeValidator = (getFieldsValue) => {
  return (rule, value, callback) => {
    const form = getFieldsValue()
    if (form.expected_checkin && form.expected_checkout) {
      if (new Date(form.expected_checkout) <= new Date(form.expected_checkin)) {
        return callback(new Error('预计离开时间必须晚于预计入住时间'))
      }
    }
    callback()
  }
}

// 入住时间校验：离开时间 >= 入住时间
export const checkinTimeValidator = (getFieldsValue) => {
  return (rule, value, callback) => {
    const form = getFieldsValue()
    if (form.checkin_time && form.checkout_time) {
      if (new Date(form.checkout_time) < new Date(form.checkin_time)) {
        return callback(new Error('离开时间不能早于入住时间'))
      }
    }
    callback()
  }
}

// 正整数校验
export const positiveIntValidator = (rule, value, callback) => {
  if (value === null || value === undefined || value === '') {
    return callback(new Error('请输入数值'))
  }
  if (!Number.isInteger(Number(value)) || Number(value) <= 0) {
    return callback(new Error('必须为大于0的整数'))
  }
  callback()
}

// 非负整数校验（积分用）
export const nonNegativeIntValidator = (rule, value, callback) => {
  if (value === null || value === undefined || value === '') {
    return callback(new Error('请输入数值'))
  }
  if (!Number.isInteger(Number(value)) || Number(value) < 0) {
    return callback(new Error('必须为非负整数'))
  }
  callback()
}

// 正数校验（价格用）
export const positiveNumberValidator = (rule, value, callback) => {
  if (value === null || value === undefined || value === '') {
    return callback(new Error('请输入价格'))
  }
  if (Number(value) <= 0) {
    return callback(new Error('价格必须大于0'))
  }
  callback()
}
