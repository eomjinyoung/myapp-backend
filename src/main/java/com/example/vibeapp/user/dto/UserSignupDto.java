package com.example.vibeapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 객체")
public record UserSignupDto(
        @Schema(description = "이름", example = "홍길동", maxLength = 50) @NotBlank(message = "이름은 필수 입력 항목입니다.") @Size(max = 50, message = "이름은 50자 이내여야 합니다.") String name,

        @Schema(description = "이메일", example = "user@example.com", maxLength = 50) @NotBlank(message = "이메일은 필수 입력 항목입니다.") @Email(message = "유효한 이메일 형식이 아닙니다.") @Size(max = 50, message = "이메일은 50자 이내여야 합니다.") String email,

        @Schema(description = "비밀번호", example = "1234", minLength = 4, maxLength = 20) @NotBlank(message = "비밀번호는 필수 입력 항목입니다.") @Size(min = 4, max = 20, message = "비밀번호는 4자에서 20자 사이여야 합니다.") String password,

        @Schema(description = "비밀번호 확인", example = "1234") @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.") String passwordConfirm) {
    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirm);
    }
}
