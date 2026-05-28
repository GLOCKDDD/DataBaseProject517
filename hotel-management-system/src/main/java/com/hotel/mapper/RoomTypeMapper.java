package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.RoomType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客房类型表 Mapper接口
 */
@Mapper
public interface RoomTypeMapper extends BaseMapper<RoomType> {
}
