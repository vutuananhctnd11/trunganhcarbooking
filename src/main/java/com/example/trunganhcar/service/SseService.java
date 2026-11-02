package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.SseTripPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseService {

    // Thời gian timeout kết nối SSE: 30 phút (1_800_000 ms)
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    // Map lưu danh sách SseEmitter theo tripCode
    private final Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    /**
     * Tạo và đăng ký một SseEmitter cho chuyến đi theo tripCode
     */
    public SseEmitter subscribe(String tripCode) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitterMap.computeIfAbsent(tripCode, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Thiết lập callback dọn dẹp khi emitter kết thúc/timeout/lỗi
        emitter.onCompletion(() -> removeEmitter(tripCode, emitter));
        emitter.onTimeout(() -> {
            log.warn("SSE emitter timed out cho chuyến: {}", tripCode);
            removeEmitter(tripCode, emitter);
        });
        emitter.onError(e -> {
            log.warn("SSE emitter gặp lỗi cho chuyến: {}. Chi tiết: {}", tripCode, e.getMessage());
            removeEmitter(tripCode, emitter);
        });

        // Gửi event chào mừng / kết nối thành công đầu tiên
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Đã kết nối luồng theo dõi thời gian thực cho chuyến: " + tripCode, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            log.error("Không thể gửi event chào mừng cho tripCode: {}", tripCode, e);
            removeEmitter(tripCode, emitter);
        }

        return emitter;
    }

    /**
     * Bắn cập nhật trạng thái chuyến đi tới toàn bộ các client đang lắng nghe tripCode này
     */
    public void sendTripUpdate(String tripCode, SseTripPayload payload) {
        List<SseEmitter> emitters = emitterMap.get(tripCode);
        if (emitters == null || emitters.isEmpty()) {
            log.info("Không có client nào đang lắng nghe SSE cho chuyến: {}", tripCode);
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("TRIP_STATUS_CHANGED")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                log.warn("Lỗi gửi SSE tới 1 emitter của chuyến {}: {}", tripCode, e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        // Xóa các emitter đã chết hoặc ngắt kết nối
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            if (emitters.isEmpty()) {
                emitterMap.remove(tripCode);
            }
        }
    }

    /**
     * Dọn dẹp emitter khi client ngắt kết nối
     */
    private void removeEmitter(String tripCode, SseEmitter emitter) {
        List<SseEmitter> emitters = emitterMap.get(tripCode);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emitterMap.remove(tripCode);
            }
        }
        log.debug("Đã gỡ emitter cho chuyến {}", tripCode);
    }

    /**
     * Gửi định kỳ heartbeat (ping comment) mỗi 25s để tránh proxy/load balancer đóng kết nối
     */
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        if (emitterMap.isEmpty()) return;

        emitterMap.forEach((tripCode, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (Exception e) {
                    removeEmitter(tripCode, emitter);
                }
            }
        });
    }
}

