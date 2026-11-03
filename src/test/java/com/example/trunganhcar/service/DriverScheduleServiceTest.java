package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.DriverDailyTripCountDto;
import com.example.trunganhcar.dto.DriverScheduleResponse;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.repository.DriverRepository;
import com.example.trunganhcar.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverScheduleServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver sampleDriver;

    @BeforeEach
    void setUp() {
        sampleDriver = Driver.builder()
                .id(1L)
                .fullName("Trung Anh (Chủ xe)")
                .phone("0362882882")
                .vehicleModel("VinFast VF e34")
                .licensePlate("30K - 868.86")
                .rating(5.0)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Lấy tóm tắt số chuyến 30 ngày tới trả về đúng 30 ngày và đếm chính xác số chuyến")
    void getMyScheduleSummaryNext30Days_success() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(sampleDriver));

        LocalDate today = LocalDate.now();
        Trip tripToday1 = Trip.builder()
                .id(101L)
                .driverId(1L)
                .pickupTime(today.atTime(8, 0))
                .build();

        Trip tripToday2 = Trip.builder()
                .id(102L)
                .driverId(1L)
                .pickupTime(today.atTime(14, 0))
                .build();

        Trip tripTomorrow = Trip.builder()
                .id(103L)
                .driverId(1L)
                .pickupTime(today.plusDays(1).atTime(9, 30))
                .build();

        when(tripRepository.findTripsForDriverBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(tripToday1, tripToday2, tripTomorrow));

        List<DriverDailyTripCountDto> result = driverService.getMyScheduleSummaryNext30Days(1L);

        assertNotNull(result);
        assertEquals(30, result.size());

        // Ngày đầu tiên là hôm nay
        DriverDailyTripCountDto todayDto = result.get(0);
        assertTrue(todayDto.isToday());
        assertEquals(today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), todayDto.getDate());
        assertEquals(2, todayDto.getTripCount());
        assertNotNull(todayDto.getDayOfWeek());

        // Ngày mai
        DriverDailyTripCountDto tomorrowDto = result.get(1);
        assertFalse(tomorrowDto.isToday());
        assertEquals(today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), tomorrowDto.getDate());
        assertEquals(1, tomorrowDto.getTripCount());

        // Các ngày còn lại tripCount = 0
        assertEquals(0, result.get(2).getTripCount());
    }

    @Test
    @DisplayName("Lấy chi tiết lịch trình hôm nay trả về danh sách khung giờ trống")
    void getMyScheduleToday_success() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(sampleDriver));
        when(tripRepository.findBusyTripsForDriver(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        DriverScheduleResponse response = driverService.getMyScheduleToday(1L);

        assertNotNull(response);
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), response.getSelectedDate());
        assertNotNull(response.getDriver());
        assertEquals("Trung Anh (Chủ xe)", response.getDriver().getName());
        assertNotNull(response.getAvailableSlots());
        assertFalse(response.getAvailableSlots().isEmpty());
    }
}

