package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.Bill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 结账记录表 Mapper接口
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}
