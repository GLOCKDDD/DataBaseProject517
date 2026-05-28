package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 换房请求DTO
 */
@Data
public class RoomChangeDTO {
    @NotNull(message = "入住登记编号不能为空")
    private Integer checkinId;

    @NotNull(message = "新房间编号不能为空")
    private Integer newRoomId;

    private String reason;

    @NotNull(message = "操作员编号不能为空")
    private Integer createdBy;
}
