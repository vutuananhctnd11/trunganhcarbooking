package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.BannerRequest;
import com.example.trunganhcar.dto.BannerResponse;
import com.example.trunganhcar.service.BannerService;
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

@Tag(name = "7. Banner Management APIs", description = "Các API quản lý và hiển thị banner chiến dịch quảng cáo ngoài trang chủ")
@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "Lấy danh sách banner trang chủ cho Frontend", 
               description = "Trả về danh sách các banner đang ACTIVE và trong thời hạn hiển thị, sắp xếp theo displayOrder tăng dần")
    @GetMapping
    public ResponseEntity<List<BannerResponse>> getActiveBanners() {
        List<BannerResponse> response = bannerService.getActiveBanners();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy danh sách tất cả banner (Quản trị, có phân trang)")
    @GetMapping("/all")
    public ResponseEntity<Page<BannerResponse>> getAllBanners(
            @Parameter(description = "Lọc theo trạng thái (ACTIVE / INACTIVE)")
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<BannerResponse> response = bannerService.getAllBanners(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy chi tiết banner theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<BannerResponse> getBannerById(@PathVariable Long id) {
        BannerResponse response = bannerService.getBannerById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Tạo mới banner")
    @PostMapping
    public ResponseEntity<BannerResponse> createBanner(@RequestBody BannerRequest request) {
        BannerResponse response = bannerService.createBanner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cập nhật banner")
    @PutMapping("/{id}")
    public ResponseEntity<BannerResponse> updateBanner(
            @PathVariable Long id, 
            @RequestBody BannerRequest request) {
        BannerResponse response = bannerService.updateBanner(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xóa banner")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }
}

