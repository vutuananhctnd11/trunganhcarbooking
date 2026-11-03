package com.example.trunganhcar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_trips", indexes = {
        @Index(name = "idx_promo_trip_slug", columnList = "slug", unique = true),
        @Index(name = "idx_promo_trip_is_active", columnList = "is_active"),
        @Index(name = "idx_promo_trip_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nhãn nổi bật (Badge) phía trên thẻ (VD: "ĐẶT NHIỀU NHẤT", "HOT", "TIẾT KIỆM")
     */
    @Column(name = "badge_text", length = 50)
    private String badgeText;

    /**
     * Tên gói chuyến cố định (VD: "Đưa Đón Sân Bay Nội Bài")
     */
    @Column(nullable = false, length = 150)
    private String title;

    /**
     * Đường dẫn thân thiện (Slug duy nhất, VD: "dua-don-san-bay-noi-bai")
     */
    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    /**
     * Mô tả ngắn (VD: "Chuyến bay kịp giờ, khoang cốp rộng chứa 3-4 vali lớn")
     */
    @Column(length = 255)
    private String description;

    /**
     * Địa chỉ trả khách cố định (VD: "Sân bay Quốc tế Nội Bài, Phú Cường, Sóc Sơn, Hà Nội")
     */
    @Column(name = "dropoff_address", length = 255)
    private String dropoffAddress;

    /**
     * Thời gian di chuyển dự kiến (tính bằng phút) để đồng bộ khi đặt lịch
     */
    @Column(name = "travel_time")
    private Integer travelTime;

    /**
     * Giá trọn gói hiển thị (VD: 250000)
     */
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Đơn vị / Ghi chú kèm theo giá (VD: "/ Trọn gói từ nội thành")
     */
    @Column(name = "price_unit", length = 100)
    private String priceUnit;

    /**
     * Đơn giá mỗi km tiếp theo khi vượt cự ly (VD: 11900)
     */
    @Column(name = "extra_km_price", precision = 12, scale = 2)
    private BigDecimal extraKmPrice;

    /**
     * Ghi chú cho cước phí km tiếp theo (VD: "11.900 đ/km cự ly xa")
     */
    @Column(name = "extra_km_note", length = 100)
    private String extraKmNote;

    /**
     * Chính sách / ghi chú phí thời gian chờ (VD: "Miễn phí chờ khi delay")
     */
    @Column(name = "waiting_fee_note", length = 100)
    private String waitingFeeNote;

    /**
     * Danh sách các quyền lợi / ưu đãi nổi bật, lưu dưới dạng JSON String (không dùng annotation quan hệ)
     * VD: ["Theo dõi giờ hạ cánh qua số hiệu chuyến bay", "Tài xế chủ động đến sớm 10 phút hỗ trợ hành lý", ...]
     */
    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    /**
     * Chữ hiển thị trên nút bấm (VD: "Chọn gói cước này")
     */
    @Column(name = "button_text", length = 100)
    @Builder.Default
    private String buttonText = "Chọn gói cước này";

    /**
     * Thứ tự sắp xếp hiển thị ngoài trang chủ (số nhỏ hơn ưu tiên đứng trước)
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Đánh dấu gói nổi bật
     */
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    /**
     * Trạng thái hiển thị (true: đang chạy quảng cáo, false: ẩn)
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

