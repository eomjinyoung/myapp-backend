package com.example.vibeapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 응답 객체")
public record LoginResponseDto(
                @Schema(description = "액세스 토큰") @NotBlank String accessToken,
                @Schema(description = "토큰 타입", example = "Bearer") @NotBlank String tokenType,
                @Schema(description = "사용자 이름", example = "홍길동") @NotBlank String userName,
                @Schema(description = "리프레시 토큰") @NotBlank String refreshToken) {
}
