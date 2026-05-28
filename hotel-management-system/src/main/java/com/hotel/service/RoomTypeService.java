package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.RoomType;

/**
 * 客房类型服务接口
 */
public interface RoomTypeService extends IService<RoomType> {

    /**
     * 新增客房类型
     */
    void createRoomType(RoomType roomType);

    /**
     * 更新客房类型
     */
    void updateRoomType(RoomType roomType);

    /**
     * 删除客房类型（需检查是否有关联房间）
     */
    void deleteRoomType(Integer typeId);
}
