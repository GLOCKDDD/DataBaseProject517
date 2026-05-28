package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
