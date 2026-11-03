package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.PromotionTripRequest;
import com.example.trunganhcar.dto.PromotionTripResponse;
import com.example.trunganhcar.service.PromotionTripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5. Promotion Trip APIs", description = "Các API CRUD quản lý các gói cước chuyến cố định theo chương trình quảng cáo (Có phân trang)")
@RestController
@RequestMapping("/api/v1/promotion-trips")
@RequiredArgsConstructor
public class PromotionTripController {

    private final PromotionTripService promotionTripService;

    @Operation(summary = "Tạo mới gói cước chuyến cố định quảng cáo", description = "Thêm gói cước chuyến cố định mới (VD: Đưa Đón Sân Bay Nội Bài)")
    @PostMapping
    public ResponseEntity<PromotionTripResponse> createPromotionTrip(@RequestBody PromotionTripRequest request) {
        PromotionTripResponse response = promotionTripService.createPromotionTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Lấy danh sách các gói cước chuyến cố định (Phân trang)", 
               description = "Lấy danh sách các gói cước có phân trang (page, size, sort). Mặc định sắp xếp theo displayOrder tăng dần.")
    @GetMapping
    public ResponseEntity<Page<PromotionTripResponse>> getAllPromotionTrips(
            @Parameter(description = "Lọc theo trạng thái hoạt động (true: đang chạy, false: ẩn)")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PromotionTripResponse> response = promotionTripService.getAllPromotionTrips(isActive, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy chi tiết gói cước theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<PromotionTripResponse> getPromotionTripById(@PathVariable Long id) {
        PromotionTripResponse response = promotionTripService.getPromotionTripById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy chi tiết gói cước theo Slug (Dùng cho Landing page)")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PromotionTripResponse> getPromotionTripBySlug(@PathVariable String slug) {
        PromotionTripResponse response = promotionTripService.getPromotionTripBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật thông tin gói cước chuyến cố định")
    @PutMapping("/{id}")
    public ResponseEntity<PromotionTripResponse> updatePromotionTrip(
            @PathVariable Long id,
            @RequestBody PromotionTripRequest request) {
        PromotionTripResponse response = promotionTripService.updatePromotionTrip(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xóa gói cước chuyến cố định")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotionTrip(@PathVariable Long id) {
        promotionTripService.deletePromotionTrip(id);
        return ResponseEntity.noContent().build();
    }
}

