package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户信息表 - 对应数据字典 D2
 * 存储所有曾经入住或可能入住的客人自然信息
 */
@Data
@TableName("customers")
public class Customer {

    /** 客户编号，主键，自增 */
    @TableId(value = "customer_id", type = IdType.AUTO)
    private Integer customerId;

    /** 身份证号，18位，末位可为X，唯一 */
    private String idNumber;

    /** 客户姓名 */
    private String name;

    /** 联系地址 */
    private String address;

    /** 联系电话 */
    private String phone;

    /** 会员等级：普通/VIP/金卡，默认"普通" */
    private String membershipLevel;

    /** 会员积分，非负整数，默认0 */
    private Integer points;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}
