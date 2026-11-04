package com.example.trunganhcar.annotation;

import com.example.trunganhcar.validation.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom annotation kiểm tra số điện thoại Việt Nam hợp lệ
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Số điện thoại không hợp lệ (cần đúng định dạng số di động Việt Nam, ví dụ: 0988123456)!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

