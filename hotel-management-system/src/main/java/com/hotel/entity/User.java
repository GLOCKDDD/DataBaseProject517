package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 - 对应数据字典 D1
 * 存储所有能登录系统的用户（管理员、前台操作员）
 */
@Data
@TableName("users")
public class User {

    /** 用户编号，主键，自增 */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /** 登录用户名，唯一 */
    private String username;

    /** 密码哈希值（加盐），不可逆存储 */
    private String passwordHash;

    /** 用户角色：admin-管理员，frontdesk-前台操作员 */
    private String role;

    /** 权限字段，JSON或逗号分隔的权限编码 */
    private String permissions;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后登录时间，每次登录更新 */
    private LocalDateTime lastLogin;
}
