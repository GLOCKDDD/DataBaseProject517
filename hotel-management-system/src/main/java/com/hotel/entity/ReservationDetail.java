package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 预订需求明细表 - 对应数据字典 D5A
 * 拆分预订中对不同房间类型的数量需求
 */
@Data
@TableName("reservation_details")
public class ReservationDetail {

    /** 明细编号，主键，自增 */
    @TableId(value = "detail_id", type = IdType.AUTO)
    private Integer detailId;

    /** 所属预订编号，外键引用reservations.reservation_id */
    private Integer reservationId;

    /** 需求房间类型编号，外键引用room_types.type_id */
    private Integer typeId;

    /** 需求间数，必须大于0 */
    private Integer roomCount;
}
