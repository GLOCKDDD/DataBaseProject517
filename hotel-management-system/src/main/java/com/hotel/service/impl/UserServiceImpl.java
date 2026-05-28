package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.User;
import com.hotel.entity.dto.LoginDTO;
import com.hotel.entity.dto.UserDTO;
import com.hotel.mapper.UserMapper;
import com.hotel.service.UserService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 处理用户登录、注册、增删改查、角色权限分配
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     * 1. 按用户名查找用户
     * 2. 校验密码哈希
     * 3. 更新最后登录时间
     * 4. 返回用户信息（不含密码）
     */
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        // 返回用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("permissions", user.getPermissions());
        return result;
    }

    /**
     * 创建用户（管理员操作）
     * 1. 校验用户名唯一性
     * 2. 密码BCrypt加密
     * 3. 角色必须为admin或frontdesk
     */
    @Override
    public void createUser(UserDTO dto) {
        // 校验用户名唯一性
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 校验角色
        if (!"admin".equals(dto.getRole()) && !"frontdesk".equals(dto.getRole())) {
            throw new BusinessException("角色必须为admin或frontdesk");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setPermissions(dto.getPermissions());
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    /**
     * 更新用户信息
     * 如果提供了新密码则重新加密
     */
    @Override
    public void updateUser(UserDTO dto) {
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果修改了用户名，检查唯一性
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, dto.getUsername());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getPermissions() != null) {
            user.setPermissions(dto.getPermissions());
        }
        userMapper.updateById(user);
    }

    /**
     * 删除用户
     * 确保至少保留一个管理员账号
     */
    @Override
    public void deleteUser(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果删除的是管理员，检查是否还有其他管理员
        if ("admin".equals(user.getRole())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getRole, "admin");
            long adminCount = userMapper.selectCount(wrapper);
            if (adminCount <= 1) {
                throw new BusinessException("至少保留一个管理员账号");
            }
        }

        userMapper.deleteById(userId);
    }
}
