package com.sae.smartdesk.common.dto;

import java.time.Instant;

public record ApiResponse<T>(Instant timestamp, boolean success, T data, String message) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Instant.now(), true, data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(Instant.now(), true, data, message);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(Instant.now(), false, null, message);
    }
}
