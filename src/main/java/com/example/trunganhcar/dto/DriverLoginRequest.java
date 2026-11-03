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
public class DriverLoginRequest {

    @Schema(description = "Số điện thoại tài xế", example = "0362882882", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Mật khẩu tài xế", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}

