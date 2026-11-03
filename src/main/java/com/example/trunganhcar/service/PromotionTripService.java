package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.PromotionTripRequest;
import com.example.trunganhcar.dto.PromotionTripResponse;
import com.example.trunganhcar.entity.PromotionTrip;
import com.example.trunganhcar.repository.PromotionTripRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionTripService {

    private final PromotionTripRepository promotionTripRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Tạo mới một gói cước chuyến cố định theo chương trình quảng cáo
     */
    @Transactional
    public PromotionTripResponse createPromotionTrip(PromotionTripRequest request) {
        log.info("Tạo gói chuyến quảng cáo mới: {}", request.getTitle());

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getTitle());

        // Đảm bảo slug là duy nhất
        String finalSlug = slug;
        int count = 1;
        while (promotionTripRepository.existsBySlug(finalSlug)) {
            finalSlug = slug + "-" + count;
            count++;
        }

        String featuresJson = serializeFeatures(request.getFeatures());

        PromotionTrip entity = PromotionTrip.builder()
                .badgeText(request.getBadgeText())
                .title(request.getTitle().trim())
                .slug(finalSlug)
                .description(request.getDescription())
                .dropoffAddress(request.getDropoffAddress() != null ? request.getDropoffAddress().trim() : null)
                .travelTime(request.getTravelTime())
                .price(request.getPrice())
                .priceUnit(request.getPriceUnit())
                .extraKmPrice(request.getExtraKmPrice())
                .extraKmNote(request.getExtraKmNote())
                .waitingFeeNote(request.getWaitingFeeNote())
                .features(featuresJson)
                .buttonText(request.getButtonText() != null ? request.getButtonText() : "Chọn gói cước này")
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        PromotionTrip saved = promotionTripRepository.save(entity);
        return PromotionTripResponse.fromEntity(saved);
    }

    /**
     * Lấy danh sách các gói chuyến cố định có phân trang (Hỗ trợ lọc theo isActive)
     */
    @Transactional(readOnly = true)
    public Page<PromotionTripResponse> getAllPromotionTrips(Boolean isActive, Pageable pageable) {
        Page<PromotionTrip> page;
        if (isActive != null) {
            page = promotionTripRepository.findByIsActive(isActive, pageable);
        } else {
            page = promotionTripRepository.findAll(pageable);
        }
        return page.map(PromotionTripResponse::fromEntity);
    }

    /**
     * Lấy chi tiết gói chuyến theo ID
     */
    @Transactional(readOnly = true)
    public PromotionTripResponse getPromotionTripById(Long id) {
        PromotionTrip entity = findPromotionTripById(id);
        return PromotionTripResponse.fromEntity(entity);
    }

    /**
     * Lấy chi tiết gói chuyến theo Slug
     */
    @Transactional(readOnly = true)
    public PromotionTripResponse getPromotionTripBySlug(String slug) {
        PromotionTrip entity = promotionTripRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói chuyến cố định với slug: " + slug));
        return PromotionTripResponse.fromEntity(entity);
    }

    /**
     * Cập nhật thông tin gói chuyến cố định
     */
    @Transactional
    public PromotionTripResponse updatePromotionTrip(Long id, PromotionTripRequest request) {
        log.info("Cập nhật gói chuyến ID: {}", id);
        PromotionTrip entity = findPromotionTripById(id);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            entity.setTitle(request.getTitle().trim());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = toSlug(request.getSlug());
            if (!newSlug.equals(entity.getSlug()) && promotionTripRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException("Đường dẫn slug này đã tồn tại: " + newSlug);
            }
            entity.setSlug(newSlug);
        }
        if (request.getBadgeText() != null) {
            entity.setBadgeText(request.getBadgeText().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription().trim());
        }
        if (request.getDropoffAddress() != null) {
            entity.setDropoffAddress(request.getDropoffAddress().trim());
        }
        if (request.getTravelTime() != null) {
            entity.setTravelTime(request.getTravelTime());
        }
        if (request.getPrice() != null) {
            entity.setPrice(request.getPrice());
        }
        if (request.getPriceUnit() != null) {
            entity.setPriceUnit(request.getPriceUnit().trim());
        }
        if (request.getExtraKmPrice() != null) {
            entity.setExtraKmPrice(request.getExtraKmPrice());
        }
        if (request.getExtraKmNote() != null) {
            entity.setExtraKmNote(request.getExtraKmNote().trim());
        }
        if (request.getWaitingFeeNote() != null) {
            entity.setWaitingFeeNote(request.getWaitingFeeNote().trim());
        }
        if (request.getFeatures() != null) {
            entity.setFeatures(serializeFeatures(request.getFeatures()));
        }
        if (request.getButtonText() != null) {
            entity.setButtonText(request.getButtonText().trim());
        }
        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsFeatured() != null) {
            entity.setIsFeatured(request.getIsFeatured());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        PromotionTrip updated = promotionTripRepository.save(entity);
        return PromotionTripResponse.fromEntity(updated);
    }

    /**
     * Xóa gói chuyến cố định
     */
    @Transactional
    public void deletePromotionTrip(Long id) {
        log.info("Xóa gói chuyến ID: {}", id);
        PromotionTrip entity = findPromotionTripById(id);
        promotionTripRepository.delete(entity);
    }

    public PromotionTrip findPromotionTripById(Long id) {
        return promotionTripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói chuyến cố định với ID: " + id));
    }

    private String serializeFeatures(Object features) {
        if (features == null) return "[]";
        try {
            return objectMapper.writeValueAsString(features);
        } catch (JsonProcessingException e) {
            log.error("Lỗi parse danh sách features sang JSON: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * Hàm sinh slug tiếng Việt chuẩn SEO
     */
    public static String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        slug = slug.replaceAll("đ", "d").replaceAll("Đ", "D");
        slug = NONLATIN.matcher(slug).replaceAll("");
        slug = slug.replaceAll("-+", "-");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}

