package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预订记录表 Mapper接口
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
