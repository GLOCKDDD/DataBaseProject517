package com.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.Bill;
import com.hotel.entity.dto.CheckoutDTO;

import java.util.Map;

/**
 * 结账服务接口
 */
public interface BillService extends IService<Bill> {

    Map<String, Object> createBill(CheckoutDTO dto);

    Bill getBillDetail(Integer billId);

    Bill getBillByCheckinId(Integer checkinId);

    IPage<Bill> listBills(int pageNum, int pageSize);

    /** 含房间号、房型、操作员的富化账单列表 */
    IPage<Map<String, Object>> listBillsFull(int pageNum, int pageSize);

    /** 含入住信息、宾客、房间信息的富化账单详情 */
    Map<String, Object> getBillDetailFull(Integer billId);
}
