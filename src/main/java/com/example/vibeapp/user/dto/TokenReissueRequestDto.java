package com.example.vibeapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청 객체")
public record TokenReissueRequestDto(
        @Schema(description = "기존 리프레시 토큰") @NotBlank String refreshToken) {
}
