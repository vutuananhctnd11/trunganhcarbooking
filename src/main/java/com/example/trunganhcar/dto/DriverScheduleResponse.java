package com.example.trunganhcar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverScheduleResponse {

    @JsonProperty("driver")
    private DriverSummaryDto driver;

    @JsonProperty("selected_date")
    private String selectedDate;

    @JsonProperty("busy_trips")
    private List<BusyTripDto> busyTrips;

    @JsonProperty("available_slots")
    private List<String> availableSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverSummaryDto {
        private Long id;
        private String name;
        private String vehicle;

        @JsonProperty("license_plate")
        private String licensePlate;

        @JsonProperty("avatar_url")
        private String avatarUrl;

        private Double rating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusyTripDto {
        @JsonProperty("trip_id")
        private Long tripId;

        private String route;

        @JsonProperty("time_display")
        private String timeDisplay;

        @JsonProperty("start_time")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;

        @JsonProperty("tralvel_time")
        private String tralvelTime;

        @JsonProperty("busy_until")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime busyUntil;
    }
}

