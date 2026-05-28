package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.Checkin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入住登记表 Mapper接口
 */
@Mapper
public interface CheckinMapper extends BaseMapper<Checkin> {
}
