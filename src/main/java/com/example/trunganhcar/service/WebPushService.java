package com.example.trunganhcar.service;

import java.security.Security;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Urgency;

@Service 
@Slf4j 
public class WebPushService {

    private final String publicKey;
    private final PushService pushService;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebPushService(
            @Value("${webpush.public-key}") String publicKey,
            @Value("${webpush.private-key}") String privateKey,
            @Value("${webpush.subject:mailto:hotro@trunganhcar.com}") String subject,
            ObjectMapper objectMapper) throws Exception {

        // Bouncy Castle Provider bắt buộc phải được đăng ký vào Java Security
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        this.publicKey = publicKey;
        this.pushService = new PushService(publicKey, privateKey, subject);
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Gửi Web Push xuống trình duyệt khách
     * @param subscriptionJson Chuỗi JSON PushSubscription lấy từ Frontend lưu trong DB
     * @param title Tiêu đề thông báo
     * @param body Nội dung thông báo
     * @param targetUrl Link chuyển hướng khi khách chạm vào thông báo
     */
    public void sendNotification(String subscriptionJson, String title, String body, String targetUrl) {
        if (subscriptionJson == null || subscriptionJson.isBlank()) {
            log.warn("Bỏ qua gửi Web Push vì không có Subscription token");
            return;
        }

        try {
            // Parse chuỗi JSON lưu trong DB ra Object Subscription của thư viện (bỏ qua các trường mở rộng của browser như expirationTime)
            Subscription subscription = objectMapper.reader()
                    .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(subscriptionJson, Subscription.class);

            // Tạo payload JSON gửi xuống Service Worker
            Map<String, String> payload = Map.of(
                    "title", title,
                    "body", body,
                    "url", targetUrl
            );
            String payloadJson = objectMapper.writeValueAsString(payload);

            // Khởi tạo notification với độ ưu tiên cao (HIGH) để nổ chuông ngay
            Notification notification = Notification.builder()
                    .endpoint(subscription.endpoint)
                    .userPublicKey(subscription.keys.p256dh)
                    .userAuth(subscription.keys.auth)
                    .payload(payloadJson)
                    .urgency(Urgency.HIGH)
                    .build();

            // Gửi trực tiếp sang máy chủ Push của Google/Apple
            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 201) {
                log.info("Gửi Web Push thành công!");
            } else if (statusCode == 410 || statusCode == 404) {
                // Token đã hết hạn do người dùng gỡ quyền hoặc xóa cache trình duyệt
                log.warn("Subscription token đã hết hạn hoặc không còn tồn tại: {}", statusCode);
            } else {
                log.error("Push Service trả về mã lỗi: {}", statusCode);
            }

        } catch (Exception e) {
            log.error("Lỗi khi gửi Web Push: {}", e.getMessage(), e);
        }
    }
}
