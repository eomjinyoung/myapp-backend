package com.example.vibeapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.example.vibeapp.user.User;

@Schema(description = "사용자 정보 응답 객체")
public record UserResponseDto(
        @Schema(description = "사용자 번호", example = "1") Long no,
        @Schema(description = "이름", example = "홍길동") String name,
        @Schema(description = "이메일", example = "user@example.com") String email) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getNo(),
                user.getName(),
                user.getEmail());
    }
}
