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

    /**
     * 创建预订（含需求明细校验）
     */
    Map<String, Object> createReservation(ReservationDTO dto);

    /**
     * 查询预订详情（含需求明细）
     */
    Map<String, Object> getReservationDetail(Integer reservationId);

    /**
     * 分页查询预订记录
     */
    IPage<Reservation> searchReservations(String status, Integer customerId, int pageNum, int pageSize);

    /**
     * 取消预订
     */
    void cancelReservation(Integer reservationId);

    /**
     * 获取预订的需求明细列表
     */
    List<ReservationDetail> getDetails(Integer reservationId);
}
