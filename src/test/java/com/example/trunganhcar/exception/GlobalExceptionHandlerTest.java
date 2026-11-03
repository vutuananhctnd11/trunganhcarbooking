package com.example.trunganhcar.exception;

import com.example.trunganhcar.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Bắt CustomException và trả về ApiResponse đúng status và message")
    void handleCustomException_shouldReturnCorrectResponse() {
        CustomException ex = new CustomException(ErrorCode.TRIP_NOT_FOUND, "Không tìm thấy chuyến đi mã TEST12345");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleCustomException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertFalse(response.getBody().isSuccess());
        assertEquals("TRIP_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Không tìm thấy chuyến đi mã TEST12345", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Bắt IllegalArgumentException có chứa 'không tìm thấy' trả về 404 NOT_FOUND")
    void handleIllegalArgumentException_notFound_shouldReturn404() {
        IllegalArgumentException ex = new IllegalArgumentException("Không tìm thấy tài xế với ID: 99");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Không tìm thấy tài xế với ID: 99", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Bắt IllegalArgumentException thông thường trả về 400 BAD_REQUEST")
    void handleIllegalArgumentException_badRequest_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Số điện thoại không hợp lệ");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("BAD_REQUEST", response.getBody().getErrorCode());
        assertEquals("Số điện thoại không hợp lệ", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Bắt IllegalStateException trả về 400 BAD_REQUEST với INVALID_TRIP_STATUS")
    void handleIllegalStateException_shouldReturnBadRequest() {
        IllegalStateException ex = new IllegalStateException("Không thể chuyển trạng thái từ COMPLETED sang CONFIRMED");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalStateException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("INVALID_TRIP_STATUS", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Bắt AccessDeniedException trả về 403 FORBIDDEN")
    void handleAccessDeniedException_shouldReturnForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAccessDenied(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("FORBIDDEN", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Bắt AuthenticationException trả về 401 UNAUTHORIZED")
    void handleAuthenticationException_shouldReturnUnauthorized() {
        AuthenticationException ex = new AuthenticationException("Bad credentials") {};

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAuthenticationException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("UNAUTHORIZED", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Bắt Exception chưa xác định trả về 500 INTERNAL_SERVER_ERROR")
    void handleAllUncaughtException_shouldReturn500() {
        Exception ex = new NullPointerException("Null pointer reference");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
    }
}

