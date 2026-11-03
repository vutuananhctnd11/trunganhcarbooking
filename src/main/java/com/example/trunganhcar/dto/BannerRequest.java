package com.example.trunganhcar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerRequest {

    @Schema(description = "Tiêu đề banner", example = "Trực Ninh ⇄ Nội Bài - Chỉ 900K", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Khẩu hiệu / mô tả phụ", example = "Đặt xe VinFast Limo Green trọn gói 900k/chuyến (Bao trọn xe) • Đón trả tận nơi")
    private String tagline;

    @Schema(description = "Huy hiệu nổi bật (Badge)", example = "HOT DEAL 900K")
    private String badge;

    @Schema(description = "Đường dẫn hình ảnh", example = "/banner_truc_ninh_noi_bai.png", requiredMode = Schema.RequiredMode.REQUIRED)
    private String image;

    @Schema(description = "Thẻ Alt mô tả ảnh", example = "Đặt xe VinFast Limo Green từ Trực Ninh đến Nội Bài chỉ 900k Trung Anh Car")
    private String alt;

    @Schema(description = "Nút bấm hành động", example = "Đặt xe đi sân bay ngay")
    private String actionText;

    @Schema(description = "Đường dẫn liên kết khi bấm vào banner", example = "/booking?route=truc-ninh-noi-bai")
    private String actionUrl;

    @Schema(description = "Đánh dấu là graphic banner nổi bật", example = "true")
    private Boolean isGraphicBanner;

    @Schema(description = "Thứ tự sắp xếp hiển thị", example = "1")
    private Integer displayOrder;

    @Schema(description = "Trạng thái (ACTIVE / INACTIVE)", example = "ACTIVE")
    private String status;

    @Schema(description = "Thời gian bắt đầu hiển thị", example = "2026-11-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "Thời gian kết thúc hiển thị", example = "2026-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;
}

