package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.*;
import com.hotel.entity.dto.CheckoutDTO;
import com.hotel.mapper.*;
import com.hotel.service.BillService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 结账服务实现类
 * 处理退房结账的核心业务逻辑：费用计算、会员折扣、积分累计、房态更新、预订状态更新
 */
@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements BillService {

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private CheckinGuestMapper checkinGuestMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private com.hotel.mapper.UserMapper userMapper;

    /**
     * 办理结账（自动计算费用、更新积分、房态、预订状态）
     * 业务规则：
     * 1. 入住状态必须为"入住中"或"已换房"（未结算状态）
     * 2. 一条入住记录最多一条账单，防止重复结账
     * 3. 费用由入住时长×类型基础价格×会员折扣自动计算，不可随意篡改
     * 4. 结账后：入住状态→已退房，房间状态→清洁中，预订状态→已完成
     * 5. 会员积分自动累计：每消费10元积1分
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createBill(CheckoutDTO dto) {
        // 1. 查询入住登记记录
        Checkin checkin = checkinMapper.selectById(dto.getCheckinId());
        if (checkin == null) {
            throw new BusinessException("入住登记记录不存在");
        }

        // 2. 验证入住状态为未结算状态
        if (!"入住中".equals(checkin.getStatus()) && !"已换房".equals(checkin.getStatus())) {
            throw new BusinessException("该入住记录当前状态为「" + checkin.getStatus() + "」，无法办理结账");
        }

        // 3. 检查是否已存在账单，防止重复结账
        LambdaQueryWrapper<Bill> billWrapper = new LambdaQueryWrapper<>();
        billWrapper.eq(Bill::getCheckinId, dto.getCheckinId());
        if (baseMapper.selectCount(billWrapper) > 0) {
            throw new BusinessException("该入住记录已存在账单，不允许重复结账");
        }

        // 4. 查询房间信息
        Room room = roomMapper.selectById(checkin.getRoomId());
        if (room == null) {
            throw new BusinessException("关联房间不存在");
        }

        // 5. 查询房型信息，获取基础价格
        RoomType roomType = roomTypeMapper.selectById(room.getTypeId());
        if (roomType == null) {
            throw new BusinessException("房型信息不存在");
        }
        BigDecimal basePrice = roomType.getBasePrice();
        String roomTypeName = roomType.getTypeName();

        // 6. 查询主要入住客户信息
        LambdaQueryWrapper<CheckinGuest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(CheckinGuest::getCheckinId, dto.getCheckinId())
                .eq(CheckinGuest::getIsPrimary, 1);
        CheckinGuest primaryGuest = checkinGuestMapper.selectOne(guestWrapper);
        if (primaryGuest == null) {
            throw new BusinessException("未找到主要入住人信息");
        }
        Customer customer = customerMapper.selectById(primaryGuest.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户信息不存在");
        }

        // 7. 计算费用
        LocalDateTime checkinTime = checkin.getCheckinTime();
        LocalDateTime checkoutTime = LocalDateTime.now();

        // 计算入住天数，不足1天按1天计算
        long days = Duration.between(checkinTime, checkoutTime).toDays();
        if (days < 1) {
            days = 1;
        }

        // 基础房费 = 天数 × 基础单价
        BigDecimal baseFee = basePrice.multiply(BigDecimal.valueOf(days));

        // 根据会员等级确定折扣
        String membershipLevel = customer.getMembershipLevel();
        BigDecimal discount;
        if ("VIP".equals(membershipLevel)) {
            discount = new BigDecimal("0.9");
        } else if ("贵宾".equals(membershipLevel)) {
            discount = new BigDecimal("0.8");
        } else {
            discount = new BigDecimal("1.0");
            membershipLevel = "普通";
        }

        // 费用合计 = 基础房费 × 折扣，保留两位小数
        BigDecimal totalAmount = baseFee.multiply(discount).setScale(2, RoundingMode.HALF_UP);

        // 构建费用明细JSON
        String details = String.format(
                "{\"房型\":\"%s\",\"基础单价\":%s,\"入住天数\":%d,\"会员等级\":\"%s\",\"折扣\":%s,\"房费小计\":%s,\"费用合计\":%s}",
                roomTypeName, basePrice.toPlainString(), days, membershipLevel,
                discount.toPlainString(),
                baseFee.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                totalAmount.toPlainString());

        // 8. 创建账单记录
        Bill bill = new Bill();
        bill.setCheckinId(dto.getCheckinId());
        bill.setTotalAmount(totalAmount);
        bill.setPaymentTime(checkoutTime);
        bill.setDetails(details);
        bill.setCreatedBy(dto.getCreatedBy());
        baseMapper.insert(bill);

        // 9. 更新入住登记状态为"已退房"
        checkin.setStatus("已退房");
        checkin.setCheckoutTime(checkoutTime);
        checkinMapper.updateById(checkin);

        // 10. 更新房间状态为"清洁中"
        room.setStatus("清洁中");
        roomMapper.updateById(room);

        // 11. 如果关联了预订，更新预订状态为"已完成"
        if (checkin.getReservationId() != null) {
            Reservation reservation = reservationMapper.selectById(checkin.getReservationId());
            if (reservation != null) {
                reservation.setStatus("已完成");
                reservationMapper.updateById(reservation);
            }
        }

        // 12. 更新客户积分：每消费10元积1分
        int earnedPoints = totalAmount.intValue() / 10;
        customer.setPoints(customer.getPoints() + earnedPoints);
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.updateById(customer);

        // 13. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("billId", bill.getBillId());
        result.put("checkinId", dto.getCheckinId());
        result.put("totalAmount", totalAmount);
        result.put("paymentTime", checkoutTime);
        result.put("details", details);
        result.put("earnedPoints", earnedPoints);
        result.put("customerName", customer.getName());
        result.put("roomNumber", room.getRoomNumber());
        result.put("roomTypeName", roomTypeName);
        result.put("days", days);
        result.put("membershipLevel", membershipLevel);
        result.put("discount", discount);
        return result;
    }

    /**
     * 查询账单详情
     */
    @Override
    public Bill getBillDetail(Integer billId) {
        return baseMapper.selectById(billId);
    }

    /**
     * 根据入住登记编号查询账单
     */
    @Override
    public Bill getBillByCheckinId(Integer checkinId) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getCheckinId, checkinId);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 查询账单列表（分页）
     */
    @Override
    public IPage<Bill> listBills(int pageNum, int pageSize) {
        Page<Bill> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Bill::getPaymentTime);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 富化账单列表（含房间号、房型、操作员）
     */
    @Override
    public IPage<Map<String, Object>> listBillsFull(int pageNum, int pageSize) {
        Page<Bill> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Bill::getPaymentTime);
        IPage<Bill> billPage = baseMapper.selectPage(page, wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(billPage.getCurrent(), billPage.getSize(), billPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Bill bill : billPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("bill_id", bill.getBillId());
            item.put("checkin_id", bill.getCheckinId());
            item.put("total_amount", bill.getTotalAmount());
            item.put("payment_time", bill.getPaymentTime());
            item.put("details", bill.getDetails());
            item.put("created_by", bill.getCreatedBy());

            Checkin checkin = checkinMapper.selectById(bill.getCheckinId());
            if (checkin != null) {
                Room room = roomMapper.selectById(checkin.getRoomId());
                if (room != null) {
                    item.put("room_number", room.getRoomNumber());
                    RoomType rt = roomTypeMapper.selectById(room.getTypeId());
                    item.put("room_type_name", rt != null ? rt.getTypeName() : "");
                }
            }

            com.hotel.entity.User operator = bill.getCreatedBy() != null ? userMapper.selectById(bill.getCreatedBy()) : null;
            item.put("operator", operator != null ? operator.getUsername() : "");
            records.add(item);
        }
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 富化账单详情（含入住信息、宾客、房间信息）
     */
    @Override
    public Map<String, Object> getBillDetailFull(Integer billId) {
        Bill bill = baseMapper.selectById(billId);
        if (bill == null) {
            throw new com.hotel.util.BusinessException("账单不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("bill_id", bill.getBillId());
        result.put("checkin_id", bill.getCheckinId());
        result.put("total_amount", bill.getTotalAmount());
        result.put("payment_time", bill.getPaymentTime());
        result.put("details", bill.getDetails());

        Checkin checkin = checkinMapper.selectById(bill.getCheckinId());
        if (checkin != null) {
            Map<String, Object> checkinMap = new HashMap<>();
            checkinMap.put("checkin_time", checkin.getCheckinTime());
            checkinMap.put("checkout_time", checkin.getCheckoutTime());
            result.put("checkin", checkinMap);

            Room room = roomMapper.selectById(checkin.getRoomId());
            if (room != null) {
                result.put("room_number", room.getRoomNumber());
                RoomType rt = roomTypeMapper.selectById(room.getTypeId());
                result.put("room_type_name", rt != null ? rt.getTypeName() : "");
            }

            LambdaQueryWrapper<CheckinGuest> gw = new LambdaQueryWrapper<>();
            gw.eq(CheckinGuest::getCheckinId, checkin.getCheckinId());
            List<CheckinGuest> guests = checkinGuestMapper.selectList(gw);
            List<Map<String, Object>> guestList = new ArrayList<>();
            for (CheckinGuest g : guests) {
                Map<String, Object> gm = new HashMap<>();
                Customer c = customerMapper.selectById(g.getCustomerId());
                gm.put("customer_id", g.getCustomerId());
                gm.put("customer_name", c != null ? c.getName() : "");
                gm.put("is_primary", g.getIsPrimary());
                guestList.add(gm);
            }
            result.put("guests", guestList);
        } else {
            result.put("guests", new ArrayList<>());
        }
        return result;
    }
}
