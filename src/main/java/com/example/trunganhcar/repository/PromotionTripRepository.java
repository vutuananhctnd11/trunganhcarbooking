package com.example.trunganhcar.repository;

import com.example.trunganhcar.entity.PromotionTrip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionTripRepository extends JpaRepository<PromotionTrip, Long> {

    Optional<PromotionTrip> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<PromotionTrip> findByIsActive(Boolean isActive, Pageable pageable);
}

