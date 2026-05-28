package com.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hotel.entity.Room;
import com.hotel.service.RoomService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理模块 / 前台管理模块 - 客房控制器
 * 包含：客房信息增删改查、楼层/状态维护、房态查询
 */
@RestController
@RequestMapping("/api/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    /**
     * 新增客房
     * POST /api/room/add
     */
    @PostMapping("/add")
    public Result<?> addRoom(@RequestBody Room room) {
        roomService.createRoom(room);
        return Result.success("客房添加成功", null);
    }

    /**
     * 修改客房信息
     * PUT /api/room/update
     */
    @PutMapping("/update")
    public Result<?> updateRoom(@RequestBody Room room) {
        roomService.updateRoom(room);
        return Result.success("客房信息更新成功", null);
    }

    /**
     * 删除客房
     * DELETE /api/room/{roomId}
     */
    @DeleteMapping("/{roomId}")
    public Result<?> deleteRoom(@PathVariable Integer roomId) {
        roomService.deleteRoom(roomId);
        return Result.success("客房删除成功", null);
    }

    /**
     * 查询单个客房详情
     * GET /api/room/{roomId}
     */
    @GetMapping("/{roomId}")
    public Result<?> getRoomById(@PathVariable Integer roomId) {
        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error("客房不存在");
        }
        return Result.success(room);
    }

    /**
     * 分页查询客房（支持按状态、楼层、类型筛选）
     * GET /api/room/list?status=空闲&floor=1&typeId=1&pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<?> listRooms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Room> result = roomService.searchRooms(status, floor, typeId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 查询指定房型的空闲房间列表
     * GET /api/room/free/{typeId}
     */
    @GetMapping("/free/{typeId}")
    public Result<?> getFreeRooms(@PathVariable Integer typeId) {
        List<Room> rooms = roomService.getFreeRoomsByType(typeId);
        return Result.success(rooms);
    }

    /**
     * 获取房态概览统计
     * GET /api/room/overview
     */
    @GetMapping("/overview")
    public Result<?> getRoomOverview() {
        Map<String, Object> overview = roomService.getRoomStatusOverview();
        return Result.success(overview);
    }

    /**
     * 批量修改房间状态（如清洁完毕由清洁中置为空闲）
     * PUT /api/room/batch-status?roomIds=1,2,3&status=空闲
     */
    @PutMapping("/batch-status")
    public Result<?> batchUpdateStatus(
            @RequestParam List<Integer> roomIds,
            @RequestParam String status) {
        for (Integer roomId : roomIds) {
            Room room = roomService.getById(roomId);
            if (room != null) {
                room.setStatus(status);
                roomService.updateById(room);
            }
        }
        return Result.success("批量状态更新成功", null);
    }
}
