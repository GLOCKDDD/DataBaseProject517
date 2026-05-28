package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结账记录表 - 对应数据字典 D7
 * 退房时生成，关联入住记录
 */
@Data
@TableName("bills")
public class Bill {

    /** 账单编号，主键，自增 */
    @TableId(value = "bill_id", type = IdType.AUTO)
    private Integer billId;

    /** 关联入住登记编号，外键引用checkins.checkin_id，一条入住记录最多一条账单 */
    private Integer checkinId;

    /** 费用合计 */
    private BigDecimal totalAmount;

    /** 结账时间 */
    private LocalDateTime paymentTime;

    /** 费用明细，JSON或文本格式 */
    private String details;

    /** 操作前台用户编号，外键引用users.user_id */
    private Integer createdBy;
}
