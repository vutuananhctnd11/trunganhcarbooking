package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.ApiResponse;
import com.example.trunganhcar.dto.BookingRequest;
import com.example.trunganhcar.dto.TripResponse;
import com.example.trunganhcar.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "1. Booking APIs", description = "Các API phục vụ khách hàng đặt xe")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final TripService tripService;

    @Operation(summary = "Tạo đơn đặt xe mới", description = "Khách hàng tạo đơn đặt xe, sinh mã NanoID ngẫu nhiên và tự động bắn thông báo Telegram cho tài xế")
    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createBooking(@Valid @RequestBody BookingRequest request, HttpServletRequest httpRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .replacePath(null)
                .build()
                .toUriString();

        TripResponse response = tripService.createBooking(request, baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo đơn đặt xe thành công", response));
    }
}
