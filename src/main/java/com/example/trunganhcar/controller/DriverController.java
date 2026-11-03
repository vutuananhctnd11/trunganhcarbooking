package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.ApiResponse;
import com.example.trunganhcar.dto.DriverRequest;
import com.example.trunganhcar.dto.DriverResponse;
import com.example.trunganhcar.dto.DriverScheduleResponse;
import com.example.trunganhcar.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "4. Driver Management APIs", description = "Các API CRUD quản lý thông tin tài xế và phương tiện")
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Tạo mới tài xế", description = "Thêm tài xế mới kèm thông tin xe, biển số, kinh nghiệm và chat ID telegram")
    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(@RequestBody DriverRequest request) {
        DriverResponse response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo mới tài xế thành công", response));
    }

    @Operation(summary = "Lấy danh sách tài xế", description = "Xem toàn bộ tài xế trong hệ thống, có thể lọc theo isActive=true/false")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAllDrivers(
            @Parameter(description = "Lọc theo trạng thái hoạt động (true/false)")
            @RequestParam(required = false) Boolean isActive) {
        List<DriverResponse> response = driverService.getAllDrivers(isActive);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Lấy chi tiết tài xế", description = "Lấy thông tin tài xế theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(@PathVariable Long id) {
        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Cập nhật tài xế", description = "Cập nhật thông tin cá nhân, phương tiện, biển số hoặc trạng thái tài xế")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @PathVariable Long id,
            @RequestBody DriverRequest request) {
        DriverResponse response = driverService.updateDriver(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin tài xế thành công", response));
    }

    @Operation(summary = "Lấy lịch trình tài xế theo ngày", 
               description = "Lấy danh sách các chuyến bận (busy_trips) và các khung giờ còn trống (available_slots) của tài xế theo ngày được chọn")
    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<DriverScheduleResponse>> getDriverSchedule(
            @Parameter(description = "ID của tài xế", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Ngày cần xem lịch (định dạng yyyy-MM-dd, mặc định hôm nay nếu để trống)", example = "2026-11-19")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DriverScheduleResponse response = driverService.getDriverSchedule(id, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Xóa tài xế", description = "Xóa tài xế khỏi hệ thống theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài xế thành công", null));
    }
}
