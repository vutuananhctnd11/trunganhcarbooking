package com.example.trunganhcar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers", indexes = {
        @Index(name = "idx_driver_phone", columnList = "phone", unique = true),
        @Index(name = "idx_driver_license_plate", columnList = "license_plate", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    /**
     * Mật khẩu đăng nhập (mã hóa BCrypt)
     */
    @Column(length = 255)
    private String password;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "vehicle_model", length = 100)
    private String vehicleModel;

    @Column(name = "seat_count")
    private Integer seatCount;

    @Column(name = "license_plate", length = 20, unique = true)
    private String licensePlate;

    @Column(name = "telegram_chat_id", length = 50)
    private String telegramChatId;

    /**
     * Đường dẫn ảnh đại diện / chân dung của tài xế
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 5.0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
}

