package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.BookingRequest;
import com.example.trunganhcar.dto.ConflictingTripDto;
import com.example.trunganhcar.dto.DiscountValidateResponse;
import com.example.trunganhcar.dto.TripAvailabilityRequest;
import com.example.trunganhcar.dto.TripAvailabilityResponse;
import com.example.trunganhcar.dto.TripResponse;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.entity.PromotionTrip;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import com.example.trunganhcar.event.BookingCreatedEvent;
import com.example.trunganhcar.event.TripStatusChangedEvent;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.exception.ErrorCode;
import com.example.trunganhcar.repository.DiscountRepository;
import com.example.trunganhcar.repository.DriverRepository;
import com.example.trunganhcar.repository.PromotionTripRepository;
import com.example.trunganhcar.repository.TripRepository;
import com.example.trunganhcar.util.NanoIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final PromotionTripRepository promotionTripRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PricingService pricingService;
    private final DiscountService discountService;
    private final DiscountRepository discountRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    /**
     * Tạo chuyến đi mới cho khách hàng
     */
    @Transactional
    public TripResponse createBooking(BookingRequest request, String baseUrl) {
        log.info("Bắt đầu xử lý đơn đặt xe từ khách: {} - SĐT: {}", request.getCustomerName(), request.getCustomerPhone());

        // Sinh mã NanoID ngẫu nhiên (10 ký tự) và đảm bảo không bị trùng
        String code;
        int attempts = 0;
        do {
            code = NanoIdUtils.randomNanoId();
            attempts++;
            if (attempts > 5) {
                // Dự phòng nếu trùng lặp (xác suất gần như bằng 0)
                code = NanoIdUtils.randomNanoId(12);
            }
        } while (tripRepository.existsByCode(code));

        LocalDateTime pickupTime = request.getPickupTime() != null ? request.getPickupTime() : LocalDateTime.now();

        // Xử lý thời gian di chuyển, bufferMinutes (mặc định 30 phút) và tính busyUntil
        Long bufferMinutes = request.getBufferMinutes() != null ? request.getBufferMinutes() : 30L;
        Long travelTime = request.getTravelTime() != null ? request.getTravelTime() : 0L;

        // Đảm bảo tọa độ không bị null theo ràng buộc database
        String pickupCoords = request.getPickupCoordinates() != null ? request.getPickupCoordinates().trim() : "";
        String dropoffCoords = request.getDropoffCoordinates() != null ? request.getDropoffCoordinates().trim() : "";

        // Xử lý gói cước chuyến cố định theo chương trình quảng cáo
        boolean isFixed = Boolean.TRUE.equals(request.getIsFixedTrip()) || request.getPromotionTripId() != null;
        Long promoTripId = null;
        String dropoffAddress = request.getDropoffAddress() != null ? request.getDropoffAddress().trim() : "";
        boolean isRoundTrip = Boolean.TRUE.equals(request.getIsRoundTrip());

        BigDecimal calculatedBasePrice;

        if (isFixed && request.getPromotionTripId() != null) {
            promoTripId = request.getPromotionTripId();
            PromotionTrip promoTrip = promotionTripRepository.findById(promoTripId).orElse(null);

            if (promoTrip != null) {
                if (dropoffAddress.isBlank() && promoTrip.getDropoffAddress() != null) {
                    dropoffAddress = promoTrip.getDropoffAddress();
                }
                if (travelTime <= 0 && promoTrip.getTravelTime() != null) {
                    travelTime = promoTrip.getTravelTime().longValue();
                }

                BigDecimal basePromo = promoTrip.getPrice() != null ? promoTrip.getPrice() : BigDecimal.ZERO;
                if (isRoundTrip) {
                    basePromo = basePromo.multiply(BigDecimal.valueOf(1.0 + PricingService.ROUND_TRIP_RETURN_RATIO));
                }
                BigDecimal waitFee = pricingService.calculateWaitingFee(request.getWaitHours());
                calculatedBasePrice = basePromo.add(waitFee);
            } else {
                calculatedBasePrice = pricingService.calculateTripPrice(request.getDistanceKm(), request.getWaitHours(), isRoundTrip);
            }
        } else {
            // Tính cước di chuyển theo công thức chuẩn:
            // Giá mở cửa 2km đầu: 30.500, km 3-12: 14.700, km 13-25: 13.800, km 26+: 11.900
            // Chiều về 65% chiều đi, Chờ miễn phí 30p (0.5h), 30k/h tiếp theo
            calculatedBasePrice = pricingService.calculateTripPrice(request.getDistanceKm(), request.getWaitHours(), isRoundTrip);
        }

        // Quãng đường và thời gian khi đi 2 chiều:
        // "Checkbox đi 2 chiều: Chiều về chỉ tính giá 65% chiều đi, quãng đường và thời gian x2"
        BigDecimal distanceKm = request.getDistanceKm();
        long totalTravelMinutes = travelTime;
        if (isRoundTrip) {
            if (distanceKm != null) {
                distanceKm = distanceKm.multiply(BigDecimal.valueOf(2));
            }
            if (totalTravelMinutes > 0) {
                totalTravelMinutes = totalTravelMinutes * 2;
            }
        }
        if (request.getWaitHours() != null && request.getWaitHours() > 0) {
            totalTravelMinutes += Math.round(request.getWaitHours() * 60);
        }
        LocalDateTime busyUntil = pickupTime.plusMinutes(totalTravelMinutes).plusMinutes(bufferMinutes);

        // Kiểm tra xung đột lịch trình tài xế nếu có chỉ định driverId
        if (request.getDriverId() != null) {
            List<Trip> conflicts = tripRepository.findConflictingTrips(request.getDriverId(), pickupTime, busyUntil);
            if (!conflicts.isEmpty()) {
                Trip conflictTrip = conflicts.get(0);
                throw new CustomException(
                        ErrorCode.INVALID_TRIP_STATUS,
                        String.format("Tài xế đã có chuyến xe [%s] bận từ %s đến %s. Vui lòng chọn khung giờ khác!",
                                conflictTrip.getCode(),
                                conflictTrip.getPickupTime().format(TIME_FORMATTER),
                                conflictTrip.getBusyUntil().format(TIME_FORMATTER))
                );
            }
        }

        // Validate mã giảm giá và tính toán số tiền giảm
        String discountCode = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            discountCode = request.getDiscountCode().trim().toUpperCase();
            DiscountValidateResponse discountRes = discountService.validateAndCalculateDiscount(discountCode, calculatedBasePrice);
            discountAmount = discountRes.getDiscountAmount();
            calculatedBasePrice = discountRes.getFinalPrice();

            // Cập nhật số lượt sử dụng
            discountRepository.findByCodeIgnoreCase(discountCode).ifPresent(d -> {
                d.setUsedCount((d.getUsedCount() != null ? d.getUsedCount() : 0) + 1);
                discountRepository.save(d);
            });
        }

        Trip trip = Trip.builder()
                .code(code)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .pickupAddress(request.getPickupAddress().trim())
                .pickupCoordinates(pickupCoords)
                .dropoffAddress(dropoffAddress)
                .dropoffCoordinates(dropoffCoords)
                .distanceKm(distanceKm)
                .estimatedPrice(calculatedBasePrice)
                .discountAmount(discountAmount)
                .pickupTime(pickupTime)
                .travelTime(totalTravelMinutes)
                .bufferMinutes(bufferMinutes)
                .busyUntil(busyUntil)
                .driverId(request.getDriverId())
                .waitHours(request.getWaitHours())
                .isRoundTrip(isRoundTrip)
                .promotionTripId(promoTripId)
                .discountCode(discountCode)
                .luggageOption(request.getLuggageOption())
                .note(request.getNote())
                .pushNotification(request.getPushNotification())
                .status(TripStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Trip savedTrip = tripRepository.save(trip);
        log.info("Đã tạo chuyến xe thành công. ID: {}, Code: {}", savedTrip.getId(), savedTrip.getCode());

        // Bắn sự kiện BookingCreatedEvent (Event-driven) cho Telegram bot xử lý
        eventPublisher.publishEvent(new BookingCreatedEvent(this, savedTrip));

        Driver driver = savedTrip.getDriverId() != null 
                ? driverRepository.findById(savedTrip.getDriverId()).orElse(null) : null;
        if (driver == null) {
            driver = driverRepository.findById(1L)
                    .orElseGet(() -> driverRepository.findAll().stream().findFirst().orElse(null));
        }
        return TripResponse.fromEntity(savedTrip, driver, baseUrl);
    }

    /**
     * Kiểm tra xem chuyến đi sắp đặt có bị xung đột thời gian với lịch trình của tài xế không.
     * busy_until của chuyến phải trước chuyến liền kề tiếp theo, và pickup_time phải sau chuyến liền kề trước đó.
     */
    @Transactional(readOnly = true)
    public TripAvailabilityResponse checkTripAvailability(TripAvailabilityRequest request) {
        LocalDateTime pickupTime = request.getPickupTime();
        if (pickupTime == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "Thời gian đón không được để trống");
        }

        // Tính travelTime nếu chưa có
        Long travelTime = request.getTravelTime();
        if (travelTime == null || travelTime <= 0) {
            if (request.getDistanceKm() != null && request.getDistanceKm().compareTo(BigDecimal.ZERO) > 0) {
                // Ước tính thời gian chạy xe: ~35km/h, tối thiểu 15 phút
                travelTime = Math.max(15L, Math.round(request.getDistanceKm().doubleValue() * 1.8));
            } else {
                travelTime = 30L; // Mặc định 30 phút
            }
        }

        // Nếu khứ hồi, nhân đôi travelTime
        if (Boolean.TRUE.equals(request.getIsRoundTrip())) {
            travelTime = travelTime * 2;
        }

        // Cộng thời gian chờ nếu có
        if (request.getWaitHours() != null && request.getWaitHours() > 0) {
            travelTime += Math.round(request.getWaitHours() * 60);
        }

        int bufferMinutes = request.getBufferMinutes() != null ? request.getBufferMinutes() : 30;
        LocalDateTime busyUntil = pickupTime.plusMinutes(travelTime).plusMinutes(bufferMinutes);

        Long driverId = request.getDriverId();
        List<Trip> conflicts = tripRepository.findConflictingTrips(driverId, pickupTime, busyUntil);

        if (conflicts.isEmpty()) {
            return TripAvailabilityResponse.builder()
                    .available(true)
                    .message("Khung giờ khả dụng, tài xế có thể nhận chuyến!")
                    .pickupTime(pickupTime)
                    .busyUntil(busyUntil)
                    .driverId(driverId)
                    .conflicts(Collections.emptyList())
                    .build();
        } else {
            List<ConflictingTripDto> conflictDtos = conflicts.stream()
                    .map(ConflictingTripDto::fromEntity)
                    .toList();

            return TripAvailabilityResponse.builder()
                    .available(false)
                    .message(String.format("Khung giờ bị trùng với %d chuyến xe đã nhận của tài xế (từ %s đến %s).",
                            conflicts.size(),
                            pickupTime.format(TIME_FORMATTER),
                            busyUntil.format(TIME_FORMATTER)))
                    .pickupTime(pickupTime)
                    .busyUntil(busyUntil)
                    .driverId(driverId)
                    .conflicts(conflictDtos)
                    .build();
        }
    }

    /**
     * Lấy các chuyến sắp tới / đang diễn ra của khách hàng theo số điện thoại (không cần đăng nhập)
     */
    @Transactional(readOnly = true)
    public List<TripResponse> getUpcomingTripsByPhone(String phone, String baseUrl) {
        if (phone == null || phone.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "Số điện thoại không được để trống");
        }
        List<Trip> trips = tripRepository.findUpcomingTripsByCustomerPhone(phone.trim());
        return trips.stream()
                .map(t -> {
                    Driver driver = t.getDriverId() != null 
                            ? driverRepository.findById(t.getDriverId()).orElse(null) : null;
                    return TripResponse.fromEntity(t, driver, baseUrl);
                })
                .toList();
    }

    /**
     * Lấy toàn bộ lịch sử các chuyến của khách hàng theo số điện thoại (không cần đăng nhập)
     */
    @Transactional(readOnly = true)
    public List<TripResponse> getAllTripsByPhone(String phone, String baseUrl) {
        if (phone == null || phone.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "Số điện thoại không được để trống");
        }
        List<Trip> trips = tripRepository.findAllTripsByCustomerPhone(phone.trim());
        return trips.stream()
                .map(t -> {
                    Driver driver = t.getDriverId() != null 
                            ? driverRepository.findById(t.getDriverId()).orElse(null) : null;
                    return TripResponse.fromEntity(t, driver, baseUrl);
                })
                .toList();
    }

    /**
     * Lấy thông tin chi tiết chuyến đi theo code (cho trang tracking của khách)
     */
    @Transactional(readOnly = true)
    public TripResponse getTripByCode(String code, String baseUrl) {
        Trip trip = findTripByCode(code);
        Driver driver = trip.getDriverId() != null 
                ? driverRepository.findById(trip.getDriverId()).orElse(null) : null;
        if (driver == null) {
            driver = driverRepository.findById(1L)
                    .orElseGet(() -> driverRepository.findAll().stream().findFirst().orElse(null));
        }
        return TripResponse.fromEntity(trip, driver, baseUrl);
    }

    /**
     * Tìm entity Trip theo code
     */
    @Transactional(readOnly = true)
    public Trip findTripByCode(String code) {
        return tripRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND, "Không tìm thấy chuyến đi với mã: " + code));
    }

    /**
     * Cập nhật trạng thái chuyến đi theo Trip State Machine
     */
    @Transactional
    public Trip updateTripStatus(String code, TripStatus newStatus, String note) {
        return updateTripStatusWithDriver(code, newStatus, null, note);
    }

    /**
     * Tài xế bấm nhận chuyến qua Telegram: gán tài xế vào cuốc xe và chuyển trạng thái sang CONFIRMED
     */
    @Transactional
    public Trip acceptTripByDriver(String code, String telegramChatId, String note) {
        return updateTripStatusWithDriver(code, TripStatus.CONFIRMED, telegramChatId, note);
    }

    @Transactional
    public Trip updateTripStatusWithDriver(String code, TripStatus newStatus, String telegramChatId, String note) {
        Trip trip = findTripByCode(code);
        TripStatus oldStatus = trip.getStatus();

        // Kiểm tra tính hợp lệ của luồng trạng thái
        validateStateTransition(oldStatus, newStatus);

        // Nếu chuyến chưa có tài xế và được nhận, tìm tài xế theo telegramChatId để gán driverId
        if (trip.getDriverId() == null && newStatus == TripStatus.CONFIRMED && telegramChatId != null) {
            driverRepository.findByTelegramChatId(telegramChatId)
                    .ifPresent(driver -> trip.setDriverId(driver.getId()));
        }

        trip.setStatus(newStatus);
        Trip updatedTrip = tripRepository.save(trip);

        log.info("Cập nhật trạng thái chuyến {} từ {} sang {}", code, oldStatus, newStatus);

        // Bắn sự kiện TripStatusChangedEvent để đẩy realtime qua SSE
        eventPublisher.publishEvent(new TripStatusChangedEvent(this, updatedTrip, oldStatus, newStatus, note));

        return updatedTrip;
    }

    @Transactional
    public void updatePushSubscription(String code, String subscriptionJson) {
        Trip trip = findTripByCode(code);
        trip.setPushNotification(subscriptionJson);
        tripRepository.save(trip);
        log.info("Đã cập nhật Web Push Subscription cho chuyến xe [{}]", code);
    }

    /**
     * Kiểm tra chuyển đổi trạng thái hợp lệ
     * PENDING -> CONFIRMED / CANCELLED
     * CONFIRMED -> ARRIVING / CANCELLED
     * ARRIVING -> IN_TRIP / CANCELLED
     * IN_TRIP -> COMPLETED
     */
    private void validateStateTransition(TripStatus current, TripStatus target) {
        if (current == target) {
            return;
        }

        if (current == TripStatus.COMPLETED || current == TripStatus.CANCELLED) {
            throw new CustomException(ErrorCode.INVALID_TRIP_STATUS, "Chuyến đi đã kết thúc (" + current + "), không thể thay đổi trạng thái nữa.");
        }

        boolean valid = switch (current) {
            case PENDING -> target == TripStatus.CONFIRMED || target == TripStatus.CANCELLED;
            case CONFIRMED -> target == TripStatus.ARRIVING || target == TripStatus.COMPLETED || target == TripStatus.CANCELLED;
            case ARRIVING -> target == TripStatus.IN_TRIP || target == TripStatus.COMPLETED || target == TripStatus.CANCELLED;
            case IN_TRIP -> target == TripStatus.COMPLETED || target == TripStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new CustomException(ErrorCode.INVALID_TRIP_STATUS, String.format("Không thể chuyển trạng thái từ %s sang %s", current, target));
        }
    }
}
