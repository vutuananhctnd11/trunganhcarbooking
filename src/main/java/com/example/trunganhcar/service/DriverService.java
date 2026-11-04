package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.DriverDailyTripCountDto;
import com.example.trunganhcar.dto.DriverRequest;
import com.example.trunganhcar.dto.DriverResponse;
import com.example.trunganhcar.dto.DriverScheduleResponse;
import com.example.trunganhcar.dto.DriverScheduleResponse.BusyTripDto;
import com.example.trunganhcar.dto.DriverScheduleResponse.DriverSummaryDto;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.exception.ErrorCode;
import com.example.trunganhcar.repository.DriverRepository;
import com.example.trunganhcar.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Tạo mới thông tin tài xế
     */
    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        log.info("Tạo tài xế mới: {} - SĐT: {}", request.getFullName(), request.getPhone());

        if (driverRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new CustomException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        if (request.getLicensePlate() != null && !request.getLicensePlate().isBlank()) {
            String formattedPlate = com.example.trunganhcar.util.LicensePlateUtils.formatLicensePlate(request.getLicensePlate());
            request.setLicensePlate(formattedPlate);
            if (driverRepository.findByLicensePlate(formattedPlate).isPresent()) {
                throw new CustomException(ErrorCode.LICENSE_PLATE_ALREADY_EXISTS);
            }
        }

        Driver driver = Driver.builder()
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .experienceYears(request.getExperienceYears())
                .vehicleModel(request.getVehicleModel())
                .vehicleColor(request.getVehicleColor())
                .seatCount(request.getSeatCount() != null ? request.getSeatCount() : 4)
                .licensePlate(request.getLicensePlate() != null ? request.getLicensePlate().trim().toUpperCase() : null)
                .telegramChatId(request.getTelegramChatId())
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl().trim() : null)
                .rating(request.getRating() != null ? request.getRating() : 5.0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Driver saved = driverRepository.save(driver);
        return DriverResponse.fromEntity(saved);
    }

    /**
     * Lấy danh sách tất cả tài xế (có thể lọc theo trạng thái hoạt động)
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers(Boolean isActive) {
        List<Driver> drivers = driverRepository.findAll();
        return drivers.stream()
                .filter(d -> isActive == null || d.getIsActive().equals(isActive))
                .map(DriverResponse::fromEntity)
                .toList();
    }

    /**
     * Lấy chi tiết tài xế theo ID
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long id) {
        Driver driver = findDriverById(id);
        return DriverResponse.fromEntity(driver);
    }

    /**
     * Cập nhật thông tin tài xế
     */
    @Transactional
    public DriverResponse updateDriver(Long id, DriverRequest request) {
        log.info("Cập nhật thông tin tài xế ID: {}", id);
        Driver driver = findDriverById(id);

        if (request.getPhone() != null && !request.getPhone().isBlank() && !request.getPhone().equals(driver.getPhone())) {
            driverRepository.findByPhone(request.getPhone()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new CustomException(ErrorCode.PHONE_ALREADY_EXISTS, "Số điện thoại mới đã được sử dụng bởi tài xế khác!");
                }
            });
            driver.setPhone(request.getPhone().trim());
        }

        if (request.getLicensePlate() != null && !request.getLicensePlate().isBlank() && !request.getLicensePlate().equalsIgnoreCase(driver.getLicensePlate())) {
            String formattedPlate = com.example.trunganhcar.util.LicensePlateUtils.formatLicensePlate(request.getLicensePlate());
            request.setLicensePlate(formattedPlate);
            driverRepository.findByLicensePlate(formattedPlate).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new CustomException(ErrorCode.LICENSE_PLATE_ALREADY_EXISTS, "Biển số xe mới đã tồn tại trên hệ thống!");
                }
            });
            driver.setLicensePlate(formattedPlate);
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            driver.setFullName(request.getFullName().trim());
        }
        if (request.getExperienceYears() != null) {
            driver.setExperienceYears(request.getExperienceYears());
        }
        if (request.getVehicleModel() != null) {
            driver.setVehicleModel(request.getVehicleModel().trim());
        }
        if (request.getVehicleColor() != null) {
            driver.setVehicleColor(request.getVehicleColor().trim());
        }
        if (request.getSeatCount() != null) {
            driver.setSeatCount(request.getSeatCount());
        }
        if (request.getTelegramChatId() != null) {
            driver.setTelegramChatId(request.getTelegramChatId().trim());
        }
        if (request.getAvatarUrl() != null) {
            driver.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getRating() != null) {
            driver.setRating(request.getRating());
        }
        if (request.getIsActive() != null) {
            driver.setIsActive(request.getIsActive());
        }

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    /**
     * Xóa tài xế theo ID
     */
    @Transactional
    public void deleteDriver(Long id) {
        log.info("Xóa tài xế ID: {}", id);
        Driver driver = findDriverById(id);
        driverRepository.delete(driver);
    }

    /**
     * Lấy lịch trình của tài xế theo ID và ngày được chọn (bao gồm danh sách chuyến bận và các khung giờ còn trống)
     */
    @Transactional(readOnly = true)
    public DriverScheduleResponse getDriverSchedule(Long driverId, LocalDate selectedDate) {
        LocalDate targetDate = selectedDate != null ? selectedDate : LocalDate.now();
        log.info("Lấy lịch trình của tài xế ID: {} cho ngày: {}", driverId, targetDate);

        Driver driver = findDriverById(driverId);

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<Trip> busyTripEntities = tripRepository.findBusyTripsForDriver(driverId, startOfDay, endOfDay);

        List<BusyTripDto> busyTrips = new ArrayList<>();
        for (Trip trip : busyTripEntities) {
            LocalDateTime busyUntil = trip.getBusyUntil() != null 
                    ? trip.getBusyUntil() 
                    : trip.getPickupTime().plusMinutes(trip.getTravelTime() != null ? trip.getTravelTime() : 0)
                                          .plusMinutes(trip.getBufferMinutes() != null ? trip.getBufferMinutes() : 30);

            String timeDisplay = String.format("%s - %s",
                    trip.getPickupTime().format(TIME_FORMATTER),
                    busyUntil.format(TIME_FORMATTER));

            busyTrips.add(BusyTripDto.builder()
                    .tripId(trip.getId())
                    .route(buildShortRoute(trip.getPickupAddress(), trip.getDropoffAddress()))
                    .timeDisplay(timeDisplay)
                    .startTime(trip.getPickupTime())
                    .tralvelTime(String.valueOf(trip.getTravelTime() != null ? trip.getTravelTime() : 0))
                    .busyUntil(busyUntil)
                    .build());
        }

        // Tính toán các khung giờ còn trống (available_slots) từ 06:00 đến 22:00 (mỗi bước 30 phút)
        List<String> availableSlots = calculateAvailableSlots(targetDate, busyTripEntities);

        DriverSummaryDto driverSummary = DriverSummaryDto.builder()
                .id(driver.getId())
                .name(driver.getFullName())
                .vehicle(driver.getVehicleModel() != null ? driver.getVehicleModel() : "Chưa cập nhật")
                .licensePlate(driver.getLicensePlate() != null ? driver.getLicensePlate() : "")
                .avatarUrl(driver.getAvatarUrl())
                .rating(driver.getRating() != null ? driver.getRating() : 5.0)
                .build();

        return DriverScheduleResponse.builder()
                .driver(driverSummary)
                .selectedDate(targetDate.format(DATE_FORMATTER))
                .busyTrips(busyTrips)
                .availableSlots(availableSlots)
                .build();
    }

    /**
     * API xem số chuyến mỗi ngày trong 30 ngày tới của tài xế
     */
    @Transactional(readOnly = true)
    public List<DriverDailyTripCountDto> getMyScheduleSummaryNext30Days(Long driverId) {
        log.info("Lấy tóm tắt số chuyến 30 ngày tới cho tài xế ID: {}", driverId);
        findDriverById(driverId); // Kiểm tra tài xế có tồn tại

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(29);

        LocalDateTime startDateTime = today.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Trip> trips = tripRepository.findTripsForDriverBetween(driverId, startDateTime, endDateTime);

        // Gom nhóm số lượng cuốc xe theo ngày
        Map<LocalDate, Long> countByDate = trips.stream()
                .collect(Collectors.groupingBy(t -> t.getPickupTime().toLocalDate(), Collectors.counting()));

        List<DriverDailyTripCountDto> result = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            LocalDate currentDate = today.plusDays(i);
            long tripCount = countByDate.getOrDefault(currentDate, 0L);

            result.add(DriverDailyTripCountDto.builder()
                    .date(currentDate.format(DATE_FORMATTER))
                    .dayOfWeek(formatDayOfWeekVietnamese(currentDate.getDayOfWeek()))
                    .tripCount(tripCount)
                    .isToday(i == 0)
                    .build());
        }

        return result;
    }

    /**
     * API xem chi tiết lịch trình trong hôm nay của tài xế
     */
    @Transactional(readOnly = true)
    public DriverScheduleResponse getMyScheduleToday(Long driverId) {
        return getDriverSchedule(driverId, LocalDate.now());
    }

    private String formatDayOfWeekVietnamese(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }

    /**
     * Tính toán các khung giờ còn trống (không trùng với bất kỳ chuyến bận nào của tài xế)
     */
    private List<String> calculateAvailableSlots(LocalDate date, List<Trip> busyTrips) {
        List<String> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(6, 0); // 06:00 sáng
        LocalTime end = LocalTime.of(22, 0);   // 22:00 tối

        LocalTime current = start;
        while (!current.isAfter(end)) {
            LocalDateTime slotDateTime = date.atTime(current);

            boolean isBusy = false;
            for (Trip trip : busyTrips) {
                LocalDateTime pickup = trip.getPickupTime();
                LocalDateTime busyUntil = trip.getBusyUntil() != null 
                        ? trip.getBusyUntil() 
                        : pickup.plusMinutes(trip.getTravelTime() != null ? trip.getTravelTime() : 0)
                                .plusMinutes(trip.getBufferMinutes() != null ? trip.getBufferMinutes() : 30);

                // Nếu thời điểm slot nằm trong khoảng [pickup, busyUntil) thì tài xế đang bận
                if (!slotDateTime.isBefore(pickup) && slotDateTime.isBefore(busyUntil)) {
                    isBusy = true;
                    break;
                }
            }

            if (!isBusy) {
                slots.add(current.format(TIME_FORMATTER));
            }

            current = current.plusMinutes(30);
        }
        return slots;
    }

    /**
     * Tạo chuỗi tóm tắt tuyến đường ngắn gọn (VD: "Hoàn Kiếm -> Nội Bài")
     */
    private String buildShortRoute(String pickup, String dropoff) {
        return extractShortLocation(pickup) + " -> " + extractShortLocation(dropoff);
    }

    private String extractShortLocation(String address) {
        if (address == null || address.isBlank()) return "Chưa rõ";
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim();
        }
        return parts[0].trim();
    }

    /**
     * Tìm entity Driver theo ID (ném exception nếu không tìm thấy)
     */
    @Transactional(readOnly = true)
    public Driver findDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND, "Không tìm thấy tài xế với ID: " + id));
    }
}
