package com.example.trunganhcar.repository;

import com.example.trunganhcar.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * Lấy danh sách banner đang hoạt động theo thời gian hiện tại
     */
    @Query("""
        SELECT b FROM Banner b 
        WHERE b.status = 'ACTIVE' 
          AND (b.startAt IS NULL OR b.startAt <= :now)
          AND (b.endAt IS NULL OR b.endAt >= :now)
        ORDER BY b.displayOrder ASC, b.id DESC
    """)
    List<Banner> findActiveBanners(@Param("now") LocalDateTime now);

    Page<Banner> findByStatus(String status, Pageable pageable);
}

