package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 入住宾客DTO
 */
@Data
public class CheckinGuestDTO {
    @NotNull(message = "客户编号不能为空")
    private Integer customerId;

    /** 是否主要入住人：0-否，1-是 */
    private Integer isPrimary;
}
