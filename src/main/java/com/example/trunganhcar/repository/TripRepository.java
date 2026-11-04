package com.example.trunganhcar.repository;

import com.example.trunganhcar.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByCode(String code);
    boolean existsByCode(String code);

    /**
     * Tìm danh sách các chuyến xe của tài xế trong ngày (không tính chuyến đã CANCELLED)
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.driverId = :driverId 
          AND t.status <> com.example.trunganhcar.enums.TripStatus.CANCELLED
          AND (
              (t.pickupTime >= :startOfDay AND t.pickupTime <= :endOfDay)
              OR (t.busyUntil >= :startOfDay AND t.busyUntil <= :endOfDay)
              OR (t.pickupTime <= :startOfDay AND t.busyUntil >= :endOfDay)
          )
        ORDER BY t.pickupTime ASC
    """)
    List<Trip> findBusyTripsForDriver(
            @Param("driverId") Long driverId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Tìm danh sách các chuyến xe của tài xế trong một khoảng thời gian (không tính chuyến đã CANCELLED)
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.driverId = :driverId 
          AND t.status <> com.example.trunganhcar.enums.TripStatus.CANCELLED
          AND t.pickupTime >= :startDateTime AND t.pickupTime <= :endDateTime
        ORDER BY t.pickupTime ASC
    """)
    List<Trip> findTripsForDriverBetween(
            @Param("driverId") Long driverId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.status = com.example.trunganhcar.enums.TripStatus.CONFIRMED 
          AND t.pickupTime >= :fromTime 
          AND t.pickupTime <= :toTime
    """)
    List<Trip> findTripsNearPickupTime(
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query("""
        SELECT t FROM Trip t 
        WHERE t.status IN (com.example.trunganhcar.enums.TripStatus.ARRIVING, com.example.trunganhcar.enums.TripStatus.IN_TRIP, com.example.trunganhcar.enums.TripStatus.CONFIRMED)
          AND t.busyUntil IS NOT NULL 
          AND t.busyUntil <= :now
    """)
    List<Trip> findTripsPassedBusyUntil(
            @Param("now") LocalDateTime now
    );
}
