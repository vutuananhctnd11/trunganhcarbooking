package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.DriverRequest;
import com.example.trunganhcar.dto.DriverResponse;
import com.example.trunganhcar.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "4. Driver Management APIs", description = "Các API CRUD quản lý thông tin tài xế và phương tiện")
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Tạo mới tài xế", description = "Thêm tài xế mới kèm thông tin xe, biển số, kinh nghiệm và chat ID telegram")
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(@RequestBody DriverRequest request) {
        DriverResponse response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Lấy danh sách tài xế", description = "Xem toàn bộ tài xế trong hệ thống, có thể lọc theo isActive=true/false")
    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers(
            @Parameter(description = "Lọc theo trạng thái hoạt động (true/false)")
            @RequestParam(required = false) Boolean isActive) {
        List<DriverResponse> response = driverService.getAllDrivers(isActive);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy chi tiết tài xế", description = "Lấy thông tin tài xế theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật tài xế", description = "Cập nhật thông tin cá nhân, phương tiện, biển số hoặc trạng thái tài xế")
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long id,
            @RequestBody DriverRequest request) {
        DriverResponse response = driverService.updateDriver(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xóa tài xế", description = "Xóa tài xế khỏi hệ thống theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}

