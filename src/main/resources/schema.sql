-- ==============================================================================
-- 1. Bảng drivers: Quản lý thông tin tài xế và phương tiện
-- ==============================================================================
CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255),
    experience_years INT DEFAULT 0,
    vehicle_model VARCHAR(100),
    seat_count INT DEFAULT 4,
    license_plate VARCHAR(20) UNIQUE,
    telegram_chat_id VARCHAR(50),
    avatar_url VARCHAR(500),
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
    pickup_coordinates VARCHAR(255) DEFAULT '',
    dropoff_address VARCHAR(255) NOT NULL,
    dropoff_coordinates VARCHAR(255) DEFAULT '',
    distance_km DECIMAL(6, 2),
    estimated_price DECIMAL(12, 2),
    pickup_time DATETIME NOT NULL,
    driver_id BIGINT,
    wait_hours DECIMAL(4, 1) DEFAULT 0.0,
    is_round_trip BOOLEAN NOT NULL DEFAULT FALSE,
    is_fixed_trip BOOLEAN NOT NULL DEFAULT FALSE,
    fixed_trip_title VARCHAR(150),
    promotion_trip_id BIGINT,
    discount_code VARCHAR(50),
    discount_amount DECIMAL(12, 2),
    luggage_option VARCHAR(100),
    note TEXT,
    travel_time BIGINT,
    buffer_minutes BIGINT NOT NULL DEFAULT 30,
    busy_until DATETIME,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_trip_code (code),
    INDEX idx_trip_status (status),
    INDEX idx_trip_driver_id (driver_id),
    INDEX idx_trip_driver_time (driver_id, pickup_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 3. Bảng promotion_trips: Quản lý các chuyến cố định theo chương trình quảng cáo
-- ==============================================================================
CREATE TABLE IF NOT EXISTS promotion_trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge_text VARCHAR(50),
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(255),
    dropoff_address VARCHAR(255),
    travel_time INT,
    price DECIMAL(12, 2) NOT NULL,
    price_unit VARCHAR(100),
    extra_km_price DECIMAL(12, 2),
    extra_km_note VARCHAR(100),
    waiting_fee_note VARCHAR(100),
    features TEXT,
    button_text VARCHAR(100) DEFAULT 'Chọn gói cước này',
    display_order INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_promo_trip_slug (slug),
    INDEX idx_promo_trip_is_active (is_active),
    INDEX idx_promo_trip_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm tài xế mẫu nếu chưa tồn tại (tài xế demo theo ảnh)
INSERT INTO drivers (full_name, phone, password, experience_years, vehicle_model, seat_count, license_plate, is_active)
SELECT 'Trung Anh (Chủ xe)', '0362882882', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 5, 'VinFast VF e34', 5, '30K - 868.86', TRUE
WHERE NOT EXISTS (SELECT 1 FROM drivers WHERE phone = '0362882882');

-- Thêm gói cước chuyến cố định mẫu theo ảnh nếu chưa tồn tại
INSERT INTO promotion_trips (
    badge_text, title, slug, description, dropoff_address, travel_time, price, price_unit, extra_km_price, extra_km_note, 
    waiting_fee_note, features, button_text, display_order, is_featured, is_active
)
SELECT 
    'ĐẶT NHIỀU NHẤT',
    'Đưa Đón Sân Bay Nội Bài',
    'dua-don-san-bay-noi-bai',
    'Chuyến bay kịp giờ, khoang cốp rộng chứa 3-4 vali lớn',
    'Sân bay Quốc tế Nội Bài, Phú Cường, Sóc Sơn, Hà Nội',
    45,
    250000.00,
    '/ Trọn gói từ nội thành',
    11900.00,
    '11.900 đ/km cự ly xa',
    'Miễn phí chờ khi delay',
    '["Theo dõi giờ hạ cánh qua số hiệu chuyến bay", "Tài xế chủ động đến sớm 10 phút hỗ trợ hành lý", "Miễn phí chờ đợi khi chuyến bay bị delay", "Đón trả chuẩn sảnh ga T1 (Quốc nội) & T2 (Quốc tế)"]',
    'Chọn gói cước này',
    1,
    TRUE,
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM promotion_trips WHERE slug = 'dua-don-san-bay-noi-bai');

-- ==============================================================================
-- 4. Bảng banners: Quản lý banner chiến dịch ngoài trang chủ
-- ==============================================================================
CREATE TABLE IF NOT EXISTS banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    tagline TEXT,
    badge VARCHAR(100),
    image TEXT NOT NULL,
    alt VARCHAR(255),
    action_text VARCHAR(100),
    action_url VARCHAR(255),
    is_graphic_banner BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_at DATETIME,
    end_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_banner_status (status),
    INDEX idx_banner_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm 2 banner mẫu
INSERT INTO banners (id, title, tagline, badge, image, alt, action_text, action_url, is_graphic_banner, display_order, status)
SELECT 1, 
       'Trực Ninh ⇄ Nội Bài - Chỉ 900K', 
       'Đặt xe VinFast Limo Green trọn gói 900k/chuyến (Bao trọn xe) • Đón trả tận nơi', 
       'HOT DEAL 900K', 
       '/banner_truc_ninh_noi_bai.png', 
       'Đặt xe VinFast Limo Green từ Trực Ninh đến Nội Bài chỉ 900k Trung Anh Car', 
       'Đặt xe đi sân bay ngay', 
       '/booking?route=truc-ninh-noi-bai', 
       TRUE, 
       1, 
       'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM banners WHERE id = 1);

INSERT INTO banners (id, title, tagline, badge, image, alt, action_text, action_url, is_graphic_banner, display_order, status)
SELECT 2, 
       'Hành Trình Du Lịch & Về Quê Êm Ái', 
       'Không gian tĩnh lặng, không mùi say xe • Cả gia đình thoải mái suốt chuyến đi xa', 
       'GIA ĐÌNH YÊU THÍCH', 
       'https://mediagiaothong.tapchixaydung.vn/mediav2/upload/giaothong/tapchigiaothong.qltns.mediacdn.vn/481400261263945728/2025/3/31/vinfast-limo-green-1743431109130226245032.jpg', 
       'Chiến dịch xe gia đình về quê du lịch', 
       'Xem báo giá đường dài', 
       '/pricing', 
       FALSE, 
       2, 
       'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM banners WHERE id = 2);
