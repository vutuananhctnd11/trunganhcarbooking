package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.ApiResponse;
import com.example.trunganhcar.dto.BookingRequest;
import com.example.trunganhcar.dto.TripAvailabilityRequest;
import com.example.trunganhcar.dto.TripAvailabilityResponse;
import com.example.trunganhcar.dto.TripResponse;
import com.example.trunganhcar.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "1. Booking APIs", description = "Các API phục vụ khách hàng đặt xe, kiểm tra lịch bận và xem chuyến sắp tới")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final TripService tripService;

    @Operation(summary = "Tạo đơn đặt xe mới", description = "Khách hàng tạo đơn đặt xe, sinh mã NanoID ngẫu nhiên và tự động bắn thông báo Telegram cho tài xế")
    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createBooking(
            @Valid @RequestBody BookingRequest request, 
            HttpServletRequest httpRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .replacePath(null)
                .build()
                .toUriString();

        TripResponse response = tripService.createBooking(request, baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo đơn đặt xe thành công", response));
    }

    @Operation(summary = "Kiểm tra tình trạng khả dụng / xung đột thời gian của chuyến xe (POST)",
            description = "Kiểm tra xem khung giờ dự kiến đón và thời gian bận (busy_until) có bị xung đột với các chuyến xe liền kề của tài xế không")
    @PostMapping("/check-availability")
    public ResponseEntity<ApiResponse<TripAvailabilityResponse>> checkTripAvailability(
            @Valid @RequestBody TripAvailabilityRequest request) {
        TripAvailabilityResponse response = tripService.checkTripAvailability(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Kiểm tra tình trạng khả dụng / xung đột thời gian của chuyến xe (GET)",
            description = "Hỗ trợ query nhanh trên URL để check xem tài xế có rảnh trong khung giờ định đặt hay không")
    @GetMapping("/check-availability")
    public ResponseEntity<ApiResponse<TripAvailabilityResponse>> checkTripAvailabilityQuick(
            @Parameter(description = "Thời gian đón (ISO: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupTime,
            @Parameter(description = "Thời gian di chuyển dự kiến (phút)")
            @RequestParam(required = false) Long travelTime,
            @Parameter(description = "Quãng đường (km)")
            @RequestParam(required = false) BigDecimal distanceKm,
            @Parameter(description = "Thời gian chờ (giờ)")
            @RequestParam(required = false) Double waitHours,
            @Parameter(description = "Thời gian đệm nghỉ giữa các chuyến (phút)")
            @RequestParam(required = false, defaultValue = "30") Integer bufferMinutes,
            @Parameter(description = "ID tài xế muốn kiểm tra")
            @RequestParam(required = false) Long driverId,
            @Parameter(description = "Chuyến đi 2 chiều khứ hồi")
            @RequestParam(required = false, defaultValue = "false") Boolean isRoundTrip) {

        TripAvailabilityRequest request = TripAvailabilityRequest.builder()
                .pickupTime(pickupTime)
                .travelTime(travelTime)
                .distanceKm(distanceKm)
                .waitHours(waitHours)
                .bufferMinutes(bufferMinutes)
                .driverId(driverId)
                .isRoundTrip(isRoundTrip)
                .build();

        TripAvailabilityResponse response = tripService.checkTripAvailability(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Xem danh sách chuyến sắp tới theo số điện thoại",
            description = "Khách hàng không cần đăng nhập, chỉ cần nhập số điện thoại để tra cứu các chuyến đi sắp tới hoặc đang diễn ra")
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getUpcomingTrips(
            @Parameter(description = "Số điện thoại của khách hàng", required = true)
            @RequestParam String phone,
            HttpServletRequest httpRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .replacePath(null)
                .build()
                .toUriString();

        List<TripResponse> trips = tripService.getUpcomingTripsByPhone(phone, baseUrl);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @Operation(summary = "Xem toàn bộ lịch sử chuyến theo số điện thoại",
            description = "Khách hàng không cần đăng nhập, tra cứu tất cả chuyến xe đã từng đặt theo số điện thoại")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getTripHistory(
            @Parameter(description = "Số điện thoại của khách hàng", required = true)
            @RequestParam String phone,
            HttpServletRequest httpRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .replacePath(null)
                .build()
                .toUriString();

        List<TripResponse> trips = tripService.getAllTripsByPhone(phone, baseUrl);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }
}
