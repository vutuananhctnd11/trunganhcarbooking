package com.example.trunganhcar.dto;

import com.example.trunganhcar.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseTripPayload {
    private String tripCode;
    private TripStatus status;
    private String statusDescription;
    private String message;
    private String trackingUrl;
    private LocalDateTime timestamp;
}

