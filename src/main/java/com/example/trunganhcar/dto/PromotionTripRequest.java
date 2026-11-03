package com.example.trunganhcar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTripRequest {

    @Schema(description = "Nhãn nổi bật", example = "ĐẶT NHIỀU NHẤT")
    private String badgeText;

    @Schema(description = "Tên gói cước chuyến cố định", example = "Đưa Đón Sân Bay Nội Bài", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Đường dẫn định danh slug (để trống sẽ tự sinh từ title)", example = "dua-don-san-bay-noi-bai")
    private String slug;

    @Schema(description = "Mô tả ngắn", example = "Chuyến bay kịp giờ, khoang cốp rộng chứa 3-4 vali lớn")
    private String description;

    @Schema(description = "Địa chỉ trả khách cố định", example = "Sân bay Quốc tế Nội Bài, Phú Cường, Sóc Sơn, Hà Nội")
    private String dropoffAddress;

    @Schema(description = "Thời gian di chuyển dự kiến (phút)", example = "45")
    private Integer travelTime;

    @Schema(description = "Giá gói trọn gói", example = "250000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "Đơn vị hoặc ghi chú kèm theo giá", example = "/ Trọn gói từ nội thành")
    private String priceUnit;

    @Schema(description = "Đơn giá cự ly vượt km tiếp theo", example = "11900")
    private BigDecimal extraKmPrice;

    @Schema(description = "Ghi chú cự ly vượt km", example = "11.900 đ/km cự ly xa")
    private String extraKmNote;

    @Schema(description = "Ghi chú phí chờ", example = "Miễn phí chờ khi delay")
    private String waitingFeeNote;

    @Schema(description = "Danh sách quyền lợi / ưu đãi", example = "[\"Theo dõi giờ hạ cánh qua số hiệu chuyến bay\", \"Tài xế chủ động đến sớm 10 phút hỗ trợ hành lý\", \"Miễn phí chờ đợi khi chuyến bay bị delay\", \"Đón trả chuẩn sảnh ga T1 (Quốc nội) & T2 (Quốc tế)\"]")
    private List<String> features;

    @Schema(description = "Nhãn nút bấm", example = "Chọn gói cước này")
    private String buttonText;

    @Schema(description = "Thứ tự sắp xếp hiển thị", example = "1")
    private Integer displayOrder;

    @Schema(description = "Đánh dấu nổi bật", example = "true")
    private Boolean isFeatured;

    @Schema(description = "Trạng thái hiển thị", example = "true")
    private Boolean isActive;
}

