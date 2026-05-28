package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.CheckinGuest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入住宾客明细表 Mapper接口
 */
@Mapper
public interface CheckinGuestMapper extends BaseMapper<CheckinGuest> {
}
