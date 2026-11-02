-- ==============================================================================
-- 1. Bảng drivers: Quản lý thông tin tài xế và phương tiện
-- ==============================================================================
CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    experience_years INT DEFAULT 0,
    vehicle_model VARCHAR(100),
    seat_count INT DEFAULT 4,
    license_plate VARCHAR(20) UNIQUE,
    telegram_chat_id VARCHAR(50),
    rating DECIMAL(2, 1) DEFAULT 5.0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_driver_phone (phone),
    INDEX idx_driver_license_plate (license_plate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 2. Bảng trips: Quản lý đơn đặt xe Trung Anh Car
-- ==============================================================================
CREATE TABLE IF NOT EXISTS trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    dropoff_address VARCHAR(255) NOT NULL,
    distance_km DECIMAL(6, 2),
    estimated_price DECIMAL(12, 2),
    pickup_time DATETIME NOT NULL,
    driver_id BIGINT,
    wait_hours DECIMAL(4, 1) DEFAULT 0.0,
    is_round_trip BOOLEAN NOT NULL DEFAULT FALSE,
    luggage_option VARCHAR(100),
    note TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_trip_code (code),
    INDEX idx_trip_status (status),
    INDEX idx_trip_driver_id (driver_id),
    CONSTRAINT fk_trips_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm tài xế mẫu nếu chưa tồn tại
INSERT INTO drivers (full_name, phone, experience_years, vehicle_model, seat_count, license_plate, is_active)
SELECT 'Nguyễn Trung Anh', '0912345678', 5, 'Toyota Fortuner 2022', 7, '30H-888.88', TRUE
WHERE NOT EXISTS (SELECT 1 FROM drivers WHERE phone = '0912345678');
