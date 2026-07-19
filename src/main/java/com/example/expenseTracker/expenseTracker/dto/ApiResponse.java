package com.example.expenseTracker.expenseTracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApiResponse <T>(
        boolean isSuccess,
        String message,
        T data,
        LocalDateTime timeStamp
) {
    // Utility constructor for simple Success responses with NO data payload
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    // Utility constructor for Success responses WITH data payload (e.g., passing your list of expenses)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    // Utility constructor for Error responses
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
