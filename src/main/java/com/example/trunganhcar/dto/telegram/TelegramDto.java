package com.example.trunganhcar.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class TelegramDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Update {
        @JsonProperty("update_id")
        private Long updateId;

        @JsonProperty("message")
        private Message message;

        @JsonProperty("callback_query")
        private CallbackQuery callbackQuery;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("message_id")
        private Integer messageId;

        @JsonProperty("chat")
        private Chat chat;

        @JsonProperty("text")
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chat {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("title")
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("username")
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallbackQuery {
        @JsonProperty("id")
        private String id;

        @JsonProperty("from")
        private User from;

        @JsonProperty("message")
        private Message message;

        @JsonProperty("data")
        private String data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SendMessageRequest {
        @JsonProperty("chat_id")
        private String chatId;

        @JsonProperty("text")
        private String text;

        @JsonProperty("parse_mode")
        @Builder.Default
        private String parseMode = "HTML";

        @JsonProperty("reply_markup")
        private InlineKeyboardMarkup replyMarkup;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EditMessageTextRequest {
        @JsonProperty("chat_id")
        private String chatId;

        @JsonProperty("message_id")
        private Integer messageId;

        @JsonProperty("text")
        private String text;

        @JsonProperty("parse_mode")
        @Builder.Default
        private String parseMode = "HTML";

        @JsonProperty("reply_markup")
        private InlineKeyboardMarkup replyMarkup;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerCallbackQueryRequest {
        @JsonProperty("callback_query_id")
        private String callbackQueryId;

        @JsonProperty("text")
        private String text;

        @JsonProperty("show_alert")
        private Boolean showAlert;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InlineKeyboardMarkup {
        @JsonProperty("inline_keyboard")
        private List<List<InlineKeyboardButton>> inlineKeyboard;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InlineKeyboardButton {
        @JsonProperty("text")
        private String text;

        @JsonProperty("callback_data")
        private String callbackData;

        @JsonProperty("url")
        private String url;
    }
}

