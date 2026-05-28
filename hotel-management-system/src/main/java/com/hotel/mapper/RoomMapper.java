package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 客房表 Mapper接口
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 查询指定房型的空闲房间数量
     */
    @Select("SELECT COUNT(*) FROM rooms WHERE type_id = #{typeId} AND status = '空闲'")
    int countFreeRoomsByType(@Param("typeId") Integer typeId);

    /**
     * 查询指定房型的房间总数
     */
    @Select("SELECT COUNT(*) FROM rooms WHERE type_id = #{typeId}")
    int countTotalByType(@Param("typeId") Integer typeId);

    /**
     * 查询指定房型中当前被占用的房间数（状态为"占用"）
     */
    @Select("SELECT COUNT(*) FROM rooms WHERE type_id = #{typeId} AND status = '占用'")
    int countOccupiedByType(@Param("typeId") Integer typeId);

    /**
     * 查询指定房型在指定时间段内已被确认预订的房间数
     * 统计与 [expectedCheckin, expectedCheckout) 时间段重叠的"已确认"预订的需求间数
     * 时间段重叠条件：已有预订的入住时间 < 新预订的离开时间 AND 已有预订的离开时间 > 新预订的入住时间
     */
    @Select("SELECT IFNULL(SUM(rd.room_count), 0) FROM reservation_details rd " +
            "JOIN reservations r ON rd.reservation_id = r.reservation_id " +
            "WHERE rd.type_id = #{typeId} " +
            "AND r.status = '已确认' " +
            "AND r.expected_checkin < #{checkout} " +
            "AND r.expected_checkout > #{checkin}")
    int countReservedByTypeAndDateRange(@Param("typeId") Integer typeId,
                                         @Param("checkin") LocalDateTime checkin,
                                         @Param("checkout") LocalDateTime checkout);
}
