package com.example.trunganhcar.repository;

import com.example.trunganhcar.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByPhone(String phone);
    Optional<Driver> findByTelegramChatId(String telegramChatId);
    Optional<Driver> findByLicensePlate(String licensePlate);
}

