package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.RoomChange;
import com.hotel.entity.dto.RoomChangeDTO;

import java.util.List;

/**
 * 换房服务接口
 */
public interface RoomChangeService extends IService<RoomChange> {

    /**
     * 办理换房
     */
    void createRoomChange(RoomChangeDTO dto);

    /**
     * 查询某入住记录的换房历史
     */
    List<RoomChange> getChangesByCheckin(Integer checkinId);
}
