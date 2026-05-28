package com.hotel.controller;

import com.hotel.entity.RoomType;
import com.hotel.service.RoomTypeService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理模块 - 客房类型控制器
 * 包含：客房类型增删改查、价格/最大人数维护
 */
@RestController
@RequestMapping("/api/roomtype")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    /**
     * 查询所有客房类型
     * GET /api/roomtype/list
     */
    @GetMapping("/list")
    public Result<?> listRoomTypes() {
        List<RoomType> list = roomTypeService.list();
        return Result.success(list);
    }

    /**
     * 查询单个客房类型
     * GET /api/roomtype/{typeId}
     */
    @GetMapping("/{typeId}")
    public Result<?> getRoomTypeById(@PathVariable Integer typeId) {
        RoomType roomType = roomTypeService.getById(typeId);
        if (roomType == null) {
            return Result.error("客房类型不存在");
        }
        return Result.success(roomType);
    }

    /**
     * 新增客房类型
     * POST /api/roomtype/add
     */
    @PostMapping("/add")
    public Result<?> addRoomType(@RequestBody RoomType roomType) {
        roomTypeService.createRoomType(roomType);
        return Result.success("客房类型添加成功", null);
    }

    /**
     * 修改客房类型
     * PUT /api/roomtype/update
     */
    @PutMapping("/update")
    public Result<?> updateRoomType(@RequestBody RoomType roomType) {
        roomTypeService.updateRoomType(roomType);
        return Result.success("客房类型更新成功", null);
    }

    /**
     * 删除客房类型
     * DELETE /api/roomtype/{typeId}
     */
    @DeleteMapping("/{typeId}")
    public Result<?> deleteRoomType(@PathVariable Integer typeId) {
        roomTypeService.deleteRoomType(typeId);
        return Result.success("客房类型删除成功", null);
    }
}
