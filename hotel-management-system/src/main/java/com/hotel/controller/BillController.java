package com.hotel.controller;

import com.hotel.entity.Bill;
import com.hotel.entity.dto.CheckoutDTO;
import com.hotel.service.BillService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 前台管理模块 - 结账管理控制器
 * 包含：结账处理、账单查询
 */
@RestController
@RequestMapping("/api/bill")
public class BillController {

    @Autowired
    private BillService billService;

    /**
     * 办理结账（自动计算费用、更新积分、房态、预订状态）
     * POST /api/bill/checkout
     */
    @PostMapping("/checkout")
    public Result<?> checkout(@Valid @RequestBody CheckoutDTO dto) {
        Map<String, Object> result = billService.createBill(dto);
        return Result.success("结账成功", result);
    }

    /**
     * 查询账单详情
     * GET /api/bill/{billId}
     */
    @GetMapping("/{billId}")
    public Result<?> getBillById(@PathVariable Integer billId) {
        Bill bill = billService.getBillDetail(billId);
        if (bill == null) {
            return Result.error("账单不存在");
        }
        return Result.success(bill);
    }

    /**
     * 根据入住登记编号查询账单
     * GET /api/bill/checkin/{checkinId}
     */
    @GetMapping("/checkin/{checkinId}")
    public Result<?> getBillByCheckinId(@PathVariable Integer checkinId) {
        Bill bill = billService.getBillByCheckinId(checkinId);
        if (bill == null) {
            return Result.error("未找到关联账单");
        }
        return Result.success(bill);
    }
}
