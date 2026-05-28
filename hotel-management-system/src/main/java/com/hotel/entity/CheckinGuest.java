package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 入住宾客明细表 - 对应数据字典 D6A
 * 记录某个房间内实际入住的所有客人
 */
@Data
@TableName("checkin_guests")
public class CheckinGuest {

    /** 宾客明细编号，主键，自增 */
    @TableId(value = "guest_id", type = IdType.AUTO)
    private Integer guestId;

    /** 所属入住登记编号，外键引用checkins.checkin_id */
    private Integer checkinId;

    /** 宾客客户编号，外键引用customers.customer_id */
    private Integer customerId;

    /** 是否主要入住人：0-否，1-是 */
    private Integer isPrimary;
}
