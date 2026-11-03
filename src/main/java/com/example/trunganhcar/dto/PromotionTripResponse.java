package com.example.trunganhcar.dto;

import com.example.trunganhcar.entity.PromotionTrip;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PromotionTripResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Long id;
    private String badgeText;
    private String title;
    private String slug;
    private String description;
    private String dropoffAddress;
    private Integer travelTime;
    private BigDecimal price;
    private String priceUnit;
    private BigDecimal extraKmPrice;
    private String extraKmNote;
    private String waitingFeeNote;
    private List<String> features;
    private String buttonText;
    private Integer displayOrder;
    private Boolean isFeatured;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static PromotionTripResponse fromEntity(PromotionTrip entity) {
        if (entity == null) return null;

        List<String> featureList = new ArrayList<>();
        if (entity.getFeatures() != null && !entity.getFeatures().isBlank()) {
            try {
                featureList = OBJECT_MAPPER.readValue(entity.getFeatures(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Không thể parse features JSON cho PromotionTrip ID {}: {}", entity.getId(), e.getMessage());
                // Fallback nếu chuỗi không phải JSON thì phân tách theo dòng
                featureList = List.of(entity.getFeatures().split("\\r?\\n"));
            }
        }

        return PromotionTripResponse.builder()
                .id(entity.getId())
                .badgeText(entity.getBadgeText())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .dropoffAddress(entity.getDropoffAddress())
                .travelTime(entity.getTravelTime())
                .price(entity.getPrice())
                .priceUnit(entity.getPriceUnit())
                .extraKmPrice(entity.getExtraKmPrice())
                .extraKmNote(entity.getExtraKmNote())
                .waitingFeeNote(entity.getWaitingFeeNote())
                .features(featureList)
                .buttonText(entity.getButtonText())
                .displayOrder(entity.getDisplayOrder())
                .isFeatured(entity.getIsFeatured())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

