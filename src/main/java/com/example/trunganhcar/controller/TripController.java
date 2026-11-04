package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.ApiResponse;
import com.example.trunganhcar.dto.TripResponse;
import com.example.trunganhcar.service.SseService;
import com.example.trunganhcar.service.TripService;
import com.example.trunganhcar.service.WebPushService;
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

import java.util.Map;

@Tag(name = "2. Trip & Tracking APIs", description = "Các API theo dõi chuyến đi và lắng nghe sự kiện realtime qua SSE")
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final SseService sseService;
    private final WebPushService webPushService;

    @Operation(summary = "Lấy VAPID Public Key", description = "Lấy khóa công khai VAPID để Frontend đăng ký PushSubscription với Service Worker")
    @GetMapping("/push-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPushPublicKey() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("publicKey", webPushService.getPublicKey())));
    }

    @Operation(summary = "Lưu PushSubscription của khách hàng", description = "Cập nhật token PushSubscription của trình duyệt khách hàng cho chuyến xe để nhận Web Push")
    @PostMapping("/{code}/push-subscription")
    public ResponseEntity<ApiResponse<Void>> updatePushSubscription(
            @Parameter(description = "Mã chuyến xe NanoID, ví dụ: aB3dE5gH7j") @PathVariable String code,
            @RequestBody Map<String, String> body) {
        String subscription = body.get("subscription");
        tripService.updatePushSubscription(code, subscription);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "Lấy thông tin chuyến đi", description = "Lấy chi tiết đơn hàng cho trang tracking của khách hàng qua mã chuyến (NanoID)")
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<TripResponse>> getTrip(
            @Parameter(description = "Mã chuyến xe NanoID, ví dụ: aB3dE5gH7j") @PathVariable String code, 
            HttpServletRequest httpRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .replacePath(null)
                .build()
                .toUriString();

        TripResponse trip = tripService.getTripByCode(code, baseUrl);
        return ResponseEntity.ok(ApiResponse.success(trip));
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
