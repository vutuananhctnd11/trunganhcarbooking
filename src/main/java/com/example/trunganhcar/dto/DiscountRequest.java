package com.example.trunganhcar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRequest {

    @Schema(description = "Mã giảm giá (viết hoa, không dấu)", example = "TRUNGANH50K", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Tiêu đề mã giảm giá", example = "Giảm 50K cho chuyến đi sân bay", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Mô tả điều kiện áp dụng", example = "Áp dụng cho mọi chuyến xe có giá trị từ 200.000đ trở lên")
    private String description;

    @Schema(description = "Loại giảm giá (FIXED_AMOUNT: số tiền cố định, PERCENTAGE: phần trăm)", example = "FIXED_AMOUNT")
    @Builder.Default
    private String discountType = "FIXED_AMOUNT";

    @Schema(description = "Giá trị giảm (VD: 50000 hoặc 10%)", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal discountValue;

    @Schema(description = "Số tiền giảm tối đa (nếu là PERCENTAGE)", example = "100000")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "Giá trị đơn hàng tối thiểu để được áp dụng", example = "200000")
    private BigDecimal minTripPrice;

    @Schema(description = "Số lượt sử dụng tối đa (null nếu không giới hạn)", example = "100")
    private Integer usageLimit;

    @Schema(description = "Thời gian bắt đầu có hiệu lực", example = "2026-11-01T00:00:00")
    private LocalDateTime startAt;

    @Schema(description = "Thời gian hết hạn", example = "2026-12-31T23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "Trạng thái kích hoạt", example = "true")
    @Builder.Default
    private Boolean isActive = true;
}

