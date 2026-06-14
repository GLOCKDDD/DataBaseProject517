package com.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 查询账单列表（分页，含房间号、房型、操作员）
     * GET /api/bill/list?pageNum=1&pageSize=20
     */
    @GetMapping("/list")
    public Result<?> listBills(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(billService.listBillsFull(pageNum, pageSize));
    }

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
     * 查询账单详情（含入住信息、宾客、房间信息）
     * GET /api/bill/{billId}
     */
    @GetMapping("/{billId}")
    public Result<?> getBillById(@PathVariable Integer billId) {
        Map<String, Object> detail = billService.getBillDetailFull(billId);
        return Result.success(detail);
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
