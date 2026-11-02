package com.example.trunganhcar.repository;

import com.example.trunganhcar.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByCode(String code);
    boolean existsByCode(String code);
}

