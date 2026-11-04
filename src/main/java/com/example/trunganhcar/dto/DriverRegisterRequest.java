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
public class DriverRegisterRequest {

    @Schema(description = "Họ và tên tài xế", example = "Vũ Tuấn Anh", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Số điện thoại liên hệ", example = "0988123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Biển số xe", example = "30K - 999.88", requiredMode = Schema.RequiredMode.REQUIRED)
    private String licensePlate;

    @Schema(description = "Dòng xe VinFast", example = "VinFast Limo Green (VF 8)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String vehicleModel;

    @Schema(description = "Màu sơn xe", example = "🔴 Đỏ Crimson (Đỏ đô ánh kim)")
    private String vehicleColor;

    @Schema(description = "Mật khẩu đăng nhập tài xế tự nhập", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
