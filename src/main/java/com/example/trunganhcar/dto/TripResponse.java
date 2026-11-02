package com.example.trunganhcar.dto;

import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private Long id;
    private String code;
    private String customerName;
    private String customerPhone;
    private String pickupAddress;
    private String dropoffAddress;
    private BigDecimal distanceKm;
    private BigDecimal estimatedPrice;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickupTime;

    // Các thông tin mở rộng của chuyến đi
    private Double waitHours;
    private Boolean isRoundTrip;
    private String luggageOption;

    // Thông tin tài xế (nếu đã nhận chuyến hoặc được gán)
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String driverVehicleModel;
    private Integer driverSeatCount;
    private String driverLicensePlate;
    private Double driverRating;

    private String note;
    private TripStatus status;
    private String statusDescription;
    private String trackingUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static TripResponse fromEntity(Trip trip, String baseUrl) {
        String base = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "";
        Driver driver = trip.getDriver();

        return TripResponse.builder()
                .id(trip.getId())
                .code(trip.getCode())
                .customerName(trip.getCustomerName())
                .customerPhone(trip.getCustomerPhone())
                .pickupAddress(trip.getPickupAddress())
                .dropoffAddress(trip.getDropoffAddress())
                .distanceKm(trip.getDistanceKm())
                .estimatedPrice(trip.getEstimatedPrice())
                .pickupTime(trip.getPickupTime())
                .waitHours(trip.getWaitHours())
                .isRoundTrip(trip.getIsRoundTrip())
                .luggageOption(trip.getLuggageOption())
                // Thông tin tài xế
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driver != null ? driver.getFullName() : null)
                .driverPhone(driver != null ? driver.getPhone() : null)
                .driverVehicleModel(driver != null ? driver.getVehicleModel() : null)
                .driverSeatCount(driver != null ? driver.getSeatCount() : null)
                .driverLicensePlate(driver != null ? driver.getLicensePlate() : null)
                .driverRating(driver != null ? driver.getRating() : null)
                .note(trip.getNote())
                .status(trip.getStatus())
                .statusDescription(trip.getStatus().getDescription())
                .trackingUrl(base + "/trip/" + trip.getCode())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }
}
