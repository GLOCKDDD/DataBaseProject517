package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客房表 - 对应数据字典 D4
 * 每个具体的物理房间，关联到类型
 */
@Data
@TableName("rooms")
public class Room {

    /** 房间编号，主键，自增 */
    @TableId(value = "room_id", type = IdType.AUTO)
    private Integer roomId;

    /** 房间号，如101、202、A301，唯一 */
    private String roomNumber;

    /** 所属类型编号，外键引用room_types.type_id */
    private Integer typeId;

    /** 房间状态：空闲/占用/清洁中/维修中，默认"空闲" */
    private String status;

    /** 所在楼层 */
    private String floor;

    /** 备注 */
    private String remark;

    /** 房型名称，非数据库字段，查询时由服务层填充 */
    @TableField(exist = false)
    private String typeName;
}
