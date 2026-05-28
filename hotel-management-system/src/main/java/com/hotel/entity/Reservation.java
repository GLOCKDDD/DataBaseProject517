package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预订记录表 - 对应数据字典 D5
 * 记录一次预订的总体信息
 */
@Data
@TableName("reservations")
public class Reservation {

    /** 预订编号，主键，自增 */
    @TableId(value = "reservation_id", type = IdType.AUTO)
    private Integer reservationId;

    /** 主要联系人客户编号，外键引用customers.customer_id */
    private Integer customerId;

    /** 操作前台用户编号，外键引用users.user_id */
    private Integer createdBy;

    /** 预计入住时间 */
    private LocalDateTime expectedCheckin;

    /** 预计离开时间，必须大于预计入住时间 */
    private LocalDateTime expectedCheckout;

    /** 总入住人数 */
    private Integer guestCount;

    /** 预订状态：已确认/已入住/已取消/已完成，默认"已确认" */
    private String status;

    /** 预订创建时间 */
    private LocalDateTime createdAt;

    /** 备注 */
    private String remark;
}
