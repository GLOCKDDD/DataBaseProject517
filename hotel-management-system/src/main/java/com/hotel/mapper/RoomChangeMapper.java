package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.RoomChange;
import org.apache.ibatis.annotations.Mapper;

/**
 * 换房记录表 Mapper接口
 */
@Mapper
public interface RoomChangeMapper extends BaseMapper<RoomChange> {
}
