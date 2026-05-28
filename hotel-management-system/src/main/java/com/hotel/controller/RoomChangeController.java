package com.hotel.controller;

import com.hotel.entity.RoomChange;
import com.hotel.entity.dto.RoomChangeDTO;
import com.hotel.service.RoomChangeService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 前台管理模块 - 换房管理控制器
 * 包含：换房操作、换房历史查询
 */
@RestController
@RequestMapping("/api/roomchange")
public class RoomChangeController {

    @Autowired
    private RoomChangeService roomChangeService;

    /**
     * 办理换房
     * POST /api/roomchange/create
     */
    @PostMapping("/create")
    public Result<?> createRoomChange(@Valid @RequestBody RoomChangeDTO dto) {
        roomChangeService.createRoomChange(dto);
        return Result.success("换房成功", null);
    }

    /**
     * 查询某入住记录的换房历史
     * GET /api/roomchange/list/{checkinId}
     */
    @GetMapping("/list/{checkinId}")
    public Result<?> getChangesByCheckin(@PathVariable Integer checkinId) {
        List<RoomChange> changes = roomChangeService.getChangesByCheckin(checkinId);
        return Result.success(changes);
    }
}
