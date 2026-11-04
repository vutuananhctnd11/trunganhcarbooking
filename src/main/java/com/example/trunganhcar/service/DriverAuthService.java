package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.DriverAuthResponse;
import com.example.trunganhcar.dto.DriverLoginRequest;
import com.example.trunganhcar.dto.DriverRegisterRequest;
import com.example.trunganhcar.dto.DriverResponse;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.exception.ErrorCode;
import com.example.trunganhcar.repository.DriverRepository;
import com.example.trunganhcar.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverAuthService {

    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String DEMO_PHONE = "0362882882";

    /**
     * Đăng ký tài xế mới (người dùng tự nhập mật khẩu)
     */
    @Transactional
    public DriverAuthResponse register(DriverRegisterRequest request) {
        log.info("Xử lý đăng ký tài xế mới: {} - SĐT: {}", request.getFullName(), request.getPhone());

        String cleanPhone = request.getPhone().trim().replaceAll("[^0-9]", "");
        String cleanPlate = com.example.trunganhcar.util.LicensePlateUtils.formatLicensePlate(request.getLicensePlate());

        if (cleanPhone.isBlank()) {
            throw new CustomException("Số điện thoại không được để trống!");
        }

        if (driverRepository.findByPhone(cleanPhone).isPresent()) {
            throw new CustomException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        if (driverRepository.findByLicensePlate(cleanPlate).isPresent()) {
            throw new CustomException(ErrorCode.LICENSE_PLATE_ALREADY_EXISTS);
        }

        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            throw new CustomException("Mật khẩu phải có tối thiểu 6 ký tự!");
        }

        // Tự động xác định số chỗ ngồi theo dòng xe
        int seatCount = 5;
        String modelLower = request.getVehicleModel().toLowerCase();
        if (modelLower.contains("vf 9") || modelLower.contains("7 chỗ") || modelLower.contains("limo")) {
            seatCount = 7;
        }

        Driver driver = Driver.builder()
                .fullName(request.getFullName().trim())
                .phone(cleanPhone)
                .licensePlate(cleanPlate)
                .vehicleModel(request.getVehicleModel().trim())
                .vehicleColor(request.getVehicleColor() != null ? request.getVehicleColor().trim() : "🔴 Đỏ Crimson (Đỏ đô ánh kim)")
                .seatCount(seatCount)
                .password(passwordEncoder.encode(request.getPassword().trim()))
                .experienceYears(1)
                .rating(5.0)
                .isActive(true)
                .build();

        Driver saved = driverRepository.save(driver);
        log.info("Đăng ký tài xế thành công ID: {}", saved.getId());

        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getPhone(), saved.getFullName());

        return DriverAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .driver(DriverResponse.fromEntity(saved))
                .build();
    }

    /**
     * Đăng nhập tài xế bằng Số điện thoại và Mật khẩu
     */
    @Transactional(readOnly = true)
    public DriverAuthResponse login(DriverLoginRequest request) {
        String cleanPhone = request.getPhone().trim().replaceAll("[^0-9]", "");
        log.info("Xử lý đăng nhập tài xế SĐT: {}", cleanPhone);

        Driver driver = driverRepository.findByPhone(cleanPhone)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (driver.getPassword() == null || !passwordEncoder.matches(request.getPassword(), driver.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtTokenProvider.generateToken(driver.getId(), driver.getPhone(), driver.getFullName());

        return DriverAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .driver(DriverResponse.fromEntity(driver))
                .build();
    }

    /**
     * Đăng nhập nhanh tài xế mẫu (Nút "Vào xem ngay" trên UI demo)
     */
    @Transactional
    public DriverAuthResponse loginDemoDriver() {
        log.info("Xử lý đăng nhập nhanh tài xế mẫu Trung Anh (Chủ xe)...");

        Driver driver = driverRepository.findByPhone(DEMO_PHONE).orElseGet(() -> {
            Driver newDemo = Driver.builder()
                    .fullName("Trung Anh (Chủ xe)")
                    .phone(DEMO_PHONE)
                    .password(passwordEncoder.encode("123456"))
                    .vehicleModel("VinFast VF e34")
                    .seatCount(5)
                    .licensePlate("30K - 868.86")
                    .experienceYears(5)
                    .rating(5.0)
                    .isActive(true)
                    .build();
            return driverRepository.save(newDemo);
        });

        String token = jwtTokenProvider.generateToken(driver.getId(), driver.getPhone(), driver.getFullName());

        return DriverAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .driver(DriverResponse.fromEntity(driver))
                .build();
    }
}
