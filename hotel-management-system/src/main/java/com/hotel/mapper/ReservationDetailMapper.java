package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.ReservationDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预订需求明细表 Mapper接口
 */
@Mapper
public interface ReservationDetailMapper extends BaseMapper<ReservationDetail> {
}
