package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.mapper.RoomMapper;
import com.hotel.mapper.RoomTypeMapper;
import com.hotel.service.RoomService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客房服务实现类
 * 处理客房信息增删改查、楼层/状态维护、房态查询
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    /**
     * 新增客房
     * 校验：房间号唯一、所属类型存在
     */
    @Override
    public void createRoom(Room room) {
        // 校验房间号唯一性
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomNumber, room.getRoomNumber());
        if (roomMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("房间号已存在");
        }

        // 校验类型存在
        RoomType roomType = roomTypeMapper.selectById(room.getTypeId());
        if (roomType == null) {
            throw new BusinessException("客房类型不存在");
        }

        // 默认状态为空闲
        if (room.getStatus() == null || room.getStatus().isEmpty()) {
            room.setStatus("空闲");
        }

        roomMapper.insert(room);
    }

    /**
     * 更新客房信息
     */
    @Override
    public void updateRoom(Room room) {
        Room existing = roomMapper.selectById(room.getRoomId());
        if (existing == null) {
            throw new BusinessException("客房不存在");
        }

        // 如果修改了房间号，检查唯一性
        if (room.getRoomNumber() != null && !room.getRoomNumber().equals(existing.getRoomNumber())) {
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Room::getRoomNumber, room.getRoomNumber());
            if (roomMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("房间号已存在");
            }
        }

        // 校验类型存在
        if (room.getTypeId() != null) {
            RoomType roomType = roomTypeMapper.selectById(room.getTypeId());
            if (roomType == null) {
                throw new BusinessException("客房类型不存在");
            }
        }

        roomMapper.updateById(room);
    }

    /**
     * 删除客房
     * 占用中的房间不能删除
     */
    @Override
    public void deleteRoom(Integer roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("客房不存在");
        }
        if ("占用".equals(room.getStatus())) {
            throw new BusinessException("占用中的房间不能删除");
        }
        roomMapper.deleteById(roomId);
    }

    /**
     * 查询指定房型的空闲房间列表
     */
    @Override
    public List<Room> getFreeRoomsByType(Integer typeId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getTypeId, typeId).eq(Room::getStatus, "空闲");
        return roomMapper.selectList(wrapper);
    }

    /**
     * 分页查询房间（支持按状态、楼层、类型筛选）
     */
    @Override
    public IPage<Room> searchRooms(String status, String floor, Integer typeId, int pageNum, int pageSize) {
        Page<Room> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Room::getStatus, status);
        }
        if (floor != null && !floor.isEmpty()) {
            wrapper.eq(Room::getFloor, floor);
        }
        if (typeId != null) {
            wrapper.eq(Room::getTypeId, typeId);
        }
        wrapper.orderByAsc(Room::getRoomNumber);
        return roomMapper.selectPage(page, wrapper);
    }

    /**
     * 获取房态概览统计
     * 返回各状态房间数量和总数
     */
    @Override
    public Map<String, Object> getRoomStatusOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("total", roomMapper.selectCount(null));

        LambdaQueryWrapper<Room> freeWrapper = new LambdaQueryWrapper<>();
        freeWrapper.eq(Room::getStatus, "空闲");
        overview.put("空闲", roomMapper.selectCount(freeWrapper));

        LambdaQueryWrapper<Room> occupiedWrapper = new LambdaQueryWrapper<>();
        occupiedWrapper.eq(Room::getStatus, "占用");
        overview.put("占用", roomMapper.selectCount(occupiedWrapper));

        LambdaQueryWrapper<Room> cleaningWrapper = new LambdaQueryWrapper<>();
        cleaningWrapper.eq(Room::getStatus, "清洁中");
        overview.put("清洁中", roomMapper.selectCount(cleaningWrapper));

        LambdaQueryWrapper<Room> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.eq(Room::getStatus, "维修中");
        overview.put("维修中", roomMapper.selectCount(repairWrapper));

        return overview;
    }
}
