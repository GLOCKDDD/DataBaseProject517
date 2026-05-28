package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 换房记录表 - 对应数据字典 D8
 * 记录换房历史，方便追溯
 */
@Data
@TableName("room_changes")
public class RoomChange {

    /** 换房编号，主键，自增 */
    @TableId(value = "change_id", type = IdType.AUTO)
    private Integer changeId;

    /** 关联入住登记编号，外键引用checkins.checkin_id */
    private Integer checkinId;

    /** 原房间编号，外键引用rooms.room_id */
    private Integer oldRoomId;

    /** 新房间编号，外键引用rooms.room_id */
    private Integer newRoomId;

    /** 换房时间 */
    private LocalDateTime changeTime;

    /** 换房原因 */
    private String reason;
}
