package com.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationDetail;
import com.hotel.entity.dto.ReservationDTO;
import com.hotel.service.ReservationService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 前台管理模块 - 预订管理控制器
 * 包含：创建预订、预订需求明细、校验房间可预订数量、修改预订状态
 */
@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    /**
     * 创建预订（含需求明细校验）
     * POST /api/reservation/create
     */
    @PostMapping("/create")
    public Result<?> createReservation(@Valid @RequestBody ReservationDTO dto) {
        Map<String, Object> result = reservationService.createReservation(dto);
        return Result.success("预订创建成功", result);
    }

    /**
     * 查询预订详情（含需求明细）
     * GET /api/reservation/{reservationId}
     */
    @GetMapping("/{reservationId}")
    public Result<?> getReservationDetail(@PathVariable Integer reservationId) {
        Map<String, Object> detail = reservationService.getReservationDetail(reservationId);
        return Result.success(detail);
    }

    /**
     * 分页查询预订记录
     * GET /api/reservation/list?status=已确认&customerId=1&pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<?> listReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Reservation> result = reservationService.searchReservations(status, customerId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 取消预订
     * PUT /api/reservation/cancel/{reservationId}
     */
    @PutMapping("/cancel/{reservationId}")
    public Result<?> cancelReservation(@PathVariable Integer reservationId) {
        reservationService.cancelReservation(reservationId);
        return Result.success("预订已取消", null);
    }

    /**
     * 获取预订的需求明细列表
     * GET /api/reservation/{reservationId}/details
     */
    @GetMapping("/{reservationId}/details")
    public Result<?> getReservationDetails(@PathVariable Integer reservationId) {
        List<ReservationDetail> details = reservationService.getDetails(reservationId);
        return Result.success(details);
    }
}
