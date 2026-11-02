package com.example.trunganhcar.event;

import com.example.trunganhcar.dto.SseTripPayload;
import com.example.trunganhcar.service.SseService;
import com.example.trunganhcar.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripEventListener {

    private final TelegramBotService telegramBotService;
    private final SseService sseService;

    @Async
    @EventListener
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Nhận sự kiện BookingCreatedEvent cho chuyến mã: {}", event.getTrip().getCode());
        telegramBotService.sendNewTripNotification(event.getTrip());
    }

    @Async
    @EventListener
    public void handleTripStatusChanged(TripStatusChangedEvent event) {
        log.info("Nhận sự kiện TripStatusChangedEvent: mã={} [{} -> {}]",
                event.getTrip().getCode(), event.getPreviousStatus(), event.getCurrentStatus());

        SseTripPayload payload = SseTripPayload.builder()
                .tripCode(event.getTrip().getCode())
                .status(event.getCurrentStatus())
                .statusDescription(event.getCurrentStatus().getDescription())
                .message(event.getNote() != null ? event.getNote() : event.getCurrentStatus().getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        sseService.sendTripUpdate(event.getTrip().getCode(), payload);
    }
}

