package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入住登记表 - 对应数据字典 D6
 * 每次为客人分配一个房间即生成一条登记记录
 */
@Data
@TableName("checkins")
public class Checkin {

    /** 登记编号，主键，自增 */
    @TableId(value = "checkin_id", type = IdType.AUTO)
    private Integer checkinId;

    /** 关联预订编号，可空，非空时必须存在 */
    private Integer reservationId;

    /** 分配房间编号，外键引用rooms.room_id */
    private Integer roomId;

    /** 入住时间 */
    private LocalDateTime checkinTime;

    /** 实际退房时间，结账时填入 */
    private LocalDateTime checkoutTime;

    /** 入住状态：入住中/已退房/已换房，默认"入住中" */
    private String status;
}
