package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.BookingRequest;
import com.example.trunganhcar.dto.DiscountValidateResponse;
import com.example.trunganhcar.dto.TripAvailabilityRequest;
import com.example.trunganhcar.dto.TripAvailabilityResponse;
import com.example.trunganhcar.dto.TripResponse;
import com.example.trunganhcar.entity.Discount;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.repository.DiscountRepository;
import com.example.trunganhcar.repository.DriverRepository;
import com.example.trunganhcar.repository.PromotionTripRepository;
import com.example.trunganhcar.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PromotionTripRepository promotionTripRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PricingService pricingService;

    @Mock
    private DiscountService discountService;

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private TripService tripService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = Driver.builder()
                .id(1L)
                .fullName("Trung Anh")
                .phone("0362882882")
                .approvalStatus("APPROVED")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Kiểm tra khả dụng: Tài xế rảnh trong khung giờ yêu cầu")
    void checkTripAvailability_available() {
        LocalDateTime pickup = LocalDateTime.of(2026, 11, 10, 8, 0);
        TripAvailabilityRequest request = TripAvailabilityRequest.builder()
                .pickupTime(pickup)
                .travelTime(60L)
                .bufferMinutes(30)
                .driverId(1L)
                .build();

        when(tripRepository.findConflictingTrips(eq(1L), eq(pickup), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        TripAvailabilityResponse response = tripService.checkTripAvailability(request);

        assertNotNull(response);
        assertTrue(response.isAvailable());
        assertEquals(0, response.getConflicts().size());
    }

    @Test
    @DisplayName("Kiểm tra khả dụng: Phát hiện xung đột lịch trình (ví dụ cuốc 10h-13h vs 9h50-12h)")
    void checkTripAvailability_hasConflict() {
        LocalDateTime pickup = LocalDateTime.of(2026, 11, 10, 9, 50);
        TripAvailabilityRequest request = TripAvailabilityRequest.builder()
                .pickupTime(pickup)
                .travelTime(100L) // 9h50 -> 11h30 + 30p buffer = 12h00
                .bufferMinutes(30)
                .driverId(1L)
                .build();

        Trip existingTrip = Trip.builder()
                .id(100L)
                .code("EXISTING01")
                .pickupAddress("Hà Nội")
                .dropoffAddress("Nội Bài")
                .pickupTime(LocalDateTime.of(2026, 11, 10, 10, 0))
                .busyUntil(LocalDateTime.of(2026, 11, 10, 13, 0))
                .status(TripStatus.CONFIRMED)
                .build();

        when(tripRepository.findConflictingTrips(eq(1L), eq(pickup), any(LocalDateTime.class)))
                .thenReturn(List.of(existingTrip));

        TripAvailabilityResponse response = tripService.checkTripAvailability(request);

        assertNotNull(response);
        assertFalse(response.isAvailable());
        assertEquals(1, response.getConflicts().size());
        assertEquals("EXISTING01", response.getConflicts().get(0).getCode());
    }

    @Test
    @DisplayName("Đặt xe áp dụng mã giảm giá: Giá và số tiền giảm được lưu đầy đủ")
    void createBooking_withDiscount() {
        LocalDateTime pickup = LocalDateTime.of(2026, 11, 10, 14, 0);
        BookingRequest request = BookingRequest.builder()
                .customerName("Nguyễn Văn A")
                .customerPhone("0912345678")
                .pickupAddress("Hà Nội")
                .dropoffAddress("Nam Định")
                .distanceKm(BigDecimal.valueOf(90))
                .pickupTime(pickup)
                .discountCode("TRUNGANH50K")
                .build();

        BigDecimal originalPrice = BigDecimal.valueOf(1000000);
        BigDecimal discountAmount = BigDecimal.valueOf(50000);
        BigDecimal finalPrice = BigDecimal.valueOf(950000);

        when(pricingService.calculateTripPrice(eq(BigDecimal.valueOf(90)), any(), eq(false)))
                .thenReturn(originalPrice);

        when(discountService.validateAndCalculateDiscount("TRUNGANH50K", originalPrice))
                .thenReturn(DiscountValidateResponse.builder()
                        .valid(true)
                        .originalPrice(originalPrice)
                        .discountAmount(discountAmount)
                        .finalPrice(finalPrice)
                        .build());

        Discount discountEntity = Discount.builder().code("TRUNGANH50K").usedCount(0).build();
        when(discountRepository.findByCodeIgnoreCase("TRUNGANH50K")).thenReturn(Optional.of(discountEntity));

        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        TripResponse response = tripService.createBooking(request, "http://localhost:8080");

        assertNotNull(response);
        assertEquals(finalPrice, response.getEstimatedPrice());
        assertEquals(discountAmount, response.getDiscountAmount());
        assertEquals("TRUNGANH50K", response.getDiscountCode());
    }

    @Test
    @DisplayName("Lấy danh sách các chuyến sắp tới của khách hàng theo số điện thoại")
    void getUpcomingTripsByPhone_success() {
        Trip trip = Trip.builder()
                .id(1L)
                .code("UPCOMING1")
                .customerName("Khách Thân Thiết")
                .customerPhone("0988777666")
                .pickupAddress("Điểm đón A")
                .dropoffAddress("Điểm đến B")
                .pickupTime(LocalDateTime.now().plusHours(2))
                .status(TripStatus.CONFIRMED)
                .driverId(1L)
                .build();

        when(tripRepository.findUpcomingTripsByCustomerPhone("0988777666"))
                .thenReturn(List.of(trip));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        List<TripResponse> results = tripService.getUpcomingTripsByPhone("0988777666", "http://localhost:8080");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("UPCOMING1", results.get(0).getCode());
        assertEquals("0988777666", results.get(0).getCustomerPhone());
    }
}

