package com.example.trunganhcar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trungAnhCarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trung Anh Car Booking API")
                        .description("Tài liệu API hệ thống đặt xe gia đình/tiện chuyến Trung Anh Car - Tích hợp Telegram Bot & Server-Sent Events (SSE)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Trung Anh Car Dev Team")
                                .email("support@trunganhcar.vn")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Môi trường Localhost")
                ));
    }
}

