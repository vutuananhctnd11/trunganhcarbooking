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

    private String pickupCoordinates;

    private String dropoffAddress;

    private String dropoffCoordinates;

    private BigDecimal distanceKm;

    private BigDecimal estimatedPrice;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickupTime;

    private Long driverId;

    private Double waitHours;

    private Boolean isRoundTrip;

    private Boolean isFixedTrip;

    private String fixedTripTitle;

    private Long promotionTripId;

    private String discountCode;

    private BigDecimal discountAmount;

    private String luggageOption;

    private Long travelTime;

    private Long bufferMinutes;

    private String note;
}

