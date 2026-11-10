package com.example.trunganhcar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRejectRequest {

    @Schema(description = "Lý do từ chối phê duyệt tài xế", example = "Biển số xe không rõ ràng hoặc hình ảnh giấy tờ chưa đạt chuẩn")
    private String reason;
}

