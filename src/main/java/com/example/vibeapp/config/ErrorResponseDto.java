package com.example.vibeapp.config;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답 객체")
public record ErrorResponseDto(
        @Schema(description = "에러 메시지", example = "잘못된 요청입니다.") String message,
        @Schema(description = "HTTP 상태 코드", example = "400") int status) {
}
