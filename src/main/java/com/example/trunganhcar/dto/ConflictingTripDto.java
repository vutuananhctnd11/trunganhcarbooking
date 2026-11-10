package com.example.trunganhcar.dto;

import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
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
public class ConflictingTripDto {
    private String code;
    private String pickupAddress;
    private String dropoffAddress;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickupTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime busyUntil;

    private TripStatus status;
    private String statusDescription;

    public static ConflictingTripDto fromEntity(Trip trip) {
        if (trip == null) return null;
        return ConflictingTripDto.builder()
                .code(trip.getCode())
                .pickupAddress(trip.getPickupAddress())
                .dropoffAddress(trip.getDropoffAddress())
                .pickupTime(trip.getPickupTime())
                .busyUntil(trip.getBusyUntil())
                .status(trip.getStatus())
                .statusDescription(trip.getStatus() != null ? trip.getStatus().getDescription() : "")
                .build();
    }
}

