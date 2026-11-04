package com.example.trunganhcar.validation;

import com.example.trunganhcar.annotation.ValidBookingRequest;
import com.example.trunganhcar.dto.BookingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingRequestValidator implements ConstraintValidator<ValidBookingRequest, BookingRequest> {

    @Override
    public boolean isValid(BookingRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        // 1. Validate customerName
        if (request.getCustomerName() == null || request.getCustomerName().trim().isBlank()) {
            addViolation(context, "customerName", "Họ tên khách hàng không được để trống!");
            valid = false;
        } else if (request.getCustomerName().trim().length() < 2) {
            addViolation(context, "customerName", "Họ tên khách hàng phải có ít nhất 2 ký tự!");
            valid = false;
        }

        // 2. Validate customerPhone
        if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isBlank()) {
            addViolation(context, "customerPhone", "Số điện thoại khách hàng không được để trống!");
            valid = false;
        }

        // 3. Validate pickupAddress
        if (request.getPickupAddress() == null || request.getPickupAddress().trim().isBlank()) {
            addViolation(context, "pickupAddress", "Điểm đón khách không được để trống!");
            valid = false;
        }

        // 4. Validate dropoffAddress (Nếu không chọn gói cước cố định thì bắt buộc nhập)
        boolean isFixedTrip = request.getPromotionTripId() != null || Boolean.TRUE.equals(request.getIsFixedTrip());
        if (!isFixedTrip && (request.getDropoffAddress() == null || request.getDropoffAddress().trim().isBlank())) {
            addViolation(context, "dropoffAddress", "Điểm đến không được để trống!");
            valid = false;
        }

        // 5. Validate distanceKm
        if (!isFixedTrip) {
            if (request.getDistanceKm() == null || request.getDistanceKm().compareTo(BigDecimal.ZERO) <= 0) {
                addViolation(context, "distanceKm", "Quãng đường di chuyển phải lớn hơn 0 km!");
                valid = false;
            }
        }

        // 6. Validate waitHours
        if (request.getWaitHours() != null && request.getWaitHours() < 0) {
            addViolation(context, "waitHours", "Thời gian chờ không được là số âm!");
            valid = false;
        }

        // 7. Validate pickupTime
        if (request.getPickupTime() != null && request.getPickupTime().isBefore(LocalDateTime.now().minusHours(2))) {
            addViolation(context, "pickupTime", "Thời gian đón không được trong quá khứ quá 2 giờ!");
            valid = false;
        }

        return valid;
    }

    private void addViolation(ConstraintValidatorContext context, String propertyName, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }
}

