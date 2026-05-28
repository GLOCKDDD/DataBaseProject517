package com.hotel.entity.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预订请求DTO
 */
@Data
public class ReservationDTO {
    @NotNull(message = "客户编号不能为空")
    private Integer customerId;

    @NotNull(message = "操作员编号不能为空")
    private Integer createdBy;

    @NotNull(message = "预计入住时间不能为空")
    private LocalDateTime expectedCheckin;

    @NotNull(message = "预计离开时间不能为空")
    private LocalDateTime expectedCheckout;

    @Min(value = 1, message = "入住人数至少为1")
    private Integer guestCount;

    private String remark;

    @NotEmpty(message = "预订需求明细不能为空")
    private List<ReservationDetailDTO> details;
}
