package com.example.vibeapp.user;

import com.example.vibeapp.config.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.vibeapp.config.JwtTokenProvider;
import com.example.vibeapp.security.AuditLog;
import com.example.vibeapp.security.LoginAttemptService;
import com.example.vibeapp.security.TokenBlacklistService;
import com.example.vibeapp.user.dto.LoginRequestDto;
import com.example.vibeapp.user.dto.LoginResponseDto;
import com.example.vibeapp.user.dto.TokenReissueRequestDto;
import com.example.vibeapp.user.dto.UserResponseDto;
import com.example.vibeapp.user.dto.UserSignupDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Tag(name = "User", description = "사용자 및 인증 관련 API")
@RestController
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService blacklistService;
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    public UserController(UserService userService, AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository,
            TokenBlacklistService blacklistService, UserRepository userRepository,
            LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistService = blacklistService;
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "429", description = "요청 제한 초과", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/api/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody UserSignupDto userSignupDto) {
        userService.signup(userSignupDto);
        return ResponseEntity.ok().build();
    }

    @AuditLog("LOGIN")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 토큰을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "계정 잠금 상태", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "429", description = "요청 제한 초과", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/api/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        if (loginAttemptService.isLocked(loginRequest.email())) {
            return ResponseEntity.status(403)
                    .body("Account is locked due to too many failed attempts. Please try again after 15 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

            String accessToken = tokenProvider.createAccessToken(loginRequest.email());
            String refreshToken = tokenProvider.createRefreshToken(loginRequest.email());

            Object principal = authentication.getPrincipal();
            if (principal instanceof com.example.vibeapp.security.SecurityUser securityUser) {
                User user = securityUser.getUser();

                // 로그인 성공 시 로직
                loginAttemptService.loginSucceeded(user.getEmail());
                refreshTokenRepository.deleteByUser(user);

                LocalDateTime expiryDate = LocalDateTime.now()
                        .plusNanos(tokenProvider.getRefreshTokenExpirationTime() * 1000000);
                refreshTokenRepository.save(new RefreshToken(user, refreshToken, expiryDate));

                return ResponseEntity.ok(new LoginResponseDto(accessToken, "Bearer", user.getName(), refreshToken));
            }
        } catch (org.springframework.security.core.AuthenticationException e) {
            loginAttemptService.loginFailed(loginRequest.email());
            return ResponseEntity.status(401).body("Invalid email or password.");
        }

        throw new IllegalStateException("Unexpected authentication state");
    }

    @AuditLog("REISSUE")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/api/reissue")
    @Transactional
    public ResponseEntity<LoginResponseDto> reissue(@Valid @RequestBody TokenReissueRequestDto reissueRequest) {
        String refreshTokenString = reissueRequest.refreshToken();

        if (!tokenProvider.validateToken(refreshTokenString)) {
            return ResponseEntity.status(401).build();
        }

        // 리프레시 토큰 재사용 감지 (Replay Attack Detection)
        String compromisedEmail = blacklistService.getRotatedTokenUserEmail(refreshTokenString);
        if (compromisedEmail != null) {
            // 이미 사용된 토큰이 다시 들어옴 -> 공격 상태로 간주
            userRepository.findByEmail(compromisedEmail).ifPresent(refreshTokenRepository::deleteByUser);
            return ResponseEntity.status(401).build();
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElse(null);

        if (refreshToken == null || refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            if (refreshToken != null) {
                refreshTokenRepository.delete(refreshToken);
            }
            return ResponseEntity.status(401).build();
        }

        User user = refreshToken.getUser();
        String newAccessToken = tokenProvider.createAccessToken(user.getEmail());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getEmail());

        // 리프레시 토큰 로테이션
        refreshTokenRepository.delete(refreshToken);
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusNanos(tokenProvider.getRefreshTokenExpirationTime() * 1000000);
        refreshTokenRepository.save(new RefreshToken(user, newRefreshToken, expiryDate));

        // 사용된 기존 토큰을 Redis에 저장하여 재사용 감지용으로 활용
        blacklistService.addRotatedToken(refreshTokenString, user.getEmail(),
                tokenProvider.getRemainingExpirationTime(refreshTokenString));

        return ResponseEntity.ok(new LoginResponseDto(newAccessToken, "Bearer", user.getName(), newRefreshToken));
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "현재 로그인된 사용자의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/api/user/me")
    public ResponseEntity<UserResponseDto> me(
            @AuthenticationPrincipal com.example.vibeapp.security.SecurityUser securityUser) {
        if (securityUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserResponseDto.from(securityUser.getUser()));
    }

    @AuditLog("PASSWORD_CHANGE")
    @Operation(summary = "비밀번호 변경", description = "로그인된 사용자의 비밀번호를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/api/user/password")
    @Transactional
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal com.example.vibeapp.security.SecurityUser securityUser,
            @Valid @RequestBody com.example.vibeapp.user.dto.PasswordChangeDto passwordChangeDto) {
        if (securityUser == null) {
            return ResponseEntity.status(401).build();
        }
        userService.changePassword(securityUser.getUser().getEmail(), passwordChangeDto);
        return ResponseEntity.ok().build();
    }

    @AuditLog("LOGOUT")
    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃 처리하고 토큰을 무효화합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/api/logout")
    @Transactional
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletRequest request) {
        if (authentication != null
                && authentication.getPrincipal() instanceof com.example.vibeapp.security.SecurityUser securityUser) {
            refreshTokenRepository.deleteByUser(securityUser.getUser());
        }

        String bearerToken = request.getHeader("Authorization");
        if (org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);
            long remainingTime = tokenProvider.getRemainingExpirationTime(jwt);
            blacklistService.addToBlacklist(jwt, remainingTime);
        }

        return ResponseEntity.ok().build();
    }
}
