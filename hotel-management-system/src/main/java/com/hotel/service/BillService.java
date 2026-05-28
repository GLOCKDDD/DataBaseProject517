package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.Bill;
import com.hotel.entity.dto.CheckoutDTO;

import java.util.Map;

/**
 * 结账服务接口
 */
public interface BillService extends IService<Bill> {

    /**
     * 办理结账（自动计算费用、更新积分、房态、预订状态）
     */
    Map<String, Object> createBill(CheckoutDTO dto);

    /**
     * 查询账单详情
     */
    Bill getBillDetail(Integer billId);

    /**
     * 根据入住登记编号查询账单
     */
    Bill getBillByCheckinId(Integer checkinId);
}
