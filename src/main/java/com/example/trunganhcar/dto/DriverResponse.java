package com.example.trunganhcar.dto;

import com.example.trunganhcar.entity.Driver;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private Long id;
    private String fullName;
    private String phone;
    private Integer experienceYears;
    private String vehicleModel;
    private String vehicleColor;
    private Integer seatCount;
    private String licensePlate;
    private String telegramChatId;
    private String avatarUrl;
    private Double rating;
    private String approvalStatus;
    private String rejectionReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;

    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static DriverResponse fromEntity(Driver driver) {
        if (driver == null) return null;
        return DriverResponse.builder()
                .id(driver.getId())
                .fullName(driver.getFullName())
                .phone(driver.getPhone())
                .experienceYears(driver.getExperienceYears())
                .vehicleModel(driver.getVehicleModel())
                .vehicleColor(driver.getVehicleColor())
                .seatCount(driver.getSeatCount())
                .licensePlate(driver.getLicensePlate())
                .telegramChatId(driver.getTelegramChatId())
                .avatarUrl(driver.getAvatarUrl())
                .rating(driver.getRating())
                .approvalStatus(driver.getApprovalStatus())
                .rejectionReason(driver.getRejectionReason())
                .approvedAt(driver.getApprovedAt())
                .isActive(driver.getIsActive())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}
