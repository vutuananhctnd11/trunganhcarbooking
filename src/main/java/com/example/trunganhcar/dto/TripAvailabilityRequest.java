package com.example.trunganhcar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
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
public class TripAvailabilityRequest {

    @NotNull(message = "Thời gian đón không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickupTime;

    /**
     * Thời gian di chuyển (phút). Nếu không cung cấp có thể tính ước lượng từ distanceKm.
     */
    private Long travelTime;

    /**
     * Quãng đường di chuyển (km). Dùng để ước tính travelTime nếu travelTime chưa có.
     */
    private BigDecimal distanceKm;

    /**
     * Thời gian chờ đón chiều về (giờ)
     */
    private Double waitHours;

    /**
     * Thời gian đệm nghỉ giữa các chuyến (phút). Mặc định là 30 phút.
     */
    @Builder.Default
    private Integer bufferMinutes = 30;

    /**
     * ID của tài xế muốn kiểm tra. Nếu null, có thể kiểm tra toàn bộ hoặc tài xế mặc định.
     */
    private Long driverId;

    /**
     * Chuyến đi 2 chiều (khứ hồi). Khi đi 2 chiều, thời gian di chuyển sẽ nhân đôi.
     */
    @Builder.Default
    private Boolean isRoundTrip = false;
}
