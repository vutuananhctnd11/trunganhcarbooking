package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.DiscountRequest;
import com.example.trunganhcar.dto.DiscountResponse;
import com.example.trunganhcar.dto.DiscountValidateResponse;
import com.example.trunganhcar.entity.Discount;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.exception.ErrorCode;
import com.example.trunganhcar.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;

    /**
     * Tạo mã giảm giá mới
     */
    @Transactional
    public DiscountResponse createDiscount(DiscountRequest request) {
        String cleanCode = request.getCode().trim().toUpperCase();
        log.info("Tạo mã giảm giá mới: {}", cleanCode);

        if (discountRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new CustomException(ErrorCode.DISCOUNT_ALREADY_EXISTS);
        }

        if (request.getDiscountValue() == null || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Giá trị giảm giá phải lớn hơn 0!");
        }

        Discount discount = Discount.builder()
                .code(cleanCode)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .discountType(request.getDiscountType() != null && !request.getDiscountType().isBlank() 
                        ? request.getDiscountType().trim().toUpperCase() : "FIXED_AMOUNT")
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minTripPrice(request.getMinTripPrice() != null ? request.getMinTripPrice() : BigDecimal.ZERO)
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Discount saved = discountRepository.save(discount);
        return DiscountResponse.fromEntity(saved);
    }

    /**
     * Lấy danh sách mã giảm giá có phân trang
     */
    @Transactional(readOnly = true)
    public Page<DiscountResponse> getAllDiscounts(Boolean isActive, Pageable pageable) {
        Page<Discount> page;
        if (isActive != null) {
            page = discountRepository.findByIsActive(isActive, pageable);
        } else {
            page = discountRepository.findAll(pageable);
        }
        return page.map(DiscountResponse::fromEntity);
    }

    /**
     * Lấy chi tiết mã giảm giá theo ID
     */
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountById(Long id) {
        Discount discount = findDiscountById(id);
        return DiscountResponse.fromEntity(discount);
    }

    /**
     * Lấy chi tiết mã giảm giá theo Code
     */
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountByCode(String code) {
        Discount discount = findDiscountByCode(code);
        return DiscountResponse.fromEntity(discount);
    }

    /**
     * Kiểm tra tính hợp lệ và tính số tiền giảm giá khi khách áp dụng mã
     */
    @Transactional(readOnly = true)
    public DiscountValidateResponse validateAndCalculateDiscount(String code, BigDecimal tripPrice) {
        if (code == null || code.isBlank()) {
            throw new CustomException("Mã giảm giá không được để trống!");
        }

        if (tripPrice == null || tripPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Giá trị chuyến đi phải lớn hơn 0!");
        }

        String cleanCode = code.trim().toUpperCase();
        Discount discount = discountRepository.findByCodeIgnoreCase(cleanCode)
                .orElseThrow(() -> new CustomException(ErrorCode.DISCOUNT_NOT_FOUND, "Mã giảm giá '" + cleanCode + "' không tồn tại trong hệ thống!"));

        LocalDateTime now = LocalDateTime.now();

        if (Boolean.FALSE.equals(discount.getIsActive())) {
            throw new CustomException(ErrorCode.DISCOUNT_INACTIVE, "Mã giảm giá '" + cleanCode + "' hiện đang bị tạm dừng kích hoạt!");
        }

        if (discount.getStartAt() != null && now.isBefore(discount.getStartAt())) {
            throw new CustomException(ErrorCode.DISCOUNT_NOT_STARTED, "Mã giảm giá '" + cleanCode + "' chưa đến thời gian áp dụng!");
        }

        if (discount.getEndAt() != null && now.isAfter(discount.getEndAt())) {
            throw new CustomException(ErrorCode.DISCOUNT_EXPIRED, "Mã giảm giá '" + cleanCode + "' đã hết hạn sử dụng!");
        }

        if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new CustomException(ErrorCode.DISCOUNT_USAGE_EXCEEDED, "Mã giảm giá '" + cleanCode + "' đã hết lượt sử dụng!");
        }

        if (discount.getMinTripPrice() != null && tripPrice.compareTo(discount.getMinTripPrice()) < 0) {
            throw new CustomException(ErrorCode.DISCOUNT_MIN_PRICE_NOT_MET, 
                    String.format("Giá trị chuyến đi tối thiểu để áp dụng mã này là %s đ!", discount.getMinTripPrice().toPlainString()));
        }

        // Tính toán số tiền được giảm
        BigDecimal discountAmount;
        if ("PERCENTAGE".equalsIgnoreCase(discount.getDiscountType())) {
            // Giảm theo phần trăm
            discountAmount = tripPrice.multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            if (discount.getMaxDiscountAmount() != null && discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0) {
                discountAmount = discount.getMaxDiscountAmount();
            }
        } else {
            // Giảm theo số tiền cố định
            discountAmount = discount.getDiscountValue();
        }

        // Số tiền giảm không được vượt quá giá trị chuyến xe
        if (discountAmount.compareTo(tripPrice) > 0) {
            discountAmount = tripPrice;
        }

        BigDecimal finalPrice = tripPrice.subtract(discountAmount);

        return DiscountValidateResponse.builder()
                .valid(true)
                .message("Áp dụng mã giảm giá thành công!")
                .originalPrice(tripPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .discount(DiscountResponse.fromEntity(discount))
                .build();
    }

    /**
     * Cập nhật thông tin mã giảm giá
     */
    @Transactional
    public DiscountResponse updateDiscount(Long id, DiscountRequest request) {
        log.info("Cập nhật mã giảm giá ID: {}", id);
        Discount discount = findDiscountById(id);

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(discount.getCode()) && discountRepository.existsByCodeIgnoreCase(newCode)) {
                throw new CustomException(ErrorCode.DISCOUNT_ALREADY_EXISTS);
            }
            discount.setCode(newCode);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            discount.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            discount.setDescription(request.getDescription().trim());
        }
        if (request.getDiscountType() != null && !request.getDiscountType().isBlank()) {
            discount.setDiscountType(request.getDiscountType().trim().toUpperCase());
        }
        if (request.getDiscountValue() != null) {
            discount.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMaxDiscountAmount() != null) {
            discount.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getMinTripPrice() != null) {
            discount.setMinTripPrice(request.getMinTripPrice());
        }
        if (request.getUsageLimit() != null) {
            discount.setUsageLimit(request.getUsageLimit());
        }
        if (request.getStartAt() != null) {
            discount.setStartAt(request.getStartAt());
        }
        if (request.getEndAt() != null) {
            discount.setEndAt(request.getEndAt());
        }
        if (request.getIsActive() != null) {
            discount.setIsActive(request.getIsActive());
        }

        Discount updated = discountRepository.save(discount);
        return DiscountResponse.fromEntity(updated);
    }

    /**
     * Xóa mã giảm giá
     */
    @Transactional
    public void deleteDiscount(Long id) {
        log.info("Xóa mã giảm giá ID: {}", id);
        Discount discount = findDiscountById(id);
        discountRepository.delete(discount);
    }

    public Discount findDiscountById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DISCOUNT_NOT_FOUND, "Không tìm thấy mã giảm giá với ID: " + id));
    }

    public Discount findDiscountByCode(String code) {
        return discountRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new CustomException(ErrorCode.DISCOUNT_NOT_FOUND, "Không tìm thấy mã giảm giá với mã: " + code));
    }
}

