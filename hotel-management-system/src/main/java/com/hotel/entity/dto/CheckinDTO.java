package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 入住请求DTO
 */
@Data
public class CheckinDTO {
    /** 关联预订编号，可为空（无预订直接入住） */
    private Integer reservationId;

    @NotNull(message = "房间编号不能为空")
    private Integer roomId;

    @NotNull(message = "操作员编号不能为空")
    private Integer createdBy;

    @NotEmpty(message = "入住宾客列表不能为空")
    private List<CheckinGuestDTO> guests;
}
