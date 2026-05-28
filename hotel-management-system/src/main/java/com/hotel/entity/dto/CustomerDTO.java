package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 客户信息请求DTO
 */
@Data
public class CustomerDTO {
    private Integer customerId;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[0-9]{17}[0-9Xx]$", message = "身份证号格式不正确，需18位")
    private String idNumber;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String address;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String membershipLevel;
    private Integer points;
}
