package com.example.trunganhcar.controller;

import com.example.trunganhcar.dto.telegram.TelegramDto.Update;
import com.example.trunganhcar.service.TelegramBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3. Telegram Webhook APIs", description = "Endpoint tiếp nhận webhook từ Telegram Bot hoặc giả lập callback từ tài xế")
@RestController
@RequestMapping("/api/v1/driver/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramBotService telegramBotService;

    @Operation(summary = "Tiếp nhận Webhook Telegram", description = "Xử lý CallbackQuery khi tài xế bấm nút trên Telegram Bot (ACCEPT, REJECT, ARRIVING, DONE...)")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleTelegramWebhook(@RequestBody Update update) {
        log.debug("Nhận webhook Telegram: updateId={}", update.getUpdateId());

        if (update.getCallbackQuery() != null) {
            log.info("Nhận callback query: data={} từ user: {}",
                    update.getCallbackQuery().getData(),
                    update.getCallbackQuery().getFrom() != null ? update.getCallbackQuery().getFrom().getUsername() : "unknown");

            telegramBotService.handleCallbackQuery(update.getCallbackQuery());
        }

        // Luôn trả về 200 OK để Telegram ghi nhận thành công và không retry
        return ResponseEntity.ok("OK");
    }
}

