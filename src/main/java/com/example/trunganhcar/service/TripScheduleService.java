package com.example.trunganhcar.service;

import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import com.example.trunganhcar.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service chạy định kỳ quản lý lịch trình và gửi thông báo nhắc nhở tự động qua Telegram:
 * 1. Nhắc trước giờ đón 1 tiếng (yêu cầu tài xế chuẩn bị và xuất phát đến điểm đón)
 * 2. Khi hết giờ chuyến đi (hết busyUntil, tự động hoàn thành chuyến và gửi Web Push cho khách)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripScheduleService {

    private final TripRepository tripRepository;
    private final TelegramBotService telegramBotService;
    private final TripService tripService;

    // Bộ nhớ cache lưu các mã chuyến đã gửi thông báo để tránh gửi lặp
    private final Set<String> reminded1hTripCodes = ConcurrentHashMap.newKeySet();
    private final Set<String> finishedNotifiedTripCodes = ConcurrentHashMap.newKeySet();

    /**
     * Quét định kỳ mỗi 60 phút
     */
    @Scheduled(fixedRate = 600000)
    public void scanAndSendTripReminders() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Quét các chuyến CONFIRMED cách giờ đón từ 50 đến 65 phút (chuẩn bị đón trước 1 tiếng)
        LocalDateTime fromTime = now.plusMinutes(50);
        LocalDateTime toTime = now.plusMinutes(65);

        List<Trip> upcomingTrips = tripRepository.findTripsNearPickupTime(fromTime, toTime);
        for (Trip trip : upcomingTrips) {
            if (!reminded1hTripCodes.contains(trip.getCode())) {
                log.info("Phát hiện chuyến đi sắp đón khách sau 1 tiếng: mã={}", trip.getCode());
                try {
                    telegramBotService.sendPickupReminder(trip);
                    reminded1hTripCodes.add(trip.getCode());
                } catch (Exception e) {
                    log.error("Lỗi khi gửi thông báo nhắc đón trước 1 tiếng cho chuyến {}: {}", trip.getCode(), e.getMessage());
                }
            }
        }

        // 2. Quét các chuyến đang chạy (ARRIVING, IN_TRIP, CONFIRMED) đã đến/quá thời gian kết thúc dự kiến (busyUntil <= now)
        List<Trip> expiredTrips = tripRepository.findTripsPassedBusyUntil(now);
        for (Trip trip : expiredTrips) {
            if (!finishedNotifiedTripCodes.contains(trip.getCode())) {
                log.info("Phát hiện chuyến đi đã hết thời gian dự kiến: mã={}, busyUntil={}", trip.getCode(), trip.getBusyUntil());
                try {
                    Trip completedTrip = trip;
                    if (trip.getStatus() != TripStatus.COMPLETED) {
                        completedTrip = tripService.updateTripStatus(trip.getCode(), TripStatus.COMPLETED, "Cuốc xe đã kết thúc theo lịch trình");
                    }
                    telegramBotService.sendTripFinishedReminder(completedTrip);
                    finishedNotifiedTripCodes.add(trip.getCode());
                } catch (Exception e) {
                    log.error("Lỗi khi tự động hoàn thành chuyến đi {}: {}", trip.getCode(), e.getMessage());
                }
            }
        }
    }
}

