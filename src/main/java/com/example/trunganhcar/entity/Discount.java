package com.example.trunganhcar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts", indexes = {
        @Index(name = "idx_discount_code", columnList = "code", unique = true),
        @Index(name = "idx_discount_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Ví dụ: "TRUNGANH50K", "SUMMER10"

    @Column(nullable = false, length = 150)
    private String title; // "Ưu đãi 50.000đ mừng khai trương"

    @Column(columnDefinition = "TEXT")
    private String description; // Điều kiện chi tiết

    @Column(name = "discount_type", nullable = false, length = 20)
    @Builder.Default
    private String discountType = "FIXED_AMOUNT"; // "FIXED_AMOUNT" hoặc "PERCENTAGE"

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue; // Ví dụ: 50000 (VND) hoặc 10 (%)

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount; // Mức giảm tối đa (khi kiểu là PERCENTAGE)

    @Column(name = "min_trip_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minTripPrice = BigDecimal.ZERO; // Giá trị chuyến tối thiểu để áp dụng

    @Column(name = "usage_limit")
    private Integer usageLimit; // Tổng số lượt dùng tối đa (null = không giới hạn)

    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0; // Số lượt đã dùng

    @Column(name = "start_at")
    private LocalDateTime startAt; // Thời điểm bắt đầu hiệu lực

    @Column(name = "end_at")
    private LocalDateTime endAt; // Thời điểm hết hạn

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
