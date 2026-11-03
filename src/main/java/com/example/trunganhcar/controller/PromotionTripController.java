package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.ApiResponse;
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

import java.util.List;

@Tag(name = "5. Promotion Trip APIs", description = "Các API CRUD quản lý các gói cước chuyến cố định theo chương trình quảng cáo (Có phân trang)")
@RestController
@RequestMapping("/api/v1/promotion-trips")
@RequiredArgsConstructor
public class PromotionTripController {

    private final PromotionTripService promotionTripService;

    @Operation(summary = "Tạo mới gói cước chuyến cố định quảng cáo", description = "Thêm gói cước chuyến cố định mới (VD: Đưa Đón Sân Bay Nội Bài)")
    @PostMapping
    public ResponseEntity<ApiResponse<PromotionTripResponse>> createPromotionTrip(@RequestBody PromotionTripRequest request) {
        PromotionTripResponse response = promotionTripService.createPromotionTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo mới gói cước cố định thành công", response));
    }

    @Operation(summary = "Lấy danh sách các gói cước chuyến cố định (Tối đa 3 gói theo displayOrder)", 
               description = "Lấy ra tối đa 3 gói cước chuyến cố định đang hoạt động, sắp xếp theo displayOrder tăng dần để hiển thị trang chủ")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionTripResponse>>> getPromotionTrips(
            @Parameter(description = "Lọc theo trạng thái hoạt động (mặc định: true)")
            @RequestParam(required = false, defaultValue = "true") Boolean isActive) {
        List<PromotionTripResponse> response = promotionTripService.getTopPromotionTrips(isActive);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Lấy danh sách tất cả gói cước chuyến cố định (Quản trị, có phân trang)", 
               description = "Lấy danh sách toàn bộ các gói cước có phân trang (page, size, sort).")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<PromotionTripResponse>>> getAllPromotionTrips(
            @Parameter(description = "Lọc theo trạng thái hoạt động (true: đang chạy, false: ẩn)")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PromotionTripResponse> response = promotionTripService.getAllPromotionTrips(isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Lấy chi tiết gói cước theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionTripResponse>> getPromotionTripById(@PathVariable Long id) {
        PromotionTripResponse response = promotionTripService.getPromotionTripById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Lấy chi tiết gói cước theo Slug (Dùng cho Landing page)")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PromotionTripResponse>> getPromotionTripBySlug(@PathVariable String slug) {
        PromotionTripResponse response = promotionTripService.getPromotionTripBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Cập nhật thông tin gói cước chuyến cố định")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionTripResponse>> updatePromotionTrip(
            @PathVariable Long id,
            @RequestBody PromotionTripRequest request) {
        PromotionTripResponse response = promotionTripService.updatePromotionTrip(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật gói cước thành công", response));
    }

    @Operation(summary = "Xóa gói cước chuyến cố định")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotionTrip(@PathVariable Long id) {
        promotionTripService.deletePromotionTrip(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa gói cước thành công", null));
    }
}
