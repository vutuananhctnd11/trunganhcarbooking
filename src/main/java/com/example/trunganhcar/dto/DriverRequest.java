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
public class DriverRequest {

    @Schema(description = "Họ và tên tài xế", example = "Nguyễn Trung Anh")
    private String fullName;

    @Schema(description = "Số điện thoại tài xế", example = "0912345678")
    private String phone;

    @Schema(description = "Số năm kinh nghiệm lái xe", example = "5")
    private Integer experienceYears;

    @Schema(description = "Dòng xe / loại xe", example = "Toyota Fortuner 2022")
    private String vehicleModel;

    @Schema(description = "Số lượng chỗ ngồi", example = "7")
    private Integer seatCount;

    @Schema(description = "Biển số xe", example = "30H-888.88")
    private String licensePlate;

    @Schema(description = "Chat ID trên Telegram của tài xế", example = "123456789")
    private String telegramChatId;

    @Schema(description = "Đánh giá chất lượng (1-5 sao)", example = "5.0")
    private Double rating;

    @Schema(description = "Trạng thái sẵn sàng hoạt động", example = "true")
    private Boolean isActive;
}

