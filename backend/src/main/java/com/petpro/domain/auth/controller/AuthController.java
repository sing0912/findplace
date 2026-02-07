package com.petpro.domain.auth.controller;

import com.petpro.domain.auth.dto.AuthRequest;
import com.petpro.domain.auth.dto.AuthResponse;
import com.petpro.domain.auth.service.AuthService;
import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러
 *
 * 사용자 인증 관련 REST API 엔드포인트
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ==================== 회원가입 ====================

    @Operation(summary = "회원가입", description = "이메일로 회원가입합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse.RegisterResult>> register(
            @Valid @RequestBody AuthRequest.Register request) {
        AuthResponse.RegisterResult response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // ==================== 로그인 ====================

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.Token>> login(
            @Valid @RequestBody AuthRequest.Login request) {
        AuthResponse.Token response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AuthResponse.SuccessMessage>> logout() {
        // 클라이언트에서 토큰 삭제 처리
        return ResponseEntity.ok(ApiResponse.success(AuthResponse.SuccessMessage.of("로그아웃되었습니다.")));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.Token>> refresh(
            @Valid @RequestBody AuthRequest.RefreshToken request) {
        AuthResponse.Token response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 중복 확인 ====================

    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<AuthResponse.AvailabilityCheck>> checkEmail(
            @RequestParam String email) {
        AuthResponse.AvailabilityCheck response = authService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<AuthResponse.AvailabilityCheck>> checkNickname(
            @RequestParam String nickname) {
        AuthResponse.AvailabilityCheck response = authService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 소셜 로그인 ====================

    @Operation(summary = "소셜 로그인", description = "소셜 로그인을 처리합니다. (kakao, naver, google)")
    @PostMapping("/oauth/{provider}/callback")
    public ResponseEntity<ApiResponse<AuthResponse.OAuthResult>> oauthLogin(
            @PathVariable String provider,
            @Valid @RequestBody AuthRequest.OAuthLogin request) {
        AuthResponse.OAuthResult response = authService.oauthLogin(provider, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 아이디 찾기 ====================

    @Operation(summary = "아이디 찾기 - 인증 요청", description = "이름과 전화번호로 인증번호를 발송합니다.")
    @PostMapping("/find-id/request")
    public ResponseEntity<ApiResponse<AuthResponse.VerificationRequestResult>> findIdRequest(
            @Valid @RequestBody AuthRequest.FindIdRequest request) {
        AuthResponse.VerificationRequestResult response = authService.findIdRequest(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "아이디 찾기 - 인증 확인", description = "인증번호를 확인하고 이메일을 반환합니다.")
    @PostMapping("/find-id/verify")
    public ResponseEntity<ApiResponse<AuthResponse.FindIdResult>> findIdVerify(
            @Valid @RequestBody AuthRequest.VerifyRequest request) {
        AuthResponse.FindIdResult response = authService.findIdVerify(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "아이디 찾기 - 재전송", description = "인증번호를 재전송합니다.")
    @PostMapping("/find-id/resend")
    public ResponseEntity<ApiResponse<AuthResponse.VerificationRequestResult>> findIdResend(
            @Valid @RequestBody AuthRequest.ResendRequest request) {
        AuthResponse.VerificationRequestResult response = authService.findIdResend(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 비밀번호 재설정 ====================

    @Operation(summary = "비밀번호 재설정 - 인증 요청", description = "이메일과 전화번호로 인증번호를 발송합니다.")
    @PostMapping("/reset-password/request")
    public ResponseEntity<ApiResponse<AuthResponse.VerificationRequestResult>> resetPasswordRequest(
            @Valid @RequestBody AuthRequest.ResetPasswordRequest request) {
        AuthResponse.VerificationRequestResult response = authService.resetPasswordRequest(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 재설정 - 인증 확인", description = "인증번호를 확인하고 재설정 토큰을 발급합니다.")
    @PostMapping("/reset-password/verify")
    public ResponseEntity<ApiResponse<AuthResponse.PasswordResetToken>> resetPasswordVerify(
            @Valid @RequestBody AuthRequest.VerifyRequest request) {
        AuthResponse.PasswordResetToken response = authService.resetPasswordVerify(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 재설정 - 변경", description = "새 비밀번호를 설정합니다.")
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<ApiResponse<AuthResponse.SuccessMessage>> resetPasswordConfirm(
            @Valid @RequestBody AuthRequest.ResetPasswordConfirm request) {
        AuthResponse.SuccessMessage response = authService.resetPasswordConfirm(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
