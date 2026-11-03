package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.BookingRequest;
import com.example.trunganhcar.dto.TripResponse;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.entity.PromotionTrip;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import com.example.trunganhcar.event.BookingCreatedEvent;
import com.example.trunganhcar.event.TripStatusChangedEvent;
import com.example.trunganhcar.repository.DriverRepository;
import com.example.trunganhcar.repository.PromotionTripRepository;
import com.example.trunganhcar.repository.TripRepository;
import com.example.trunganhcar.util.NanoIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final PromotionTripRepository promotionTripRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        LocalDateTime busyUntil = pickupTime.plusMinutes(travelTime).plusMinutes(bufferMinutes);

        // Đảm bảo tọa độ không bị null theo ràng buộc database
        String pickupCoords = request.getPickupCoordinates() != null ? request.getPickupCoordinates().trim() : "";
        String dropoffCoords = request.getDropoffCoordinates() != null ? request.getDropoffCoordinates().trim() : "";

        // Xử lý gói cước chuyến cố định theo chương trình quảng cáo
        boolean isFixed = Boolean.TRUE.equals(request.getIsFixedTrip());
        Long promoTripId = null;
        String fixedTitle = null;
        String dropoffAddress = request.getDropoffAddress() != null ? request.getDropoffAddress().trim() : "";

        if (isFixed) {
            promoTripId = request.getPromotionTripId();
            fixedTitle = request.getFixedTripTitle();
            if (promoTripId != null) {
                PromotionTrip promoTrip = promotionTripRepository.findById(promoTripId).orElse(null);
                if (promoTrip != null) {
                    if (fixedTitle == null || fixedTitle.isBlank()) {
                        fixedTitle = promoTrip.getTitle();
                    }
                    // Đồng bộ địa chỉ trả khách từ gói cước cố định nếu request không truyền
                    if (dropoffAddress.isBlank() && promoTrip.getDropoffAddress() != null) {
                        dropoffAddress = promoTrip.getDropoffAddress();
                    }
                    // Đồng bộ thời gian di chuyển từ gói cước cố định nếu request không truyền
                    if (travelTime <= 0 && promoTrip.getTravelTime() != null) {
                        travelTime = promoTrip.getTravelTime().longValue();
                        busyUntil = pickupTime.plusMinutes(travelTime).plusMinutes(bufferMinutes);
                    }
                }
            }
        }

        Trip trip = Trip.builder()
                .code(code)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .pickupAddress(request.getPickupAddress().trim())
                .pickupCoordinates(pickupCoords)
                .dropoffAddress(dropoffAddress)
                .dropoffCoordinates(dropoffCoords)
                .distanceKm(request.getDistanceKm())
                .estimatedPrice(request.getEstimatedPrice())
                .pickupTime(pickupTime)
                .travelTime(travelTime)
                .bufferMinutes(bufferMinutes)
                .busyUntil(busyUntil)
                .driverId(request.getDriverId()) // Lưu trực tiếp driverId không dùng annotation quan hệ
                .waitHours(request.getWaitHours())
                .isRoundTrip(Boolean.TRUE.equals(request.getIsRoundTrip()))
                .isFixedTrip(isFixed)
                .fixedTripTitle(fixedTitle)
                .promotionTripId(promoTripId)
                .discountCode(request.getDiscountCode())
                .discountAmount(request.getDiscountAmount())
                .luggageOption(request.getLuggageOption())
                .note(request.getNote())
                .status(TripStatus.PENDING)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        log.info("Đã tạo chuyến xe thành công. ID: {}, Code: {}", savedTrip.getId(), savedTrip.getCode());

        // Bắn sự kiện BookingCreatedEvent (Event-driven) cho Telegram bot xử lý
        eventPublisher.publishEvent(new BookingCreatedEvent(this, savedTrip));

        Driver driver = savedTrip.getDriverId() != null 
                ? driverRepository.findById(savedTrip.getDriverId()).orElse(null) : null;
        return TripResponse.fromEntity(savedTrip, driver, baseUrl);
    }

    /**
     * Lấy thông tin chi tiết chuyến đi theo code (cho trang tracking của khách)
     */
    @Transactional(readOnly = true)
    public TripResponse getTripByCode(String code, String baseUrl) {
        Trip trip = findTripByCode(code);
        Driver driver = trip.getDriverId() != null 
                ? driverRepository.findById(trip.getDriverId()).orElse(null) : null;
        return TripResponse.fromEntity(trip, driver, baseUrl);
    }

    /**
     * Tìm entity Trip theo code
     */
    @Transactional(readOnly = true)
    public Trip findTripByCode(String code) {
        return tripRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến đi với mã: " + code));
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
            throw new IllegalStateException("Chuyến đi đã kết thúc (" + current + "), không thể thay đổi trạng thái nữa.");
        }

        boolean valid = switch (current) {
            case PENDING -> target == TripStatus.CONFIRMED || target == TripStatus.CANCELLED;
            case CONFIRMED -> target == TripStatus.ARRIVING || target == TripStatus.COMPLETED || target == TripStatus.CANCELLED;
            case ARRIVING -> target == TripStatus.IN_TRIP || target == TripStatus.COMPLETED || target == TripStatus.CANCELLED;
            case IN_TRIP -> target == TripStatus.COMPLETED || target == TripStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new IllegalStateException(String.format("Không thể chuyển trạng thái từ %s sang %s", current, target));
        }
    }
}

