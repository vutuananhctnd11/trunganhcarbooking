package com.example.trunganhcar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram")
@Getter
@Setter
public class TelegramProperties {
    /**
     * Bot token được cấp bởi @BotFather trên Telegram
     */
    private String botToken = "";

    /**
     * Chat ID của tài xế hoặc nhóm chat nhận đơn
     */
    private String driverChatId = "";

    /**
     * Telegram Bot API base URL
     */
    private String apiUrl = "https://api.telegram.org";

    /**
     * URL public của backend server (dùng cho webhook, redirect gọi điện thoại)
     */
    private String appBaseUrl = "http://localhost:8080";
}

