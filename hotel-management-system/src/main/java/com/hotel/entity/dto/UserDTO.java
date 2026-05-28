package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户创建/修改请求DTO
 */
@Data
public class UserDTO {
    private Integer userId;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;

    private String permissions;
}
