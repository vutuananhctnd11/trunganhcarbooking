package com.example.trunganhcar.dto;

import com.example.trunganhcar.entity.Banner;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerResponse {

    private Long id;
    private String title;
    private String tagline;
    private String badge;
    private String image;
    private String alt;
    private String actionText;
    private String actionUrl;
    private Boolean isGraphicBanner;

    private Integer displayOrder;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;

    public static BannerResponse fromEntity(Banner banner) {
        if (banner == null) return null;
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .tagline(banner.getTagline())
                .badge(banner.getBadge())
                .image(banner.getImage())
                .alt(banner.getAlt())
                .actionText(banner.getActionText())
                .actionUrl(banner.getActionUrl())
                .isGraphicBanner(banner.getIsGraphicBanner())
                .displayOrder(banner.getDisplayOrder())
                .status(banner.getStatus())
                .startAt(banner.getStartAt())
                .endAt(banner.getEndAt())
                .build();
    }
}

