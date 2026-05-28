package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.User;
import com.hotel.entity.dto.LoginDTO;
import com.hotel.entity.dto.UserDTO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    Map<String, Object> login(LoginDTO dto);

    /**
     * 创建用户（管理员操作）
     */
    void createUser(UserDTO dto);

    /**
     * 更新用户信息
     */
    void updateUser(UserDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Integer userId);
}
