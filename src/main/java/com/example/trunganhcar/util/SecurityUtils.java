package com.example.trunganhcar.util;

import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Lấy ID của tài xế đang đăng nhập từ Security Context
     */
    public static Long getCurrentDriverId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Vui lòng đăng nhập tài xế để thực hiện thao tác này!");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long driverId) {
            return driverId;
        } else if (principal instanceof String principalStr) {
            try {
                return Long.parseLong(principalStr);
            } catch (NumberFormatException e) {
                throw new CustomException(ErrorCode.UNAUTHORIZED, "Thông tin xác thực không hợp lệ!");
            }
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED, "Không tìm thấy thông tin tài xế đang đăng nhập!");
    }
}

