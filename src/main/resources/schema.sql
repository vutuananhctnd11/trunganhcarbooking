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

-- Thêm mã giảm giá mẫu
INSERT INTO discounts (code, title, description, discount_type, discount_value, min_trip_price, is_active)
SELECT 'TRUNGANH50K', 'Giảm ngay 50.000đ', 'Áp dụng cho mọi chuyến đi có giá trị từ 200.000đ trở lên', 'FIXED_AMOUNT', 50000.00, 200000.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM discounts WHERE code = 'TRUNGANH50K');

INSERT INTO discounts (code, title, description, discount_type, discount_value, max_discount_amount, min_trip_price, is_active)
SELECT 'WELCOME10', 'Giảm 10% khách hàng mới', 'Giảm 10% tối đa 100.000đ cho khách hàng đặt xe lần đầu', 'PERCENTAGE', 10.00, 100000.00, 150000.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM discounts WHERE code = 'WELCOME10');
