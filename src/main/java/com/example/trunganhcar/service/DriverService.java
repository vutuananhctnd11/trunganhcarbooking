package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.DriverRequest;
import com.example.trunganhcar.dto.DriverResponse;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;

    /**
     * Tạo mới thông tin tài xế
     */
    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        log.info("Tạo tài xế mới: {} - SĐT: {}", request.getFullName(), request.getPhone());

        if (driverRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại này đã được đăng ký cho tài xế khác!");
        }

        if (request.getLicensePlate() != null && !request.getLicensePlate().isBlank()) {
            if (driverRepository.findByLicensePlate(request.getLicensePlate().trim()).isPresent()) {
                throw new IllegalArgumentException("Biển số xe này đã tồn tại trong hệ thống!");
            }
        }

        Driver driver = Driver.builder()
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .experienceYears(request.getExperienceYears())
                .vehicleModel(request.getVehicleModel())
                .seatCount(request.getSeatCount() != null ? request.getSeatCount() : 4)
                .licensePlate(request.getLicensePlate() != null ? request.getLicensePlate().trim().toUpperCase() : null)
                .telegramChatId(request.getTelegramChatId())
                .rating(request.getRating() != null ? request.getRating() : 5.0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Driver saved = driverRepository.save(driver);
        return DriverResponse.fromEntity(saved);
    }

    /**
     * Lấy danh sách tất cả tài xế (có thể lọc theo trạng thái hoạt động)
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers(Boolean isActive) {
        List<Driver> drivers = driverRepository.findAll();
        return drivers.stream()
                .filter(d -> isActive == null || d.getIsActive().equals(isActive))
                .map(DriverResponse::fromEntity)
                .toList();
    }

    /**
     * Lấy chi tiết tài xế theo ID
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long id) {
        Driver driver = findDriverById(id);
        return DriverResponse.fromEntity(driver);
    }

    /**
     * Cập nhật thông tin tài xế
     */
    @Transactional
    public DriverResponse updateDriver(Long id, DriverRequest request) {
        log.info("Cập nhật thông tin tài xế ID: {}", id);
        Driver driver = findDriverById(id);

        if (request.getPhone() != null && !request.getPhone().isBlank() && !request.getPhone().equals(driver.getPhone())) {
            driverRepository.findByPhone(request.getPhone()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Số điện thoại mới đã được sử dụng bởi tài xế khác!");
                }
            });
            driver.setPhone(request.getPhone().trim());
        }

        if (request.getLicensePlate() != null && !request.getLicensePlate().isBlank() && !request.getLicensePlate().equalsIgnoreCase(driver.getLicensePlate())) {
            driverRepository.findByLicensePlate(request.getLicensePlate().trim().toUpperCase()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Biển số xe mới đã tồn tại trên hệ thống!");
                }
            });
            driver.setLicensePlate(request.getLicensePlate().trim().toUpperCase());
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            driver.setFullName(request.getFullName().trim());
        }
        if (request.getExperienceYears() != null) {
            driver.setExperienceYears(request.getExperienceYears());
        }
        if (request.getVehicleModel() != null) {
            driver.setVehicleModel(request.getVehicleModel().trim());
        }
        if (request.getSeatCount() != null) {
            driver.setSeatCount(request.getSeatCount());
        }
        if (request.getTelegramChatId() != null) {
            driver.setTelegramChatId(request.getTelegramChatId().trim());
        }
        if (request.getRating() != null) {
            driver.setRating(request.getRating());
        }
        if (request.getIsActive() != null) {
            driver.setIsActive(request.getIsActive());
        }

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    /**
     * Xóa tài xế theo ID
     */
    @Transactional
    public void deleteDriver(Long id) {
        log.info("Xóa tài xế ID: {}", id);
        Driver driver = findDriverById(id);
        driverRepository.delete(driver);
    }

    /**
     * Tìm entity Driver theo ID (ném exception nếu không tìm thấy)
     */
    @Transactional(readOnly = true)
    public Driver findDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế với ID: " + id));
    }
}

