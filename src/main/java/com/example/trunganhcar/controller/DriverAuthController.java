package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.ApiResponse;
import com.example.trunganhcar.dto.DriverAuthResponse;
import com.example.trunganhcar.dto.DriverLoginRequest;
import com.example.trunganhcar.dto.DriverRegisterRequest;
import com.example.trunganhcar.service.DriverAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "6. Driver Authentication APIs", description = "Các API Đăng Ký & Đăng Nhập Cổng Tài Xế Trung Anh")
@RestController
@RequestMapping("/api/v1/auth/driver")
@RequiredArgsConstructor
public class DriverAuthController {

    private final DriverAuthService driverAuthService;

    @Operation(summary = "Đăng ký tài xế mới", 
               description = "Đăng ký tài xế mới với họ tên, số điện thoại, biển số xe, dòng xe VinFast và mật khẩu tự nhập")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DriverAuthResponse>> register(@RequestBody DriverRegisterRequest request) {
        DriverAuthResponse response = driverAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký tài xế thành công", response));
    }

    @Operation(summary = "Đăng nhập tài xế", 
               description = "Đăng nhập vào Cổng tài xế bằng Số điện thoại và Mật khẩu")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<DriverAuthResponse>> login(@RequestBody DriverLoginRequest request) {
        DriverAuthResponse response = driverAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @Operation(summary = "Đăng nhập nhanh tài xế mẫu (Vào xem ngay)", 
               description = "Đăng nhập tức thì với tài xế mẫu Trung Anh (Chủ xe) - SĐT: 0362.882.882")
    @PostMapping("/demo")
    public ResponseEntity<ApiResponse<DriverAuthResponse>> loginDemo() {
        DriverAuthResponse response = driverAuthService.loginDemoDriver();
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập tài xế mẫu thành công", response));
    }
}
