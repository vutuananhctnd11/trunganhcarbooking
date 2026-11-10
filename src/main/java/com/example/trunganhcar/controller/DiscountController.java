package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.*;
import com.example.trunganhcar.service.DiscountService;
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

@Tag(name = "8. Discount / Voucher APIs", description = "Các API Quản lý và Áp dụng mã giảm giá / Voucher")
@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @Operation(summary = "Tạo mới mã giảm giá", description = "Tạo mã giảm giá mới (theo số tiền cố định FIXED_AMOUNT hoặc theo phần trăm PERCENTAGE)")
    @PostMapping
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(@RequestBody DiscountRequest request) {
        DiscountResponse response = discountService.createDiscount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo mới mã giảm giá thành công", response));
    }

    @Operation(summary = "Lấy danh sách mã giảm giá đang hoạt động (cho khách đặt xe)", description = "Xem các voucher đang có hiệu lực để khách hàng bấm áp dụng trực tiếp")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getActiveDiscounts() {
        List<DiscountResponse> response = discountService.getActiveDiscounts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Lấy danh sách mã giảm giá (Phân trang)", description = "Xem danh sách mã giảm giá trong hệ thống, hỗ trợ lọc theo trạng thái hoạt động")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DiscountResponse>>> getAllDiscounts(
            @Parameter(description = "Lọc theo trạng thái hoạt động (true/false)")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DiscountResponse> response = discountService.getAllDiscounts(isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Lấy chi tiết mã giảm giá theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountById(@PathVariable Long id) {
        DiscountResponse response = discountService.getDiscountById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Lấy chi tiết mã giảm giá theo Code", description = "Tìm kiếm thông tin mã giảm giá bằng mã code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountByCode(@PathVariable String code) {
        DiscountResponse response = discountService.getDiscountByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Kiểm tra và tính tiền giảm giá khi đặt xe", 
               description = "Frontend gọi API này khi khách hàng nhập mã giảm giá để kiểm tra tính hợp lệ và lấy số tiền được giảm")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<DiscountValidateResponse>> validateDiscount(@RequestBody DiscountValidateRequest request) {
        DiscountValidateResponse response = discountService.validateAndCalculateDiscount(request.getCode(), request.getTripPrice());
        return ResponseEntity.ok(ApiResponse.success("Mã giảm giá hợp lệ", response));
    }

    @Operation(summary = "Cập nhật mã giảm giá")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(
            @PathVariable Long id,
            @RequestBody DiscountRequest request) {
        DiscountResponse response = discountService.updateDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật mã giảm giá thành công", response));
    }

    @Operation(summary = "Xóa mã giảm giá")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa mã giảm giá thành công", null));
    }
}
