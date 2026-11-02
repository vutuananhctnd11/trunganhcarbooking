package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.TripResponse;
import com.example.trunganhcar.service.SseService;
import com.example.trunganhcar.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "2. Trip & Tracking APIs", description = "Các API theo dõi chuyến đi và lắng nghe sự kiện realtime qua SSE")
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final SseService sseService;

    @Operation(summary = "Lấy thông tin chuyến đi", description = "Lấy chi tiết đơn hàng cho trang tracking của khách hàng qua mã chuyến (NanoID)")
    @GetMapping("/{code}")
    public ResponseEntity<TripResponse> getTrip(
            @Parameter(description = "Mã chuyến xe NanoID, ví dụ: aB3dE5gH7j") @PathVariable String code, 
            HttpServletRequest httpRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .replacePath(null)
                .build()
                .toUriString();

        TripResponse trip = tripService.getTripByCode(code, baseUrl);
        return ResponseEntity.ok(trip);
    }

    @Operation(summary = "Đăng ký kết nối SSE realtime", description = "Mở kết nối Server-Sent Events (SSE) để nhận sự thay đổi trạng thái cuốc xe không cần F5")
    @GetMapping(value = "/{code}/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeTripEvents(
            @Parameter(description = "Mã chuyến xe NanoID, ví dụ: aB3dE5gH7j") @PathVariable String code) {
        // Kiểm tra xem trip code có tồn tại hay không trước khi mở kết nối SSE
        tripService.findTripByCode(code);
        return sseService.subscribe(code);
    }
}

