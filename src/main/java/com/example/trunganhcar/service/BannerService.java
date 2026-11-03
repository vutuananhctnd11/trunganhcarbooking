package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.BannerRequest;
import com.example.trunganhcar.dto.BannerResponse;
import com.example.trunganhcar.entity.Banner;
import com.example.trunganhcar.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {

    private final BannerRepository bannerRepository;

    /**
     * Lấy danh sách banner đang hoạt động (dành cho Frontend trang chủ)
     */
    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBanners() {
        LocalDateTime now = LocalDateTime.now();
        List<Banner> banners = bannerRepository.findActiveBanners(now);
        return banners.stream()
                .map(BannerResponse::fromEntity)
                .toList();
    }

    /**
     * Lấy danh sách toàn bộ banner có phân trang (dành cho trang Quản trị)
     */
    @Transactional(readOnly = true)
    public Page<BannerResponse> getAllBanners(String status, Pageable pageable) {
        Page<Banner> page;
        if (status != null && !status.isBlank()) {
            page = bannerRepository.findByStatus(status.trim().toUpperCase(), pageable);
        } else {
            page = bannerRepository.findAll(pageable);
        }
        return page.map(BannerResponse::fromEntity);
    }

    /**
     * Lấy chi tiết banner theo ID
     */
    @Transactional(readOnly = true)
    public BannerResponse getBannerById(Long id) {
        Banner banner = findBannerById(id);
        return BannerResponse.fromEntity(banner);
    }

    /**
     * Tạo mới banner
     */
    @Transactional
    public BannerResponse createBanner(BannerRequest request) {
        log.info("Tạo mới banner: {}", request.getTitle());

        Banner banner = Banner.builder()
                .title(request.getTitle().trim())
                .tagline(request.getTagline())
                .badge(request.getBadge())
                .image(request.getImage().trim())
                .alt(request.getAlt())
                .actionText(request.getActionText())
                .actionUrl(request.getActionUrl())
                .isGraphicBanner(Boolean.TRUE.equals(request.getIsGraphicBanner()))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .status(request.getStatus() != null && !request.getStatus().isBlank() 
                        ? request.getStatus().trim().toUpperCase() : "ACTIVE")
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();

        Banner saved = bannerRepository.save(banner);
        return BannerResponse.fromEntity(saved);
    }

    /**
     * Cập nhật thông tin banner
     */
    @Transactional
    public BannerResponse updateBanner(Long id, BannerRequest request) {
        log.info("Cập nhật banner ID: {}", id);
        Banner banner = findBannerById(id);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            banner.setTitle(request.getTitle().trim());
        }
        if (request.getTagline() != null) {
            banner.setTagline(request.getTagline());
        }
        if (request.getBadge() != null) {
            banner.setBadge(request.getBadge());
        }
        if (request.getImage() != null && !request.getImage().isBlank()) {
            banner.setImage(request.getImage().trim());
        }
        if (request.getAlt() != null) {
            banner.setAlt(request.getAlt());
        }
        if (request.getActionText() != null) {
            banner.setActionText(request.getActionText());
        }
        if (request.getActionUrl() != null) {
            banner.setActionUrl(request.getActionUrl());
        }
        if (request.getIsGraphicBanner() != null) {
            banner.setIsGraphicBanner(request.getIsGraphicBanner());
        }
        if (request.getDisplayOrder() != null) {
            banner.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            banner.setStatus(request.getStatus().trim().toUpperCase());
        }
        if (request.getStartAt() != null) {
            banner.setStartAt(request.getStartAt());
        }
        if (request.getEndAt() != null) {
            banner.setEndAt(request.getEndAt());
        }

        Banner updated = bannerRepository.save(banner);
        return BannerResponse.fromEntity(updated);
    }

    /**
     * Xóa banner theo ID
     */
    @Transactional
    public void deleteBanner(Long id) {
        log.info("Xóa banner ID: {}", id);
        Banner banner = findBannerById(id);
        bannerRepository.delete(banner);
    }

    public Banner findBannerById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner với ID: " + id));
    }
}

