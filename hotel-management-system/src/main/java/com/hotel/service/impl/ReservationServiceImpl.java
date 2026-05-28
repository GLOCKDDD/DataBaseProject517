package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationDetail;
import com.hotel.entity.dto.ReservationDTO;
import com.hotel.entity.dto.ReservationDetailDTO;
import com.hotel.mapper.CustomerMapper;
import com.hotel.mapper.ReservationDetailMapper;
import com.hotel.mapper.ReservationMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.mapper.RoomTypeMapper;
import com.hotel.service.ReservationService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 预订服务实现类
 * 处理预订创建、查询、取消等核心业务逻辑及业务规则校验
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReservationDetailMapper reservationDetailMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    /**
     * 创建预订（含需求明细校验）
     * 业务规则：
     * 1. 预计离开时间必须大于预计入住时间
     * 2. 客户编号必须存在
     * 3. 每条明细的房间类型必须存在，需求间数必须大于0
     * 4. 每种房型的空闲房间数必须满足需求间数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createReservation(ReservationDTO dto) {
        // 校验预计离开时间必须大于预计入住时间
        if (!dto.getExpectedCheckout().isAfter(dto.getExpectedCheckin())) {
            throw new BusinessException("预计离开时间必须大于预计入住时间");
        }

        // 校验客户是否存在
        if (customerMapper.selectById(dto.getCustomerId()) == null) {
            throw new BusinessException("客户不存在");
        }

        // 遍历预订明细，逐条校验房型及可用房间数
        for (ReservationDetailDTO detailDTO : dto.getDetails()) {
            if (roomTypeMapper.selectById(detailDTO.getTypeId()) == null) {
                throw new BusinessException("房间类型不存在，typeId=" + detailDTO.getTypeId());
            }
            if (detailDTO.getRoomCount() == null || detailDTO.getRoomCount() <= 0) {
                throw new BusinessException("需求间数必须大于0");
            }
            // 可预订数 = 该房型总数 - 已占用数 - 时间段内已确认预订数
            int totalRooms = roomMapper.countTotalByType(detailDTO.getTypeId());
            int occupiedCount = roomMapper.countOccupiedByType(detailDTO.getTypeId());
            int reservedCount = roomMapper.countReservedByTypeAndDateRange(
                    detailDTO.getTypeId(), dto.getExpectedCheckin(), dto.getExpectedCheckout());
            int availableCount = totalRooms - occupiedCount - reservedCount;
            if (availableCount < detailDTO.getRoomCount()) {
                throw new BusinessException("房型(typeId=" + detailDTO.getTypeId()
                        + ")可预订房间不足，当前可预订" + availableCount + "间，需求" + detailDTO.getRoomCount() + "间");
            }
        }

        // 构建预订主记录
        Reservation reservation = new Reservation();
        reservation.setCustomerId(dto.getCustomerId());
        reservation.setCreatedBy(dto.getCreatedBy());
        reservation.setExpectedCheckin(dto.getExpectedCheckin());
        reservation.setExpectedCheckout(dto.getExpectedCheckout());
        reservation.setGuestCount(dto.getGuestCount());
        reservation.setStatus("已确认");
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setRemark(dto.getRemark());
        reservationMapper.insert(reservation);

        // 逐条插入预订需求明细
        for (ReservationDetailDTO detailDTO : dto.getDetails()) {
            ReservationDetail detail = new ReservationDetail();
            detail.setReservationId(reservation.getReservationId());
            detail.setTypeId(detailDTO.getTypeId());
            detail.setRoomCount(detailDTO.getRoomCount());
            reservationDetailMapper.insert(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reservationId", reservation.getReservationId());
        return result;
    }

    /**
     * 查询预订详情（含需求明细）
     */
    @Override
    public Map<String, Object> getReservationDetail(Integer reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预订记录不存在");
        }
        LambdaQueryWrapper<ReservationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationDetail::getReservationId, reservationId);
        List<ReservationDetail> details = reservationDetailMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("reservation", reservation);
        result.put("details", details);
        return result;
    }

    /**
     * 分页查询预订记录
     */
    @Override
    public IPage<Reservation> searchReservations(String status, Integer customerId, int pageNum, int pageSize) {
        Page<Reservation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Reservation::getStatus, status);
        }
        if (customerId != null) {
            wrapper.eq(Reservation::getCustomerId, customerId);
        }
        wrapper.orderByDesc(Reservation::getCreatedAt);
        return reservationMapper.selectPage(page, wrapper);
    }

    /**
     * 取消预订
     * 仅"已确认"状态的预订可以取消
     */
    @Override
    public void cancelReservation(Integer reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预订记录不存在");
        }
        if (!"已确认".equals(reservation.getStatus())) {
            throw new BusinessException("只有已确认的预订才能取消，当前状态：" + reservation.getStatus());
        }
        reservation.setStatus("已取消");
        reservationMapper.updateById(reservation);
    }

    /**
     * 获取预订的需求明细列表
     */
    @Override
    public List<ReservationDetail> getDetails(Integer reservationId) {
        LambdaQueryWrapper<ReservationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationDetail::getReservationId, reservationId);
        return reservationDetailMapper.selectList(wrapper);
    }
}
