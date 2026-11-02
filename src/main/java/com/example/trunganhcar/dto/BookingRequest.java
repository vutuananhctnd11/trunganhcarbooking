package com.example.trunganhcar.dto;

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
public class BookingRequest {

    private String customerName;

    private String customerPhone;

    private String pickupAddress;

    private String dropoffAddress;

    private BigDecimal distanceKm;

    private BigDecimal estimatedPrice;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickupTime;

    private Long driverId;

    private Double waitHours;

    private Boolean isRoundTrip;

    private String luggageOption;

    private String note;
}

