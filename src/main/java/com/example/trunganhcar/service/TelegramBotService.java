package com.example.trunganhcar.service;

import com.example.trunganhcar.config.TelegramProperties;
import com.example.trunganhcar.dto.telegram.TelegramDto.*;
import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getInstance(new Locale("vi", "VN"));

    public TelegramBotService(TelegramProperties properties, @Lazy TripService tripService) {
        this.properties = properties;
        this.tripService = tripService;
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

        try {
            switch (action) {
                case "ACCEPT" -> {
                    Trip trip = tripService.acceptTripByDriver(tripCode, chatId != null ? chatId.toString() : null, "Tài xế đã nhận cuốc xe");
                    answerAlertText = "✅ Bạn đã nhận cuốc xe: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "✅ ĐÃ NHẬN CUỐC THÀNH CÔNG");
                    }
                }
                case "REJECT" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.CANCELLED, "Tài xế đã từ chối cuốc xe");
                    answerAlertText = "❌ Bạn đã từ chối cuốc xe: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "❌ TÀI XẾ ĐÃ TỪ CHỐI CUỐC");
                    }
                }
                case "ARRIVING" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.ARRIVING, "Tài xế đang đến đón");
                    answerAlertText = "🚗 Đang di chuyển đến điểm đón khách: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "🚗 TÀI XẾ ĐANG ĐẾN ĐÓN");
                    }
                }
                case "DONE", "COMPLETED" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.COMPLETED, "Cuốc xe đã hoàn thành");
                    answerAlertText = "🏁 Chúc mừng bạn đã hoàn thành cuốc xe: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "🏁 CUỐC XE ĐÃ HOÀN THÀNH");
                    }
                }
                case "CANCELLED" -> {
                    Trip trip = tripService.updateTripStatus(tripCode, TripStatus.CANCELLED, "Tài xế hủy chuyến");
                    answerAlertText = "❌ Đã hủy cuốc: " + tripCode;
                    if (messageId != null && chatId != null) {
                        editTelegramMessageAfterAction(chatId, messageId, trip, "❌ CUỐC XE ĐÃ BỊ HỦY");
                    }
                }
                default -> answerAlertText = "Thao tác không hợp lệ: " + action;
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý callback action: {} cho trip: {}", action, tripCode, e);
            answerAlertText = "Lỗi: " + e.getMessage();
        }

        // Gọi answerCallbackQuery để Telegram tắt icon loading trên nút bấm
        answerCallbackQuery(callbackQuery.getId(), answerAlertText);
    }

    /**
     * 3. Sửa lại tin nhắn gốc (editMessageText) trên Telegram kèm inline keyboard mới
     */
    private void editTelegramMessageAfterAction(Long chatId, Integer messageId, Trip trip, String statusHeader) {
        String updatedText = "<b>[TRUNG ANH CAR] - " + statusHeader + "</b>\n\n" + buildNewTripNotificationText(trip);
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
        AnswerCallbackQueryRequest request = AnswerCallbackQueryRequest.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .showAlert(false)
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
     * Nội dung tin nhắn gửi Telegram:
     * 🚗 <b>CÓ CUỐC XE MỚI!</b>
     * - Mã chuyến: {code}
     * - Khách hàng: {customer_name} - {customer_phone}
     * - Điểm đón: {pickup_address}
     * - Điểm đến: {dropoff_address}
     * - Giờ đón: {pickup_time}
     * - Ghi chú: {note}
     */
    public String buildNewTripNotificationText(Trip trip) {
        String pickupTimeStr = trip.getPickupTime() != null ? trip.getPickupTime().format(DATE_TIME_FORMATTER) : "Ngay bây giờ";
        String noteStr = (trip.getNote() != null && !trip.getNote().isBlank()) ? trip.getNote() : "Không có";
        String priceStr = trip.getEstimatedPrice() != null ? CURRENCY_FORMATTER.format(trip.getEstimatedPrice()) + " VNĐ" : "Thỏa thuận";
        String distanceStr = trip.getDistanceKm() != null ? trip.getDistanceKm() + " km" : "Chưa xác định";

        String tripTypeStr = Boolean.TRUE.equals(trip.getIsRoundTrip()) ? "🔄 Khứ hồi (2 chiều)" : "➡️ 1 chiều";
        String waitHoursStr = (trip.getWaitHours() != null && trip.getWaitHours() > 0) ? trip.getWaitHours() + " giờ" : "Không chờ";
        String luggageStr = (trip.getLuggageOption() != null && !trip.getLuggageOption().isBlank()) ? trip.getLuggageOption() : "Tiêu chuẩn";

        String driverInfo = "";
        if (trip.getDriver() != null) {
            driverInfo = String.format("""
                    • <b>Tài xế:</b> %s (%s)
                    • <b>Xe:</b> %s (%d chỗ) - <b>%s</b>
                    """,
                    escapeHtml(trip.getDriver().getFullName()),
                    trip.getDriver().getPhone(),
                    escapeHtml(trip.getDriver().getVehicleModel() != null ? trip.getDriver().getVehicleModel() : "Chưa rõ"),
                    trip.getDriver().getSeatCount() != null ? trip.getDriver().getSeatCount() : 4,
                    escapeHtml(trip.getDriver().getLicensePlate() != null ? trip.getDriver().getLicensePlate() : "")
            );
        }

        return """
                🚗 <b>CÓ CUỐC XE MỚI!</b>
                • <b>Mã chuyến:</b> <code>%s</code>
                • <b>Khách hàng:</b> %s - <code>%s</code>
                • <b>Điểm đón:</b> %s
                • <b>Điểm đến:</b> %s
                • <b>Giờ đón:</b> %s
                • <b>Loại chuyến:</b> %s
                • <b>Thời gian chờ:</b> %s
                • <b>Hành lý:</b> %s
                • <b>Quãng đường:</b> %s
                • <b>Giá dự kiến:</b> <b>%s</b>
                %s• <b>Ghi chú:</b> %s
                • <b>Trạng thái:</b> <i>%s</i>
                """.formatted(
                trip.getCode(),
                escapeHtml(trip.getCustomerName()),
                trip.getCustomerPhone(),
                escapeHtml(trip.getPickupAddress()),
                escapeHtml(trip.getDropoffAddress()),
                pickupTimeStr,
                tripTypeStr,
                waitHoursStr,
                escapeHtml(luggageStr),
                distanceStr,
                priceStr,
                driverInfo,
                escapeHtml(noteStr),
                trip.getStatus().getDescription()
        );
    }

    /**
     * Bàn phím ban đầu khi đơn mới PENDING:
     * Hàng 1: [Nhận cuốc] | [Từ chối]
     * Hàng 2: [Nhắn Zalo cho khách] | [Gọi khách]
     */
    public InlineKeyboardMarkup buildPendingInlineKeyboard(Trip trip) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

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
                        .url(buildZaloChatUrl(trip))
                        .build(),
                InlineKeyboardButton.builder()
                        .text("📞 Gọi khách")
                        .url(buildCallCustomerUrl(trip))
                        .build()
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
                                .url(buildZaloChatUrl(trip))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("📞 Gọi khách")
                                .url(buildCallCustomerUrl(trip))
                                .build()
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
                                .text("💬 Nhắn Zalo cho khách")
                                .url(buildZaloChatUrl(trip))
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("📞 Gọi khách")
                                .url(buildCallCustomerUrl(trip))
                                .build()
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
     * URL Scheme mở Zalo cá nhân:
     * https://zalo.me/{phone}?text=Chao%20ban%2C%20tai%20xe%20Trung%20Anh%20da%20nhan%20chuyen%20xe%20luc%20...
     */
    public String buildZaloChatUrl(Trip trip) {
        String cleanPhone = trip.getCustomerPhone().replaceAll("[^0-9]", "");
        String pickupTimeStr = trip.getPickupTime() != null ? trip.getPickupTime().format(DATE_TIME_FORMATTER) : "sớm nhất";
        String rawMessage = String.format(
                "Chao ban, tai xe Trung Anh da nhan chuyen xe luc %s (ma: %s). Minh dang sap xep don ban tai %s nhe!",
                pickupTimeStr,
                trip.getCode(),
                trip.getPickupAddress()
        );
        String encodedMessage = URLEncoder.encode(rawMessage, StandardCharsets.UTF_8);
        return String.format("https://zalo.me/%s?text=%s", cleanPhone, encodedMessage);
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
