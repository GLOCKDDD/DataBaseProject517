package com.hotel.entity.dto;

import lombok.Data;

/**
 * 用户创建/修改请求DTO
 * username 和 role 对于部分更新（如仅更新权限）可以为空，服务层负责校验
 */
@Data
public class UserDTO {
    private Integer userId;

    private String username;

    private String password;

    private String role;

    private String permissions;
}
