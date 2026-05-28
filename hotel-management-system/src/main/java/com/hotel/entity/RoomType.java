package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 客房类型表 - 对应数据字典 D3
 * 维护酒店提供的房间类型及其基础价格
 */
@Data
@TableName("room_types")
public class RoomType {

    /** 类型编号，主键，自增 */
    @TableId(value = "type_id", type = IdType.AUTO)
    private Integer typeId;

    /** 类型名称，如单人间/双人间/套房，唯一 */
    private String typeName;

    /** 基础价格（元/晚），必须大于0 */
    private BigDecimal basePrice;

    /** 最大入住人数 */
    private Integer capacity;

    /** 类型描述 */
    private String description;
}
