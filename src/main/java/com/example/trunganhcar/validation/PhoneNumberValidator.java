package com.example.trunganhcar.validation;

import com.example.trunganhcar.annotation.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    // Regex số điện thoại di động Việt Nam: 10 chữ số bắt đầu bằng 03, 05, 07, 08, 09 (hoặc +84)
    private static final Pattern VIETNAMESE_PHONE_PATTERN = Pattern.compile("^(?:\\+84|0)(?:3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String cleanPhone = value.trim().replaceAll("[\\s\\-\\.]", "");
        return VIETNAMESE_PHONE_PATTERN.matcher(cleanPhone).matches();
    }
}

