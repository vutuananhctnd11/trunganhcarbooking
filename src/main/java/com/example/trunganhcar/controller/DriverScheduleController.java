package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.ApiResponse;
import com.example.trunganhcar.dto.DriverDailyTripCountDto;
import com.example.trunganhcar.dto.DriverScheduleResponse;
import com.example.trunganhcar.service.DriverService;
import com.example.trunganhcar.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "4. Driver Schedule APIs", description = "Các API Lịch Trình cá nhân của Tài Xế Đang Đăng Nhập (Yêu cầu JWT Bearer Token)")
@RestController
@RequestMapping("/api/v1/driver/my-schedule")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DriverScheduleController {

    private final DriverService driverService;

    @Operation(
            summary = "Xem số chuyến mỗi ngày trong 30 ngày tới của tài xế đang đăng nhập",
            description = "Trả về danh sách 30 ngày kể từ hôm nay, kèm thứ trong tuần, số chuyến xe đã nhận trong từng ngày và đánh dấu ngày hôm nay"
    )
    @GetMapping("/next-30-days")
    public ResponseEntity<ApiResponse<List<DriverDailyTripCountDto>>> getNext30DaysTripSummary() {
        Long currentDriverId = SecurityUtils.getCurrentDriverId();
        List<DriverDailyTripCountDto> summary = driverService.getMyScheduleSummaryNext30Days(currentDriverId);
        return ResponseEntity.ok(ApiResponse.success("Lấy tóm tắt lịch trình 30 ngày thành công", summary));
    }

    @Operation(
            summary = "Xem chi tiết lịch trình trong hôm nay của tài xế đang đăng nhập",
            description = "Trả về chi tiết các chuyến bận (busy_trips) và các khung giờ còn trống (available_slots) của tài xế trong ngày hôm nay"
    )
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<DriverScheduleResponse>> getTodaySchedule() {
        Long currentDriverId = SecurityUtils.getCurrentDriverId();
        DriverScheduleResponse response = driverService.getMyScheduleToday(currentDriverId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết lịch trình hôm nay thành công", response));
    }

    @Operation(
            summary = "Xem chi tiết lịch trình theo ngày tùy chọn của tài xế đang đăng nhập",
            description = "Cho phép tài xế xem chi tiết các chuyến bận và khung giờ trống của một ngày cụ thể (định dạng yyyy-MM-dd)"
    )
    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<DriverScheduleResponse>> getScheduleByDate(
            @Parameter(description = "Ngày cần xem (định dạng yyyy-MM-dd)", example = "2026-11-05")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long currentDriverId = SecurityUtils.getCurrentDriverId();
        DriverScheduleResponse response = driverService.getDriverSchedule(currentDriverId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

