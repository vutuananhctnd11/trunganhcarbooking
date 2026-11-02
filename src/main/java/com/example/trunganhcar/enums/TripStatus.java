package com.example.trunganhcar.enums;

import lombok.Getter;

@Getter
public enum TripStatus {
    PENDING("Chờ tài xế nhận chuyến"),
    CONFIRMED("Tài xế đã nhận chuyến"),
    ARRIVING("Tài xế đang đến đón"),
    IN_TRIP("Đang trong chuyến đi"),
    COMPLETED("Chuyến đi đã hoàn thành"),
    CANCELLED("Chuyến đi đã bị hủy");

    private final String description;

    TripStatus(String description) {
        this.description = description;
    }
}

