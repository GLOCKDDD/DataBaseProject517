package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 结账请求DTO
 */
@Data
public class CheckoutDTO {
    @NotNull(message = "入住登记编号不能为空")
    private Integer checkinId;

    @NotNull(message = "操作员编号不能为空")
    private Integer createdBy;
}
