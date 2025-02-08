package com.naver.et0709.weatherwear.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpResponseDto {
    private boolean result;
    private String message;
    private Object data;

    public static SignUpResponseDto setSuccess(String message) {
        return SignUpResponseDto.builder()
                .result(true)
                .message(message)
                .build();
    }

    public static SignUpResponseDto setFailed(String message) {
        return SignUpResponseDto.builder()
                .result(false)
                .message(message)
                .build();
    }

    public static SignUpResponseDto setSuccessData(String message, Object data) {
        return SignUpResponseDto.builder()
                .result(true)
                .message(message)
                .data(data)
                .build();
    }

    public static SignUpResponseDto setFailedData(String message, Object data) {
        return SignUpResponseDto.builder()
                .result(false)
                .message(message)
                .data(data)
                .build();
    }
}
