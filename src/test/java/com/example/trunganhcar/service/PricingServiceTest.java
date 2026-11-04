package com.example.trunganhcar.service;

import com.example.trunganhcar.util.LicensePlateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    @Test
    @DisplayName("Cự ly <= 2 km: áp dụng giá mở cửa 30.500 VNĐ")
    void testTier1_OpeningFare() {
        BigDecimal price1 = pricingService.calculateOneWayFare(BigDecimal.valueOf(1.5));
        assertEquals(BigDecimal.valueOf(30500), price1);

        BigDecimal price2 = pricingService.calculateOneWayFare(BigDecimal.valueOf(2.0));
        assertEquals(BigDecimal.valueOf(30500), price2);
    }

    @Test
    @DisplayName("Cự ly km thứ 3 - 12: 30.500 + (km - 2) * 14.700")
    void testTier2_Fare() {
        // 5 km: 30.500 + 3 * 14.700 = 30.500 + 44.100 = 74.600
        BigDecimal price = pricingService.calculateOneWayFare(BigDecimal.valueOf(5.0));
        assertEquals(BigDecimal.valueOf(74600.0), price);

        // 12 km: 30.500 + 10 * 14.700 = 177.500
        BigDecimal price12 = pricingService.calculateOneWayFare(BigDecimal.valueOf(12.0));
        assertEquals(BigDecimal.valueOf(177500.0), price12);
    }

    @Test
    @DisplayName("Cự ly km thứ 13 - 25: 177.500 + (km - 12) * 13.800")
    void testTier3_Fare() {
        // 20 km: 177.500 + 8 * 13.800 = 177.500 + 110.400 = 287.900
        BigDecimal price = pricingService.calculateOneWayFare(BigDecimal.valueOf(20.0));
        assertEquals(BigDecimal.valueOf(287900.0), price);
    }

    @Test
    @DisplayName("Cự ly > 25 km: khớp chính xác 117.3 km ra 1.455.000 VNĐ")
    void testTier4_117_3km() {
        // 117.3 km: 356.900 + (117.3 - 25) * 11.900 = 356.900 + 1.098.370 = 1.455.270 -> làm tròn 1.455.000 VNĐ
        BigDecimal total = pricingService.calculateTripPrice(BigDecimal.valueOf(117.3), 0.0, false);
        assertEquals(BigDecimal.valueOf(1455000), total);
    }

    @Test
    @DisplayName("Khứ hồi 2 chiều: Chiều về tính 65% (tổng x 1.65)")
    void testRoundTrip() {
        // 10 km 1 chiều: 30.500 + 8 * 14.700 = 148.100
        // 2 chiều: 148.100 * 1.65 = 244.365 -> làm tròn 244.000 VNĐ
        BigDecimal total = pricingService.calculateTripPrice(BigDecimal.valueOf(10.0), 0.0, true);
        assertEquals(BigDecimal.valueOf(244000), total);
    }

    @Test
    @DisplayName("Thời gian chờ: Miễn phí 30 phút (0.5h), 30k/h sau đó")
    void testWaitingFee() {
        assertEquals(BigDecimal.ZERO, pricingService.calculateWaitingFee(0.3));
        assertEquals(BigDecimal.ZERO, pricingService.calculateWaitingFee(0.5));

        // Chờ 1.5 giờ: tính 1.0 giờ = 30.000 đ
        assertEquals(BigDecimal.valueOf(30000.0), pricingService.calculateWaitingFee(1.5));
    }

    @Test
    @DisplayName("Format BKS thành chuẩn 35H - 182.89")
    void testLicensePlateFormatting() {
        assertEquals("35H - 182.89", LicensePlateUtils.formatLicensePlate("35h18289"));
        assertEquals("35H - 182.89", LicensePlateUtils.formatLicensePlate("35H-18289"));
        assertEquals("35H - 182.89", LicensePlateUtils.formatLicensePlate("35H 18289"));
        assertEquals("35H - 182.89", LicensePlateUtils.formatLicensePlate("35H - 182.89"));
        assertEquals("30K - 868.86", LicensePlateUtils.formatLicensePlate("30k86886"));
        assertEquals("29A - 123.45", LicensePlateUtils.formatLicensePlate("29a12345"));
        assertEquals("29LD - 123.45", LicensePlateUtils.formatLicensePlate("29ld12345"));
        assertEquals("29A - 1234", LicensePlateUtils.formatLicensePlate("29a1234"));
    }
}

