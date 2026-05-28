package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 预订需求明细DTO
 */
@Data
public class ReservationDetailDTO {
    @NotNull(message = "房间类型编号不能为空")
    private Integer typeId;

    @Min(value = 1, message = "需求间数至少为1")
    private Integer roomCount;
}
