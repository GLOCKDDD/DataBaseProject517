package com.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hotel.entity.Checkin;
import com.hotel.entity.CheckinGuest;
import com.hotel.entity.dto.CheckinDTO;
import com.hotel.service.CheckinService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 前台管理模块 - 入住管理控制器
 * 包含：关联/不关联预订办理入住、分配空闲房间、多宾客登记、更新房态
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    /**
     * 办理入住（关联预订或直接入住）
     * POST /api/checkin/create
     */
    @PostMapping("/create")
    public Result<?> createCheckin(@Valid @RequestBody CheckinDTO dto) {
        Map<String, Object> result = checkinService.createCheckin(dto);
        return Result.success("入住办理成功", result);
    }

    /**
     * 查询入住详情（含宾客信息）
     * GET /api/checkin/{checkinId}
     */
    @GetMapping("/{checkinId}")
    public Result<?> getCheckinDetail(@PathVariable Integer checkinId) {
        Map<String, Object> detail = checkinService.getCheckinDetail(checkinId);
        return Result.success(detail);
    }

    /**
     * 分页查询入住记录
     * GET /api/checkin/list?status=入住中&roomId=1&pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<?> listCheckins(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Checkin> result = checkinService.searchCheckins(status, roomId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取入住宾客列表
     * GET /api/checkin/{checkinId}/guests
     */
    @GetMapping("/{checkinId}/guests")
    public Result<?> getCheckinGuests(@PathVariable Integer checkinId) {
        List<CheckinGuest> guests = checkinService.getGuests(checkinId);
        return Result.success(guests);
    }

    /**
     * 查询富化入住列表（含房间号、房型、宾客信息）
     * GET /api/checkin/listfull?status=入住中
     */
    @GetMapping("/listfull")
    public Result<?> listCheckinsFull(@RequestParam(required = false) String status) {
        return Result.success(checkinService.listCheckinsFull(status));
    }
}
