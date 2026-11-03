package com.example.trunganhcar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountValidateRequest {

    @Schema(description = "Mã giảm giá cần áp dụng", example = "TRUNGANH50K", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Tổng giá trị chuyến xe ban đầu (VND)", example = "250000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal tripPrice;
}

