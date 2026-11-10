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

    // --- THÔNG TIN KHÁCH HÀNG ---
    private String customerName;
    private String customerPhone;
    private String note;

    // --- HÀNH TRÌNH ---
    private String pickupAddress;
    private String dropoffAddress;
    private BigDecimal distanceKm;
    private Boolean isRoundTrip;
    private Double waitHours;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickupTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime busyUntil;

    private BigDecimal estimatedPrice;
    private String discountCode;
    private BigDecimal discountAmount;

    private TripStatus status;
    private String statusDescription;

    // --- TÀI XẾ (Nested object) ---
    private DriverInfo driver;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfo {
        private String name;
        private String avatar;
        private Integer experience;
        private String vehicleModel;
        private String vehicleColor;
        private String licensePlate;
        private String phone;
    }

    public static TripResponse fromEntity(Trip trip, String baseUrl) {
        return fromEntity(trip, null, baseUrl);
    }

    public static TripResponse fromEntity(Trip trip, Driver driver, String baseUrl) {
        String name = (driver != null && driver.getFullName() != null) ? driver.getFullName() : "Trung Anh";
        String avatar = (driver != null && driver.getAvatarUrl() != null && !driver.getAvatarUrl().isBlank())
                ? driver.getAvatarUrl()
                : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80";
        Integer exp = (driver != null && driver.getExperienceYears() != null) ? driver.getExperienceYears() : 8;
        String vModel = (driver != null && driver.getVehicleModel() != null && !driver.getVehicleModel().isBlank())
                ? driver.getVehicleModel()
                : "VinFast VF e34 (Limo Green Edition)";
        String vColor = (driver != null && driver.getVehicleColor() != null && !driver.getVehicleColor().isBlank())
                ? driver.getVehicleColor()
                : "Đỏ Crimson (Đỏ đô ánh kim)";
        String plate = (driver != null && driver.getLicensePlate() != null && !driver.getLicensePlate().isBlank())
                ? driver.getLicensePlate()
                : "35H - 182.29";
        String phone = (driver != null && driver.getPhone() != null && !driver.getPhone().isBlank())
                ? driver.getPhone()
                : "0362882882";

        DriverInfo driverInfo = DriverInfo.builder()
                .name(name)
                .avatar(avatar)
                .experience(exp)
                .vehicleModel(vModel)
                .vehicleColor(vColor)
                .licensePlate(plate)
                .phone(phone)
                .build();

        return TripResponse.builder()
                .id(trip.getId())
                .code(trip.getCode())
                // Thông tin khách hàng
                .customerName(trip.getCustomerName())
                .customerPhone(trip.getCustomerPhone())
                .note(trip.getNote())
                // Hành trình
                .pickupAddress(trip.getPickupAddress())
                .dropoffAddress(trip.getDropoffAddress())
                .distanceKm(trip.getDistanceKm())
                .isRoundTrip(trip.getIsRoundTrip())
                .waitHours(trip.getWaitHours())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .pickupTime(trip.getPickupTime())
                .busyUntil(trip.getBusyUntil())
                .estimatedPrice(trip.getEstimatedPrice())
                .discountCode(trip.getDiscountCode())
                .discountAmount(trip.getDiscountAmount())
                .status(trip.getStatus())
                .statusDescription(trip.getStatus() != null ? trip.getStatus().getDescription() : "")
                // Tài xế (nested object)
                .driver(driverInfo)
                .build();
    }
}
