package com.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationDetail;
import com.hotel.entity.dto.ReservationDTO;

import java.util.List;
import java.util.Map;

/**
 * 预订服务接口
 */
public interface ReservationService extends IService<Reservation> {

    Map<String, Object> createReservation(ReservationDTO dto);

    Map<String, Object> getReservationDetail(Integer reservationId);

    IPage<Map<String, Object>> searchReservations(String status, Integer customerId, int pageNum, int pageSize);

    void cancelReservation(Integer reservationId);

    List<ReservationDetail> getDetails(Integer reservationId);
}
