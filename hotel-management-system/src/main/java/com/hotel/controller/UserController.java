package com.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.entity.User;
import com.hotel.entity.dto.LoginDTO;
import com.hotel.entity.dto.UserDTO;
import com.hotel.mapper.UserMapper;
import com.hotel.service.UserService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 权限管理模块 - 用户控制器
 * 包含：用户登录、注册、增删改查、角色权限分配
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        Map<String, Object> result = userService.login(dto);
        return Result.success("登录成功", result);
    }

    /**
     * 用户注册（管理员创建操作员账号）
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody UserDTO dto) {
        userService.createUser(dto);
        return Result.success("用户创建成功", null);
    }

    /**
     * 修改用户信息
     * PUT /api/user/update
     */
    @PutMapping("/update")
    public Result<?> updateUser(@Valid @RequestBody UserDTO dto) {
        userService.updateUser(dto);
        return Result.success("用户信息更新成功", null);
    }

    /**
     * 删除用户
     * DELETE /api/user/{userId}
     */
    @DeleteMapping("/{userId}")
    public Result<?> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return Result.success("用户删除成功", null);
    }

    /**
     * 查询用户列表（分页）
     * GET /api/user/list?pageNum=1&pageSize=10&role=admin
     */
    @GetMapping("/list")
    public Result<?> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result;
        if (role != null && !role.isEmpty()) {
            result = userService.page(page,
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getRole, role)
                            .orderByDesc(User::getCreatedAt));
        } else {
            result = userService.page(page,
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .orderByDesc(User::getCreatedAt));
        }
        return Result.success(result);
    }

    /**
     * 查询单个用户详情
     * GET /api/user/{userId}
     */
    @GetMapping("/{userId}")
    public Result<?> getUserById(@PathVariable Integer userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPasswordHash(null); // 不返回密码
        return Result.success(user);
    }
}
