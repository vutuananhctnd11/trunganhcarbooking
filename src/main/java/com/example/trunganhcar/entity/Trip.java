package com.example.trunganhcar.entity;

import com.example.trunganhcar.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_code", columnList = "code", unique = true),
        @Index(name = "idx_trip_status", columnList = "status"),
        @Index(name = "idx_trip_driver_id", columnList = "driver_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "pickup_coordinates", nullable = false)
    private String pickupCoordinates;

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @Column(name = "dropoff_coordinates", nullable = false)
    private String dropoffCoordinates;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_price", precision = 12, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "wait_hours")
    private Double waitHours;

    @Column(name = "is_round_trip")
    @Builder.Default
    private Boolean isRoundTrip = false;

    /**
     * Đánh dấu có phải chuyến cố định theo gói cước quảng cáo không
     */
    @Column(name = "is_fixed_trip")
    @Builder.Default
    private Boolean isFixedTrip = false;

    /**
     * Tên gói cước chuyến cố định (VD: "Đưa Đón Sân Bay Nội Bài")
     */
    @Column(name = "fixed_trip_title", length = 150)
    private String fixedTripTitle;

    /**
     * ID của gói cước trong bảng promotion_trips (lưu ID trực tiếp, không dùng annotation quan hệ)
     */
    @Column(name = "promotion_trip_id")
    private Long promotionTripId;

    /**
     * Mã giảm giá áp dụng (nếu có, VD: "VIETMAP50K")
     */
    @Column(name = "discount_code", length = 50)
    private String discountCode;

    /**
     * Số tiền được giảm giá (VD: 50000)
     */
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "luggage_option", length = 100)
    private String luggageOption;

    @Column(columnDefinition = "TEXT")
    private String note;

    //notethời gian di chuyển trong chuyến
    @Column(name = "travel_time")
    private Long travelTime;

    //thời gian chờ giữa các chuyến
    @Column(name = "buffer_minutes", nullable = false, columnDefinition = "integer default 30")
    private Long bufferMinutes;

    //thời gian bận trong chuyến
    @Column(name = "busy_until")
    private LocalDateTime busyUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TripStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
