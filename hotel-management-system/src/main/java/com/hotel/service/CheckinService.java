package com.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.Checkin;
import com.hotel.entity.CheckinGuest;
import com.hotel.entity.dto.CheckinDTO;

import java.util.List;
import java.util.Map;

/**
 * 入住服务接口
 */
public interface CheckinService extends IService<Checkin> {

    /**
     * 办理入住（关联预订或直接入住）
     */
    Map<String, Object> createCheckin(CheckinDTO dto);

    /**
     * 查询入住详情（含宾客信息）
     */
    Map<String, Object> getCheckinDetail(Integer checkinId);

    /**
     * 分页查询入住记录
     */
    IPage<Checkin> searchCheckins(String status, Integer roomId, int pageNum, int pageSize);

    /**
     * 获取入住宾客列表
     */
    List<CheckinGuest> getGuests(Integer checkinId);

    /**
     * 获取富化入住列表（含房间号、房型、宾客信息）
     */
    List<Map<String, Object>> listCheckinsFull(String status);
}
