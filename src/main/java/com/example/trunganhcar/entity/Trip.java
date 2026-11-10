package com.example.trunganhcar.entity;

import com.example.trunganhcar.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_code", columnList = "code", unique = true),
        @Index(name = "idx_trip_status", columnList = "status"),
        @Index(name = "idx_trip_driver_id", columnList = "driver_id"),
        @Index(name = "idx_trip_customer_phone", columnList = "customer_phone")
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

    @Column(name = "promotion_trip_id")
    private Long promotionTripId;

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "luggage_option", length = 100)
    private String luggageOption;

    @Column(columnDefinition = "TEXT")
    private String note;

    // thời gian di chuyển trong chuyến
    @Column(name = "travel_time")
    private Long travelTime;

    // thời gian chờ giữa các chuyến
    @Column(name = "buffer_minutes", nullable = false, columnDefinition = "integer default 30")
    private Long bufferMinutes;

    // thời gian bận trong chuyến
    @Column(name = "busy_until")
    private LocalDateTime busyUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TripStatus status;

    @Column(name = "push_notificationtrip", columnDefinition = "TEXT")
    private String pushNotification;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
}
