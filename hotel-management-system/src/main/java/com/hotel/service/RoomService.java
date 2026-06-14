package com.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.Room;

import java.util.List;
import java.util.Map;

/**
 * 客房服务接口
 */
public interface RoomService extends IService<Room> {

    /**
     * 新增客房
     */
    void createRoom(Room room);

    /**
     * 更新客房信息
     */
    void updateRoom(Room room);

    /**
     * 删除客房
     */
    void deleteRoom(Integer roomId);

    /**
     * 查询指定房型的空闲房间列表
     */
    List<Room> getFreeRoomsByType(Integer typeId);

    /**
     * 分页查询房间（支持按状态、楼层、类型、房间号关键字筛选）
     */
    IPage<Room> searchRooms(String status, String floor, Integer typeId, String keyword, int pageNum, int pageSize);

    /**
     * 获取房态概览统计
     */
    Map<String, Object> getRoomStatusOverview();
}
