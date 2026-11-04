package com.example.trunganhcar.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service tính cước vận chuyển chuẩn theo quy định:
 * • Giá mở cửa (2 km đầu tiên): 30.500 VNĐ
 * • Từ km thứ 3 đến km thứ 12: 14.700 VNĐ/km
 * • Từ km thứ 13 đến km thứ 25: 13.800 VNĐ/km
 * • Từ km thứ 26 trở đi: 11.900 VNĐ/km
 * • Checkbox đi 2 chiều: Chiều về chỉ tính giá 65% chiều đi, quãng đường và thời gian x2
 * • Thời gian chờ: Miễn phí 30 phút đầu (0.5h), 30.000 đ/giờ tiếp theo
 */
@Service
public class PricingService {

    public static final BigDecimal OPENING_FARE_2KM = BigDecimal.valueOf(30500); // 2 km đầu tiên
    public static final BigDecimal RATE_KM_3_TO_12 = BigDecimal.valueOf(14700);  // km 3 - 12
    public static final BigDecimal RATE_KM_13_TO_25 = BigDecimal.valueOf(13800); // km 13 - 25
    public static final BigDecimal RATE_KM_26_PLUS = BigDecimal.valueOf(11900);  // km 26 trở đi

    public static final BigDecimal WAITING_HOURLY_RATE = BigDecimal.valueOf(30000); // 30.000 đ/h chờ
    public static final double FREE_WAITING_HOURS = 0.5; // Miễn phí 30 phút đầu

    public static final double ROUND_TRIP_RETURN_RATIO = 0.65; // Chiều về 65%

    /**
     * Tính cước chuyến đi 1 chiều theo bậc km
     */
    public BigDecimal calculateOneWayFare(BigDecimal distanceKm) {
        if (distanceKm == null || distanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal km2 = BigDecimal.valueOf(2.0);
        BigDecimal km12 = BigDecimal.valueOf(12.0);
        BigDecimal km25 = BigDecimal.valueOf(25.0);

        // Bậc 1: <= 2 km đầu tiên: Trọn gói 30.500 đ
        if (distanceKm.compareTo(km2) <= 0) {
            return OPENING_FARE_2KM;
        }

        // Bậc 2: từ km 3 đến km 12 (tối đa 10 km tiếp theo)
        if (distanceKm.compareTo(km12) <= 0) {
            BigDecimal tier2Km = distanceKm.subtract(km2);
            return OPENING_FARE_2KM.add(tier2Km.multiply(RATE_KM_3_TO_12));
        }

        // Bậc 3: từ km 13 đến km 25 (10 km bậc 2 + phần vượt đến 25)
        BigDecimal tier2MaxKm = km12.subtract(km2); // 10 km
        BigDecimal tier2Total = tier2MaxKm.multiply(RATE_KM_3_TO_12); // 147.000 đ

        if (distanceKm.compareTo(km25) <= 0) {
            BigDecimal tier3Km = distanceKm.subtract(km12);
            return OPENING_FARE_2KM
                    .add(tier2Total)
                    .add(tier3Km.multiply(RATE_KM_13_TO_25));
        }

        // Bậc 4: từ km 26 trở đi
        BigDecimal tier3MaxKm = km25.subtract(km12); // 13 km
        BigDecimal tier3Total = tier3MaxKm.multiply(RATE_KM_13_TO_25); // 179.400 đ
        BigDecimal tier4Km = distanceKm.subtract(km25);

        return OPENING_FARE_2KM
                .add(tier2Total)
                .add(tier3Total)
                .add(tier4Km.multiply(RATE_KM_26_PLUS));
    }

    /**
     * Tính phí thời gian chờ: Miễn phí 30 phút đầu (0.5h), 30.000 đ/giờ tiếp theo
     */
    public BigDecimal calculateWaitingFee(Double waitHours) {
        if (waitHours == null || waitHours <= FREE_WAITING_HOURS) {
            return BigDecimal.ZERO;
        }

        double billableHours = waitHours - FREE_WAITING_HOURS;
        return BigDecimal.valueOf(billableHours).multiply(WAITING_HOURLY_RATE);
    }

    /**
     * Tính toán tổng giá cước chuyến đi hoàn chỉnh (làm tròn đến hàng nghìn VNĐ)
     *
     * @param oneWayDistanceKm Quãng đường 1 chiều (km)
     * @param waitHours        Thời gian chờ của khách (giờ)
     * @param isRoundTrip      Tích chọn đi 2 chiều hay không
     * @return Tổng cước vận chuyển trước khi trừ mã giảm giá (Giá gốc)
     */
    public BigDecimal calculateTripPrice(BigDecimal oneWayDistanceKm, Double waitHours, Boolean isRoundTrip) {
        if (oneWayDistanceKm == null || oneWayDistanceKm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 1. Tính cước di chuyển 1 chiều
        BigDecimal oneWayFare = calculateOneWayFare(oneWayDistanceKm);

        // 2. Tính cước đi 2 chiều: Chiều về tính 65% chiều đi (tổng = 1 + 0.65 = 1.65)
        BigDecimal travelFare;
        if (Boolean.TRUE.equals(isRoundTrip)) {
            travelFare = oneWayFare.multiply(BigDecimal.valueOf(1.0 + ROUND_TRIP_RETURN_RATIO));
        } else {
            travelFare = oneWayFare;
        }

        // 3. Tính phí thời gian chờ
        BigDecimal waitingFee = calculateWaitingFee(waitHours);

        // 4. Tổng cước gốc
        BigDecimal rawTotal = travelFare.add(waitingFee);

        // 5. Làm tròn đến hàng nghìn VNĐ gần nhất (VD: 1.455.270 -> 1.455.000)
        return rawTotal.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(1000));
    }
}

