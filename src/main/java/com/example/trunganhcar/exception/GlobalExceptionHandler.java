package com.example.trunganhcar.exception;

import com.example.trunganhcar.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Bắt ngoại lệ CustomException do ứng dụng chủ động ném ra
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        log.warn("CustomException [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ex.getStatus(), ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Bắt ngoại lệ IllegalArgumentException (thường xảy ra khi tham số hoặc kiểm tra dữ liệu không hợp lệ)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = ErrorCode.BAD_REQUEST.getCode();
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("không tìm thấy")) {
            status = HttpStatus.NOT_FOUND;
            errorCode = ErrorCode.NOT_FOUND.getCode();
        }

        ApiResponse<Object> response = ApiResponse.error(status, errorCode, ex.getMessage());
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Bắt ngoại lệ IllegalStateException (ví dụ vi phạm luồng chuyển trạng thái cuốc xe)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("IllegalStateException: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_TRIP_STATUS.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt ngoại lệ kiểm tra tính hợp lệ dữ liệu Validation (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Dữ liệu đầu vào không hợp lệ: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .errorCode(ErrorCode.BAD_REQUEST.getCode())
                .message("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại!")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi thiếu tham số @RequestParam bắt buộc
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        String msg = String.format("Thiếu tham số bắt buộc trong yêu cầu: '%s'", ex.getParameterName());
        log.warn("Missing parameter: {}", msg);
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST.getCode(), msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi sai kiểu dữ liệu tham số (vd truyền text vào Long id)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("Tham số '%s' có giá trị không đúng định dạng", ex.getName());
        log.warn("Type mismatch: {}", msg);
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST.getCode(), msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi phương thức HTTP không được hỗ trợ (VD gọi GET vào POST endpoint)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String msg = String.format("Phương thức HTTP '%s' không được hỗ trợ cho đường dẫn này", ex.getMethod());
        log.warn("Method not supported: {}", msg);
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getCode(), msg);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Bắt lỗi không tìm thấy tài nguyên endpoint URL
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("No resource found: {}", ex.getResourcePath());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND.getCode(), "Đường dẫn tài nguyên không tồn tại: " + ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Bắt lỗi phân quyền Spring Security AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Truy cập bị từ chối: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN.getCode(), "Bạn không có quyền thực hiện thao tác này!");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Bắt lỗi xác thực Spring Security AuthenticationException
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Lỗi xác thực: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getCode(), "Xác thực không thành công hoặc phiên đăng nhập đã hết hạn!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Bắt tất cả các Exception không mong muốn khác (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllUncaughtException(Exception ex) {
        log.error("Lỗi hệ thống không xử lý được (Internal Server Error): ", ex);
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "Đã xảy ra lỗi ngoài ý muốn trên hệ thống. Vui lòng thử lại sau!"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

