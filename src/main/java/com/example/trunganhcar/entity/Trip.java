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

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_price", precision = 12, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    /**
     * ID của tài xế nhận hoặc được gán cuốc xe (lưu ID trực tiếp, không dùng annotation quan hệ)
     */
    @Column(name = "driver_id")
    private Long driverId;

    /**
     * Số giờ tài xế chờ khách (cho chuyến 2 chiều/khứ hồi)
     */
    @Column(name = "wait_hours")
    private Double waitHours;

    /**
     * Chuyến đi khứ hồi (2 chiều) hay 1 chiều
     */
    @Column(name = "is_round_trip")
    @Builder.Default
    private Boolean isRoundTrip = false;

    /**
     * Tùy chọn hành lý: Ít, Vừa, Nhiều hành lý/Cồng kềnh
     */
    @Column(name = "luggage_option", length = 100)
    private String luggageOption;

    @Column(columnDefinition = "TEXT")
    private String note;

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
