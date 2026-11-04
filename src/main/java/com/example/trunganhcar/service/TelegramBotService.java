package com.example.trunganhcar.service;

import com.example.trunganhcar.config.TelegramProperties;
import com.example.trunganhcar.dto.telegram.TelegramDto.*;
import com.example.trunganhcar.entity.Discount;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.repository.DiscountRepository;
import com.example.trunganhcar.repository.DriverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class TelegramBotService {

    private final TelegramProperties properties;
    private final RestClient restClient;
    private final TripService tripService;
    private final DriverRepository driverRepository;
    private final DiscountRepository discountRepository;
    private final SseService sseService;
    private final WebPushService webPushService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm • dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getInstance(new Locale("vi", "VN"));

    public TelegramBotService(TelegramProperties properties,
                              @Lazy TripService tripService,
                              DriverRepository driverRepository,
                              DiscountRepository discountRepository,
                              SseService sseService,
                              WebPushService webPushService) {
        this.properties = properties;
        this.tripService = tripService;
        this.driverRepository = driverRepository;
        this.discountRepository = discountRepository;
        this.sseService = sseService;
        this.webPushService = webPushService;
        this.restClient = RestClient.builder().build();
    }

    /**
     * 1. Gửi tin nhắn thông báo cuốc xe mới về Telegram tài xế kèm Inline Keyboard
     */
    public void sendNewTripNotification(Trip trip) {
        if (properties.getBotToken().isBlank() || properties.getDriverChatId().isBlank()) {
            log.warn("Chưa cấu hình telegram.bot-token hoặc telegram.driver-chat-id. Bỏ qua gửi tin nhắn Telegram.");
            return;
        }

        String messageText = buildNewTripNotificationText(trip);
        InlineKeyboardMarkup keyboardMarkup = buildPendingInlineKeyboard(trip);

        SendMessageRequest request = SendMessageRequest.builder()
                .chatId(properties.getDriverChatId())
                .text(messageText)
                .parseMode("HTML")
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            String url = String.format("%s/bot%s/sendMessage", properties.getApiUrl(), properties.getBotToken());
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Đã gửi tin thông báo cuốc xe mới mã [{}] tới Telegram tài xế.", trip.getCode());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo chuyến đi tới Telegram: {}", e.getMessage(), e);
        }
    }

    /**
     * 2. Xử lý CallbackQuery khi tài xế bấm các nút trên Telegram
     */
    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        if (callbackData == null || !callbackData.contains("_")) {
            return;
        }

        String[] parts = callbackData.split("_", 2);
        String action = parts[0];
        String tripCode = parts[1];

        Integer messageId = callbackQuery.getMessage() != null ? callbackQuery.getMessage().getMessageId() : null;
        Long chatId = callbackQuery.getMessage() != null && callbackQuery.getMessage().getChat() != null
                ? callbackQuery.getMessage().getChat().getId() : null;

        String answerAlertText;
        boolean showAlert = false;

        try {
            switch (action) {
                case "CALL" -> {
                    answerAlertText = "📞 SĐT Khách hàng: " + tripCode + "\n\n(Bấm trực tiếp vào số điện thoại trong tin nhắn để gọi ngay)";
                    showAlert = true;
                }
                case "ACCEPT" -> {
                    Trip trip = tripService.acceptTripByDriver(tripCode, chatId != null ? chatId.toString() : null, "Tài xế đã nhận cuốc xe");
                    answerAlertText = "✅ Đã xác nhận nhận chuyến: " + tripCode;
                    Driver driver = trip.getDriverId() != null ? driverRepository.findById(trip.getDriverId()).orElse(null) : null;
                    String driverName = driver != null ? driver.getFullName() : "Trung Anh";
                    if (messageId != null && chatId != null) {
                        editTelegramMessage(chatId, messageId, buildAcceptedTripShortText(trip), buildAcceptedInlineKeyboard(trip, driverName));
                    }
                    sendWebPushToCustomer(trip, "Tài xế đã nhận chuyến!", "Tài xế " + driverName + " đã tiếp nhận chuyến xe #" + tripCode + ". Bấm vào đây để theo dõi trạng thái chuyến đi!.");
                }
                case "RENOTIFY", "RENOTIFY_CUSTOMER" -> {
                    Trip trip = tripService.findTripByCode(tripCode);
                    Driver driver = trip.getDriverId() != null ? driverRepository.findById(trip.getDriverId()).orElse(null) : null;
                    String driverName = driver != null ? driver.getFullName() : "Trung Anh";
                    sendWebPushToCustomer(trip, "Thông báo từ tài xế " + driverName, "Tài xế đã gửi lại thông tin xác nhận cho chuyến đi #" + tripCode + ". Vui lòng kiểm tra lộ trình.");
                    answerAlertText = "📢 Đã gửi lại thông báo Web Push kèm link tra cứu đến khách hàng!";
                }
                case "PUSH_COMPLETE", "NOTIFY_PUSH" -> {
                    Trip trip = tripService.findTripByCode(tripCode);
                    sendWebPushToCustomer(trip, "Chuyến đi đã hoàn thành!", "Cảm ơn quý khách đã hoàn thành chuyến đi #" + tripCode + " cùng Trung Anh Car. Hẹn gặp lại quý khách!");
                    answerAlertText = "🔔 Đã gửi thông báo Web Push cảm ơn đến khách hàng!";
                }
                case "REJECT" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.CANCELLED, "Tài xế đã từ chối cuốc xe");
                    answerAlertText = "❌ Bạn đã từ chối cuốc xe: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "❌ TÀI XẾ ĐÃ TỪ CHỐI CUỐC");
                    }
                    sendWebPushToCustomer(trip, "Chuyến xe bị từ chối", "Rất tiếc tài xế hiện chưa thể nhận chuyến đi #" + tripCode + ". Chúng tôi đang sắp xếp xe khác cho bạn.");
                }
                case "ARRIVING" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.ARRIVING, "Tài xế đang đến đón");
                    answerAlertText = "🚗 Đang di chuyển đến điểm đón khách: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "🚗 TÀI XẾ ĐANG ĐẾN ĐÓN");
                    }
                    sendWebPushToCustomer(trip, "Tài xế đang đến đón!", "Tài xế đang di chuyển tới điểm đón của bạn. Vui lòng chuẩn bị hành lý.");
                }
                case "DONE", "COMPLETED" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.COMPLETED, "Cuốc xe đã hoàn thành");
                    answerAlertText = "🏁 Chúc mừng bạn đã hoàn thành cuốc xe: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "🏁 CUỐC XE ĐÃ HOÀN THÀNH");
                    }
                    sendWebPushToCustomer(trip, "Chuyến đi đã hoàn thành!", "Cảm ơn quý khách đã hoàn thành chuyến đi #" + tripCode + " cùng Trung Anh Car. Chúc quý khách một ngày tốt lành!");
                }
                case "CANCELLED" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.CANCELLED, "Tài xế hủy chuyến");
                    answerAlertText = "❌ Đã hủy cuốc: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "❌ CUỐC XE ĐÃ BỊ HỦY");
                    }
                    sendWebPushToCustomer(trip, "Chuyến xe đã bị hủy", "Chuyến xe #" + tripCode + " đã bị hủy.");
                }
                default -> answerAlertText = "Thao tác không hợp lệ: " + action;
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý callback action: {} cho trip: {}", action, tripCode, e);
            answerAlertText = "Lỗi: " + e.getMessage();
        }

        // Gọi answerCallbackQuery để Telegram tắt icon loading trên nút bấm
        answerCallbackQuery(callbackQuery.getId(), answerAlertText, showAlert);
    }

    /**
     * 3. Sửa lại tin nhắn gốc (editMessageText) trên Telegram kèm inline keyboard mới
     */
    private void editTelegramMessageAfterAction(Long chatId, Integer messageId, Trip trip, String statusHeader) {
        String updatedText = buildNewTripNotificationText(trip);
        InlineKeyboardMarkup markup = buildInlineKeyboardForStatus(trip);

        EditMessageTextRequest request = EditMessageTextRequest.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(updatedText)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();

        try {
            String url = String.format("%s/bot%s/editMessageText", properties.getApiUrl(), properties.getBotToken());
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Đã cập nhật tin nhắn Telegram messageId: {} sang trạng thái: {}", messageId, trip.getStatus());
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật tin nhắn Telegram messageId {}: {}", messageId, e.getMessage());
        }
    }

    /**
     * Trả lời Callback Query của Telegram (Hiển thị popup alert hoặc toast thông báo)
     */
    private void answerCallbackQuery(String callbackQueryId, String text) {
        answerCallbackQuery(callbackQueryId, text, false);
    }

    private void answerCallbackQuery(String callbackQueryId, String text, boolean showAlert) {
        AnswerCallbackQueryRequest request = AnswerCallbackQueryRequest.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .showAlert(showAlert)
                .build();

        try {
            String url = String.format("%s/bot%s/answerCallbackQuery", properties.getApiUrl(), properties.getBotToken());
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Lỗi khi gọi answerCallbackQuery: {}", e.getMessage());
        }
    }

    /**
     * Nội dung tin nhắn gửi Telegram theo đúng thiết kế mẫu:
     * 🚨 CUỐC XE MỚI • #{code}
     * ───────────────────────
     * ⏰ Giờ đón: {time} • {date}
     * 💰 Giá gốc: ... VNĐ
     * 🏷️ Voucher: -... VNĐ (CODE)
     * 👉 THỰC THU: ... VNĐ (1 chiều)
     * (hoặc nếu không có voucher: 💰 Giá dự kiến: ... VNĐ (1 chiều))
     * 📏 Lộ trình: ... km
     * 
     * 📍 Điểm đón:
     * ...
     * 
     * 🏁 Điểm đến:
     * ...
     * 
     * 👤 Khách hàng: ...
     * 📞 Điện thoại: ...
     * 🧳 Hành lý: ...
     * 
     * 🚗 Xe: VinFast VF e34 (30K-868.86)
     * ───────────────────────
     * Trạng thái: ⌛ Đang chờ xác nhận...
     */
    public String buildNewTripNotificationText(Trip trip) {
        String pickupTimeStr = trip.getPickupTime() != null 
                ? trip.getPickupTime().format(DATE_TIME_FORMATTER) 
                : "Ngay bây giờ";
        String distanceStr = trip.getDistanceKm() != null 
                ? trip.getDistanceKm() + " km" 
                : "Chưa xác định";
        String tripTypeSuffix = Boolean.TRUE.equals(trip.getIsRoundTrip()) ? " (2 chiều)" : " (1 chiều)";

        // Xử lý thông tin giá và voucher
        String priceSection;
        if (trip.getDiscountCode() != null && !trip.getDiscountCode().isBlank()) {
            String discountCode = trip.getDiscountCode().trim();
            BigDecimal basePrice = trip.getEstimatedPrice() != null ? trip.getEstimatedPrice() : BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO;

            Discount discount = discountRepository.findByCodeIgnoreCase(discountCode).orElse(null);
            if (discount != null) {
                if ("PERCENTAGE".equalsIgnoreCase(discount.getDiscountType())) {
                    discountAmount = basePrice.multiply(discount.getDiscountValue())
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                    if (discount.getMaxDiscountAmount() != null && discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0) {
                        discountAmount = discount.getMaxDiscountAmount();
                    }
                } else {
                    discountAmount = discount.getDiscountValue();
                }
                if (discountAmount.compareTo(basePrice) > 0) {
                    discountAmount = basePrice;
                }
            }

            BigDecimal finalPrice = basePrice.subtract(discountAmount);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }

            priceSection = String.format("""
                    💰 Giá gốc: %s VNĐ
                    🏷️ Voucher: -%s VNĐ (%s)
                    👉 THỰC THU: %s VNĐ%s""",
                    CURRENCY_FORMATTER.format(basePrice),
                    CURRENCY_FORMATTER.format(discountAmount),
                    discountCode,
                    CURRENCY_FORMATTER.format(finalPrice),
                    tripTypeSuffix
            );
        } else {
            String priceStr = trip.getEstimatedPrice() != null 
                    ? CURRENCY_FORMATTER.format(trip.getEstimatedPrice()) + " VNĐ" 
                    : "Thỏa thuận";
            priceSection = String.format("💰 Giá dự kiến: %s%s", priceStr, tripTypeSuffix);
        }

        String luggageStr = (trip.getLuggageOption() != null && !trip.getLuggageOption().isBlank()) 
                ? trip.getLuggageOption() : "Tiêu chuẩn";

        String noteLine = "";
        if (trip.getNote() != null && !trip.getNote().isBlank()) {
            noteLine = "\n📝 Ghi chú: " + escapeHtml(trip.getNote());
        }

        String vehicleInfo = "Chưa gán xe";
        if (trip.getDriverId() != null) {
            Driver driver = driverRepository.findById(trip.getDriverId()).orElse(null);
            if (driver != null) {
                String model = driver.getVehicleModel() != null ? driver.getVehicleModel() : "VinFast";
                String plate = driver.getLicensePlate() != null ? driver.getLicensePlate() : "";
                vehicleInfo = plate.isBlank() ? model : String.format("%s (%s)", model, plate);
            }
        }

        String statusStr = getStatusDisplay(trip.getStatus());

        return String.format("""
                🚨 CUỐC XE MỚI • #%s
                ───────────────────────
                ⏰ Giờ đón: %s
                %s
                📏 Lộ trình: %s

                📍 Điểm đón:
                %s

                🏁 Điểm đến:
                %s

                👤 Khách hàng: %s
                📞 Điện thoại: %s
                🧳 Hành lý: %s%s

                🚗 Xe: %s
                ───────────────────────
                Trạng thái: %s""",
                trip.getCode(),
                pickupTimeStr,
                priceSection,
                distanceStr,
                escapeHtml(trip.getPickupAddress()),
                escapeHtml(trip.getDropoffAddress()),
                escapeHtml(trip.getCustomerName()),
                trip.getCustomerPhone(),
                escapeHtml(luggageStr),
                noteLine,
                escapeHtml(vehicleInfo),
                statusStr
        );
    }

    private String getStatusDisplay(TripStatus status) {
        if (status == null) return "⌛ Đang chờ xác nhận...";
        return switch (status) {
            case PENDING -> "⌛ Đang chờ xác nhận...";
            case CONFIRMED -> "✅ Đã nhận cuốc";
            case ARRIVING -> "🚗 Đang đến đón...";
            case IN_TRIP -> "🚕 Đang di chuyển...";
            case COMPLETED -> "🏁 Đã hoàn thành";
            case CANCELLED -> "❌ Đã hủy";
        };
    }

    /**
     * Bàn phím ban đầu khi đơn mới PENDING:
     * Hàng 1: [Nhận cuốc] | [Từ chối]
     * Hàng 2: [Nhắn Zalo cho khách] | [Gọi khách]
     */
    public InlineKeyboardMarkup buildPendingInlineKeyboard(Trip trip) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Driver driver =  driverRepository.findById(trip.getDriverId())
        .orElseThrow(() -> new CustomException("Không tìm thấy tài xế"));

        // Hàng 1: [Nhận cuốc] [Từ chối]
        List<InlineKeyboardButton> row1 = List.of(
                InlineKeyboardButton.builder()
                        .text("✅ Nhận cuốc")
                        .callbackData("ACCEPT_" + trip.getCode())
                        .build(),
                InlineKeyboardButton.builder()
                        .text("❌ Từ chối")
                        .callbackData("REJECT_" + trip.getCode())
                        .build()
        );
        rows.add(row1);

        // Hàng 2: [Nhắn Zalo cho khách] [Gọi khách]
        List<InlineKeyboardButton> row2 = List.of(
                InlineKeyboardButton.builder()
                        .text("💬 Nhắn Zalo cho khách")
                        .url(buildZaloChatUrl(trip, driver.getFullName()))
                        .build(),
                buildCallCustomerButton(trip)
        );
        rows.add(row2);

        return InlineKeyboardMarkup.builder().inlineKeyboard(rows).build();
    }

    /**
     * Bàn phím sau khi tài xế đã nhận cuốc (CONFIRMED hoặc ARRIVING)
     * Hàng 1: [Đang đến đón] | [Hoàn thành cuốc]
     * Hàng 2: [Nhắn Zalo cho khách] | [Gọi khách]
     * Hàng 3: [Hủy cuốc]
     */
    public InlineKeyboardMarkup buildInlineKeyboardForStatus(Trip trip) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Driver driver =  driverRepository.findById(trip.getDriverId())
        .orElseThrow(() -> new CustomException("Không tìm thấy tài xế"));

        switch (trip.getStatus()) {
            case CONFIRMED -> {
                // Hàng 1: Các nút điều phối tiếp theo
                rows.add(List.of(
                        InlineKeyboardButton.builder()
                                .text("🚗 Đang đến đón")
                                .callbackData("ARRIVING_" + trip.getCode())
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("🏁 Hoàn thành cuốc")
                                .callbackData("DONE_" + trip.getCode())
                                .build()
                ));
                // Hàng 2: Nút liên lạc
                rows.add(List.of(
                        InlineKeyboardButton.builder()
                                .text("💬 Nhắn Zalo cho khách")
                                .url(buildZaloChatUrl(trip, driver.getFullName()))
                                .build(),
                        buildCallCustomerButton(trip)
                ));
                // Hàng 3: Hủy cuốc nếu có sự cố
                rows.add(List.of(
                        InlineKeyboardButton.builder()
                                .text("❌ Hủy cuốc")
                                .callbackData("CANCELLED_" + trip.getCode())
                                .build()
                ));
            }
            case ARRIVING -> {
                // Khi đang đến đón: có thể hoàn thành cuốc hoặc liên lạc
                rows.add(List.of(
                        InlineKeyboardButton.builder()
                                .text("🏁 Hoàn thành cuốc")
                                .callbackData("DONE_" + trip.getCode())
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("❌ Hủy cuốc")
                                .callbackData("CANCELLED_" + trip.getCode())
                                .build()
                ));
                rows.add(List.of(
                        InlineKeyboardButton.builder()
                                .text("🗺️ Chỉ đường GG Map")
                                .url(buildGoogleMapsUrl(trip))
                                .build(),
                        buildCallCustomerButton(trip)
                ));
            }
            case IN_TRIP -> {
                rows.add(List.of(
                        InlineKeyboardButton.builder()
                                .text("🏁 Hoàn thành cuốc")
                                .callbackData("DONE_" + trip.getCode())
                                .build()
                ));
            }
            case COMPLETED, CANCELLED -> {
                // Đã kết thúc: không còn nút thao tác điều phối nữa
            }
            default -> {}
        }

        return InlineKeyboardMarkup.builder().inlineKeyboard(rows).build();
    }

    /**
     * URL Google Maps chỉ đường đến điểm đón
     */
    public String buildGoogleMapsUrl(Trip trip) {
        String destination;
        if (trip.getPickupCoordinates() != null && !trip.getPickupCoordinates().isBlank()) {
            destination = trip.getPickupCoordinates().trim();
        } else {
            destination = trip.getPickupAddress();
        }
        return "https://www.google.com/maps/dir/?api=1&destination="
                + URLEncoder.encode(destination, StandardCharsets.UTF_8);
    }

    /**
     * Đường link tra cứu chuyến đi cho khách hàng
     */
    public String buildTrackingUrl(Trip trip) {
        String baseUrl = properties.getAppBaseUrl() != null && !properties.getAppBaseUrl().isBlank()
                ? properties.getAppBaseUrl() : "http://localhost:5173";
        return String.format("%s/trip/%s", baseUrl, trip.getCode());
    }

    /**
     * Bắn thông báo Web Push / Realtime trực tiếp tới khách hàng
     */
    public void sendWebPushToCustomer(Trip trip, String title, String message) {
        String trackingUrl = buildTrackingUrl(trip);

        // 1. Gửi qua kênh SSE realtime cho khách hàng đang mở trình duyệt
        try {
            sseService.sendInAppNotification(trip.getCode(), title, message, trackingUrl);
        } catch (Exception e) {
            log.warn("Không thể gửi SSE tới khách hàng: {}", e.getMessage());
        }

        // 2. Gửi Web Push Notification qua VAPID tới thiết bị/trình duyệt của khách (kể cả khi đã đóng web)
        try {
            if (trip.getPushNotification() != null && !trip.getPushNotification().isBlank()) {
                webPushService.sendNotification(trip.getPushNotification(), title, message, trackingUrl);
            }
        } catch (Exception e) {
            log.warn("Không thể gửi Web Push qua VAPID tới khách hàng: {}", e.getMessage());
        }

        log.info("Đã gửi Web Push & SSE tới khách hàng cho chuyến [{}]: {}", trip.getCode(), message);
    }

    /**
     * Thông báo ngắn gọn sau khi tài xế xác nhận nhận chuyến
     */
    public String buildAcceptedTripShortText(Trip trip) {
        String pickupTimeStr = trip.getPickupTime() != null ? trip.getPickupTime().format(DATE_TIME_FORMATTER) : "Ngay bây giờ";
        String priceStr = trip.getEstimatedPrice() != null ? CURRENCY_FORMATTER.format(trip.getEstimatedPrice()) + " VNĐ" : "Thỏa thuận";
        Driver driver = trip.getDriverId() != null ? driverRepository.findById(trip.getDriverId()).orElse(null) : null;
        String vehicleInfo = driver != null ? String.format("%s (%s)",
                driver.getVehicleModel() != null ? driver.getVehicleModel() : "VinFast",
                driver.getLicensePlate() != null ? driver.getLicensePlate() : "") : "Đã tiếp nhận";

        return String.format("""
                ✅ <b>TÀI XẾ ĐÃ NHẬN CHUYẾN!</b> • #%s
                ───────────────────────
                ⏰ <b>Giờ đón:</b> %s
                📍 <b>Điểm đón:</b> %s
                🏁 <b>Điểm đến:</b> %s
                👤 <b>Khách hàng:</b> %s - <code>%s</code>
                🚗 <b>Xe:</b> %s
                💰 <b>Giá cước:</b> %s
                ───────────────────────
                🌐 <i>Đã gửi thông báo Web Push & link tra cứu đến khách hàng!</i>""",
                trip.getCode(),
                pickupTimeStr,
                escapeHtml(trip.getPickupAddress()),
                escapeHtml(trip.getDropoffAddress()),
                escapeHtml(trip.getCustomerName()),
                trip.getCustomerPhone(),
                escapeHtml(vehicleInfo),
                priceStr
        );
    }

    /**
     * Bàn phím nút bấm sau khi tài xế xác nhận
     */
    public InlineKeyboardMarkup buildAcceptedInlineKeyboard(Trip trip, String driverName) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Hàng 1: [Nhắn Zalo cho khách] [Báo lại cho khách]
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("💬 Nhắn Zalo cho khách")
                        .url(buildZaloChatUrl(trip, driverName))
                        .build(),
                InlineKeyboardButton.builder()
                        .text("📢 Báo lại cho khách")
                        .callbackData("RENOTIFY_" + trip.getCode())
                        .build()
        ));

        // Hàng 2: [Chỉ đường GG Map] [Gọi khách]
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("🗺️ Chỉ đường GG Map")
                        .url(buildGoogleMapsUrl(trip))
                        .build(),
                buildCallCustomerButton(trip)
        ));

        // Hàng 3: [Đang đến đón] [Hủy cuốc]
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("🚗 Đang đến đón")
                        .callbackData("ARRIVING_" + trip.getCode())
                        .build(),
                InlineKeyboardButton.builder()
                        .text("❌ Hủy cuốc")
                        .callbackData("CANCELLED_" + trip.getCode())
                        .build()
        ));

        return InlineKeyboardMarkup.builder().inlineKeyboard(rows).build();
    }

    /**
     * Nội dung thông báo nhắc nhở trước giờ đón 1 tiếng
     */
    public String buildPickupReminderText(Trip trip) {
        String pickupTimeStr = trip.getPickupTime() != null ? trip.getPickupTime().format(DATE_TIME_FORMATTER) : "Sắp tới";
        String luggageStr = (trip.getLuggageOption() != null && !trip.getLuggageOption().isBlank()) ? trip.getLuggageOption() : "Tiêu chuẩn";

        return String.format("""
                ⏰ <b>YÊU CẦU ĐẾN ĐÓN KHÁCH (CÒN 1 TIẾNG)</b> • #%s
                ───────────────────────
                🔔 <i>Chuyến đi sẽ bắt đầu trong khoảng 1 tiếng nữa. Vui lòng kiểm tra phương tiện và xuất phát đến điểm đón!</i>

                ⏰ <b>Giờ đón:</b> %s
                📍 <b>Điểm đón:</b>
                %s

                👤 <b>Khách hàng:</b> %s (<code>%s</code>)
                🧳 <b>Hành lý:</b> %s
                ───────────────────────
                Trạng thái: ⌛ <i>Chuẩn bị xuất phát</i>""",
                trip.getCode(),
                pickupTimeStr,
                escapeHtml(trip.getPickupAddress()),
                escapeHtml(trip.getCustomerName()),
                trip.getCustomerPhone(),
                escapeHtml(luggageStr)
        );
    }

    /**
     * Bàn phím nút bấm nhắc nhở trước 1 tiếng: Đang đến đón, Chỉ đường GG Map, Nhắn Zalo, Gọi SĐT
     */
    public InlineKeyboardMarkup buildPickupReminderInlineKeyboard(Trip trip, String driverName) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Hàng 1: [Đang đến đón] [Chỉ đường GG Map]
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("🚗 Đang đến đón")
                        .callbackData("ARRIVING_" + trip.getCode())
                        .build(),
                InlineKeyboardButton.builder()
                        .text("🗺️ Chỉ đường GG Map")
                        .url(buildGoogleMapsUrl(trip))
                        .build()
        ));

        // Hàng 2: [Nhắn Zalo] [Gọi SĐT]
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("💬 Nhắn Zalo")
                        .url(buildZaloChatUrl(trip, driverName))
                        .build(),
                buildCallCustomerButton(trip)
        ));

        return InlineKeyboardMarkup.builder().inlineKeyboard(rows).build();
    }

    /**
     * Gửi tin nhắn thông báo nhắc trước 1 tiếng tới Telegram tài xế
     */
    public void sendPickupReminder(Trip trip) {
        Driver driver = trip.getDriverId() != null ? driverRepository.findById(trip.getDriverId()).orElse(null) : null;
        String driverName = driver != null ? driver.getFullName() : "Trung Anh";
        String text = buildPickupReminderText(trip);
        InlineKeyboardMarkup markup = buildPickupReminderInlineKeyboard(trip, driverName);
        sendMessage(properties.getDriverChatId(), text, markup);
    }

    /**
     * Nội dung thông báo khi hết giờ chuyến đi (hết busyUntil)
     */
    public String buildTripFinishedReminderText(Trip trip) {
        String priceStr = trip.getEstimatedPrice() != null ? CURRENCY_FORMATTER.format(trip.getEstimatedPrice()) + " VNĐ" : "Thỏa thuận";

        return String.format("""
                🏁 <b>CUỐC XE ĐÃ HẾT GIỜ / DỰ KIẾN HOÀN THÀNH</b> • #%s
                ───────────────────────
                🔔 <i>Cuốc xe đã đến thời gian kết thúc dự kiến. Bấm xác nhận hoàn thành và gửi thông báo cảm ơn đến khách hàng!</i>

                📍 <b>Điểm đến:</b> %s
                👤 <b>Khách hàng:</b> %s (<code>%s</code>)
                👉 <b>Cước phí:</b> %s
                ───────────────────────
                Trạng thái: 🚕 <i>Chờ hoàn thành</i>""",
                trip.getCode(),
                escapeHtml(trip.getDropoffAddress()),
                escapeHtml(trip.getCustomerName()),
                trip.getCustomerPhone(),
                priceStr
        );
    }

    /**
     * Bàn phím nút bấm khi hết giờ chuyến
     */
    public InlineKeyboardMarkup buildTripFinishedInlineKeyboard(Trip trip) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Hàng 1: [Hoàn thành chuyến]
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("🏁 Hoàn thành chuyến")
                        .callbackData("DONE_" + trip.getCode())
                        .build()
        ));

        // Hàng 2: [Gửi thông báo Web Push đến KH]
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("🔔 Gửi thông báo Web Push đến KH")
                        .callbackData("PUSH_COMPLETE_" + trip.getCode())
                        .build()
        ));

        return InlineKeyboardMarkup.builder().inlineKeyboard(rows).build();
    }

    /**
     * Gửi tin nhắn nhắc hoàn thành chuyến tới Telegram tài xế
     */
    public void sendTripFinishedReminder(Trip trip) {
        String text = buildTripFinishedReminderText(trip);
        InlineKeyboardMarkup markup = buildTripFinishedInlineKeyboard(trip);
        sendMessage(properties.getDriverChatId(), text, markup);
    }

    /**
     * Gửi tin nhắn mới tới Telegram
     */
    public void sendMessage(String chatId, String text, InlineKeyboardMarkup markup) {
        if (properties.getBotToken().isBlank() || chatId == null || chatId.isBlank()) {
            return;
        }
        SendMessageRequest request = SendMessageRequest.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();
        try {
            String url = String.format("%s/bot%s/sendMessage", properties.getApiUrl(), properties.getBotToken());
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Lỗi khi gửi tin nhắn Telegram tới chatId {}: {}", chatId, e.getMessage());
        }
    }

    /**
     * Cập nhật tin nhắn Telegram đã có
     */
    public void editTelegramMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup markup) {
        EditMessageTextRequest request = EditMessageTextRequest.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();

        try {
            String url = String.format("%s/bot%s/editMessageText", properties.getApiUrl(), properties.getBotToken());
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Đã cập nhật tin nhắn Telegram messageId: {}", messageId);
        } catch (Exception e) {
            log.error("Lỗi khi edit tin nhắn Telegram messageId {}: {}", messageId, e.getMessage());
        }
    }

    /**
     * URL Scheme mở Zalo cá nhân:
     * https://zalo.me/{phone}?text=Chao%20ban%2C%20tai%20xe%20Trung%20Anh%20da%20nhan%20chuyen%20xe%20luc%20...
     */
    public String buildZaloChatUrl(Trip trip, String driverName) {
        String cleanPhone = trip.getCustomerPhone().replaceAll("[^0-9]", "");
        String pickupTimeStr = trip.getPickupTime() != null ? trip.getPickupTime().format(DATE_TIME_FORMATTER) : "sớm nhất";
        String rawMessage = String.format(
                "Chao ban, tai xe %s da nhan chuyen xe luc %s (ma: %s). Minh dang sap xep don ban tai %s nhe!",
                driverName,
                pickupTimeStr,
                trip.getCode(),
                trip.getPickupAddress()
        );
        String encodedMessage = URLEncoder.encode(rawMessage, StandardCharsets.UTF_8);
        return String.format("https://zalo.me/%s?text=%s", cleanPhone, encodedMessage);
    }

    /**
     * Kiểm tra xem Base URL có phải là domain công khai hợp lệ cho Telegram Bot API hay không.
     * Telegram Bot API chặn các URL chứa localhost hoặc private IP (127.0.0.1) trong inline keyboard button.
     */
    public boolean isPublicHttpUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String lower = url.trim().toLowerCase();
        if (lower.contains("localhost") || lower.contains("127.0.0.1") || lower.contains("0.0.0.0")) {
            return false;
        }
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /**
     * Tạo nút gọi khách hàng an toàn cho InlineKeyboard:
     * - Nếu chạy với domain công khai: sử dụng URL chuyển hướng qua /api/v1/call/{phone} -> tel:{phone}.
     * - Nếu chạy localhost (dev): sử dụng CallbackButton để hiển thị SĐT popup, tránh bị Telegram trả về lỗi 400 Wrong HTTP URL.
     */
    public InlineKeyboardButton buildCallCustomerButton(Trip trip) {
        String cleanPhone = trip.getCustomerPhone().replaceAll("[^0-9+]", "");
        String baseUrl = properties.getAppBaseUrl();

        if (isPublicHttpUrl(baseUrl)) {
            return InlineKeyboardButton.builder()
                    .text("📞 Gọi khách")
                    .url(String.format("%s/api/v1/call/%s", baseUrl.trim(), cleanPhone))
                    .build();
        } else {
            return InlineKeyboardButton.builder()
                    .text("📞 Gọi: " + cleanPhone)
                    .callbackData("CALL_" + cleanPhone)
                    .build();
        }
    }

    /**
     * URL gọi khách hàng:
     * Do Telegram Bot API giới hạn thuộc tính 'url' của InlineKeyboardButton chỉ cho phép http/https/tg,
     * ta sử dụng URL chuyển hướng: https://{backend_domain}/api/v1/call/{phone}
     * Endpoint này sẽ trả về HTTP 302 chuyển hướng tới tel:{phone}, giúp mở ứng dụng gọi điện trên điện thoại của tài xế.
     */
    public String buildCallCustomerUrl(Trip trip) {
        String cleanPhone = trip.getCustomerPhone().replaceAll("[^0-9+]", "");
        String baseUrl = properties.getAppBaseUrl() != null && !properties.getAppBaseUrl().isBlank()
                ? properties.getAppBaseUrl() : "http://localhost:8080";
        return String.format("%s/api/v1/call/%s", baseUrl, cleanPhone);
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
