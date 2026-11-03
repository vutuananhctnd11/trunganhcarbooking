package com.example.trunganhcar.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Lỗi hệ thống chung
    SUCCESS(HttpStatus.OK, "Thao tác thành công"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa xác thực hoặc không có quyền truy cập"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu yêu cầu"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức HTTP không được hỗ trợ"),
    CONFLICT(HttpStatus.CONFLICT, "Dữ liệu bị trùng lặp hoặc xảy ra xung đột"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau"),

    // Nghiệp vụ Đặt xe & Chuyến đi (Trip)
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin chuyến đi với mã cung cấp"),
    INVALID_TRIP_STATUS(HttpStatus.BAD_REQUEST, "Không thể chuyển trạng thái chuyến đi do vi phạm quy trình"),

    // Nghiệp vụ Tài xế (Driver) & Auth
    DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tài xế"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Số điện thoại này đã được đăng ký tài xế trong hệ thống"),
    LICENSE_PLATE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Biển số xe này đã tồn tại trong hệ thống"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Số điện thoại hoặc mật khẩu không chính xác"),
    DRIVER_INACTIVE(HttpStatus.FORBIDDEN, "Tài khoản tài xế hiện đang bị khóa hoặc ngưng hoạt động"),

    // Nghiệp vụ Banner quảng cáo
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin banner"),

    // Nghiệp vụ Gói cước chuyến cố định (PromotionTrip)
    PROMOTION_TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin gói cước chuyến cố định"),

    // Nghiệp vụ Mã giảm giá (Discount / Coupon)
    DISCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá"),
    DISCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Mã giảm giá này đã tồn tại trong hệ thống"),
    DISCOUNT_EXPIRED(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết hạn sử dụng"),
    DISCOUNT_NOT_STARTED(HttpStatus.BAD_REQUEST, "Mã giảm giá chưa đến thời gian áp dụng"),
    DISCOUNT_USAGE_EXCEEDED(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết lượt sử dụng"),
    DISCOUNT_MIN_PRICE_NOT_MET(HttpStatus.BAD_REQUEST, "Giá trị chuyến đi chưa đạt mức tối thiểu để áp dụng mã"),
    DISCOUNT_INACTIVE(HttpStatus.BAD_REQUEST, "Mã giảm giá hiện đang bị tạm dừng kích hoạt");

    private final HttpStatus httpStatus;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
