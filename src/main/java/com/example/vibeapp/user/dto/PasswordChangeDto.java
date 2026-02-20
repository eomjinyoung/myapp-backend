package com.example.vibeapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청 객체")
public record PasswordChangeDto(
        @Schema(description = "현재 비밀번호", example = "1234") @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.") String currentPassword,
        @Schema(description = "새 비밀번호", example = "5678", minLength = 4, maxLength = 20) @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.") @Size(min = 4, max = 20, message = "새 비밀번호는 4자에서 20자 사이여야 합니다.") String newPassword,
        @Schema(description = "새 비밀번호 확인", example = "5678") @NotBlank(message = "새 비밀번호 확인은 필수 입력 항목입니다.") String newPasswordConfirm) {
    public boolean isNewPasswordMatching() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
