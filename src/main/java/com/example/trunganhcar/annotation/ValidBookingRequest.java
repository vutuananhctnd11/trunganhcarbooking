package com.example.trunganhcar.annotation;

import com.example.trunganhcar.validation.BookingRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom annotation kiểm tra tính hợp lệ toàn diện của yêu cầu đặt xe
 */
@Documented
@Constraint(validatedBy = BookingRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingRequest {

    String message() default "Thông tin đặt xe không hợp lệ, vui lòng kiểm tra lại!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

