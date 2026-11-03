package com.example.trunganhcar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

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
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập Token JWT vào đây (Không cần nhập chữ 'Bearer ')")));
    }
}
