package com.example.trunganhcar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDailyTripCountDto {

    @Schema(description = "Ngày theo định dạng yyyy-MM-dd", example = "2026-11-03")
    private String date;

    @Schema(description = "Thứ trong tuần (Tiếng Việt)", example = "Thứ Ba")
    private String dayOfWeek;

    @Schema(description = "Tổng số chuyến xe của tài xế trong ngày", example = "3")
    private long tripCount;

    @Schema(description = "Đánh dấu có phải hôm nay hay không", example = "true")
    private boolean isToday;
}

