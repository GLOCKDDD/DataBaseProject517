package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.*;
import com.hotel.entity.dto.CheckinDTO;
import com.hotel.entity.dto.CheckinGuestDTO;
import com.hotel.mapper.*;
import com.hotel.service.CheckinService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 入住服务实现类
 * 处理入住办理、入住查询、宾客管理
 */
@Service
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements CheckinService {

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private CheckinGuestMapper checkinGuestMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReservationDetailMapper reservationDetailMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 办理入住（关联预订或直接入住）
     * 流程：
     * 1. 若提供了预订编号，校验预订存在且状态为"已确认"，更新为"已入住"
     * 2. 校验房间存在且状态为"空闲"
     * 3. 若有预订，校验房间类型与预订明细中的房型一致
     * 4. 更新房间状态为"占用"
     * 5. 创建入住登记记录
     * 6. 遍历宾客列表，校验客户存在并创建入住宾客记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createCheckin(CheckinDTO dto) {
        // 1. 处理关联预订
        Reservation reservation = null;
        if (dto.getReservationId() != null) {
            reservation = reservationMapper.selectById(dto.getReservationId());
            if (reservation == null) {
                throw new BusinessException("预订记录不存在");
            }
            if (!"已确认".equals(reservation.getStatus())) {
                throw new BusinessException("预订状态不是已确认，当前状态：" + reservation.getStatus());
            }
            reservation.setStatus("已入住");
            reservationMapper.updateById(reservation);
        }

        // 2. 校验房间状态
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        if (!"空闲".equals(room.getStatus())) {
            throw new BusinessException("房间当前不可入住，状态：" + room.getStatus());
        }

        // 3. 校验房间类型与预订匹配
        if (reservation != null) {
            LambdaQueryWrapper<ReservationDetail> dw = new LambdaQueryWrapper<>();
            dw.eq(ReservationDetail::getReservationId, reservation.getReservationId());
            List<ReservationDetail> details = reservationDetailMapper.selectList(dw);
            Set<Integer> reservedTypeIds = details.stream()
                    .map(ReservationDetail::getTypeId)
                    .collect(Collectors.toSet());
            if (!reservedTypeIds.contains(room.getTypeId())) {
                throw new BusinessException("所选房间的房型与预订不匹配");
            }
        }

        // 4. 更新房间状态为"占用"
        room.setStatus("占用");
        roomMapper.updateById(room);

        // 5. 创建入住登记记录
        Checkin checkin = new Checkin();
        checkin.setReservationId(dto.getReservationId());
        checkin.setRoomId(dto.getRoomId());
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setStatus("入住中");
        checkinMapper.insert(checkin);

        // 6. 创建入住宾客记录
        for (CheckinGuestDTO guestDTO : dto.getGuests()) {
            Customer customer = customerMapper.selectById(guestDTO.getCustomerId());
            if (customer == null) {
                throw new BusinessException("客户不存在，客户编号：" + guestDTO.getCustomerId());
            }
            CheckinGuest guest = new CheckinGuest();
            guest.setCheckinId(checkin.getCheckinId());
            guest.setCustomerId(guestDTO.getCustomerId());
            guest.setIsPrimary(guestDTO.getIsPrimary() != null ? guestDTO.getIsPrimary() : 0);
            checkinGuestMapper.insert(guest);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("checkinId", checkin.getCheckinId());
        result.put("roomId", room.getRoomId());
        result.put("roomNumber", room.getRoomNumber());
        result.put("checkinTime", checkin.getCheckinTime());
        result.put("status", checkin.getStatus());
        return result;
    }

    /**
     * 查询入住详情（含宾客信息和房间信息）
     */
    @Override
    public Map<String, Object> getCheckinDetail(Integer checkinId) {
        Checkin checkin = checkinMapper.selectById(checkinId);
        if (checkin == null) {
            throw new BusinessException("入住记录不存在");
        }
        LambdaQueryWrapper<CheckinGuest> gw = new LambdaQueryWrapper<>();
        gw.eq(CheckinGuest::getCheckinId, checkinId);
        List<CheckinGuest> guests = checkinGuestMapper.selectList(gw);
        Room room = roomMapper.selectById(checkin.getRoomId());

        Map<String, Object> result = new HashMap<>();
        result.put("checkin", checkin);
        result.put("guests", guests);
        result.put("room", room);
        return result;
    }

    /**
     * 分页查询入住记录
     */
    @Override
    public IPage<Checkin> searchCheckins(String status, Integer roomId, int pageNum, int pageSize) {
        Page<Checkin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Checkin::getStatus, status);
        }
        if (roomId != null) {
            wrapper.eq(Checkin::getRoomId, roomId);
        }
        wrapper.orderByDesc(Checkin::getCheckinTime);
        return checkinMapper.selectPage(page, wrapper);
    }

    /**
     * 获取入住宾客列表
     */
    @Override
    public List<CheckinGuest> getGuests(Integer checkinId) {
        LambdaQueryWrapper<CheckinGuest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinGuest::getCheckinId, checkinId);
        return checkinGuestMapper.selectList(wrapper);
    }
}
